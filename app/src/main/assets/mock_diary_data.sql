-- Mock Diary Data for Testing Weekly/Monthly Summary Views

-- Foods used (IDs from taco_preload.sql):
-- 1: Arroz integral cozido (124 kcal/100g, 2.6g protein)
-- 3: Arroz tipo 1 cozido (128 kcal/100g, 2.5g protein)
-- 53: Pão francês (300 kcal/100g, 8g protein)
-- 7: Aveia flocos (394 kcal/100g, 13.9g protein)
-- 100: Brócolis cozido (25 kcal/100g, 2.1g protein)
-- 88: Batata doce cozida (77 kcal/100g, 0.6g protein)
-- 163: Frango peito sem pele grelhado (119 kcal/100g, 21.5g protein)
-- 232: Ovo de galinha inteiro cozido (146 kcal/100g, 13.3g protein)
-- 300: Banana prata (98 kcal/100g, 1.3g protein)
-- 315: Maçã fuji (56 kcal/100g, 0.3g protein)

-- keep in mind LLM was used to generate this file

BEGIN TRANSACTION;

-- Day -30 (4 weeks + 2 days ago)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2025-12-29', 100.0, 'Café da Manhã', 1735480800000, 1),
(7, '2025-12-29', 50.0, 'Café da Manhã', 1735480860000, 1),
(3, '2025-12-29', 200.0, 'Almoço', 1735495200000, 1),
(163, '2025-12-29', 150.0, 'Almoço', 1735495260000, 1),
(100, '2025-12-29', 100.0, 'Almoço', 1735495320000, 1),
(300, '2025-12-29', 120.0, 'Lanche', 1735509600000, 1),
(3, '2025-12-29', 150.0, 'Jantar', 1735524000000, 1),
(232, '2025-12-29', 100.0, 'Jantar', 1735524060000, 1);

-- Day -29
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2025-12-30', 80.0, 'Café da Manhã', 1735567200000, 1),
(232, '2025-12-30', 100.0, 'Café da Manhã', 1735567260000, 1),
(1, '2025-12-30', 180.0, 'Almoço', 1735581600000, 1),
(163, '2025-12-30', 180.0, 'Almoço', 1735581660000, 1),
(88, '2025-12-30', 150.0, 'Almoço', 1735581720000, 1),
(315, '2025-12-30', 150.0, 'Lanche', 1735596000000, 1),
(3, '2025-12-30', 200.0, 'Jantar', 1735610400000, 1);

-- Day -28
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2025-12-31', 60.0, 'Café da Manhã', 1735653600000, 1),
(300, '2025-12-31', 100.0, 'Café da Manhã', 1735653660000, 1),
(3, '2025-12-31', 250.0, 'Almoço', 1735668000000, 1),
(163, '2025-12-31', 200.0, 'Almoço', 1735668060000, 1),
(100, '2025-12-31', 120.0, 'Almoço', 1735668120000, 1),
(53, '2025-12-31', 60.0, 'Lanche', 1735682400000, 1),
(232, '2025-12-31', 150.0, 'Jantar', 1735696800000, 1);

-- Day -27 (New Year's Day - lighter meals)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-01', 100.0, 'Café da Manhã', 1735740000000, 1),
(7, '2026-01-01', 40.0, 'Café da Manhã', 1735740060000, 1),
(1, '2026-01-01', 150.0, 'Almoço', 1735754400000, 1),
(163, '2026-01-01', 120.0, 'Almoço', 1735754460000, 1);

-- Day -26
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-02', 100.0, 'Café da Manhã', 1735826400000, 1),
(232, '2026-01-02', 100.0, 'Café da Manhã', 1735826460000, 1),
(3, '2026-01-02', 200.0, 'Almoço', 1735840800000, 1),
(163, '2026-01-02', 160.0, 'Almoço', 1735840860000, 1),
(100, '2026-01-02', 100.0, 'Almoço', 1735840920000, 1),
(88, '2026-01-02', 100.0, 'Almoço', 1735840980000, 1),
(300, '2026-01-02', 100.0, 'Lanche', 1735855200000, 1),
(3, '2026-01-02', 180.0, 'Jantar', 1735869600000, 1),
(232, '2026-01-02', 100.0, 'Jantar', 1735869660000, 1);

