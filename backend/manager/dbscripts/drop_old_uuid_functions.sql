--Drops all old UUID functions since we have an internal implementation
drop function if exists  uuid_nil();
drop function if exists  uuid_ns_dns();
drop function if exists  uuid_ns_url();
drop function if exists  uuid_ns_oid();
drop function if exists  uuid_ns_x500();
drop function if exists  uuid_generate_v1();
drop function if exists  uuid_generate_v1mc();
drop function if exists  uuid_generate_v3(namespace uuid, name text);
drop function if exists  uuid_generate_v4();
drop function if exists  uuid_generate_v5(namespace uuid, name text);
