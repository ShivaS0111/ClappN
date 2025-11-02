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
-- Table structure for table `product_lot`
--

DROP TABLE IF EXISTS `product_lot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_lot` (
  `active` bit(1) NOT NULL,
  `blocked` int NOT NULL,
  `quantity` int NOT NULL,
  `sold` int NOT NULL,
  `unit_price` double NOT NULL,
  `country_id` bigint NOT NULL,
  `currency_id` bigint NOT NULL,
  `expiry_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `purchased_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `lot_code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK89vae19yuyyvv8fcajrjemnde` (`country_id`),
  KEY `FK53x5bylitl23p4cdlal196sx4` (`currency_id`),
  KEY `FK8vttfpofteuq1blkfudfnreh7` (`product_id`),
  CONSTRAINT `FK53x5bylitl23p4cdlal196sx4` FOREIGN KEY (`currency_id`) REFERENCES `currency` (`id`),
  CONSTRAINT `FK89vae19yuyyvv8fcajrjemnde` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`),
  CONSTRAINT `FK8vttfpofteuq1blkfudfnreh7` FOREIGN KEY (`product_id`) REFERENCES `store_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_lot`
--

LOCK TABLES `product_lot` WRITE;
/*!40000 ALTER TABLE `product_lot` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_lot` ENABLE KEYS */;
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
