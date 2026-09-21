package com.anthony.delivery_box_api;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String txref;

    @Positive
    @Max(500)
    private Double weightLimit;

    @Min(0)
    @Max(100)
    private Integer batteryCapacity;

    @Enumerated(EnumType.STRING)
    private BoxState state;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTxref() { return txref; }
    public void setTxref(String txref) { this.txref = txref; }

    public Double getWeightLimit() { return weightLimit; }
    public void setWeightLimit(Double weightLimit) { this.weightLimit = weightLimit; }

    public Integer getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(Integer batteryCapacity) { this.batteryCapacity = batteryCapacity; }

    public BoxState getState() { return state; }
    public void setState(BoxState state) { this.state = state; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}