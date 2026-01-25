package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.*;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.valueobject.OrderApprovalId;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Restaurant extends AggregateRoot<RestaurantId> {
    private RestaurantName restaurantName;
    private final List<Product> menu;
    private OrderApproval orderApproval;
    private boolean active;
    private OrderDetail orderDetail;

    public void updateName(String restaurantName) {
        if (restaurantName != null && !restaurantName.isBlank()) {
            this.restaurantName = new RestaurantName(restaurantName);
        }
    }

    public void updateActiveStatus(Boolean active) {
        if (active != null) {
            this.active = active;
        }
    }

    public void initializeRestaurant() {
        if (getId() == null) {
            setId(new RestaurantId(UUID.randomUUID()));
        }

        if (getRestaurantName() == null) {
            throw new RestaurantDomainException("Restaurant name cannot be null!");
        }

        if (!menu.isEmpty()) {
            menu.forEach(product -> {
                if (product.getId() == null) {
                    product.setId(new ProductId(UUID.randomUUID()));
                }
                product.validateProduct();
            });
        }
    }

    public void initOrderDetail(OrderDetail orderDetail) {
        this.orderDetail = orderDetail;
    }

    public void addProduct(Product product) {
        if (menu.stream().anyMatch(p -> p.getName().equalsIgnoreCase(product.getName()))) {
            throw new RestaurantDomainException("Product with name " + product.getName() + " already exists!");
        }

        if (!product.getPrice().isGreaterThanZero()) {
            throw new RestaurantDomainException("Product price must be greater than zero!");
        }

        if (product.getId() == null) {
            product.setId(new ProductId(UUID.randomUUID()));
        }
        this.menu.add(product);
    }

    public void validateOrder(List<String> failureMessages) {
        // 1. KURAL: Restoran Aktif mi?
        if (!this.isActive()) {
            failureMessages.add("Restaurant with name " + this.restaurantName + " is currently not active!");
        }

        // 2. KURAL: Ödeme Tamamlanmış mı?
        if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
            failureMessages.add("Payment is not completed for order: " + orderDetail.getId());
        }

        // Key: ProductId, Value: Product (Restoranın gerçek ürünü)
        Map<ProductId, Product> restaurantMenu = this.menu.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 3. Kural: Ürün Kontrolü (Siparişteki her ürün için)
        Money totalAmount = Money.ZERO;

        for (Map.Entry<ProductId, Integer> productQuantity : orderDetail.getProductQuantities().entrySet()) {
            ProductId orderedProductId = productQuantity.getKey();
            Integer requestedQuantity = productQuantity.getValue();
            Product menuProduct = restaurantMenu.get(orderedProductId);

            // Ürün menüde var mı?
            if (menuProduct == null) {
                failureMessages.add("Product with id: " + orderedProductId.getValue() + " is not found.");
                continue;
            }

            // Ürün satışta mı?
            if (!menuProduct.isAvailable()) {
                failureMessages.add("Product with name: " + menuProduct.getName() + " is not available.");
            }

            // Stok Yeterli mi?
            if (menuProduct.getStock() < requestedQuantity) {
                failureMessages.add("Product with name: " + menuProduct.getName()
                        + " has insufficient stock. Requested: " + requestedQuantity
                        + ", Available: " + menuProduct.getStock());
            }

            // Fiyat hesaplama
            Money itemTotal = menuProduct.getPrice().multiply(requestedQuantity);
            totalAmount = totalAmount.add(itemTotal);
        }

        // 4. KURAL: Toplam Tutar Tutuyor mu?
        if (!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Price total is not correct for order: " + orderDetail.getId());
        }
    }

    public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
                .restaurantId(this.getId())
                .orderId(this.getOrderDetail().getId())
                .approvalStatus(orderApprovalStatus)
                .build();
    }

    private Restaurant(Builder builder) {
        setId(builder.restaurantId);
        restaurantName = builder.restaurantName;
        menu = builder.menu != null ? builder.menu : new ArrayList<>();
        orderApproval = builder.orderApproval;
        active = builder.active;
        orderDetail = builder.orderDetail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RestaurantName getRestaurantName() {
        return restaurantName;
    }

    public List<Product> getMenu() {
        return menu;
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public static final class Builder {
        private RestaurantId restaurantId;
        private RestaurantName restaurantName;
        private List<Product> menu;
        private OrderApproval orderApproval;
        private boolean active;
        private OrderDetail orderDetail;

        private Builder() {
        }

        public Builder restaurantId(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder restaurantName(RestaurantName val) {
            restaurantName = val;
            return this;
        }

        public Builder menu(List<Product> val) {
            menu = val;
            return this;
        }

        public Builder orderApproval(OrderApproval val) {
            orderApproval = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Builder orderDetail(OrderDetail val) {
            orderDetail = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
