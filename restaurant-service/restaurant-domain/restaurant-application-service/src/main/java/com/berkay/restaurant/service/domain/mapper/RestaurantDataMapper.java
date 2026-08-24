package com.berkay.restaurant.service.domain.mapper;

import com.berkay.domain.valueobject.*;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateProductCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetPublicProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.entity.Product;
import com.berkay.restaurant.service.domain.entity.Restaurant;
import com.berkay.restaurant.service.domain.event.OrderApprovalEvent;
import com.berkay.restaurant.service.domain.event.RestaurantInformationEvent;
import com.berkay.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.berkay.restaurant.service.domain.outbox.model.RestaurantEventPayload;
import com.berkay.restaurant.service.domain.valueobject.RestaurantName;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.berkay.domain.DomainConstants.UTC;

@Component
public class RestaurantDataMapper {

    public Restaurant createRestaurantCommandToRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        return Restaurant.builder()
                // ID atanmıyor, Domain Service'deki initializeRestaurant metodunda atanacak.
                .restaurantName(new RestaurantName(createRestaurantCommand.getRestaurantName()))
                .active(createRestaurantCommand.isActive())
                .available(false)
                .menu(createProductCommandsToProducts(createRestaurantCommand.getProducts()))
                .address(new com.berkay.restaurant.service.domain.valueobject.Address(
                        createRestaurantCommand.getStreet(),
                        createRestaurantCommand.getCity(),
                        createRestaurantCommand.getPostalCode()
                ))
                .phoneNumber(createRestaurantCommand.getPhoneNumber())
                .minimumOrderAmount(createRestaurantCommand.getMinimumOrderAmount() != null ? new Money(createRestaurantCommand.getMinimumOrderAmount()) : null)
                .deliveryFee(createRestaurantCommand.getDeliveryFee() != null ? new Money(createRestaurantCommand.getDeliveryFee()) : null)
                .averageDeliveryTimeInMinutes(createRestaurantCommand.getAverageDeliveryTimeInMinutes())
                .description(createRestaurantCommand.getDescription())
                .logoUrl(createRestaurantCommand.getLogoUrl())
                .bannerUrl(createRestaurantCommand.getBannerUrl())
                .build();
    }

    private List<Product> createProductCommandsToProducts(List<CreateProductCommand> createProductCommands) {
        return createProductCommands.stream()
                .map(productCommand -> Product.builder()
                        .name(productCommand.getName())
                        .price(new Money(productCommand.getPrice()))
                        .stock(productCommand.getStock())
                        .available(productCommand.getAvailable())
                        .hidden(productCommand.getHidden())
                        .imageUrl(productCommand.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public CreateRestaurantResponse restaurantToCreateRestaurantResponse(Restaurant restaurant) {
        return CreateRestaurantResponse.builder()
                .restaurantId(restaurant.getId().getValue())
                .message("Restaurant created successfully")
                .build();
    }

    public OrderEventPayload
    orderApprovalEventToOrderEventPayload(OrderApprovalEvent orderApprovalEvent) {
        return OrderEventPayload.builder()
                .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
                .restaurantId(orderApprovalEvent.getRestaurantId().getValue().toString())
                .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getApprovalStatus().name())
                .createdAt(orderApprovalEvent.getCreatedAt())
                .failureMessages(orderApprovalEvent.getFailureMessages())
                .build();
    }

    public Product addProductCommandToProduct(AddProductCommand addProductCommand) {
        return Product.builder()
                .name(addProductCommand.getName())
                .price(new Money(addProductCommand.getPrice()))
                .stock(addProductCommand.getStock())
                .available(addProductCommand.getAvailable())
                .hidden(addProductCommand.getHidden())
                .imageUrl(addProductCommand.getImageUrl())
                .build();
    }

    public AddProductResponse productToAddProductResponse(Product product) {
        return AddProductResponse.builder()
                .productId(product.getId().getValue())
                .message("Product added successfully")
                .build();
    }

    public RestaurantEventPayload restaurantInformationEventToRestaurantEventPayload(RestaurantInformationEvent restaurantInformationEvent, String merchantId, String eventType) {
        return RestaurantEventPayload.builder()
                .eventType(eventType)
                .restaurantId(restaurantInformationEvent.getRestaurant().getId().getValue().toString())
                .merchantId(merchantId)
                .name(restaurantInformationEvent.getRestaurant().getRestaurantName() != null ? restaurantInformationEvent.getRestaurant().getRestaurantName().getRestaurantName() : null)
                .active(restaurantInformationEvent.getRestaurant().isActive())
                .available(restaurantInformationEvent.getRestaurant().isAvailable())
                .minimumOrderAmount(restaurantInformationEvent.getRestaurant().getMinimumOrderAmount() != null ? restaurantInformationEvent.getRestaurant().getMinimumOrderAmount().getAmount() : null)
                .deliveryFee(restaurantInformationEvent.getRestaurant().getDeliveryFee() != null ? restaurantInformationEvent.getRestaurant().getDeliveryFee().getAmount() : null)
                .createdAt(restaurantInformationEvent.getCreatedAt())
                .products(restaurantInformationEvent.getRestaurant().getMenu().stream().map(product ->
                        RestaurantEventPayload.ProductPayload.builder()
                                .productId(product.getId().getValue().toString())
                                .name(product.getName())
                                .price(product.getPrice().getAmount())
                                .available(product.isAvailable())
                                .hidden(product.isHidden())
                                .build()).toList())
                .build();
    }

    // Repository.save() metodundan dönen güncel objeyi kullanmazsak,
    // Event içine eski @Version bilgisi gider ve veri tutarsızlığı oluşur.
    //Eğer eski objeyi yollasaydın, Kafka'ya giden mesajda versiyon 1 yazardı ama veritabanında versiyon 2 olurdu.
    // Order servisi mesajı aldığında "Bu veri eski mi?" diye şüpheye düşer veya validasyon hatası verirdi.
    // TODO: Yukarıdaki yorumun doğru çalışması için versiyon bilgisi entity'e eklenecek ki OptimisticLocking gerçekleştirilsin! Burası şimdilik altyapı sağlamaktadır
    public RestaurantInformationEvent restaurantToRestaurantInformationEvent(Restaurant restaurant) {
        return new RestaurantInformationEvent(
                restaurant,
                ZonedDateTime.now(ZoneId.of(UTC))
        );
    }

    public GetProductQueryResponse productToGetProductQueryResponse(Product product) {
        return GetProductQueryResponse.builder()
                .productId(product.getId().getValue())
                .name(product.getName())
                .price(product.getPrice().getAmount())
                .stock(product.getStock())
                .available(product.isAvailable())
                .hidden(product.isHidden())
                .imageUrl(product.getImageUrl())
                .build();
    }

    public GetPublicProductQueryResponse productToGetPublicProductQueryResponse(Product product) {
        return GetPublicProductQueryResponse.builder()
                .productId(product.getId().getValue())
                .name(product.getName())
                .price(product.getPrice().getAmount())
                .inStock(product.getStock() > 0)
                .imageUrl(product.getImageUrl())
                .build();
    }

    public GetRestaurantQueryResponse restaurantToGetRestaurantQueryResponse(Restaurant restaurant) {
        return GetRestaurantQueryResponse.builder()
                .restaurantId(restaurant.getId().getValue())
                .name(restaurant.getRestaurantName().getRestaurantName())
                .active(restaurant.isActive())
                .menu(restaurant.getMenu().stream()
                        .map(this::productToGetProductQueryResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public com.berkay.restaurant.service.domain.dto.read.RestaurantModel restaurantToRestaurantModel(Restaurant restaurant) {
        return com.berkay.restaurant.service.domain.dto.read.RestaurantModel.builder()
                .restaurantId(restaurant.getId().getValue())
                .name(restaurant.getRestaurantName().getRestaurantName())
                .description(restaurant.getDescription())
                .logoUrl(restaurant.getLogoUrl())
                .bannerUrl(restaurant.getBannerUrl())
                .cuisines(restaurant.getCuisines() != null ? restaurant.getCuisines().stream().map(c -> com.berkay.restaurant.service.domain.dto.read.CuisineModel.builder()
                        .id(c.getId().getValue())
                        .name(c.getName())
                        .code(c.getCode())
                        .description(c.getDescription())
                        .iconUrl(c.getIconUrl())
                        .isActive(c.isActive())
                        .build()).collect(Collectors.toList()) : null)
                .averageDeliveryTimeInMinutes(restaurant.getAverageDeliveryTimeInMinutes())
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee().getAmount() : null)
                .minimumOrderAmount(restaurant.getMinimumOrderAmount() != null ? restaurant.getMinimumOrderAmount().getAmount() : null)
                .address(restaurant.getAddress())
                .active(restaurant.isActive())
                .available(restaurant.isAvailable())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount() != null ? restaurant.getMinimumOrderAmount().getAmount() : null)
                .deliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee().getAmount() : null)
                .build();
    }
}
