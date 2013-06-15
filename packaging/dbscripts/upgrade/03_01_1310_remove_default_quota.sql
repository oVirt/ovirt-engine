-- this upgrade will delete all default quota(s) from the system
CREATE OR REPLACE function __temp_Upgrade_remove_default_quota_03_01_1180() returns void
as $function$
begin
  if (EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name ilike 'quota' AND column_name ilike 'is_default_quota')) THEN
    -- removing ref to default quota for images
    UPDATE images
    SET quota_id = NULL
    WHERE quota_id IN
    (SELECT id FROM quota WHERE is_default_quota = true);
    -- removing ref to default quota for vms
    UPDATE vm_static
    SET quota_id = NULL
    WHERE quota_id IN
    (SELECT id FROM quota WHERE is_default_quota = true);
    -- removing all default quota from the system
    DELETE FROM quota
    WHERE is_default_quota = true;
  END if;
END; $function$
language plpgsql;

SELECT __temp_Upgrade_remove_default_quota_03_01_1180();

DROP function __temp_Upgrade_remove_default_quota_03_01_1180();
-- droping default quota column in quota table
SELECT fn_db_drop_column('quota', 'is_default_quota');

