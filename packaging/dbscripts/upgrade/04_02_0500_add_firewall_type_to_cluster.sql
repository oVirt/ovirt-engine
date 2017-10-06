SELECT fn_db_add_column('cluster', 'firewall_type', 'INT NOT NULL DEFAULT 1');

--
-- We need to set firewall type iptables for all clusters with version < 4.2
-- to maintain backward compatibility and at the same time for new
-- installations to leave firewall type firewalld for default cluster
--
UPDATE cluster
  SET firewall_type = 0
  WHERE compatibility_version IN ('3.6', '4.0', '4.1');
