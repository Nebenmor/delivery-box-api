package com.anthony.delivery_box_api.controller;
import com.anthony.delivery_box_api.model.Box;
import com.anthony.delivery_box_api.model.Item;
import com.anthony.delivery_box_api.service.BoxService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/boxes")
public class BoxController {

    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    @PostMapping
    public ResponseEntity<Box> createBox(@Valid @RequestBody Box box) {
        Box saved = boxService.createBox(box);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}")
        public ResponseEntity<Box> getBoxById(@PathVariable Long id) {
        return ResponseEntity.ok(boxService.getBoxById(id));
}

    @PostMapping("/{id}/items")
    public ResponseEntity<Item> loadItem(@PathVariable Long id, @Valid @RequestBody Item item) {
        Item saved = boxService.loadItem(id, item);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<Item>> getItemsInBox(@PathVariable Long id) {
        return ResponseEntity.ok(boxService.getItemsInBox(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Box>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }

    @GetMapping("/{id}/battery")
    public ResponseEntity<Integer> getBatteryLevel(@PathVariable Long id) {
        return ResponseEntity.ok(boxService.getBatteryLevel(id));
    }
}