-- V13__insert_initial_cuisines.sql

-- Insert default cuisines with real Unsplash images
INSERT INTO "restaurant".cuisines (id, name, code, description, icon_url, is_active) VALUES
('4eacf15e-f9f7-4664-8487-2c92afda119d', 'Burger', 'burger', 'Lezzetli ve doyurucu hamburger çeşitleri', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=800&auto=format&fit=crop', true),
('5c107d88-42fe-4a16-82ba-a442400171ca', 'Pizza', 'pizza', 'İtalyan usulü ince hamur pizzalar', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=800&auto=format&fit=crop', true),
('37ab4ad1-b78e-48ce-9783-f709e567839d', 'Sushi', 'sushi', 'Taze ve özenle hazırlanmış uzak doğu lezzetleri', 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=800&auto=format&fit=crop', true),
('348769f8-dc22-4884-b2b2-9e042b984ef9', 'Italian', 'italian', 'Makarna ve geleneksel İtalyan tatları', 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?q=80&w=800&auto=format&fit=crop', true),
('b8426fd7-d07c-4301-b934-be34fdddd4a4', 'Mexican', 'mexican', 'Baharatlı ve ateşli Meksika yemekleri', 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?q=80&w=800&auto=format&fit=crop', true),
('4457543d-7049-4631-a69d-8cde1b7b6fa8', 'Seafood', 'seafood', 'Günlük taze deniz ürünleri', 'https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?q=80&w=800&auto=format&fit=crop', true),
('9dbfdc72-3c0f-4bfd-b46e-352dc802c106', 'Steakhouse', 'steakhouse', 'Özel dinlendirilmiş et çeşitleri', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=800&auto=format&fit=crop', true),
('4a9f3c88-262b-4f57-8d38-a6ae19a6fb1e', 'Asian', 'asian', 'Asya mutfağının seçkin yemekleri', 'https://images.unsplash.com/photo-1552611052-33e04de081de?q=80&w=800&auto=format&fit=crop', true),
('6b474c44-2fe6-4248-8f21-1ab6bd3672ad', 'Coffee', 'coffee', 'Taze kavrulmuş nitelikli kahveler', 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?q=80&w=800&auto=format&fit=crop', true),
('a5988a39-885f-40dd-a1f4-ec69c0a25f9a', 'Chicken', 'chicken', 'Çıtır ve lezzetli tavuk menüleri', 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?q=80&w=800&auto=format&fit=crop', true),

-- Türk Mutfağı
('96b27ef3-b24a-4ab7-87b3-76ecd7d8c0d6', 'Kebap', 'kebap', 'Geleneksel Türk kebap çeşitleri', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=800&auto=format&fit=crop', true),
('4b5c38fb-28c7-4a64-a3a3-4d03e6010b4d', 'Pilav', 'pilav', 'Nefis nohutlu ve tavuklu pilavlar', 'https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=800&auto=format&fit=crop', true),
('11d233d9-9581-46c0-8f33-130449ead077', 'Tost ve Sandviç', 'tost_ve_sandvic', 'Hızlı ve doyurucu atıştırmalıklar', 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?q=80&w=800&auto=format&fit=crop', true),
('4ecdd96d-3ad9-4164-89a7-31eba3aad7d9', 'Pastane ve Fırın', 'pastane_ve_firin', 'Taptaze simit, poğaça ve tatlılar', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=800&auto=format&fit=crop', true),
('cc4211a6-fe93-4e64-b79a-6598fc2c2de2', 'Kahvaltı ve Börek', 'kahvalti_ve_borek', 'Güne zinde başlamak için kahvaltılıklar', 'https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?q=80&w=800&auto=format&fit=crop', true),
('b5f16cdc-15d8-4b3f-9cf9-17053d7c069b', 'Lahmacun', 'lahmacun', 'Çıtır çıtır Türk pizzası', 'https://images.unsplash.com/photo-1620374231652-cbcec8987e94?q=80&w=800&auto=format&fit=crop', true),
('84cddd4b-19b0-45f8-81a9-f28bbf8d1074', 'Tantuni', 'tantuni', 'Mersin''in meşhur lezzeti', 'https://images.unsplash.com/photo-1633321702518-7feccafb94d5?q=80&w=800&auto=format&fit=crop', true),
('36d82e08-de9d-4397-b58b-3d271fe12a4f', 'Pide', 'pide', 'Odun ateşinde pişmiş pideler', 'https://images.unsplash.com/photo-1653982960203-c8361d7bed96?q=80&w=800&auto=format&fit=crop', true),
('c1152f1c-b391-49c7-983a-23eb442f5f48', 'Ev Yemekleri', 'ev_yemekleri', 'Anne elinden çıkmış gibi tencere yemekleri', 'https://images.unsplash.com/photo-1648455320791-a667c8aab7e4?q=80&w=800&auto=format&fit=crop', true),

-- Diğer
('bbc0860b-898f-4bef-8cb5-2718cc827525', 'Other', 'other', 'Kategori dışı diğer tüm lezzetler', 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=800&auto=format&fit=crop', true);

