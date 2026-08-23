package com.berkay.restaurant.service.domain.entity;

import com.berkay.domain.entity.AggregateRoot;
import com.berkay.domain.valueobject.*;
import com.berkay.restaurant.service.domain.exception.RestaurantDomainException;
import com.berkay.restaurant.service.domain.valueobject.OrderApprovalId;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import com.berkay.restaurant.service.domain.valueobject.Address;
import com.berkay.restaurant.service.domain.valueobject.CuisineType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.berkay.restaurant.service.domain.event.OrderApprovedEvent;
import com.berkay.restaurant.service.domain.event.OrderRejectedEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static com.berkay.domain.DomainConstants.UTC;


public class Restaurant extends AggregateRoot<RestaurantId> {
    private RestaurantName restaurantName;
    private final List<Product> menu;
    private OrderApproval orderApproval;
    private boolean active;
    private boolean available;
    private OrderDetail orderDetail;
    
    private Address address;
    private String phoneNumber;
    private Money minimumOrderAmount;
    private Money deliveryFee;
    private Integer averageDeliveryTimeInMinutes;
    private CuisineType cuisineType;
    private String description;
    private String logoUrl;

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


    public void updateMinimumOrderAmount(Money minimumOrderAmount) {
        if (minimumOrderAmount != null && minimumOrderAmount.isGreaterThanZero()) {
            this.minimumOrderAmount = minimumOrderAmount;
        } else if (minimumOrderAmount != null && minimumOrderAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            this.minimumOrderAmount = minimumOrderAmount;
        }
    }

    public void updateDeliveryFee(Money deliveryFee) {
        if (deliveryFee != null && deliveryFee.isGreaterThanZero()) {
            this.deliveryFee = deliveryFee;
        } else if (deliveryFee != null && deliveryFee.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            this.deliveryFee = deliveryFee;
        }
    }

    public void updateAvailability(Boolean available) {
        if (available != null) {
            this.available = available;
        }
    }

    public void updateAddress(Address address) {
        if (address != null) {
            this.address = address;
        }
    }

    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updateAverageDeliveryTime(Integer time) {
        if (time != null && time >= 0) {
            this.averageDeliveryTimeInMinutes = time;
        }
    }

    public void updateCuisineType(CuisineType type) {
        if (type != null) {
            this.cuisineType = type;
        }
    }

    public void updateDescription(String desc) {
        if (desc != null) {
            this.description = desc;
        }
    }

    public void updateLogoUrl(String url) {
        if (url != null) {
            this.logoUrl = url;
        }
    }

