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
-- Dumping events for database 'clapp'
--

--
-- Dumping routines for database 'clapp'
--
/*!50003 DROP PROCEDURE IF EXISTS `fix_auto_increment_all` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `fix_auto_increment_all`()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1
            @p1 = RETURNED_SQLSTATE, @p2 = MESSAGE_TEXT;
        SELECT CONCAT('ERROR: ', @p1, ' - ', @p2) AS error_message;
        DROP TEMPORARY TABLE IF EXISTS temp_fks;
        DROP TEMPORARY TABLE IF EXISTS temp_sql;
    END;

    -- Save foreign keys
    DROP TEMPORARY TABLE IF EXISTS temp_fks;
    CREATE TEMPORARY TABLE temp_fks AS
    SELECT
        TABLE_NAME,
        CONSTRAINT_NAME,
        COLUMN_NAME,
        REFERENCED_TABLE_NAME,
        REFERENCED_COLUMN_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND REFERENCED_TABLE_NAME IS NOT NULL;

    -- Drop all foreign keys
    ALTER TABLE user DROP FOREIGN KEY FK_user_role;
    ALTER TABLE user_permission DROP FOREIGN KEY FK_user_permission_user;
    ALTER TABLE user_permission DROP FOREIGN KEY FK_user_permission_permission;
    
    -- Modify id columns to ensure AUTO_INCREMENT
    ALTER TABLE business_type MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
    ALTER TABLE user MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
    ALTER TABLE role MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
    ALTER TABLE permission MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
    ALTER TABLE user_permission MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

    -- Restore foreign keys
    ALTER TABLE user ADD CONSTRAINT FK_user_role
        FOREIGN KEY (role_id) REFERENCES role(id);
    ALTER TABLE user_permission ADD CONSTRAINT FK_user_permission_user
        FOREIGN KEY (user_id) REFERENCES user(id);
    ALTER TABLE user_permission ADD CONSTRAINT FK_user_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permission(id);

    -- Cleanup
    DROP TEMPORARY TABLE IF EXISTS temp_fks;
    DROP TEMPORARY TABLE IF EXISTS temp_sql;
    
    SELECT 'Auto-increment fix completed successfully' AS status;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-31 12:41:36
