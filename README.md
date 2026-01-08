💳 High-Performance Card Processing System
Ushbu loyiha bank karta operatsiyalari va tranzaksiyalarini boshqarish uchun mo'ljallangan yuqori darajadagi backend xizmatidir. Loyiha faqatgina funksional emas, balki FinTech standartlariga xos bo'lgan xavfsizlik, ma'lumotlar yaxlitligi va monitoring talablariga muvofiq ishlab chiqilgan.

🌟 Advanced FinTech Features (Asosiy afzalliklari):
Idempotency (Distributed Locks): Tarmoqdagi uzilishlar yoki so'rovlarning takrorlanishi (retry) natijasida pul mablag'larining ikki marta yechilishini oldini olish uchun Idempotency-Key mexanizmi joriy qilingan. Kalitlar yuqori tezlikni ta'minlash uchun Redisda saqlanadi.

Concurrency Control (If-Match): Ma'lumotlar bazasida "yo'qolgan yangilanishlar" (lost updates)ning oldini olish uchun HTTP If-Match (ETag) va Optimistic Locking mexanizmidan foydalanilgan.

Database Partitioning: Tranzaksiyalar tarixi kabi katta hajmli jadvallar Table Partitioning (tranzaksiya vaqti bo'yicha) orqali optimallashtirilgan. Bu so'rovlar tezligini va tizimning kengayuvchanligini (scalability) ta'minlaydi.

Transactional Integrity: Barcha pul o'tkazmalari ACID tamoyillariga muvofiq, Spring @Transactional va DB darajasidagi cheklovlar bilan himoyalangan.

🚀 Texnologiyalar:
Java 17 / Spring Boot 3.x

PostgreSQL — Ma'lumotlar ombori va murakkab tranzaksiyalar uchun.

Redis — Idempotency-kalitlari va keshlashtirish uchun.

Docker & Docker Compose — Microservice-ready muhit.

Prometheus & Grafana — Real-time metrikalar va monitoring dashboardlari.

Swagger/OpenAPI — Standartlashtirilgan API hujjatlari.

🛠 Xususiyatlari:
P2P Transfer API: Xavfsiz va tezkor pul o'tkazmalari.

Fault Tolerance: Docker orqali barcha servislarning sog'lig'ini tekshirish (Healthchecks).

Monitoring: Har bir API so'rovi uchun metrikalar yig'iladi va Grafanada vizuallashtiriladi.

Loyihada moliya tizimlarida uchraydigan real muammolarga (double-spending, race conditions, big data handling) yechim berishga harakat qilindi.
