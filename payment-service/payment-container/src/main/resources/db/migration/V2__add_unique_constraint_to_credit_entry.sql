-- Eğer kullanıcının cüzdanı yoksa ve aynı anda birden fazla para yüklenmesi yapılmaya kalkılırsa
-- credit entry içinde aynı kişiye ait birden fazla kayıt açılacaktır. Bunun olmasını engellemek için unique constraint ekleniyor.
-- Aksi halde bir kişinin birden fazla cüzdanı olması durumu yaşanacaktır.
ALTER TABLE "payment".credit_entry
    ADD CONSTRAINT uq_credit_entry_customer_id UNIQUE (customer_id);