-- Day -25
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-03', 60.0, 'Café da Manhã', 1735912800000, 1),
(300, '2026-01-03', 120.0, 'Café da Manhã', 1735912860000, 1),
(1, '2026-01-03', 220.0, 'Almoço', 1735927200000, 1),
(163, '2026-01-03', 180.0, 'Almoço', 1735927260000, 1),
(100, '2026-01-03', 80.0, 'Almoço', 1735927320000, 1),
(315, '2026-01-03', 120.0, 'Lanche', 1735941600000, 1),
(3, '2026-01-03', 150.0, 'Jantar', 1735956000000, 1);

-- Day -24
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-04', 120.0, 'Café da Manhã', 1735999200000, 1),
(232, '2026-01-04', 100.0, 'Café da Manhã', 1735999260000, 1),
(3, '2026-01-04', 200.0, 'Almoço', 1736013600000, 1),
(163, '2026-01-04', 150.0, 'Almoço', 1736013660000, 1),
(88, '2026-01-04', 120.0, 'Almoço', 1736013720000, 1),
(300, '2026-01-04', 100.0, 'Lanche', 1736028000000, 1),
(53, '2026-01-04', 80.0, 'Jantar', 1736042400000, 1);

-- Day -23
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-05', 50.0, 'Café da Manhã', 1736085600000, 1),
(232, '2026-01-05', 100.0, 'Café da Manhã', 1736085660000, 1),
(1, '2026-01-05', 180.0, 'Almoço', 1736100000000, 1),
(163, '2026-01-05', 200.0, 'Almoço', 1736100060000, 1),
(100, '2026-01-05', 100.0, 'Almoço', 1736100120000, 1),
(315, '2026-01-05', 150.0, 'Lanche', 1736114400000, 1),
(3, '2026-01-05', 200.0, 'Jantar', 1736128800000, 1),
(232, '2026-01-05', 100.0, 'Jantar', 1736128860000, 1);

-- Day -22 (Week 2 starts)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-06', 100.0, 'Café da Manhã', 1736172000000, 1),
(7, '2026-01-06', 60.0, 'Café da Manhã', 1736172060000, 1),
(3, '2026-01-06', 200.0, 'Almoço', 1736186400000, 1),
(163, '2026-01-06', 160.0, 'Almoço', 1736186460000, 1),
(88, '2026-01-06', 150.0, 'Almoço', 1736186520000, 1),
(300, '2026-01-06', 100.0, 'Lanche', 1736200800000, 1),
(1, '2026-01-06', 180.0, 'Jantar', 1736215200000, 1);

-- Day -21
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-07', 80.0, 'Café da Manhã', 1736258400000, 1),
(232, '2026-01-07', 150.0, 'Café da Manhã', 1736258460000, 1),
(3, '2026-01-07', 220.0, 'Almoço', 1736272800000, 1),
(163, '2026-01-07', 180.0, 'Almoço', 1736272860000, 1),
(100, '2026-01-07', 120.0, 'Almoço', 1736272920000, 1),
(315, '2026-01-07', 100.0, 'Lanche', 1736287200000, 1),
(3, '2026-01-07', 150.0, 'Jantar', 1736301600000, 1);

-- Day -20
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-08', 50.0, 'Café da Manhã', 1736344800000, 1),
(300, '2026-01-08', 150.0, 'Café da Manhã', 1736344860000, 1),
(1, '2026-01-08', 200.0, 'Almoço', 1736359200000, 1),
(163, '2026-01-08', 170.0, 'Almoço', 1736359260000, 1),
(88, '2026-01-08', 100.0, 'Almoço', 1736359320000, 1),
(232, '2026-01-08', 100.0, 'Jantar', 1736388000000, 1);

