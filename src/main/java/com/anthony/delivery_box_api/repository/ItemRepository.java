package com.anthony.delivery_box_api.repository;
import com.anthony.delivery_box_api.model.Item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}