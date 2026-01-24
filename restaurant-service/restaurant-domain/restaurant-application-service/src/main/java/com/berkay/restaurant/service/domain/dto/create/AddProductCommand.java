package com.berkay.restaurant.service.domain.dto.create;

import com.berkay.restaurant.service.domain.dto.base.BaseProductCommand;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddProductCommand extends BaseProductCommand {
    @NotNull(message = "Restaurant id cannot be null!") // Burada zorunlu çünkü bir restorana ekleme yapıyoruz. CreateProductCommand'deki gibi restorant ile birlikte oluşturmuyoruz.
    private final UUID restaurantId;
}