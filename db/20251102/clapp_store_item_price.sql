-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: clapp
-- ------------------------------------------------------
-- Server version	8.0.43

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
-- Table structure for table `store_item_price`
--

DROP TABLE IF EXISTS `store_item_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_item_price` (
  `id` bigint NOT NULL,
  `min_quantity` int DEFAULT NULL,
  `price` double NOT NULL,
  `status` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `product_lot_id` bigint DEFAULT NULL,
  `service_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `valid_from` datetime(6) DEFAULT NULL,
  `valid_to` datetime(6) DEFAULT NULL,
  `override_type` enum('BULK','CUSTOMER_SPECIFIC','DEFAULT','PROMOTION') DEFAULT NULL,
  `currency` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2uwbu3b8x8qijtwfrbk3qquf2` (`product_lot_id`),
  KEY `FK2v4bcb8gluks5arhrv49icf1q` (`service_id`),
  CONSTRAINT `FK2uwbu3b8x8qijtwfrbk3qquf2` FOREIGN KEY (`product_lot_id`) REFERENCES `product_lot` (`id`),
  CONSTRAINT `FK2v4bcb8gluks5arhrv49icf1q` FOREIGN KEY (`service_id`) REFERENCES `store_offered_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_item_price`
--

LOCK TABLES `store_item_price` WRITE;
/*!40000 ALTER TABLE `store_item_price` DISABLE KEYS */;
INSERT INTO `store_item_price` VALUES (352,NULL,499,0,'2025-11-02 21:55:52.596842',0,NULL,1,'2025-11-02 22:08:59.719051','2025-11-02 21:55:52.547824','2025-11-02 22:08:59.658491',NULL,1),(402,NULL,529,0,'2025-11-02 22:08:59.703354',0,NULL,1,'2025-11-02 22:14:03.174153','2025-11-02 22:08:59.671946','2025-11-02 22:14:03.124555',NULL,1),(452,NULL,519,0,'2025-11-02 22:14:03.156197',0,NULL,1,'2025-11-02 22:14:03.156197','2025-11-02 22:14:03.124555',NULL,NULL,1);
/*!40000 ALTER TABLE `store_item_price` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-02 22:18:38
