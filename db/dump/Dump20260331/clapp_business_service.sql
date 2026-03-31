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
-- Table structure for table `business_service`
--

DROP TABLE IF EXISTS `business_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_service` (
  `id` bigint NOT NULL,
  `amount` float NOT NULL,
  `created_at` date DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `currency` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `duration` bigint DEFAULT NULL,
  `service_name` varchar(255) NOT NULL,
  `status` int NOT NULL,
  `updated_at` date DEFAULT NULL,
  `business_type` bigint NOT NULL,
  `gallery_urls` json DEFAULT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
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
INSERT INTO `business_service` VALUES (1,500,'2026-03-14',NULL,NULL,'Meat cutting at home',2,'Meat Cutting Service',1,'2026-03-14',1,NULL,NULL),(2,600,'2026-03-14',NULL,NULL,'met cutting',60,'Curry Cut / Biryani Cut @Home',1,'2026-03-14',1,NULL,NULL);
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

-- Dump completed on 2026-03-31 12:41:31
