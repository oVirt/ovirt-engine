-- Add a column to the groups table to store the identifier assigned by the
-- external directory. This new column is initialized with a copy of the
-- identifier of the group, as before this change we used the external
-- identifier also as internal identifier.

alter table ad_groups add column external_id bytea not null default '';
update ad_groups set external_id = decode(replace(id::text, '-', ''), 'hex');
select fn_db_create_constraint('ad_groups', 'groups_domain_external_id_unique', 'unique (domain, external_id)');
