SELECT fn_db_add_column('network_cluster', 'default_route', 'boolean NOT NULL DEFAULT false');

UPDATE
  network_cluster
SET
  default_route = TRUE
WHERE
  management = TRUE;

