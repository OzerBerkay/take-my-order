package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.create.AddProductRequest;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
import com.berkay.restaurant.service.domain.dto.delete.DeleteProductCommand;
import com.berkay.restaurant.service.domain.dto.read.GetProductListQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetProductQuery;
import com.berkay.restaurant.service.domain.dto.read.GetProductQueryResponse;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQuery;
import com.berkay.restaurant.service.domain.dto.read.GetRestaurantQueryResponse;
import com.berkay.restaurant.service.domain.dto.update.UpdateProductRequest;
import com.berkay.restaurant.service.domain.dto.update.product.UpdateProductCommand;
import com.berkay.restaurant.service.domain.dto.update.restaurant.UpdateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.update.UpdateRestaurantRequest;
import com.berkay.restaurant.service.domain.mapper.ProductRequestMapper;
import com.berkay.restaurant.service.domain.mapper.RestaurantRequestMapper;
import com.berkay.restaurant.service.domain.ports.input.service.RestaurantApplicationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/restaurants", produces = "application/vnd.api.v1+json")
public class RestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;
    private final RestaurantRequestMapper restaurantRequestMapper;
    private final ProductRequestMapper productRequestMapper;

    public RestaurantController(RestaurantApplicationService restaurantApplicationService,
                                RestaurantRequestMapper restaurantRequestMapper,
                                ProductRequestMapper productRequestMapper) {

        this.restaurantApplicationService = restaurantApplicationService;
        this.restaurantRequestMapper = restaurantRequestMapper;
        this.productRequestMapper = productRequestMapper;
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.isMerchant(authentication)")
    @PostMapping
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantCommand createRestaurantCommand,
                                                                     @org.springframework.security.core.annotation.AuthenticationPrincipal java.util.UUID internalId) {
        createRestaurantCommand.setMerchantId(internalId.toString());
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        CreateRestaurantResponse response = restaurantApplicationService.createRestaurant(createRestaurantCommand);
        return ResponseEntity.ok(response);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.hasPermissionForRestaurant(authentication, 'can_create_product', #restaurantId)")
    @PostMapping("/{restaurantId}/products")
    public ResponseEntity<AddProductResponse> addProduct(@PathVariable UUID restaurantId,
                                                         @RequestBody @Valid AddProductRequest addProductRequest) {
        log.info("Adding product to restaurant with id: {}", restaurantId);

        AddProductCommand addProductCommand = productRequestMapper.addProductRequestToAddProductCommand(restaurantId, addProductRequest);
        AddProductResponse response = restaurantApplicationService.addProduct(addProductCommand);

        return ResponseEntity.ok(response);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.hasPermissionForRestaurant(authentication, 'can_manage_restaurant', #restaurantId)")
    @PatchMapping("/{restaurantId}")
    public ResponseEntity<String> updateRestaurant(@PathVariable UUID restaurantId,
                                                   @RequestBody @Valid UpdateRestaurantRequest updateRestaurantRequest) {
        log.info("Updating restaurant with id: {}", restaurantId);

        UpdateRestaurantCommand updateRestaurantCommand = restaurantRequestMapper
                .updateRestaurantRequestToCommand(restaurantId, updateRestaurantRequest);

        restaurantApplicationService.updateRestaurant(updateRestaurantCommand);
        return ResponseEntity.ok("Restaurant updated");
    }

    /* Ürün Güncelleme (Fiyat/Stok/Durum)
    Neden restaurantId'ye de ihtiyaç duyuldu? Bunun 2 sebebi var
    1) DDD'de Product, Restaurant'ın bir parçasıdır. Eğer URL'i /products/{productId} yaparsak, dış dünyaya "Product tek başına bağımsız bir varlıktır" mesajı veririz.
    2) Kötü niyetli kullanıcı, Restoran A'nın sahibiyken, Restoran B'ye ait bir ürünün UUID'sini buldu ve güncellemeye çalıştı
    Sadece ProductId olsaydı: Veritabanından o ürünü bulur ve güncellerdik. Yanlışlıkla başka restoranın menüsü değişirdi
    RestaurantId de olursa: Servis katmanında (Handler'da) şöyle bir kontrol yapma şansımız olur: "Gelen ürün ID'si veritabanında var mı?
     EVET. Peki bu ürün, URL'deki restaurantId'ye mi ait? HAYIR! O zaman hata fırlat.
     TODO: Authorization kısmı projeye eklendiğinde bu kısım da anlamlı olacak.
     */
    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.hasPermissionForRestaurant(authentication, 'can_update_product', #restaurantId)")
    @PutMapping("/{restaurantId}/products/{productId}")
    public ResponseEntity<String> updateProduct(@PathVariable UUID restaurantId,
                                                @PathVariable UUID productId,
                                                @RequestBody @Valid UpdateProductRequest updateProductRequest) {
        log.info("Updating product with id: {} in restaurant: {}", productId, restaurantId);

        UpdateProductCommand updateProductCommand = productRequestMapper
                .updateProductRequestToUpdateProductCommand(restaurantId, productId, updateProductRequest);

        restaurantApplicationService.updateProduct(updateProductCommand);
        return ResponseEntity.ok("Product updated");
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.hasPermissionForRestaurant(authentication, 'can_delete_product', #restaurantId)")
    @DeleteMapping("/{restaurantId}/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable UUID restaurantId,
                                                @PathVariable UUID productId) {
        log.info("Deleting product with id: {} in restaurant: {}", productId, restaurantId);

        // Request DTO olmadığı için Command'i direkt burada oluşturuyoruz
        DeleteProductCommand deleteProductCommand = DeleteProductCommand.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .build();

        restaurantApplicationService.deleteProduct(deleteProductCommand);

        return ResponseEntity.ok("Product deleted successfully");
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.isMerchant(authentication)")
    @GetMapping
    public ResponseEntity<com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse> getRestaurants(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UUID internalId) {
        log.info("Getting all restaurants for merchant with id: {}", internalId);
        com.berkay.restaurant.service.domain.dto.read.GetRestaurantListQueryResponse response = 
                restaurantApplicationService.getRestaurants(internalId);
        return ResponseEntity.ok(response);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.isMemberOfRestaurant(authentication, #restaurantId)")
    @GetMapping("/{restaurantId}")
    public ResponseEntity<GetRestaurantQueryResponse> getRestaurant(@PathVariable UUID restaurantId) {
        log.info("Getting restaurant with id: {}", restaurantId);

        GetRestaurantQuery getRestaurantQuery = GetRestaurantQuery.builder()
                .restaurantId(restaurantId)
                .build();

        GetRestaurantQueryResponse response = restaurantApplicationService.getRestaurant(getRestaurantQuery);
        return ResponseEntity.ok(response);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.isMemberOfRestaurant(authentication, #restaurantId)")
    @GetMapping("/{restaurantId}/products/{productId}")
    public ResponseEntity<GetProductQueryResponse> getProduct(@PathVariable UUID restaurantId,
                                                              @PathVariable UUID productId) {
        log.info("Getting product with id: {} from restaurant: {}", productId, restaurantId);

        GetProductQuery getProductQuery = GetProductQuery.builder()
                .restaurantId(restaurantId)
                .productId(productId)
                .build();

        GetProductQueryResponse response = restaurantApplicationService.getProduct(getProductQuery);
        return ResponseEntity.ok(response);
    }

    @org.springframework.security.access.prepost.PreAuthorize("@restaurantAuthService.isMemberOfRestaurant(authentication, #restaurantId)")
    @GetMapping("/{restaurantId}/products")
    public ResponseEntity<GetProductListQueryResponse> getProducts(@PathVariable UUID restaurantId) {
        log.info("Getting all products from restaurant: {}", restaurantId);

        GetProductListQueryResponse response = restaurantApplicationService.getProducts(restaurantId);
        return ResponseEntity.ok(response);
    }
}