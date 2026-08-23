package com.berkay.restaurant.service.domain.ports.input.service;

import com.berkay.restaurant.service.domain.dto.approve.ApproveRestaurantOrderCommand;

import com.berkay.restaurant.service.domain.dto.reject.RejectRestaurantOrderCommand;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface RestaurantOrderApplicationService {
    void approveOrder(@Valid ApproveRestaurantOrderCommand approveRestaurantOrderCommand);
    void rejectOrder(@Valid RejectRestaurantOrderCommand rejectRestaurantOrderCommand);
}
