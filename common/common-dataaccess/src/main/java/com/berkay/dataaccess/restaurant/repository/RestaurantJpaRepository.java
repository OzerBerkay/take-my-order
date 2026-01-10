package com.berkay.dataaccess.restaurant.repository;

import com.berkay.dataaccess.restaurant.entity.RestaurantEntity;
import com.berkay.dataaccess.restaurant.entity.RestaurantEntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

}
