-- Delete permissions if were given for VmNetworkInterface as
-- it has been removed from the entities hierarchy

delete from permissions where object_type_id = 20;
