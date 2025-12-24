CREATE PROCEDURE fix_auto_increment_all()
BEGIN
    DECLARE sql_cmd TEXT;

    -- Step 1: Save foreign keys
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

    -- Step 2: Drop foreign keys
    DROP TEMPORARY TABLE IF EXISTS temp_sql;
    CREATE TEMPORARY TABLE temp_sql (sql_text TEXT);

    INSERT INTO temp_sql(sql_text)
    SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` DROP FOREIGN KEY `', CONSTRAINT_NAME,'`')
    FROM temp_fks;

    WHILE (SELECT COUNT(*) FROM temp_sql) > 0 DO
        SELECT sql_text INTO sql_cmd FROM temp_sql LIMIT 1;
        SET @s = sql_cmd;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        DELETE FROM temp_sql LIMIT 1;
    END WHILE;

    -- Step 3: Modify id columns
    INSERT INTO temp_sql(sql_text)
    SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND COLUMN_NAME='id';

    WHILE (SELECT COUNT(*) FROM temp_sql) > 0 DO
        SELECT sql_text INTO sql_cmd FROM temp_sql LIMIT 1;
        SET @s = sql_cmd;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        DELETE FROM temp_sql LIMIT 1;
    END WHILE;

    -- Step 4: Add foreign keys back
    INSERT INTO temp_sql(sql_text)
    SELECT CONCAT('ALTER TABLE `', TABLE_NAME,
                  '` ADD CONSTRAINT `', CONSTRAINT_NAME,
                  '` FOREIGN KEY (`', COLUMN_NAME,'`) REFERENCES `',
                  REFERENCED_TABLE_NAME,'`(`', REFERENCED_COLUMN_NAME,'`)')
    FROM temp_fks;

    WHILE (SELECT COUNT(*) FROM temp_sql) > 0 DO
        SELECT sql_text INTO sql_cmd FROM temp_sql LIMIT 1;
        SET @s = sql_cmd;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        DELETE FROM temp_sql LIMIT 1;
    END WHILE;

END ;
