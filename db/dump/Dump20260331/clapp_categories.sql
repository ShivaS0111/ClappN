CREATE DATABASE  IF NOT EXISTS `clapp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `clapp`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: clapp
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsaok720gsu4u2wrgbk10b5n8d` (`parent_id`),
  CONSTRAINT `FKsaok720gsu4u2wrgbk10b5n8d` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,NULL,'Electronics',1,NULL),(2,NULL,'Fashion',1,NULL),(3,NULL,'Home & Kitchen',0,NULL),(4,NULL,'Sports & Outdoors',0,NULL),(5,NULL,'Books & Stationery',0,NULL),(6,NULL,'Automotive',0,NULL),(7,NULL,'Toys & Games',0,NULL),(8,NULL,'Beauty & Personal Care',0,NULL),(9,NULL,'Health & Wellness',0,NULL),(10,NULL,'Grocery & Gourmet',0,NULL),(11,NULL,'Jewelry',0,NULL),(12,NULL,'Baby Products',0,NULL),(13,NULL,'Pet Supplies',0,NULL),(14,NULL,'Garden & Tools',0,NULL),(15,NULL,'Office Supplies',0,NULL),(16,NULL,'Music Instruments',0,NULL),(17,NULL,'Movies & Entertainment',0,NULL),(18,NULL,'Industrial Supplies',0,NULL),(19,NULL,'Travel & Luggage',0,NULL),(20,NULL,'Software',0,NULL),(21,NULL,'Mobiles',1,1),(22,NULL,'Laptops',1,1),(23,NULL,'Cameras',1,1),(24,NULL,'Audio Devices',1,1),(25,NULL,'Wearables',1,1),(26,NULL,'Gaming Consoles',1,1),(27,NULL,'Smart Home Devices',1,1),(28,NULL,'Accessories',1,1),(73,NULL,'Furniture',0,3),(74,NULL,'Decor',0,3),(75,NULL,'Kitchenware',0,3),(76,NULL,'Appliances',0,3),(77,NULL,'Storage',0,3),(78,NULL,'Sofas',0,73),(79,NULL,'Beds',0,73),(80,NULL,'Tables',0,73),(81,NULL,'Chairs',0,73),(82,NULL,'Wardrobes',0,73),(83,NULL,'Wall Art',0,74),(84,NULL,'Clocks',0,74),(85,NULL,'Lamps',0,74),(86,NULL,'Curtains',0,74),(87,NULL,'Rugs',0,74),(88,NULL,'Vases',0,74),(89,NULL,'Cookware',0,75),(90,NULL,'Dinner Sets',0,75),(91,NULL,'Cutlery',0,75),(92,NULL,'Storage Containers',0,75),(93,NULL,'Utensils',0,75),(94,NULL,'Refrigerators',0,76),(95,NULL,'Washing Machines',0,76),(96,NULL,'Microwaves',0,76),(97,NULL,'Blenders',0,76),(98,NULL,'Vacuum Cleaners',0,76),(99,NULL,'Fitness Equipment',0,4),(100,NULL,'Outdoor Gear',0,4),(101,NULL,'Team Sports',0,4),(102,NULL,'Cycling',0,4),(103,NULL,'Camping & Hiking',0,4),(104,NULL,'Treadmills',0,99),(105,NULL,'Dumbbells',0,99),(106,NULL,'Yoga Mats',0,99),(107,NULL,'Resistance Bands',0,99),(108,NULL,'Cricket',0,101),(109,NULL,'Football',0,101),(110,NULL,'Basketball',0,101),(111,NULL,'Badminton',0,101),(112,NULL,'Cricket Bats',0,108),(113,NULL,'Cricket Balls',0,108),(114,NULL,'Football Shoes',0,109),(115,NULL,'Goalkeeper Gloves',0,109),(116,NULL,'Basketballs',0,110),(117,NULL,'Badminton Rackets',0,111),(118,NULL,'Shuttlecocks',0,111),(119,NULL,'Books',0,5),(120,NULL,'Notebooks',0,5),(121,NULL,'Art Supplies',0,5),(122,NULL,'Office Essentials',0,5),(123,NULL,'Fiction',0,119),(124,NULL,'Non-Fiction',0,119),(125,NULL,'Comics',0,119),(126,NULL,'Educational',0,119),(127,NULL,'Children',0,119),(128,NULL,'Car Accessories',0,6),(129,NULL,'Bike Accessories',0,6),(130,NULL,'Oils & Fluids',0,6),(131,NULL,'Tyres',0,6),(132,NULL,'Cleaning Kits',0,6),(133,NULL,'Skincare',0,8),(134,NULL,'Haircare',0,8),(135,NULL,'Fragrances',0,8),(136,NULL,'Bath & Body',0,8),(137,NULL,'Beverages',0,10),(138,NULL,'Snacks',0,10),(139,NULL,'Dairy Products',0,10),(140,NULL,'Cooking Essentials',0,10),(141,NULL,'Organic Food',0,10),(142,NULL,'Gold Jewelry',0,11),(143,NULL,'Silver Jewelry',0,11),(144,NULL,'Diamond Jewelry',0,11),(145,NULL,'Fashion Jewelry',0,11),(146,NULL,'Dog Supplies',0,13),(147,NULL,'Cat Supplies',0,13),(148,NULL,'Fish Supplies',0,13),(149,NULL,'Bird Supplies',0,13),(150,NULL,'Suitcases',0,19),(151,NULL,'Backpacks',0,19),(152,NULL,'Travel Accessories',0,19),(153,NULL,'Duffel Bags',0,19),(154,NULL,'Operating Systems',0,20),(155,NULL,'Office Suites',0,20),(156,NULL,'Security Software',0,20),(157,NULL,'Development Tools',0,20),(158,NULL,'Plants & Seeds',0,14),(159,NULL,'Gardening Tools',0,14),(160,NULL,'Outdoor Furniture',0,14),(161,NULL,'Lawn Care',0,14),(162,NULL,'Planters',0,14),(163,NULL,'Guitars',0,16),(164,NULL,'Keyboards',0,16),(165,NULL,'Drums',0,16),(166,NULL,'Microphones',0,16),(167,NULL,'DJ Equipment',0,16),(168,NULL,'Diapers',0,12),(169,NULL,'Baby Food',0,12),(170,NULL,'Toys',0,12),(171,NULL,'Strollers',0,12),(172,NULL,'Clothing',0,12),(173,NULL,'Supplements',0,9),(174,NULL,'Medical Equipment',0,9),(175,NULL,'Personal Hygiene',0,9),(176,NULL,'First Aid',0,9),(177,NULL,'Fitness Nutrition',0,9),(178,NULL,'Action Figures',0,7),(179,NULL,'Board Games',0,7),(180,NULL,'Educational Toys',0,7),(181,NULL,'Outdoor Toys',0,7),(182,NULL,'Puzzles',0,7),(183,NULL,'Dolls',0,7),(184,NULL,'Safety Equipment',0,18),(185,NULL,'Power Tools',0,18),(186,NULL,'Electrical Supplies',0,18),(187,NULL,'Fasteners',0,18),(188,NULL,'Hand Tools',0,18),(189,NULL,'DVDs',0,17),(190,NULL,'Blu-Ray',0,17),(191,NULL,'Merchandise',0,17),(192,NULL,'Collectibles',0,17),(193,NULL,'Notebooks',0,15),(194,NULL,'Pens & Pencils',0,15),(195,NULL,'Folders',0,15),(196,NULL,'Desk Accessories',0,15),(197,NULL,'Paper',0,15),(198,NULL,'Envelopes',0,15),(199,NULL,'Food',0,NULL);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-31 12:41:29
