-- Create materialized_views table
CREATE FUNCTION __temp__0030_add_materialized_views_table()
RETURNS VOID
AS $procedure$
BEGIN
    IF not exists (select 1 from information_schema.tables where table_schema = 'public' and table_name ='materialized_views') then
        CREATE TABLE materialized_views (
          mv_name NAME NOT NULL PRIMARY KEY,
          v_name NAME NOT NULL,
          refresh_rate_in_sec INTEGER,
          last_refresh TIMESTAMP WITH TIME ZONE,
          avg_cost_ms int not null default 0
        );
    END IF;
END; $procedure$
LANGUAGE plpgsql;

select __temp__0030_add_materialized_views_table();
DROP FUNCTION __temp__0030_add_materialized_views_table();


