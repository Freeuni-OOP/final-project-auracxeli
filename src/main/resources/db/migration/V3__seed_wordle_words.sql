INSERT INTO wordle_words (word, scheduled_date, added_by, created_at) VALUES
    ('ბურთი', DATE_ADD(CURDATE(), INTERVAL -2 DAY), NULL, NOW()),
    ('სკამი', DATE_ADD(CURDATE(), INTERVAL -1 DAY), NULL, NOW()),
    ('წიგნი', DATE_ADD(CURDATE(), INTERVAL 0 DAY),  NULL, NOW()),
    ('ძაღლი', DATE_ADD(CURDATE(), INTERVAL 1 DAY),  NULL, NOW()),
    ('ცხენი', DATE_ADD(CURDATE(), INTERVAL 2 DAY),  NULL, NOW()),
    ('ვაშლი', DATE_ADD(CURDATE(), INTERVAL 3 DAY),  NULL, NOW()),
    ('სახლი', DATE_ADD(CURDATE(), INTERVAL 4 DAY),  NULL, NOW()),
    ('თვალი', DATE_ADD(CURDATE(), INTERVAL 5 DAY),  NULL, NOW()),
    ('თევზი', DATE_ADD(CURDATE(), INTERVAL 6 DAY),  NULL, NOW()),
    ('წყალი', DATE_ADD(CURDATE(), INTERVAL 7 DAY),  NULL, NOW()),
    ('თოვლი', DATE_ADD(CURDATE(), INTERVAL 8 DAY),  NULL, NOW()),
    ('ვარდი', DATE_ADD(CURDATE(), INTERVAL 9 DAY),  NULL, NOW());
