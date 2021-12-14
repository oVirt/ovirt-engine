TRUNCATE TABLE vds_statistics;
INSERT INTO vds_statistics(vds_id) SELECT vds_id FROM vds_static;
