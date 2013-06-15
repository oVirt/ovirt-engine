-- Change compatibility level to 3.1 from 3.2 for RemoveGlusterServer
update action_version_map set cluster_minimal_version = '3.1' where action_type = 1412;
