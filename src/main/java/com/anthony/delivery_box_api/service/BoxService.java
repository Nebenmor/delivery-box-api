package com.anthony.delivery_box_api.service;
import com.anthony.delivery_box_api.model.Box;
import com.anthony.delivery_box_api.model.BoxState;
import com.anthony.delivery_box_api.model.Item;
import com.anthony.delivery_box_api.repository.BoxRepository;
import com.anthony.delivery_box_api.repository.ItemRepository;
import com.anthony.delivery_box_api.exception.BoxException;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BoxService {

    private final BoxRepository boxRepository;
    private final ItemRepository itemRepository;

    // Constructor injection, Spring automatically provides these
    public BoxService(BoxRepository boxRepository, ItemRepository itemRepository) {
        this.boxRepository = boxRepository;
        this.itemRepository = itemRepository;
    }

    public Box createBox(Box box) {
        box.setState(BoxState.IDLE);
        return boxRepository.save(box);
    }

    public List<Box> getAvailableBoxes() {
        return boxRepository.findByState(BoxState.IDLE);
    }

    public Integer getBatteryLevel(Long boxId) {
        Box box = getBoxOrThrow(boxId);
        return box.getBatteryCapacity();
    }

    public List<Item> getItemsInBox(Long boxId) {
        Box box = getBoxOrThrow(boxId);
        return box.getItems();
    }

    public Item loadItem(Long boxId, Item item) {
        Box box = getBoxOrThrow(boxId);

        // Rule 1: battery must be at least 25% to enter LOADING state
        if (box.getBatteryCapacity() < 25) {
            throw new BoxException("Cannot load box: battery level is below 25%");
        }

        // Rule 2: total weight (existing items + new item) must not exceed weightLimit
        double currentWeight = box.getItems().stream()
                .mapToDouble(Item::getWeight)
                .sum();
        double newTotalWeight = currentWeight + item.getWeight();

        if (newTotalWeight > box.getWeightLimit()) {
            throw new BoxException("Cannot load item: exceeds box weight limit of " + box.getWeightLimit() + "g");
        }

        // All checks passed, associate item with box and update state
        item.setBox(box);
        box.setState(BoxState.LOADING);
        boxRepository.save(box);

        return itemRepository.save(item);
    }

    private Box getBoxOrThrow(Long boxId) {
        return boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxException("Box not found with id: " + boxId));
    }
}