SELECT fn_db_add_column('images', 'volume_classification', 'SMALLINT');

UPDATE images
  SET volume_classification = CASE WHEN active THEN 0 ELSE 1 END;


