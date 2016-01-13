-- This is a virtual view used by schema.py only
-- please do not use this view for any other purpose
-- The view will be dropped and schema.py will be modified
-- to refer to the cluster table when version 04_00 is squashed
CREATE VIEW vds_groups
AS
SELECT *
FROM cluster;