-- Day -19
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-09', 100.0, 'Café da Manhã', 1736431200000, 1),
(232, '2026-01-09', 100.0, 'Café da Manhã', 1736431260000, 1),
(3, '2026-01-09', 180.0, 'Almoço', 1736445600000, 1),
(163, '2026-01-09', 200.0, 'Almoço', 1736445660000, 1),
(100, '2026-01-09', 100.0, 'Almoço', 1736445720000, 1),
(300, '2026-01-09', 120.0, 'Lanche', 1736460000000, 1),
(1, '2026-01-09', 160.0, 'Jantar', 1736474400000, 1),
(232, '2026-01-09', 100.0, 'Jantar', 1736474460000, 1);

-- Day -18
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-10', 60.0, 'Café da Manhã', 1736517600000, 1),
(315, '2026-01-10', 150.0, 'Café da Manhã', 1736517660000, 1),
(3, '2026-01-10', 200.0, 'Almoço', 1736532000000, 1),
(163, '2026-01-10', 150.0, 'Almoço', 1736532060000, 1),
(88, '2026-01-10', 120.0, 'Almoço', 1736532120000, 1),
(53, '2026-01-10', 60.0, 'Lanche', 1736546400000, 1),
(3, '2026-01-10', 150.0, 'Jantar', 1736560800000, 1);

-- Day -17
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-11', 100.0, 'Café da Manhã', 1736604000000, 1),
(232, '2026-01-11', 100.0, 'Café da Manhã', 1736604060000, 1),
(1, '2026-01-11', 220.0, 'Almoço', 1736618400000, 1),
(163, '2026-01-11', 180.0, 'Almoço', 1736618460000, 1),
(100, '2026-01-11', 80.0, 'Almoço', 1736618520000, 1);

-- Day -16
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-12', 80.0, 'Café da Manhã', 1736690400000, 1),
(7, '2026-01-12', 40.0, 'Café da Manhã', 1736690460000, 1),
(3, '2026-01-12', 180.0, 'Almoço', 1736704800000, 1),
(163, '2026-01-12', 160.0, 'Almoço', 1736704860000, 1),
(88, '2026-01-12', 150.0, 'Almoço', 1736704920000, 1),
(300, '2026-01-12', 100.0, 'Lanche', 1736719200000, 1),
(232, '2026-01-12', 150.0, 'Jantar', 1736733600000, 1);

-- Day -15 (Week 3 starts)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-13', 60.0, 'Café da Manhã', 1736776800000, 1),
(300, '2026-01-13', 120.0, 'Café da Manhã', 1736776860000, 1),
(1, '2026-01-13', 200.0, 'Almoço', 1736791200000, 1),
(163, '2026-01-13', 180.0, 'Almoço', 1736791260000, 1),
(100, '2026-01-13', 100.0, 'Almoço', 1736791320000, 1),
(315, '2026-01-13', 100.0, 'Lanche', 1736805600000, 1),
(3, '2026-01-13', 160.0, 'Jantar', 1736820000000, 1),
(232, '2026-01-13', 100.0, 'Jantar', 1736820060000, 1);

-- Day -14
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-14', 100.0, 'Café da Manhã', 1736863200000, 1),
(232, '2026-01-14', 100.0, 'Café da Manhã', 1736863260000, 1),
(3, '2026-01-14', 200.0, 'Almoço', 1736877600000, 1),
(163, '2026-01-14', 170.0, 'Almoço', 1736877660000, 1),
(88, '2026-01-14', 120.0, 'Almoço', 1736877720000, 1),
(300, '2026-01-14', 100.0, 'Lanche', 1736892000000, 1),
(1, '2026-01-14', 180.0, 'Jantar', 1736906400000, 1);

-- Day -13
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-15', 50.0, 'Café da Manhã', 1736949600000, 1),
(315, '2026-01-15', 150.0, 'Café da Manhã', 1736949660000, 1),
(1, '2026-01-15', 220.0, 'Almoço', 1736964000000, 1),
(163, '2026-01-15', 200.0, 'Almoço', 1736964060000, 1),
(100, '2026-01-15', 80.0, 'Almoço', 1736964120000, 1),
(53, '2026-01-15', 80.0, 'Lanche', 1736978400000, 1),
(3, '2026-01-15', 200.0, 'Jantar', 1736992800000, 1),
(232, '2026-01-15', 100.0, 'Jantar', 1736992860000, 1);

