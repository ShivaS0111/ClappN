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
-- Table structure for table `address_management`
--

DROP TABLE IF EXISTS `address_management`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address_management` (
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `area_id` bigint DEFAULT NULL,
  `country_id` bigint DEFAULT NULL,
  `district_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL,
  `landmark_id` bigint DEFAULT NULL,
  `place_id` bigint DEFAULT NULL,
  `reference_id` bigint NOT NULL,
  `region_id` bigint DEFAULT NULL,
  `subregion_id` bigint DEFAULT NULL,
  `zipcode_id` bigint DEFAULT NULL,
  `postal_code` varchar(20) NOT NULL,
  `city` varchar(50) NOT NULL,
  `state` varchar(50) NOT NULL,
  `street` varchar(100) NOT NULL,
  `digi_pin` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9ywpei1luy05gy8p74bujox0i` (`area_id`),
  KEY `FKih4iylojf7r67fe81mh0cnxoo` (`country_id`),
  KEY `FK6ihlk4s9ulq39nwg5opyocnfv` (`district_id`),
  KEY `FK5l8v4e2s23jrd329tamd8g8dm` (`landmark_id`),
  KEY `FKloclbc7707kq90m3b9i02fel5` (`place_id`),
  KEY `FKtklf5ed3g3g5ytugcqls3fw3h` (`region_id`),
  KEY `FK17hhkbi7ft0gi75os9cqh8ona` (`subregion_id`),
  KEY `FKbp8ijnkpcwe2n17h4s1sncv5c` (`zipcode_id`),
  CONSTRAINT `FK17hhkbi7ft0gi75os9cqh8ona` FOREIGN KEY (`subregion_id`) REFERENCES `subregion` (`id`),
  CONSTRAINT `FK5l8v4e2s23jrd329tamd8g8dm` FOREIGN KEY (`landmark_id`) REFERENCES `landmark` (`id`),
  CONSTRAINT `FK6ihlk4s9ulq39nwg5opyocnfv` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`),
  CONSTRAINT `FK9ywpei1luy05gy8p74bujox0i` FOREIGN KEY (`area_id`) REFERENCES `area` (`id`),
  CONSTRAINT `FKbp8ijnkpcwe2n17h4s1sncv5c` FOREIGN KEY (`zipcode_id`) REFERENCES `zipcode` (`id`),
  CONSTRAINT `FKih4iylojf7r67fe81mh0cnxoo` FOREIGN KEY (`country_id`) REFERENCES `country` (`id`),
  CONSTRAINT `FKloclbc7707kq90m3b9i02fel5` FOREIGN KEY (`place_id`) REFERENCES `place` (`id`),
  CONSTRAINT `FKtklf5ed3g3g5ytugcqls3fw3h` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address_management`
--

LOCK TABLES `address_management` WRITE;
/*!40000 ALTER TABLE `address_management` DISABLE KEYS */;
/*!40000 ALTER TABLE `address_management` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-02 22:18:37
