-- Seed 7 consecutive days of Georgian-language Connections puzzles starting from the merge date.
-- Each puzzle has exactly 4 groups (difficulty 1-4), each group exactly 4 words.

INSERT INTO connections_puzzles (puzzle_date, created_at) VALUES
    (DATE_ADD(CURDATE(), INTERVAL 0 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 1 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 2 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 3 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 4 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 5 DAY), NOW()),
    (DATE_ADD(CURDATE(), INTERVAL 6 DAY), NOW());

INSERT INTO connections_groups (puzzle_id, category, difficulty, created_at)
SELECT p.id, g.category, g.difficulty, NOW()
FROM connections_puzzles p
JOIN (
    SELECT 0 AS day_offset, 'სამშენებლო მასალები' AS category, 1 AS difficulty UNION ALL
    SELECT 0, 'ფრინველები', 2 UNION ALL
    SELECT 0, 'სიმბოლოები და ფიგურები', 3 UNION ALL
    SELECT 0, 'ბანქოს ნიშნები', 4 UNION ALL

    SELECT 1, 'ტექსტის/დოკუმენტის ნაწილები', 1 UNION ALL
    SELECT 1, 'გრამატიკა/ლინგვისტიკა', 2 UNION ALL
    SELECT 1, 'სახის ნაწილები', 3 UNION ALL
    SELECT 1, 'სხეულის ნაწილები სხვა მნიშვნელობით', 4 UNION ALL

    SELECT 2, 'ტრანსპორტი', 1 UNION ALL
    SELECT 2, 'რეპტილიები/ამფიბიები', 2 UNION ALL
    SELECT 2, 'ბანქოს კარტები', 3 UNION ALL
    SELECT 2, 'ჭადრაკის ფიგურები', 4 UNION ALL

    SELECT 3, 'ბოსტნეული', 1 UNION ALL
    SELECT 3, 'ყვავილები', 2 UNION ALL
    SELECT 3, 'კოსმოსი/ბუნებრივი ობიექტები', 3 UNION ALL
    SELECT 3, 'სიტყვები, რომლებიც იღებენ ბოლოსართს "-სფერი"', 4 UNION ALL

    SELECT 4, 'მეტეოროლოგია/ნალექი', 1 UNION ALL
    SELECT 4, 'მღრღნელები', 2 UNION ALL
    SELECT 4, 'ხილი და კენკრა', 3 UNION ALL
    SELECT 4, 'IT/ტექნოლოგიური ტერმინები', 4 UNION ALL

    SELECT 5, 'სახლის/შენობის ნაწილები', 1 UNION ALL
    SELECT 5, 'სახის ნაწილები', 2 UNION ALL
    SELECT 5, 'ლანდშაფტი/გარემო', 3 UNION ALL
    SELECT 5, 'სიტყვები, რომლებიც წინსართად იღებენ "დედა"-ს', 4 UNION ALL

    SELECT 6, 'ფიზიკური მახასიათებლები', 1 UNION ALL
    SELECT 6, 'გეომეტრია/ნახაზი', 2 UNION ALL
    SELECT 6, 'სხეულის დანამატები/ქსოვილები', 3 UNION ALL
    SELECT 6, 'სასვენი ნიშნები', 4
) g ON p.puzzle_date = DATE_ADD(CURDATE(), INTERVAL g.day_offset DAY);

