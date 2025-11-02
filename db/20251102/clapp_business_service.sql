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
-- Table structure for table `business_service`
--

DROP TABLE IF EXISTS `business_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_service` (
  `amount` float NOT NULL,
  `created_at` date DEFAULT NULL,
  `status` int NOT NULL,
  `updated_at` date DEFAULT NULL,
  `business_type` bigint NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `currency` bigint DEFAULT NULL,
  `id` bigint NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_eoe588si6m4t8rvnuv941oyol` (`service_name`),
  KEY `FKd9loemx8qjvnjfqo0sl1fxsku` (`business_type`),
  CONSTRAINT `FKd9loemx8qjvnjfqo0sl1fxsku` FOREIGN KEY (`business_type`) REFERENCES `business_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_service`
--

LOCK TABLES `business_service` WRITE;
/*!40000 ALTER TABLE `business_service` DISABLE KEYS */;
INSERT INTO `business_service` VALUES (499,'2025-11-02',0,'2025-11-02',11,NULL,1,1,'Complete maintenance including oiling, brake check, and chain adjustment.','General Bike Service'),(299,'2025-11-02',0,'2025-11-02',11,NULL,1,2,'Draining old oil and refilling with manufacturer-recommended engine oil.','Engine Oil Change'),(399,'2025-11-02',0,'2025-11-02',11,NULL,1,3,'Brake inspection, pad replacement, and fluid top-up.','Brake Service'),(899,'2025-11-02',0,'2025-11-02',11,NULL,1,4,'Clutch plate inspection and replacement if necessary.','Clutch Overhaul'),(1499,'2025-11-02',0,'2025-11-02',11,NULL,1,5,'Tyre removal, fitting new tyre, and balancing.','Tyre Replacement');
/*!40000 ALTER TABLE `business_service` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-02 22:18:35
