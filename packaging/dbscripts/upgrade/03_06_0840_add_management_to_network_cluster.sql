select fn_db_add_column('network_cluster', 'management', 'boolean NOT NULL DEFAULT false');

update network_cluster
set management = true
where network_id in (select id
                     from network
                     where name=(select COALESCE((select option_value
                                                  from vdc_options
                                                  where option_name='ManagementNetwork'
                                                  and version='general'),
                                                 'ovirtmgmt')));