INSERT INTO connections_words (group_id, word)
SELECT gr.id, w.word
FROM connections_groups gr
JOIN connections_puzzles p ON gr.puzzle_id = p.id
JOIN (
    SELECT 0 AS day_offset, 'სამშენებლო მასალები' AS category, 'ბეტონი' AS word UNION ALL
    SELECT 0, 'სამშენებლო მასალები', 'ცემენტი' UNION ALL
    SELECT 0, 'სამშენებლო მასალები', 'ქვიშა' UNION ALL
    SELECT 0, 'სამშენებლო მასალები', 'ლურსმანი' UNION ALL
    SELECT 0, 'ფრინველები', 'მტრედი' UNION ALL
    SELECT 0, 'ფრინველები', 'არწივი' UNION ALL
    SELECT 0, 'ფრინველები', 'ბეღურა' UNION ALL
    SELECT 0, 'ფრინველები', 'ბუ' UNION ALL
    SELECT 0, 'სიმბოლოები და ფიგურები', 'ვარსკვლავი' UNION ALL
    SELECT 0, 'სიმბოლოები და ფიგურები', 'ნახევარმთვარე' UNION ALL
    SELECT 0, 'სიმბოლოები და ფიგურები', 'ისარი' UNION ALL
    SELECT 0, 'სიმბოლოები და ფიგურები', 'რგოლი' UNION ALL
    SELECT 0, 'ბანქოს ნიშნები', 'გული' UNION ALL
    SELECT 0, 'ბანქოს ნიშნები', 'ყვავი' UNION ALL
    SELECT 0, 'ბანქოს ნიშნები', 'აგური' UNION ALL
    SELECT 0, 'ბანქოს ნიშნები', 'ჯვარი' UNION ALL

    SELECT 1, 'ტექსტის/დოკუმენტის ნაწილები', 'პარაგრაფი' UNION ALL
    SELECT 1, 'ტექსტის/დოკუმენტის ნაწილები', 'პუნქტი' UNION ALL
    SELECT 1, 'ტექსტის/დოკუმენტის ნაწილები', 'აბზაცი' UNION ALL
    SELECT 1, 'ტექსტის/დოკუმენტის ნაწილები', 'სტატია' UNION ALL
    SELECT 1, 'გრამატიკა/ლინგვისტიკა', 'ბრუნვა' UNION ALL
    SELECT 1, 'გრამატიკა/ლინგვისტიკა', 'დრო' UNION ALL
    SELECT 1, 'გრამატიკა/ლინგვისტიკა', 'რიცხვი' UNION ALL
    SELECT 1, 'გრამატიკა/ლინგვისტიკა', 'სქესი' UNION ALL
    SELECT 1, 'სახის ნაწილები', 'თვალი' UNION ALL
    SELECT 1, 'სახის ნაწილები', 'ყური' UNION ALL
    SELECT 1, 'სახის ნაწილები', 'ცხვირი' UNION ALL
    SELECT 1, 'სახის ნაწილები', 'ტუჩი' UNION ALL
    SELECT 1, 'სხეულის ნაწილები სხვა მნიშვნელობით', 'მუხლი' UNION ALL
    SELECT 1, 'სხეულის ნაწილები სხვა მნიშვნელობით', 'თავი' UNION ALL
    SELECT 1, 'სხეულის ნაწილები სხვა მნიშვნელობით', 'პირი' UNION ALL
    SELECT 1, 'სხეულის ნაწილები სხვა მნიშვნელობით', 'ენა' UNION ALL

    SELECT 2, 'ტრანსპორტი', 'მანქანა' UNION ALL
    SELECT 2, 'ტრანსპორტი', 'ავტობუსი' UNION ALL
    SELECT 2, 'ტრანსპორტი', 'მატარებელი' UNION ALL
    SELECT 2, 'ტრანსპორტი', 'თვითმფრინავი' UNION ALL
    SELECT 2, 'რეპტილიები/ამფიბიები', 'გველი' UNION ALL
    SELECT 2, 'რეპტილიები/ამფიბიები', 'ხვლიკი' UNION ALL
    SELECT 2, 'რეპტილიები/ამფიბიები', 'ნიანგი' UNION ALL
    SELECT 2, 'რეპტილიები/ამფიბიები', 'ქამელეონი' UNION ALL
    SELECT 2, 'ბანქოს კარტები', 'ვალეტი' UNION ALL
    SELECT 2, 'ბანქოს კარტები', 'დამა' UNION ALL
    SELECT 2, 'ბანქოს კარტები', 'ტუზი' UNION ALL
    SELECT 2, 'ბანქოს კარტები', 'მეფე' UNION ALL
    SELECT 2, 'ჭადრაკის ფიგურები', 'კუ' UNION ALL
    SELECT 2, 'ჭადრაკის ფიგურები', 'ლაზიერი' UNION ALL
    SELECT 2, 'ჭადრაკის ფიგურები', 'პაიკი' UNION ALL
    SELECT 2, 'ჭადრაკის ფიგურები', 'ეტლი' UNION ALL

    SELECT 3, 'ბოსტნეული', 'კიტრი' UNION ALL
    SELECT 3, 'ბოსტნეული', 'პომიდორი' UNION ALL
    SELECT 3, 'ბოსტნეული', 'ბადრიჯანი' UNION ALL
    SELECT 3, 'ბოსტნეული', 'ნიორი' UNION ALL
    SELECT 3, 'ყვავილები', 'მიხაკი' UNION ALL
    SELECT 3, 'ყვავილები', 'ტიტა' UNION ALL
    SELECT 3, 'ყვავილები', 'ენძელა' UNION ALL
    SELECT 3, 'ყვავილები', 'გვირილა' UNION ALL
    SELECT 3, 'კოსმოსი/ბუნებრივი ობიექტები', 'მზე' UNION ALL
    SELECT 3, 'კოსმოსი/ბუნებრივი ობიექტები', 'მთვარე' UNION ALL
    SELECT 3, 'კოსმოსი/ბუნებრივი ობიექტები', 'ვარსკვლავი' UNION ALL
    SELECT 3, 'კოსმოსი/ბუნებრივი ობიექტები', 'ღრუბელი' UNION ALL
    SELECT 3, 'სიტყვები, რომლებიც იღებენ ბოლოსართს "-სფერი"', 'ვარდი' UNION ALL
    SELECT 3, 'სიტყვები, რომლებიც იღებენ ბოლოსართს "-სფერი"', 'იასამანი' UNION ALL
    SELECT 3, 'სიტყვები, რომლებიც იღებენ ბოლოსართს "-სფერი"', 'ცა' UNION ALL
    SELECT 3, 'სიტყვები, რომლებიც იღებენ ბოლოსართს "-სფერი"', 'სტაფილო' UNION ALL

    SELECT 4, 'მეტეოროლოგია/ნალექი', 'წვიმა' UNION ALL
    SELECT 4, 'მეტეოროლოგია/ნალექი', 'თოვლი' UNION ALL
    SELECT 4, 'მეტეოროლოგია/ნალექი', 'სეტყვა' UNION ALL
    SELECT 4, 'მეტეოროლოგია/ნალექი', 'ნისლი' UNION ALL
    SELECT 4, 'მღრღნელები', 'ვირთხა' UNION ALL
    SELECT 4, 'მღრღნელები', 'ციყვი' UNION ALL
    SELECT 4, 'მღრღნელები', 'ზაზუნა' UNION ALL
    SELECT 4, 'მღრღნელები', 'თახვი' UNION ALL
    SELECT 4, 'ხილი და კენკრა', 'მსხალი' UNION ALL
    SELECT 4, 'ხილი და კენკრა', 'ატამი' UNION ALL
    SELECT 4, 'ხილი და კენკრა', 'ყურძენი' UNION ALL
    SELECT 4, 'ხილი და კენკრა', 'ქლიავი' UNION ALL
    SELECT 4, 'IT/ტექნოლოგიური ტერმინები', 'თაგვი' UNION ALL
    SELECT 4, 'IT/ტექნოლოგიური ტერმინები', 'ღრუბელი' UNION ALL
    SELECT 4, 'IT/ტექნოლოგიური ტერმინები', 'ვაშლი' UNION ALL
    SELECT 4, 'IT/ტექნოლოგიური ტერმინები', 'მაყვალი' UNION ALL

    SELECT 5, 'სახლის/შენობის ნაწილები', 'კედელი' UNION ALL
    SELECT 5, 'სახლის/შენობის ნაწილები', 'ჭერი' UNION ALL
    SELECT 5, 'სახლის/შენობის ნაწილები', 'იატაკი' UNION ALL
    SELECT 5, 'სახლის/შენობის ნაწილები', 'სახურავი' UNION ALL
    SELECT 5, 'სახის ნაწილები', 'თვალი' UNION ALL
    SELECT 5, 'სახის ნაწილები', 'ყური' UNION ALL
    SELECT 5, 'სახის ნაწილები', 'ცხვირი' UNION ALL
    SELECT 5, 'სახის ნაწილები', 'ტუჩი' UNION ALL
    SELECT 5, 'ლანდშაფტი/გარემო', 'მთა' UNION ALL
    SELECT 5, 'ლანდშაფტი/გარემო', 'ტყე' UNION ALL
    SELECT 5, 'ლანდშაფტი/გარემო', 'მდინარე' UNION ALL
    SELECT 5, 'ლანდშაფტი/გარემო', 'ტბა' UNION ALL
    SELECT 5, 'სიტყვები, რომლებიც წინსართად იღებენ "დედა"-ს', 'ენა' UNION ALL
    SELECT 5, 'სიტყვები, რომლებიც წინსართად იღებენ "დედა"-ს', 'მიწა' UNION ALL
    SELECT 5, 'სიტყვები, რომლებიც წინსართად იღებენ "დედა"-ს', 'ბოძი' UNION ALL
    SELECT 5, 'სიტყვები, რომლებიც წინსართად იღებენ "დედა"-ს', 'ბუნება' UNION ALL

    SELECT 6, 'ფიზიკური მახასიათებლები', 'მსუბუქი' UNION ALL
    SELECT 6, 'ფიზიკური მახასიათებლები', 'დიდი' UNION ALL
    SELECT 6, 'ფიზიკური მახასიათებლები', 'მცირე' UNION ALL
    SELECT 6, 'ფიზიკური მახასიათებლები', 'გრძელი' UNION ALL
    SELECT 6, 'გეომეტრია/ნახაზი', 'მონაკვეთი' UNION ALL
    SELECT 6, 'გეომეტრია/ნახაზი', 'სხივი' UNION ALL
    SELECT 6, 'გეომეტრია/ნახაზი', 'კუთხე' UNION ALL
    SELECT 6, 'გეომეტრია/ნახაზი', 'მრუდი' UNION ALL
    SELECT 6, 'სხეულის დანამატები/ქსოვილები', 'კბილი' UNION ALL
    SELECT 6, 'სხეულის დანამატები/ქსოვილები', 'თმა' UNION ALL
    SELECT 6, 'სხეულის დანამატები/ქსოვილები', 'კანი' UNION ALL
    SELECT 6, 'სხეულის დანამატები/ქსოვილები', 'ძვალი' UNION ALL
    SELECT 6, 'სასვენი ნიშნები', 'მძიმე' UNION ALL
    SELECT 6, 'სასვენი ნიშნები', 'ფრჩხილი' UNION ALL
    SELECT 6, 'სასვენი ნიშნები', 'ბრჭყალი' UNION ALL
    SELECT 6, 'სასვენი ნიშნები', 'წერტილი'
) w ON p.puzzle_date = DATE_ADD(CURDATE(), INTERVAL w.day_offset DAY) AND gr.category = w.category;
