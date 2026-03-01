-- liquibase formatted sql

-- changeset vbanasevych:2

-- категорії справ
INSERT INTO case_categories (name)
VALUES ('Кримінальне право'),
       ('Цивільне право'),
       ('Сімейне право'),
       ('Господарське право'),
       ('Адміністративне право');

-- користувачі
-- Адмін
INSERT INTO users (email, password_hash, full_name, phone, role)
VALUES ('viktoriabanasevic@gmail.com', '$2a$10$hvfSFh8EXDOOFmyzv/85jOcEqf81JebB8mgSLHAZ5WUpMN5839bFO', 'Вікторія Банасевич', '+380501111111', 'ADMIN');

-- Адвокати
INSERT INTO users (email, password_hash, full_name, phone, role)
VALUES ('kovalenko@bureau.com', '$2a$10$hvfSFh8EXDOOFmyzv/85jOcEqf81JebB8mgSLHAZ5WUpMN5839bFO', 'Дмитро Коваленко', '+380671112233', 'LAWYER'),
       ('shevchenko@bureau.com', '$2a$10$hvfSFh8EXDOOFmyzv/85jOcEqf81JebB8mgSLHAZ5WUpMN5839bFO', 'Олена Шевченко', '+380631112233', 'LAWYER'),
       ('rybachyk@bureau.com', '$2a$10$hvfSFh8EXDOOFmyzv/85jOcEqf81JebB8mgSLHAZ5WUpMN5839bFO', 'Марина Рибачук', '+380509678415', 'LAWYER');

-- Клієнти
INSERT INTO users (email, password_hash, full_name, phone, role)
VALUES ('client1@gmail.com', '$2a$10$cV9Q0ixoUjA5b9MC5I5deuwXk4d6xMeSSZojgIFvnC4erGIvM81yS', 'Андрій Мельник', '+380991234567', 'CLIENT'),
       ('client2@gmail.com', '$2a$10$cV9Q0ixoUjA5b9MC5I5deuwXk4d6xMeSSZojgIFvnC4erGIvM81yS', 'Марія Бойко', '+380991234568', 'CLIENT'),
       ('vbanasevych@knu.ua', '$2a$10$cV9Q0ixoUjA5b9MC5I5deuwXk4d6xMeSSZojgIFvnC4erGIvM81yS', 'Вікторія Банасевич', '+380991234569', 'CLIENT'),
       ('client4@gmail.com', '$2a$10$cV9Q0ixoUjA5b9MC5I5deuwXk4d6xMeSSZojgIFvnC4erGIvM81yS', 'Ірина Ковальчук', '+380991234570', 'CLIENT');

-- профілі адвокатів
INSERT INTO lawyers (user_id, hourly_rate, city, bio)
VALUES (2, 1500.00, 'Київ', 'Досвідчений адвокат у кримінальних справах. Понад 10 років практики.'),
       (3, 1200.00, 'Львів', 'Спеціалістка з сімейного та цивільного права. Допомога при розлученнях та поділі майна.'),
       (4, 1000.00, 'Київ', 'Експерт з адміністративного права.');

-- спеціалізації адвокатів
INSERT INTO lawyer_categories (lawyer_id, case_category_id)
VALUES (2, 1),
       (3, 2),
       (3, 3),
       (4, 5);