package com.berkay.restaurant.service.domain.rest;

import com.berkay.restaurant.service.domain.dto.create.AddProductRequest;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductCommand;
import com.berkay.restaurant.service.domain.dto.create.product.AddProductResponse;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantCommand;
import com.berkay.restaurant.service.domain.dto.create.restaurant.CreateRestaurantResponse;
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

    @PostMapping
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantCommand createRestaurantCommand) {
        log.info("Creating restaurant with name: {}", createRestaurantCommand.getRestaurantName());
        CreateRestaurantResponse response = restaurantApplicationService.createRestaurant(createRestaurantCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{restaurantId}/products")
    public ResponseEntity<AddProductResponse> addProduct(@PathVariable UUID restaurantId,
                                                         @RequestBody @Valid AddProductRequest addProductRequest) {
        log.info("Adding product to restaurant with id: {}", restaurantId);

        AddProductCommand addProductCommand = productRequestMapper.addProductRequestToAddProductCommand(restaurantId, addProductRequest);
        AddProductResponse response = restaurantApplicationService.addProduct(addProductCommand);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{restaurantId}")
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
}