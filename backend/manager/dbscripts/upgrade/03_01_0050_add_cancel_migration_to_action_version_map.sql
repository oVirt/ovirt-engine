insert into action_version_map (action_type, cluster_minimal_version, storage_pool_minimal_version)
    select 41, '3.1', '3.1'
        where not exists (select 1 from action_version_map where action_type = 41);