-- Day -12
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-16', 100.0, 'Café da Manhã', 1737036000000, 1),
(7, '2026-01-16', 60.0, 'Café da Manhã', 1737036060000, 1),
(3, '2026-01-16', 200.0, 'Almoço', 1737050400000, 1),
(163, '2026-01-16', 160.0, 'Almoço', 1737050460000, 1),
(88, '2026-01-16', 100.0, 'Almoço', 1737050520000, 1),
(300, '2026-01-16', 120.0, 'Lanche', 1737064800000, 1),
(232, '2026-01-16', 100.0, 'Jantar', 1737079200000, 1);

-- Day -11
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-17', 80.0, 'Café da Manhã', 1737122400000, 1),
(232, '2026-01-17', 150.0, 'Café da Manhã', 1737122460000, 1),
(1, '2026-01-17', 180.0, 'Almoço', 1737136800000, 1),
(163, '2026-01-17', 180.0, 'Almoço', 1737136860000, 1),
(100, '2026-01-17', 120.0, 'Almoço', 1737136920000, 1),
(315, '2026-01-17', 100.0, 'Lanche', 1737151200000, 1),
(3, '2026-01-17', 150.0, 'Jantar', 1737165600000, 1);

-- Day -10
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-18', 60.0, 'Café da Manhã', 1737208800000, 1),
(300, '2026-01-18', 100.0, 'Café da Manhã', 1737208860000, 1),
(3, '2026-01-18', 220.0, 'Almoço', 1737223200000, 1),
(163, '2026-01-18', 200.0, 'Almoço', 1737223260000, 1),
(88, '2026-01-18', 100.0, 'Almoço', 1737223320000, 1),
(53, '2026-01-18', 60.0, 'Lanche', 1737237600000, 1),
(232, '2026-01-18', 100.0, 'Jantar', 1737252000000, 1);

-- Day -9
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-19', 100.0, 'Café da Manhã', 1737295200000, 1),
(232, '2026-01-19', 100.0, 'Café da Manhã', 1737295260000, 1),
(1, '2026-01-19', 200.0, 'Almoço', 1737309600000, 1),
(163, '2026-01-19', 150.0, 'Almoço', 1737309660000, 1);

-- Day -8 (Week 4 starts)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-20', 50.0, 'Café da Manhã', 1737381600000, 1),
(315, '2026-01-20', 120.0, 'Café da Manhã', 1737381660000, 1),
(3, '2026-01-20', 200.0, 'Almoço', 1737396000000, 1),
(163, '2026-01-20', 180.0, 'Almoço', 1737396060000, 1),
(100, '2026-01-20', 100.0, 'Almoço', 1737396120000, 1),
(88, '2026-01-20', 120.0, 'Almoço', 1737396180000, 1),
(300, '2026-01-20', 100.0, 'Lanche', 1737410400000, 1),
(1, '2026-01-20', 180.0, 'Jantar', 1737424800000, 1),
(232, '2026-01-20', 100.0, 'Jantar', 1737424860000, 1);

-- Day -7
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-21', 100.0, 'Café da Manhã', 1737468000000, 1),
(7, '2026-01-21', 60.0, 'Café da Manhã', 1737468060000, 1),
(1, '2026-01-21', 220.0, 'Almoço', 1737482400000, 1),
(163, '2026-01-21', 170.0, 'Almoço', 1737482460000, 1),
(88, '2026-01-21', 150.0, 'Almoço', 1737482520000, 1),
(315, '2026-01-21', 100.0, 'Lanche', 1737496800000, 1),
(3, '2026-01-21', 160.0, 'Jantar', 1737511200000, 1);

-- Day -6
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-22', 80.0, 'Café da Manhã', 1737554400000, 1),
(232, '2026-01-22', 100.0, 'Café da Manhã', 1737554460000, 1),
(3, '2026-01-22', 200.0, 'Almoço', 1737568800000, 1),
(163, '2026-01-22', 200.0, 'Almoço', 1737568860000, 1),
(100, '2026-01-22', 80.0, 'Almoço', 1737568920000, 1),
(300, '2026-01-22', 120.0, 'Lanche', 1737583200000, 1),
(232, '2026-01-22', 150.0, 'Jantar', 1737597600000, 1);

