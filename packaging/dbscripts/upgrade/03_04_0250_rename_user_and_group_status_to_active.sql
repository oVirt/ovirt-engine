-- Rename the status column of users and groups to active and change its
-- type from integer to boolean. What used to be 0 (meaning inactive) will
-- become false and what used to be 1 (meaning active) will become true.

alter table users add column active boolean not null default false;
update users set active = status::boolean;
alter table users drop column status;

alter table ad_groups add column active boolean not null default false;
update ad_groups set active = status::boolean;
alter table ad_groups drop column status;
