-- adding description column to policy unit
SELECT fn_db_add_column('policy_units', 'description', 'text');
-- updating current policy units with descriptions
UPDATE policy_units SET description =
CASE
    WHEN name = 'Migration' THEN 'While migrating a VM, filters out the VM''s running host'
    WHEN name = 'MigrationDomain' THEN 'Filters out hosts from different VM''s migration domain, and all non-UP state hosts'
    WHEN name = 'PinToHost' THEN 'Filters out all hosts that VM is not pinned to'
    WHEN name = 'CPU' THEN 'Filters out hosts with less CPUs than VM''s CPUs'
    WHEN name = 'Memory' THEN 'Filters out hosts that have insufficient memory to run the VM'
    WHEN name = 'Network' THEN 'Filters out hosts that are missing networks required by VM NICs, or missing cluster''s display network'
-- None:
-- load balancing logic
    WHEN id = '38440000-8cf0-14bd-c43e-10b96e4ef00a' THEN 'No load balancing operation'
-- weight function
    WHEN id = '38440000-8cf0-14bd-c43e-10b96e4ef00b' THEN 'Follows Even Distribution weight module'
-- Power saving:
-- load balancing logic
    WHEN id = '736999d0-1023-46a4-9a75-1316ed50e151' THEN 'Load balancing VMs in cluster according to hosts CPU load, striving cluster''s hosts CPU load to be over ''LowUtilization'' and under ''HighUtilization'''
-- weight function
    WHEN id = '736999d0-1023-46a4-9a75-1316ed50e15b' THEN 'Gives hosts with higher CPU usage, higher weight (means that hosts with lower CPU usage are more likely to be selected)'
-- Even distribution:
-- load balancing logic
    WHEN id = '7db4ab05-81ab-42e8-868a-aee2df483ed2' THEN 'Load balancing VMs in cluster according to hosts CPU load, striving cluster''s hosts CPU load to be under ''HighUtilization'''
-- weight function
    WHEN id = '7db4ab05-81ab-42e8-868a-aee2df483edb' THEN 'Gives hosts with lower CPU usage, higher weight (means that hosts with higher CPU usage are more likely to be selected)'
END;