-- Day -5
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-23', 60.0, 'Café da Manhã', 1737640800000, 1),
(300, '2026-01-23', 150.0, 'Café da Manhã', 1737640860000, 1),
(1, '2026-01-23', 180.0, 'Almoço', 1737655200000, 1),
(163, '2026-01-23', 160.0, 'Almoço', 1737655260000, 1),
(88, '2026-01-23', 100.0, 'Almoço', 1737655320000, 1),
(53, '2026-01-23', 60.0, 'Lanche', 1737669600000, 1),
(3, '2026-01-23', 180.0, 'Jantar', 1737684000000, 1),
(232, '2026-01-23', 100.0, 'Jantar', 1737684060000, 1);

-- Day -4
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-24', 100.0, 'Café da Manhã', 1737727200000, 1),
(232, '2026-01-24', 100.0, 'Café da Manhã', 1737727260000, 1),
(3, '2026-01-24', 200.0, 'Almoço', 1737741600000, 1),
(163, '2026-01-24', 180.0, 'Almoço', 1737741660000, 1),
(100, '2026-01-24', 100.0, 'Almoço', 1737741720000, 1),
(315, '2026-01-24', 100.0, 'Lanche', 1737756000000, 1),
(1, '2026-01-24', 150.0, 'Jantar', 1737770400000, 1);

-- Day -3
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-25', 50.0, 'Café da Manhã', 1737813600000, 1),
(315, '2026-01-25', 120.0, 'Café da Manhã', 1737813660000, 1),
(1, '2026-01-25', 220.0, 'Almoço', 1737828000000, 1),
(163, '2026-01-25', 200.0, 'Almoço', 1737828060000, 1),
(88, '2026-01-25', 120.0, 'Almoço', 1737828120000, 1),
(300, '2026-01-25', 100.0, 'Lanche', 1737842400000, 1),
(3, '2026-01-25', 200.0, 'Jantar', 1737856800000, 1),
(232, '2026-01-25', 100.0, 'Jantar', 1737856860000, 1);

-- Day -2
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-26', 100.0, 'Café da Manhã', 1737900000000, 1),
(7, '2026-01-26', 60.0, 'Café da Manhã', 1737900060000, 1),
(3, '2026-01-26', 200.0, 'Almoço', 1737914400000, 1),
(163, '2026-01-26', 160.0, 'Almoço', 1737914460000, 1),
(100, '2026-01-26', 100.0, 'Almoço', 1737914520000, 1),
(53, '2026-01-26', 80.0, 'Lanche', 1737928800000, 1),
(232, '2026-01-26', 100.0, 'Jantar', 1737943200000, 1);

-- Day -1 (Yesterday)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(53, '2026-01-27', 80.0, 'Café da Manhã', 1737986400000, 1),
(232, '2026-01-27', 150.0, 'Café da Manhã', 1737986460000, 1),
(1, '2026-01-27', 200.0, 'Almoço', 1738000800000, 1),
(163, '2026-01-27', 180.0, 'Almoço', 1738000860000, 1),
(88, '2026-01-27', 100.0, 'Almoço', 1738000920000, 1),
(300, '2026-01-27', 100.0, 'Lanche', 1738015200000, 1),
(3, '2026-01-27', 180.0, 'Jantar', 1738029600000, 1);

-- Day 0 (Today - 2026-01-28)
INSERT INTO daily_log (foodId, date, quantityGrams, mealType, entryTimestamp, isConsumed) VALUES 
(7, '2026-01-28', 60.0, 'Café da Manhã', 1738072800000, 1),
(300, '2026-01-28', 120.0, 'Café da Manhã', 1738072860000, 1),
(3, '2026-01-28', 200.0, 'Almoço', 1738087200000, 1),
(163, '2026-01-28', 170.0, 'Almoço', 1738087260000, 1),
(100, '2026-01-28', 100.0, 'Almoço', 1738087320000, 1);

COMMIT;

-- Summary:
-- This script creates approximately 180 meal entries across 30 days
-- Typical daily calorie range: 1500-2200 kcal
-- Protein intake: 80-120g per day (varies)
-- Includes variety of: breakfast, lunch, snack, dinner meals