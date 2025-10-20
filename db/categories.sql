use clapp;
-- ==============================================
-- CATEGORY ENTITY DATA (Multi-level, 200+ items)
-- ==============================================

-- Level 1 (Top-Level Categories)
INSERT INTO categories (id, name, parent_id) VALUES
(1, 'Electronics', NULL),
(2, 'Fashion', NULL),
(3, 'Home & Kitchen', NULL),
(4, 'Sports & Outdoors', NULL),
(5, 'Books & Stationery', NULL),
(6, 'Automotive', NULL),
(7, 'Toys & Games', NULL),
(8, 'Beauty & Personal Care', NULL),
(9, 'Health & Wellness', NULL),
(10, 'Grocery & Gourmet', NULL),
(11, 'Jewelry', NULL),
(12, 'Baby Products', NULL),
(13, 'Pet Supplies', NULL),
(14, 'Garden & Tools', NULL),
(15, 'Office Supplies', NULL),
(16, 'Music Instruments', NULL),
(17, 'Movies & Entertainment', NULL),
(18, 'Industrial Supplies', NULL),
(19, 'Travel & Luggage', NULL),
(20, 'Software', NULL);

-- ==============================================
-- Electronics and Subcategories
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(21, 'Mobiles', 1),
(22, 'Laptops', 1),
(23, 'Cameras', 1),
(24, 'Audio Devices', 1),
(25, 'Wearables', 1),
(26, 'Gaming Consoles', 1),
(27, 'Smart Home Devices', 1),
(28, 'Accessories', 1);

-- Mobiles → Brands
INSERT INTO categories (id, name, parent_id) VALUES
(29, 'Android Phones', 21),
(30, 'iPhones', 21),
(31, 'Feature Phones', 21),
(32, 'Samsung', 29),
(33, 'Xiaomi', 29),
(34, 'OnePlus', 29),
(35, 'Vivo', 29),
(36, 'Oppo', 29);

-- Laptops → Types
INSERT INTO categories (id, name, parent_id) VALUES
(37, 'Gaming Laptops', 22),
(38, 'Ultrabooks', 22),
(39, 'Business Laptops', 22),
(40, '2-in-1 Laptops', 22),
(41, 'Laptop Accessories', 22);

-- Gaming Laptops → Brands
INSERT INTO categories (id, name, parent_id) VALUES
(42, 'ASUS ROG', 37),
(43, 'MSI', 37),
(44, 'Alienware', 37),
(45, 'Acer Predator', 37);

-- Cameras
INSERT INTO categories (id, name, parent_id) VALUES
(46, 'DSLR', 23),
(47, 'Mirrorless', 23),
(48, 'Action Cameras', 23),
(49, 'Drones', 23),
(50, 'Camera Accessories', 23);

-- ==============================================
-- Fashion
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(51, 'Men', 2),
(52, 'Women', 2),
(53, 'Kids', 2);

-- Men → Clothing
INSERT INTO categories (id, name, parent_id) VALUES
(54, 'Clothing', 51),
(55, 'Footwear', 51),
(56, 'Accessories', 51);

INSERT INTO categories (id, name, parent_id) VALUES
(57, 'T-Shirts', 54),
(58, 'Shirts', 54),
(59, 'Jeans', 54),
(60, 'Jackets', 54),
(61, 'Ethnic Wear', 54);

-- Women → Clothing
INSERT INTO categories (id, name, parent_id) VALUES
(62, 'Clothing', 52),
(63, 'Footwear', 52),
(64, 'Jewellery', 52),
(65, 'Handbags', 52),
(66, 'Makeup', 52),
(67, 'Perfumes', 52);

INSERT INTO categories (id, name, parent_id) VALUES
(68, 'Sarees', 62),
(69, 'Dresses', 62),
(70, 'Tops', 62),
(71, 'Kurtas', 62),
(72, 'Lehengas', 62);

-- ==============================================
-- Home & Kitchen
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(73, 'Furniture', 3),
(74, 'Decor', 3),
(75, 'Kitchenware', 3),
(76, 'Appliances', 3),
(77, 'Storage', 3);

INSERT INTO categories (id, name, parent_id) VALUES
(78, 'Sofas', 73),
(79, 'Beds', 73),
(80, 'Tables', 73),
(81, 'Chairs', 73),
(82, 'Wardrobes', 73);

-- Decor
INSERT INTO categories (id, name, parent_id) VALUES
(83, 'Wall Art', 74),
(84, 'Clocks', 74),
(85, 'Lamps', 74),
(86, 'Curtains', 74),
(87, 'Rugs', 74),
(88, 'Vases', 74);

-- Kitchenware
INSERT INTO categories (id, name, parent_id) VALUES
(89, 'Cookware', 75),
(90, 'Dinner Sets', 75),
(91, 'Cutlery', 75),
(92, 'Storage Containers', 75),
(93, 'Utensils', 75);

-- Appliances
INSERT INTO categories (id, name, parent_id) VALUES
(94, 'Refrigerators', 76),
(95, 'Washing Machines', 76),
(96, 'Microwaves', 76),
(97, 'Blenders', 76),
(98, 'Vacuum Cleaners', 76);

-- ==============================================
-- Sports & Outdoors
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(99, 'Fitness Equipment', 4),
(100, 'Outdoor Gear', 4),
(101, 'Team Sports', 4),
(102, 'Cycling', 4),
(103, 'Camping & Hiking', 4);

