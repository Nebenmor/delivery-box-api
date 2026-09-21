package com.anthony.delivery_box_api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BoxRepository extends JpaRepository<Box, Long> {
    List<Box> findByState(BoxState state);
}