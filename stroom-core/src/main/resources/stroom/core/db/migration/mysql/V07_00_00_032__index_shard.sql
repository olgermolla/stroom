CREATE TABLE IF NOT EXISTS index_shard (
  id                    bigint(20) NOT NULL AUTO_INCREMENT,
  node_name             varchar(255) NOT NULL,
  fk_volume_id          int(11) NOT NULL,
  index_uuid            varchar(255) NOT NULL,
  commit_document_count int(11) DEFAULT NULL,
  commit_duration_ms    bigint(20) DEFAULT NULL,
  commit_ms             bigint(20) DEFAULT NULL,
  document_count        int(11) DEFAULT 0,
  file_size             bigint(20) DEFAULT 0,
  status                tinyint(4) NOT NULL,
  index_version         varchar(255) DEFAULT NULL,
  partition_name        varchar(255) NOT NULL,
  partition_from_ms     bigint(20) DEFAULT NULL,
  partition_to_ms       bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY index_shard_fk_volume_id (fk_volume_id),
  KEY index_shard_index_uuid (index_uuid),
  CONSTRAINT index_shard_fk_volume_id FOREIGN KEY (fk_volume_id) REFERENCES index_volume (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Copy data into the index table
--
DROP PROCEDURE IF EXISTS copy_index_shard;
DELIMITER //
CREATE PROCEDURE copy_index_shard ()
BEGIN
    -- If table exists (it may not if this migration runs before core stroom's) then migrate its data,
    -- if it doesn't exist then it won't ever have data to migrate
    IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'IDX_SHRD' > 0) THEN
        --
        -- Copy data into the table, use ID predicate to make it re-runnable
        --
        INSERT
        INTO index_shard (id, node_name, fk_volume_id, index_uuid, commit_document_count, commit_duration_ms, commit_ms, document_count, file_size, status, index_version, partition_name, partition_from_ms, partition_to_ms)
        SELECT s.ID, n.NAME, s.FK_VOL_ID, s.IDX_UUID, s.CMT_DOC_CT, s.CMT_DUR_MS, s.CMT_MS, s.DOC_CT, s.FILE_SZ, s.STAT, s.IDX_VER, s.PART, s.PART_FROM_MS, s.PART_TO_MS
        FROM IDX_SHRD s
        INNER JOIN ND n ON (n.ID = s.FK_ND_ID)
        WHERE s.ID > (SELECT COALESCE(MAX(id), 0) FROM index_shard)
        ORDER BY s.ID;

        -- Work out what to set our auto_increment start value to
        SELECT CONCAT('ALTER TABLE index_shard AUTO_INCREMENT = ', COALESCE(MAX(id) + 1, 1))
        INTO @alter_table_sql
        FROM index_shard;

        PREPARE alter_table_stmt FROM @alter_table_sql;
        EXECUTE alter_table_stmt;
    END IF;
END//
DELIMITER ;
CALL copy_index_shard();
DROP PROCEDURE copy_index_shard;