INSERT INTO categories (id, name, parent_id) VALUES
(104, 'Treadmills', 99),
(105, 'Dumbbells', 99),
(106, 'Yoga Mats', 99),
(107, 'Resistance Bands', 99);

INSERT INTO categories (id, name, parent_id) VALUES
(108, 'Cricket', 101),
(109, 'Football', 101),
(110, 'Basketball', 101),
(111, 'Badminton', 101);

INSERT INTO categories (id, name, parent_id) VALUES
(112, 'Cricket Bats', 108),
(113, 'Cricket Balls', 108),
(114, 'Football Shoes', 109),
(115, 'Goalkeeper Gloves', 109),
(116, 'Basketballs', 110),
(117, 'Badminton Rackets', 111),
(118, 'Shuttlecocks', 111);

-- ==============================================
-- Books & Stationery
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(119, 'Books', 5),
(120, 'Notebooks', 5),
(121, 'Art Supplies', 5),
(122, 'Office Essentials', 5);

INSERT INTO categories (id, name, parent_id) VALUES
(123, 'Fiction', 119),
(124, 'Non-Fiction', 119),
(125, 'Comics', 119),
(126, 'Educational', 119),
(127, 'Children', 119);

-- ==============================================
-- Automotive
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(128, 'Car Accessories', 6),
(129, 'Bike Accessories', 6),
(130, 'Oils & Fluids', 6),
(131, 'Tyres', 6),
(132, 'Cleaning Kits', 6);

-- ==============================================
-- Beauty & Personal Care
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(133, 'Skincare', 8),
(134, 'Haircare', 8),
(135, 'Fragrances', 8),
(136, 'Bath & Body', 8);

-- ==============================================
-- Grocery & Gourmet
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(137, 'Beverages', 10),
(138, 'Snacks', 10),
(139, 'Dairy Products', 10),
(140, 'Cooking Essentials', 10),
(141, 'Organic Food', 10);

-- ==============================================
-- Jewelry
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(142, 'Gold Jewelry', 11),
(143, 'Silver Jewelry', 11),
(144, 'Diamond Jewelry', 11),
(145, 'Fashion Jewelry', 11);

-- ==============================================
-- Pet Supplies
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(146, 'Dog Supplies', 13),
(147, 'Cat Supplies', 13),
(148, 'Fish Supplies', 13),
(149, 'Bird Supplies', 13);

-- ==============================================
-- Travel & Luggage
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(150, 'Suitcases', 19),
(151, 'Backpacks', 19),
(152, 'Travel Accessories', 19),
(153, 'Duffel Bags', 19);

-- ==============================================
-- Software
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(154, 'Operating Systems', 20),
(155, 'Office Suites', 20),
(156, 'Security Software', 20),
(157, 'Development Tools', 20);

-- ==============================================
-- Garden & Tools
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(158, 'Plants & Seeds', 14),
(159, 'Gardening Tools', 14),
(160, 'Outdoor Furniture', 14),
(161, 'Lawn Care', 14),
(162, 'Planters', 14);

-- ==============================================
-- Music Instruments
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(163, 'Guitars', 16),
(164, 'Keyboards', 16),
(165, 'Drums', 16),
(166, 'Microphones', 16),
(167, 'DJ Equipment', 16);

-- ==============================================
-- Baby Products
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(168, 'Diapers', 12),
(169, 'Baby Food', 12),
(170, 'Toys', 12),
(171, 'Strollers', 12),
(172, 'Clothing', 12);

-- ==============================================
-- Health & Wellness
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(173, 'Supplements', 9),
(174, 'Medical Equipment', 9),
(175, 'Personal Hygiene', 9),
(176, 'First Aid', 9),
(177, 'Fitness Nutrition', 9);

-- ==============================================
-- Toys & Games
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(178, 'Action Figures', 7),
(179, 'Board Games', 7),
(180, 'Educational Toys', 7),
(181, 'Outdoor Toys', 7),
(182, 'Puzzles', 7),
(183, 'Dolls', 7);

-- ==============================================
-- Industrial Supplies
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(184, 'Safety Equipment', 18),
(185, 'Power Tools', 18),
(186, 'Electrical Supplies', 18),
(187, 'Fasteners', 18),
(188, 'Hand Tools', 18);

-- ==============================================
-- Movies & Entertainment
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(189, 'DVDs', 17),
(190, 'Blu-Ray', 17),
(191, 'Merchandise', 17),
(192, 'Collectibles', 17);

-- ==============================================
-- Office Supplies
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(193, 'Notebooks', 15),
(194, 'Pens & Pencils', 15),
(195, 'Folders', 15),
(196, 'Desk Accessories', 15),
(197, 'Paper', 15),
(198, 'Envelopes', 15);

-- ==============================================
-- Final Filler Categories to reach ~220 total
-- ==============================================
INSERT INTO categories (id, name, parent_id) VALUES
(199, 'Mouse', 41),
(200, 'Keyboards', 41),
(201, 'Webcams', 41),
(202, 'Headsets', 41),
(203, 'Chargers', 28),
(204, 'Cables', 28),
(205, 'Adapters', 28),
(206, 'Smart Plugs', 27),
(207, 'Security Cameras', 27),
(208, 'Smart Lights', 27),
(209, 'Smart Thermostats', 27),
(210, 'VR Headsets', 25);
