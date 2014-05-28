-- Power saving:
-- weight function
UPDATE policy_units SET description='Gives hosts with higher CPU usage, lower weight (means that hosts with higher CPU usage are more likely to be selected)' WHERE id = '736999d0-1023-46a4-9a75-1316ed50e15b';

-- Even distribution:
-- weight function
UPDATE policy_units SET description='Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more likely to be selected)' WHERE id = '7db4ab05-81ab-42e8-868a-aee2df483edb';
