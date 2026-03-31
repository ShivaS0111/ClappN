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
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_2ojme20jpga3r4r79tdso17gi` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=171 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,NULL,'store.create'),(2,NULL,'store.read'),(3,NULL,'store.update'),(4,NULL,'store.delete'),(5,NULL,'store.metrics'),(6,NULL,'store.settings'),(7,NULL,'product.create'),(8,NULL,'product.read'),(9,NULL,'product.update'),(10,NULL,'product.delete'),(11,NULL,'product.inventory'),(12,NULL,'service.create'),(13,NULL,'service.read'),(14,NULL,'service.update'),(15,NULL,'service.delete'),(16,NULL,'business.create'),(17,NULL,'business.read'),(18,NULL,'business.update'),(19,NULL,'business.delete'),(20,NULL,'vendor.create'),(21,NULL,'vendor.read'),(22,NULL,'vendor.update'),(23,NULL,'vendor.delete'),(24,NULL,'vendor.approve'),(25,NULL,'vendor.suspend'),(26,NULL,'vendor.onboarding'),(27,NULL,'vendor.analytics'),(28,NULL,'vendor.commission.view'),(29,NULL,'vendor.commission.manage'),(30,NULL,'category.create'),(31,NULL,'category.read'),(32,NULL,'category.update'),(33,NULL,'category.delete'),(34,NULL,'coupon.create'),(35,NULL,'coupon.read'),(36,NULL,'coupon.update'),(37,NULL,'coupon.delete'),(38,NULL,'offer.create'),(39,NULL,'offer.read'),(40,NULL,'offer.update'),(41,NULL,'offer.delete'),(42,NULL,'package.create'),(43,NULL,'package.read'),(44,NULL,'package.update'),(45,NULL,'package.delete'),(46,NULL,'approval.create'),(47,NULL,'approval.read'),(48,NULL,'approval.update'),(49,NULL,'approval.delete'),(50,NULL,'approval.vendor'),(51,NULL,'approval.product'),(52,NULL,'approval.service'),(53,NULL,'report.create'),(54,NULL,'report.read'),(55,NULL,'report.update'),(56,NULL,'report.schedule'),(57,NULL,'price_history.read'),(58,NULL,'price_history.update'),(59,NULL,'store_product.create'),(60,NULL,'store_product.read'),(61,NULL,'store_product.update'),(62,NULL,'store_product.delete'),(63,NULL,'store_service.create'),(64,NULL,'store_service.read'),(65,NULL,'store_service.update'),(66,NULL,'store_service.delete'),(67,NULL,'user.create'),(68,NULL,'user.read'),(69,NULL,'user.update'),(70,NULL,'user.delete'),(71,NULL,'user.permissions'),(72,NULL,'order.create'),(73,NULL,'order.read'),(74,NULL,'order.update'),(75,NULL,'order.cancel'),(76,NULL,'order.refund'),(77,NULL,'finance.view'),(78,NULL,'finance.pricing'),(79,NULL,'finance.reports'),(80,NULL,'finance.coupons'),(81,NULL,'reports.sales'),(82,NULL,'reports.user'),(83,NULL,'reports.inventory'),(84,NULL,'reports.system'),(85,NULL,'marketing.campaigns'),(86,NULL,'marketing.packages'),(87,NULL,'marketing.analytics'),(88,NULL,'system.settings'),(89,NULL,'system.logs'),(90,NULL,'system.backup'),(91,NULL,'system.maintenance'),(92,NULL,'security.access'),(93,NULL,'security.audit'),(94,NULL,'security.monitor'),(95,NULL,'content.create'),(96,NULL,'content.read'),(97,NULL,'content.update'),(98,NULL,'content.delete'),(99,NULL,'content.publish'),(100,NULL,'content.review'),(101,NULL,'approval.discount'),(102,NULL,'approval.refund'),(103,NULL,'approval.vendor_payout'),(104,NULL,'analytics.dashboard'),(105,NULL,'analytics.sales'),(106,NULL,'analytics.customer'),(107,NULL,'analytics.inventory'),(108,NULL,'analytics.vendor'),(109,NULL,'analytics.export'),(110,NULL,'customer.create'),(111,NULL,'customer.read'),(112,NULL,'customer.update'),(113,NULL,'customer.delete'),(114,NULL,'customer.preferences'),(115,NULL,'customer.segments'),(116,NULL,'warehouse.create'),(117,NULL,'warehouse.read'),(118,NULL,'warehouse.update'),(119,NULL,'warehouse.delete'),(120,NULL,'warehouse.operations'),(121,NULL,'warehouse.transfers'),(122,NULL,'shipping.create'),(123,NULL,'shipping.read'),(124,NULL,'shipping.update'),(125,NULL,'shipping.cancel'),(126,NULL,'shipping.track'),(127,NULL,'shipping.rates'),(128,NULL,'inventory.adjust'),(129,NULL,'inventory.audit'),(130,NULL,'inventory.forecast'),(131,NULL,'inventory.alerts'),(132,NULL,'inventory.reorder'),(133,NULL,'returns.create'),(134,NULL,'returns.read'),(135,NULL,'returns.approve'),(136,NULL,'returns.process'),(137,NULL,'returns.refund'),(138,NULL,'returns.reshelve'),(139,NULL,'quality.inspect'),(140,NULL,'quality.report'),(141,NULL,'quality.escalate'),(142,NULL,'quality.standards'),(143,NULL,'notification.create'),(144,NULL,'notification.read'),(145,NULL,'notification.broadcast'),(146,NULL,'notification.templates'),(147,NULL,'dispute.view'),(148,NULL,'dispute.create'),(149,NULL,'dispute.resolve'),(150,NULL,'dispute.escalate'),(151,NULL,'subscription.create'),(152,NULL,'subscription.read'),(153,NULL,'subscription.manage'),(154,NULL,'subscription.cancel'),(155,NULL,'review.moderate'),(156,NULL,'review.respond'),(157,NULL,'review.hide'),(158,NULL,'review.analytics'),(159,NULL,'tax.manage'),(160,NULL,'tax.reports'),(161,NULL,'compliance.view'),(162,NULL,'compliance.audit'),(163,NULL,'bulk.import'),(164,NULL,'bulk.export'),(165,NULL,'bulk.update'),(166,NULL,'bulk.delete'),(167,NULL,'payout.process'),(168,NULL,'payout.view'),(169,NULL,'payout.dispute'),(170,NULL,'payout.reconcile');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-31 12:41:30
