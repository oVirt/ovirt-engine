--
-- Remove the @directory suffix from "username" column of the "users" table, as
-- this value is already stored in the "domain" column of the same table:
--
update users set username = regexp_replace(username, '@.*', '');
