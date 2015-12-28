CREATE OR REPLACE FUNCTION __temp_set_default_cluster_for_registration()
  RETURNS void AS
$BODY$
BEGIN
    IF EXISTS (select 1 from cluster where name ilike 'Default') THEN
        update vdc_options
        set option_value = (select cluster_id from cluster where name ilike 'Default')
        where option_name = 'AutoRegistrationDefaultClusterID' and version = 'general';
    ELSE
        -- Default cluster name was changed from 'Default' to something else
        -- taking the first cluster we found as default
        update vdc_options
        set option_value = (select cluster_id from cluster LIMIT 1)
        where option_name = 'AutoRegistrationDefaultClusterID' and version = 'general';
    END IF;
END; $BODY$
LANGUAGE plpgsql;

select __temp_set_default_cluster_for_registration();
drop function __temp_set_default_cluster_for_registration();


