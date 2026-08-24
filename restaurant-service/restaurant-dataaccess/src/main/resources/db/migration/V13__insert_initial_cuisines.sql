-- V13__insert_initial_cuisines.sql

-- Insert default cuisines with real Unsplash images
INSERT INTO "restaurant".cuisines (id, name, code, description, icon_url, is_active) VALUES
(gen_random_uuid(), 'Burger', 'burger', 'Lezzetli ve doyurucu hamburger çeşitleri', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Pizza', 'pizza', 'İtalyan usulü ince hamur pizzalar', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Sushi', 'sushi', 'Taze ve özenle hazırlanmış uzak doğu lezzetleri', 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Italian', 'italian', 'Makarna ve geleneksel İtalyan tatları', 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Mexican', 'mexican', 'Baharatlı ve ateşli Meksika yemekleri', 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Seafood', 'seafood', 'Günlük taze deniz ürünleri', 'https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Steakhouse', 'steakhouse', 'Özel dinlendirilmiş et çeşitleri', 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Asian', 'asian', 'Asya mutfağının seçkin yemekleri', 'https://images.unsplash.com/photo-1552611052-33e04de081de?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Coffee', 'coffee', 'Taze kavrulmuş nitelikli kahveler', 'https://images.unsplash.com/photo-1497935586351-b67a49e012bf?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Chicken', 'chicken', 'Çıtır ve lezzetli tavuk menüleri', 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?q=80&w=800&auto=format&fit=crop', true),

-- Türk Mutfağı
(gen_random_uuid(), 'Kebap', 'kebap', 'Geleneksel Türk kebap çeşitleri', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Pilav', 'pilav', 'Nefis nohutlu ve tavuklu pilavlar', 'https://images.unsplash.com/photo-1512058564366-18510be2db19?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Tost ve Sandviç', 'tost_ve_sandvic', 'Hızlı ve doyurucu atıştırmalıklar', 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Pastane ve Fırın', 'pastane_ve_firin', 'Taptaze simit, poğaça ve tatlılar', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Kahvaltı ve Börek', 'kahvalti_ve_borek', 'Güne zinde başlamak için kahvaltılıklar', 'https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Lahmacun', 'lahmacun', 'Çıtır çıtır Türk pizzası', 'https://images.unsplash.com/photo-1620374231652-cbcec8987e94?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Tantuni', 'tantuni', 'Mersin''in meşhur lezzeti', 'https://images.unsplash.com/photo-1633321702518-7feccafb94d5?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Pide', 'pide', 'Odun ateşinde pişmiş pideler', 'https://images.unsplash.com/photo-1653982960203-c8361d7bed96?q=80&w=800&auto=format&fit=crop', true),
(gen_random_uuid(), 'Ev Yemekleri', 'ev_yemekleri', 'Anne elinden çıkmış gibi tencere yemekleri', 'https://images.unsplash.com/photo-1648455320791-a667c8aab7e4?q=80&w=800&auto=format&fit=crop', true),

-- Diğer
(gen_random_uuid(), 'Other', 'other', 'Kategori dışı diğer tüm lezzetler', 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=800&auto=format&fit=crop', true);
