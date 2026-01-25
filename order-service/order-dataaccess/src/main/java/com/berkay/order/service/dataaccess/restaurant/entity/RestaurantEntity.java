package com.berkay.order.service.dataaccess.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurants")
@Entity
public class RestaurantEntity {

    @Id
    private UUID restaurantId;

    private String name;

    private boolean restaurantActive;

    /*
     orphanRemoval = true Ana sistemde ürün silinirse, burada da silinsin (Tam senkronizasyon).
     Eğer orphanRemoval = true iken, Kafka mesajında ürün listesini boş gönderirsen veya eksik gönderirsen;
     Hibernate/JPA veritabanındaki o eksik ürünleri SİLER. Çünkü senin gönderdiğin listeyi "Gerçeğin Kendisi" (Source of Truth) olarak kabul eder.
     Teorik olarak "gereksiz veri taşıyoruz" gibi dursa da, Pratikte (Restoran Domaini için) bu en doğru yöntemdir.
     Veri Boyutu Küçük: Bir restoranın kaç ürünü olabilir? 50? 100? Maksimum 200? Bu veri, Kafka ve Network için "çerez" niyetindedir.
     Güncelleme Sıklığı Düşük: Restoranlar menülerini veya isimlerini saniyede bir değiştirmezler. Haftada bir, ayda bir değiştirirler. Bu yüzden "Network şişer mi?" korkusu yersizdir.
     Eğer "Sadece değişeni gönder" (Delta Update) yapmaya çalışsaydık; ProductPriceChanged, ProductNameChanged, ProductDeleted gibi 10 farklı event tipi yönetmek zorunda kalırdık.
     Order Service'de bu eventlerin sırası karışırsa veri bozulurdu. "Tümünü gönder" mantığı (Snapshot), her zaman en güncel ve en doğru veriye sahip olmanı garanti eder. Kodu inanılmaz basitleştirir.
     Eğer Amazon gibi milyonlarca ürünü olan bir yapı olsaydı, evet bu yöntem patlardı. O zaman "Granular Events" kullanırdık ancak yönetilmesi de kat be kat daha zor olurdu
    */
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductEntity> products;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantEntity that = (RestaurantEntity) o;
        return restaurantId.equals(that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId);
    }
}
