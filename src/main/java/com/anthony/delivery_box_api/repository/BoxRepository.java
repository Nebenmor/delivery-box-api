package com.anthony.delivery_box_api.repository;
import com.anthony.delivery_box_api.model.Box;
import com.anthony.delivery_box_api.model.BoxState;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoxRepository extends JpaRepository<Box, Long> {
    List<Box> findByState(BoxState state);
}