    public void initializeRestaurant() {
        if (getId() == null) {
            setId(new RestaurantId(UUID.randomUUID()));
        }

        if (getRestaurantName() == null) {
            throw new RestaurantDomainException("Restaurant name cannot be null!");
        }

        if (minimumOrderAmount != null && !minimumOrderAmount.isGreaterThanZero() && !minimumOrderAmount.equals(Money.ZERO)) {
            throw new RestaurantDomainException("Minimum order amount must be greater than or equal to zero!");
        }

        if (deliveryFee != null && !deliveryFee.isGreaterThanZero() && !deliveryFee.equals(Money.ZERO)) {
            throw new RestaurantDomainException("Delivery fee must be greater than or equal to zero!");
        }

        if (averageDeliveryTimeInMinutes != null && averageDeliveryTimeInMinutes < 0) {
            throw new RestaurantDomainException("Average delivery time cannot be negative!");
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

        // 1.5 KURAL: Restoran Açık mı?
        if (!this.isAvailable()) {
            failureMessages.add("Restaurant with name " + this.restaurantName + " is currently not accepting orders!");
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

            // Ürün gizli mi?
            if (menuProduct.isHidden()) {
                failureMessages.add("Product with name: " + menuProduct.getName() + " is hidden and cannot be ordered.");
            }

            // Stok Yeterli mi?
            if (menuProduct.getStock() < requestedQuantity) {
                failureMessages.add("Product with name: " + menuProduct.getName()
                        + " has insufficient stock. Requested: " + requestedQuantity
                        + ", Available: " + menuProduct.getStock());
            } else {
                // Stok miktarını düş
                menuProduct.updateWith(menuProduct.getName(), menuProduct.getPrice(), menuProduct.isAvailable(), menuProduct.getStock() - requestedQuantity, menuProduct.isHidden(), menuProduct.getImageUrl());
            }

            // Fiyat hesaplama
            Money itemTotal = menuProduct.getPrice().multiply(requestedQuantity);
            totalAmount = totalAmount.add(itemTotal);
        }

        Money itemsTotal = totalAmount;

        // 4. KURAL: Sepet Tutarı Minimum Sipariş Tutarını Sağlıyor mu?
        if (this.minimumOrderAmount != null && this.minimumOrderAmount.isGreaterThanZero() && this.minimumOrderAmount.isGreaterThan(itemsTotal)) {
            failureMessages.add("Order amount is less than minimum order amount!");
        }

        // Delivery fee'yi ekle
        if (this.deliveryFee != null && this.deliveryFee.isGreaterThanZero()) {
            totalAmount = totalAmount.add(this.deliveryFee);
        }

        // 5. KURAL: Toplam Tutar Tutuyor mu?
        if (!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Total price (" + orderDetail.getTotalAmount().getAmount() 
                + ") not equal to total item price (" + itemsTotal.getAmount() 
                + ") + delivery fee (" + (this.deliveryFee != null ? this.deliveryFee.getAmount() : "0") 
                + ") = " + totalAmount.getAmount());
        }
    }

    

    public OrderApprovedEvent approveOrder() {
        if (this.orderApproval == null || this.orderApproval.getApprovalStatus() != OrderApprovalStatus.PENDING) {
            throw new RestaurantDomainException("Order is not in PENDING state for approval!");
        }
        this.orderApproval.setApprovalStatus(OrderApprovalStatus.APPROVED);
        return new OrderApprovedEvent(this.orderApproval,
                this.getId(),
                new ArrayList<>(),
                ZonedDateTime.now(ZoneId.of(UTC)));
    }

    public OrderRejectedEvent rejectOrder(List<String> failureMessages) {
        if (this.orderApproval == null || this.orderApproval.getApprovalStatus() != OrderApprovalStatus.PENDING) {
            throw new RestaurantDomainException("Order is not in PENDING state for rejection!");
        }
        this.orderApproval.setApprovalStatus(OrderApprovalStatus.REJECTED);
        return new OrderRejectedEvent(this.orderApproval,
                this.getId(),
                failureMessages,
                ZonedDateTime.now(ZoneId.of(UTC)));
    }

    public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
                .restaurantId(this.getId())
                .orderId(this.getOrderDetail().getId())
                .approvalStatus(orderApprovalStatus)
                .productQuantities(this.getOrderDetail().getProductQuantities())
                .build();
    }

    private Restaurant(Builder builder) {
        setId(builder.restaurantId);
        restaurantName = builder.restaurantName;
        menu = builder.menu != null ? builder.menu : new ArrayList<>();
        orderApproval = builder.orderApproval;
        active = builder.active;
        available = builder.available;
        orderDetail = builder.orderDetail;
        address = builder.address;
        phoneNumber = builder.phoneNumber;
        minimumOrderAmount = builder.minimumOrderAmount;
        deliveryFee = builder.deliveryFee;
        averageDeliveryTimeInMinutes = builder.averageDeliveryTimeInMinutes;
        cuisineType = builder.cuisineType;
        description = builder.description;
        logoUrl = builder.logoUrl;
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

    
    public void setOrderApproval(OrderApproval orderApproval) {
        this.orderApproval = orderApproval;
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return available;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public Address getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Money getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public Money getDeliveryFee() {
        return deliveryFee;
    }

    public Integer getAverageDeliveryTimeInMinutes() {
        return averageDeliveryTimeInMinutes;
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public static final class Builder {
        private RestaurantId restaurantId;
        private RestaurantName restaurantName;
        private List<Product> menu;
        private OrderApproval orderApproval;
        private boolean active;
        private boolean available;
        private OrderDetail orderDetail;
        private Address address;
        private String phoneNumber;
        private Money minimumOrderAmount;
        private Money deliveryFee;
        private Integer averageDeliveryTimeInMinutes;
        private CuisineType cuisineType;
        private String description;
        private String logoUrl;

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

        public Builder available(boolean val) {
            available = val;
            return this;
        }

        public Builder orderDetail(OrderDetail val) {
            orderDetail = val;
            return this;
        }

        public Builder address(Address val) {
            address = val;
            return this;
        }

        public Builder phoneNumber(String val) {
            phoneNumber = val;
            return this;
        }

        public Builder minimumOrderAmount(Money val) {
            minimumOrderAmount = val;
            return this;
        }

        public Builder deliveryFee(Money val) {
            deliveryFee = val;
            return this;
        }

        public Builder averageDeliveryTimeInMinutes(Integer val) {
            averageDeliveryTimeInMinutes = val;
            return this;
        }

        public Builder cuisineType(CuisineType val) {
            cuisineType = val;
            return this;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder logoUrl(String val) {
            logoUrl = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
