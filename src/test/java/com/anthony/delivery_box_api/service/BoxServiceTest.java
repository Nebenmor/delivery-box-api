package com.anthony.delivery_box_api.service;

import com.anthony.delivery_box_api.exception.BoxException;
import com.anthony.delivery_box_api.model.Box;
import com.anthony.delivery_box_api.model.BoxState;
import com.anthony.delivery_box_api.model.Item;
import com.anthony.delivery_box_api.repository.BoxRepository;
import com.anthony.delivery_box_api.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BoxService boxService;

    private Box testBox;

    @BeforeEach
    void setUp() {
        testBox = new Box();
        testBox.setId(1L);
        testBox.setTxref("BOX-TEST");
        testBox.setWeightLimit(500.0);
        testBox.setBatteryCapacity(100);
        testBox.setState(BoxState.IDLE);
        testBox.setItems(new ArrayList<>());
    }

    @Test
    void loadItem_shouldSucceed_whenBatteryAndWeightAreValid() {
        // Arrange
        Item item = new Item();
        item.setName("test-item");
        item.setWeight(200.0);
        item.setCode("ITEM001");

        when(boxRepository.findById(1L)).thenReturn(Optional.of(testBox));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act
        Item result = boxService.loadItem(1L, item);

        // Assert
        assertEquals(BoxState.LOADING, testBox.getState());
        assertEquals(item, result);
        verify(boxRepository).save(testBox);
        verify(itemRepository).save(item);
    }

    @Test
    void loadItem_shouldThrow_whenBatteryBelow25Percent() {
        // Arrange
        testBox.setBatteryCapacity(20);
        Item item = new Item();
        item.setWeight(100.0);

        when(boxRepository.findById(1L)).thenReturn(Optional.of(testBox));

        // Act & Assert
        BoxException exception = assertThrows(BoxException.class, () -> {
            boxService.loadItem(1L, item);
        });
        assertTrue(exception.getMessage().contains("battery"));

        // Confirm no save happened, box state must not change on rejection
        verify(itemRepository, never()).save(any());
    }

    @Test
    void loadItem_shouldThrow_whenWeightExceedsLimit() {
        // Arrange: box already has 400g loaded, limit is 500g
        Item existingItem = new Item();
        existingItem.setWeight(400.0);
        testBox.setItems(List.of(existingItem));

        Item newItem = new Item();
        newItem.setWeight(150.0); // 400 + 150 = 550, exceeds 500 limit

        when(boxRepository.findById(1L)).thenReturn(Optional.of(testBox));

        // Act & Assert
        BoxException exception = assertThrows(BoxException.class, () -> {
            boxService.loadItem(1L, newItem);
        });
        assertTrue(exception.getMessage().contains("weight limit"));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void loadItem_shouldThrow_whenBoxNotFound() {
        when(boxRepository.findById(999L)).thenReturn(Optional.empty());

        Item item = new Item();
        item.setWeight(50.0);

        BoxException exception = assertThrows(BoxException.class, () -> {
            boxService.loadItem(999L, item);
        });
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void createBox_shouldSetStateToIdle() {
        Box newBox = new Box();
        newBox.setTxref("BOX-NEW");
        newBox.setWeightLimit(300.0);
        newBox.setBatteryCapacity(90);

        when(boxRepository.save(any(Box.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Box result = boxService.createBox(newBox);

        assertEquals(BoxState.IDLE, result.getState());
        verify(boxRepository).save(newBox);
    }
}