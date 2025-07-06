-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: clapp
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Table structure for table `business_type`
--

DROP TABLE IF EXISTS `business_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_type` (
  `id` bigint NOT NULL,
  `business_name` varchar(255) NOT NULL,
  `created_at` date DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlkea5upn7687vebwnkcekk2vp` (`business_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_type`
--

LOCK TABLES `business_type` WRITE;
/*!40000 ALTER TABLE `business_type` DISABLE KEYS */;
INSERT INTO `business_type` VALUES (1,'Retail Store','2025-06-22',NULL,'A place that sells goods directly to consumers.',0,'2025-06-22'),(2,'Restaurant','2025-06-22',NULL,'An establishment where meals are prepared and served to customers.',0,'2025-06-22'),(3,'IT Services','2025-06-22',NULL,'A business offering technology solutions, support, and consulting.',0,'2025-06-22'),(4,'Construction','2025-06-22',NULL,'A business involved in building infrastructure, homes, and commercial properties.',0,'2025-06-22'),(5,'Healthcare Clinic','2025-06-22',NULL,'Provides medical services to patients on an outpatient basis.',0,'2025-06-22'),(6,'Education & Training','2025-06-22',NULL,'Offers educational courses and professional training.',0,'2025-06-22'),(7,'Logistics & Transportation','2025-06-22',NULL,'Handles the movement of goods and people from one place to another.',0,'2025-06-22'),(8,'Financial Services','2025-06-22',NULL,'Provides banking, investment, insurance, and other money-related services.',0,'2025-06-22'),(9,'Real Estate','2025-06-22',NULL,'Deals with buying, selling, and managing properties.',0,'2025-06-22'),(10,'Entertainment','2025-06-22',NULL,'Produces and distributes content for leisure, including movies, music, and events.',0,'2025-06-22'),(11,'Bike Mechanic Services','2025-06-22',NULL,'jkhk jj',0,'2025-06-22'),(52,'Fitness Training','2025-07-05',NULL,'Personalized workout plans and diet consulting',0,'2025-07-05'),(102,'String','2025-07-06',NULL,'String',0,'2025-07-06');
/*!40000 ALTER TABLE `business_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-06 14:52:08
