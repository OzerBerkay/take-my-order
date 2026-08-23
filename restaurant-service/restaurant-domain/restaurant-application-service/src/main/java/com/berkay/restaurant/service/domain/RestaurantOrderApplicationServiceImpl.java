package com.berkay.restaurant.service.domain;

import com.berkay.restaurant.service.domain.dto.approve.ApproveRestaurantOrderCommand;

import com.berkay.restaurant.service.domain.dto.reject.RejectRestaurantOrderCommand;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantOrderApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@Service
public class RestaurantOrderApplicationServiceImpl implements RestaurantOrderApplicationService {

    private final ApproveRestaurantOrderCommandHandler approveRestaurantOrderCommandHandler;
    private final RejectRestaurantOrderCommandHandler rejectRestaurantOrderCommandHandler;

    public RestaurantOrderApplicationServiceImpl(ApproveRestaurantOrderCommandHandler approveRestaurantOrderCommandHandler,
                                                 RejectRestaurantOrderCommandHandler rejectRestaurantOrderCommandHandler) {
        this.approveRestaurantOrderCommandHandler = approveRestaurantOrderCommandHandler;
        this.rejectRestaurantOrderCommandHandler = rejectRestaurantOrderCommandHandler;
    }



    @Override
    public void approveOrder(ApproveRestaurantOrderCommand command) {
        approveRestaurantOrderCommandHandler.approveOrder(command);
    }

    @Override
    public void rejectOrder(RejectRestaurantOrderCommand command) {
        rejectRestaurantOrderCommandHandler.rejectOrder(command);
    }
}
