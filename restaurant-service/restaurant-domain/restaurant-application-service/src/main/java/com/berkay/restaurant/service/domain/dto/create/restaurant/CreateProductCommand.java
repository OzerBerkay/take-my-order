package com.berkay.restaurant.service.domain.dto.create.restaurant;

import com.berkay.restaurant.service.domain.dto.base.BaseProductCommand;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(force = true) // Jackson için zorunlu
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreateProductCommand extends BaseProductCommand {
    // İçi boş, çünkü sadece ortak alanlar yetiyor.
    // restaurantId YOK. Çünkü restaurant zaten o an yaratılıyor.
}
