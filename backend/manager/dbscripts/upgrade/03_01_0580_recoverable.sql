
alter table vds_static add column recoverable boolean not null default true;
alter table storage_domain_static add column recoverable boolean not null default true;


