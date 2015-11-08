


/*
This file adds Snapshot Materialized Views support to Postgres.
A Snapshot Materialized View is actually a table built on top of
a real view that enables to select data from the Snapshot Materialized View
easily and efficiently.
Candidates for Snapshot Materialized Views are views that are based on
slowly-changing data. The Snapshot Materialized Views is actually
functioning as a cache.
The Snapshot Materialized View is refreshed per request.
The Snapshot Materialized View definitions are stored in the materialized_views table.

Flow:
1) Create the Materialized View by calling:
   CreateMaterializedView - if you are creating a new view
   CreateMaterializedViewAs - If you want to preserve the original view name
                              in this case the original view will be renamed
                              and the new Materialized View will have the original
                              view name.
   The following should be called for Custom Materialized Views
   CreateCustomMaterializedView
   CreateCustomMaterializedViewAs
2) If your Snapshot Materialized View is my_mt you should create Stored Procedures:
    MtDropmy_mtIndexes - Drops indexes on my_mt
    MtCreatemy_mtIndexes - Creates needed indexes on my_mt
    Those indexes should be defined in the "Snapshot Materialized Views Index Definitions Section"
    in post_upgrade/0020_create_materialized_views.sql file.

   Those SP are called automatically when a Snapshot Materialized View is refreshed
   to boost refresh performance.

3) You can call IsMaterializedViewRefreshed to check if it is time to refresh the view
   and if yes call RefreshMaterializedView manually.
   or
   You can define a cron job that calls RefreshAllMaterializedViews that loops over
   all  Snapshot Materialized Views and refreshes it automatically
   RefreshAllMaterializedViews recieves a boolean v_force flag, please set this flag to false
   when calling it from a cron job in order to update the materialized views only when needed.
   (This SP is called with v_force = true after create/upgrade DB)

There are also additional functions :
   CreateAllMaterializedViewsiIndexes - Creates indexes for all Snapshot Materialized views
   DropMaterializedView - Drops the Materialized View
   DropAllMaterializedViews - Drop all Materialized Views
   DropAllCustomMaterializedViews - Drop all custom Materialized Views
   UpdateMaterializedViewRefreshRate - Updates the Materialized View refresh rate
   UpdateMaterializedViewMinRefreshRate - Updates the Materialized View minimal refresh rate
   ActivateMaterializedView - activates/decativates a  Materialized View
   ActivateAllMaterializedViews - activates/decativates all Materialized Views

In addition, you can create a file named create_materialized_views.sql under dbscripts/upgrade/post_upgrade/custom/
This file may include other custom materialized views settings and is executed by the create/upgrade database scripts.

NOTE : Materialized Views are automatically refreshed upon create/upgrade
*/
----------------------------------------
-- Materialized Views Support --
----------------------------------------
-- Helper Function : converts an integer value to INTERVAL
CREATE OR REPLACE FUNCTION to_interval (sec INT)
RETURNS INTERVAL AS $$

BEGIN
    RETURN (sec || ' seconds')::INTERVAL;
END;$$

LANGUAGE 'plpgsql' IMMUTABLE STRICT;

-- CreateMaterializedViewAsCreates a new Materialized View
CREATE OR REPLACE FUNCTION CreateMaterializedView (
    v_matview NAME,
    v_view_NAME NAME,
    v_refresh_rate_in_sec INT,
    v_custom BOOLEAN,
    v_min_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF FOUND THEN RAISE EXCEPTION 'Materialized view % already exists.',
        v_matview;
    END IF;
    IF current_user != usename FROM pg_database,
        pg_user WHERE datdba = usesysid
        AND datname = current_database THEN RAISE EXCEPTION 'Materialized view % should be created with user ''engine''.',
        v_matview;
    END IF ;
    EXECUTE 'REVOKE ALL ON ' || v_view_name || ' FROM PUBLIC';

    EXECUTE 'GRANT SELECT ON ' || v_view_name || ' TO PUBLIC';

    EXECUTE 'CREATE TABLE ' || v_matview || ' AS SELECT * FROM ' || v_view_name;

    EXECUTE 'REVOKE ALL ON ' || v_matview || ' FROM PUBLIC';

    EXECUTE 'GRANT SELECT ON ' || v_matview || ' TO PUBLIC';

    INSERT INTO materialized_views (
        mv_name,
        v_name,
        refresh_rate_in_sec,
        last_refresh,
        custom,
        min_refresh_rate_in_sec
        )
    VALUES (
        v_matview,
        v_view_name,
        v_refresh_rate_in_sec,
        CURRENT_TIMESTAMP,
        v_custom,
        v_min_refresh_rate_in_sec
        );

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateMaterializedView (
    v_matview NAME,
    v_view_name NAME,
    v_refresh_rate_in_sec INT,
    v_min_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedView(v_matview, v_view_name, v_refresh_rate_in_sec, false, v_min_refresh_rate_in_sec);

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateCustomMaterializedView (
    v_matview NAME,
    v_view_name NAME,
    v_refresh_rate_in_sec INT,
    v_min_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedView(v_matview, v_view_name, v_refresh_rate_in_sec, true, v_min_refresh_rate_in_sec);

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateMaterializedView (
    v_matview NAME,
    v_view_name NAME,
    v_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedView(v_matview, v_view_name, v_refresh_rate_in_sec, false, 0);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateCustomMaterializedView (
    v_matview NAME,
    v_view_name NAME,
    v_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedView(v_matview, v_view_name, v_refresh_rate_in_sec, true, 0);
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Enables to create a New materialized view with a name of existing view
-- This is done in order to solve cases where we are forced to use the old view name for the new createed
-- Materialized View because it is used from dynamic SQL and we have to send only a DB patch without forcing
-- recompilation of engine code
CREATE OR REPLACE FUNCTION CreateMaterializedViewAs (
    v_view_name NAME,
    v_refresh_rate_in_sec INT,
    v_custom BOOLEAN,
    v_min_refesh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

v_renamed_view NAME;

BEGIN
    IF FOUND THEN
        RAISE EXCEPTION 'Materialized view % already exists.', v_view_name;
    END IF;
    v_renamed_view := v_view_name || '_mt_base';

    EXECUTE 'ALTER VIEW ' || v_view_name || ' RENAME TO ' || v_renamed_view;

    perform CreateMaterializedView(v_view_name, v_renamed_view, v_refresh_rate_in_sec, v_custom, v_min_refesh_rate_in_sec);

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateMaterializedViewAs (
    v_view_name NAME,
    v_refresh_rate_in_sec INT,
    v_min_refesh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedViewAs(v_view_name, v_refresh_rate_in_sec, false, v_min_refesh_rate_in_sec);

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateCustomMaterializedViewAs (
    v_view_name NAME,
    v_refresh_rate_in_sec INT,
    v_min_refesh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedViewAs(v_view_name, v_refresh_rate_in_sec, true, v_min_refesh_rate_in_sec);

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateMaterializedViewAs (
    v_view_name NAME,
    v_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedViewAs(v_view_name, v_refresh_rate_in_sec, false, 0);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION CreateCustomMaterializedViewAs (
    v_view_name NAME,
    v_refresh_rate_in_sec INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    PERFORM CreateMaterializedViewAs(v_view_name, v_refresh_rate_in_sec, true, 0);
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Drops a Materialized View
CREATE OR REPLACE FUNCTION DropMaterializedView (v_matview NAME)
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Materialized view % does not exist.', v_matview;
    END IF;
    EXECUTE 'DROP TABLE ' || v_matview || ' CASCADE';

    DELETE
    FROM materialized_views
    WHERE mv_name = v_matview;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

--  Drops all Materialized Views
CREATE OR REPLACE FUNCTION DropAllMaterializedViews ()
RETURNS void AS $PROCEDURE$
DECLARE v_cur CURSOR
FOR
SELECT *
FROM materialized_views;

v_record materialized_views % ROWTYPE;

BEGIN
    OPEN v_cur;

    -- loop on all entries in materialized_views
    LOOP

    FETCH v_cur
    INTO v_record;

    EXIT WHEN NOT FOUND;

    perform DropMaterializedView(v_record.mv_name);
    END LOOP;

CLOSE v_cur;END;$PROCEDURE$
LANGUAGE plpgsql;

-- Drops all custom Materialized Views
CREATE OR REPLACE FUNCTION DropAllCustomMaterializedViews ()
RETURNS void AS $PROCEDURE$
DECLARE v_cur CURSOR
FOR
SELECT *
FROM materialized_views
WHERE custom;

v_record materialized_views % ROWTYPE;

BEGIN
    OPEN v_cur;

    -- loop on all entries in materialized_views
    LOOP

    FETCH v_cur
    INTO v_record;

    EXIT WHEN NOT FOUND;

    perform DropMaterializedView(v_record.mv_name);
    END LOOP;


CLOSE v_cur;END;$PROCEDURE$
LANGUAGE plpgsql;

-- Checks if  Materialized View should be refreshed
CREATE OR REPLACE FUNCTION IsMaterializedViewRefreshed (v_matview NAME)
RETURNS boolean STABLE AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

v_is_refreshed boolean;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN RAISE EXCEPTION 'Materialized view % does not exist.',
        v_matview;
    END IF;
    -- check if materialized View should refresh
    v_is_refreshed := (
            ((CURRENT_TIMESTAMP - to_interval(refresh_rate_in_sec)) <= last_refresh)
            AND ((CURRENT_TIMESTAMP - to_interval(min_refresh_rate_in_sec)) <= last_refresh)
            )
        OR NOT active
    FROM materialized_views
    WHERE mv_name = v_matview;

    RETURN v_is_refreshed;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Refreshes a Materialized View
CREATE OR REPLACE FUNCTION RefreshMaterializedView (v_matview NAME)
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

v_drop_index_sp NAME;

v_create_index_sp NAME;

v_start_time TIMESTAMP
WITH TIME ZONE;

v_avg_cost_ms INT;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Materialized view % does not exist.', v_matview;
    END IF;
    -- get start time
    v_start_time:= CURRENT_TIMESTAMP;

    -- SP for Drop / create Index should follow naming convention Mt[Drop|Create]<v_matview>Indexes
    v_drop_index_sp := 'MtDrop' || v_matview || 'Indexes';

    v_create_index_sp := 'MtCreate' || v_matview || 'Indexes';

    IF NOT EXISTS (
            SELECT 1
            FROM information_schema.routines
            WHERE routine_name ilike v_drop_index_sp
            ) THEN v_drop_index_sp := NULL;
    END IF;
    IF NOT EXISTS (
            SELECT 1
            FROM information_schema.routines
            WHERE routine_name ilike v_create_index_sp
            ) THEN v_create_index_sp := NULL;
    END IF;
    -- Lock materialized_views table until refresh completes to prevent duplicate refreshes by other threads
    LOCK TABLE materialized_views;

    -- taking a lock on the snapshot materialized view until it refreshed
    EXECUTE 'LOCK TABLE ' || v_matview;

    -- drop indexes on the snapshot materialized view if exists
    IF (v_drop_index_sp IS NOT NULL) THEN
        EXECUTE 'select ' || v_drop_index_sp || '()';
    END IF;

    -- refresh the view
    EXECUTE 'TRUNCATE TABLE ' || v_matview;

    EXECUTE 'INSERT INTO ' || v_matview || ' SELECT * FROM ' || v_entry.v_name;

    -- restore  indexes on the snapshot materialized view if exists
    IF (v_create_index_sp IS NOT NULL) THEN
        EXECUTE 'select ' || v_create_index_sp || '()';
    END IF;

    -- update last refresh time and average cost in [ms]
    IF (v_entry.avg_cost_ms = 0) THEN
        v_avg_cost_ms:= EXTRACT(EPOCH FROM current_timestamp - v_start_time) * 1000;
    ELSE
        v_avg_cost_ms:= ((EXTRACT(EPOCH FROM current_timestamp - v_start_time) * 1000) + v_entry.avg_cost_ms) / 2;
    END IF;

    UPDATE materialized_views
    SET last_refresh = CURRENT_TIMESTAMP,
        avg_cost_ms = v_avg_cost_ms
    WHERE mv_name = v_matview;

RETURN;END;$PROCEDURE$
LANGUAGE plpgsql;

-- Refresh all materialized views (if needed)
CREATE OR REPLACE FUNCTION RefreshAllMaterializedViews (v_force boolean)
RETURNS void AS $PROCEDURE$
DECLARE v_cur CURSOR
FOR
SELECT *
FROM materialized_views;

v_record materialized_views % ROWTYPE;

BEGIN
    OPEN v_cur;

    -- loop on all entries in materialized_views and refresh only needed snapshots
    LOOP

    FETCH v_cur
    INTO v_record;

    EXIT WHEN NOT FOUND;

    IF (
            (
                v_force
                AND v_record.min_refresh_rate_in_sec = 0
                )
            OR NOT IsMaterializedViewRefreshed(v_record.mv_name)
            ) THEN perform RefreshMaterializedView(v_record.mv_name);
    END IF;
    END LOOP;

    CLOSE v_cur;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Creates all materialized views indexes
CREATE OR REPLACE FUNCTION CreateAllMaterializedViewsiIndexes ()
RETURNS void AS $PROCEDURE$
DECLARE v_cur CURSOR
FOR
SELECT *
FROM materialized_views;

v_record materialized_views % ROWTYPE;

v_create_index_sp NAME;

BEGIN
    OPEN v_cur;

    -- loop on all entries in materialized_views and create indexes(if defined)
    LOOP

    FETCH v_cur
    INTO v_record;

    EXIT WHEN NOT FOUND;

    v_create_index_sp := 'MtCreate' || v_record.mv_name || 'Indexes';

    -- Check if SP that creates the indexes exists
    IF NOT EXISTS (
            SELECT 1
            FROM information_schema.routines
            WHERE routine_name ilike v_create_index_sp
            ) THEN v_create_index_sp := NULL;
    END IF;
    IF (v_create_index_sp IS NOT NULL) THEN
        EXECUTE 'select ' || v_create_index_sp || '()';
    END IF;

    END LOOP;

CLOSE v_cur;END;$PROCEDURE$
LANGUAGE plpgsql;

-- Updates a  Materialized View refresh rate
CREATE OR REPLACE FUNCTION UpdateMaterializedViewRefreshRate (
    v_matview NAME,
    v_refresh_rate INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN RAISE EXCEPTION 'Materialized view % does not exist.',
        v_matview;
    END IF;
    UPDATE materialized_views
    SET refresh_rate_in_sec = v_refresh_rate
    WHERE mv_name = v_matview;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Updates a  Materialized View min refresh rate
CREATE OR REPLACE FUNCTION UpdateMaterializedViewMinRefreshRate (
    v_matview NAME,
    v_min_refresh_rate INT
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN RAISE EXCEPTION 'Materialized view % does not exist.',
        v_matview;
    END IF;
    UPDATE materialized_views
    SET min_refresh_rate_in_sec = v_min_refresh_rate
    WHERE mv_name = v_matview;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ActivateMaterializedView (
    v_matview NAME,
    v_active BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    SELECT *
    INTO v_entry
    FROM materialized_views
    WHERE mv_name = v_matview;

    IF NOT FOUND THEN RAISE EXCEPTION 'Materialized view % does not exist.',
        v_matview;
    END IF;
    UPDATE materialized_views
    SET active = v_active
    WHERE mv_name = v_matview;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ActivateAllMaterializedViews (v_active BOOLEAN)
RETURNS VOID AS $PROCEDURE$
DECLARE v_entry materialized_views % ROWTYPE;

BEGIN
    UPDATE materialized_views
    SET active = v_active;

    RETURN;
END;$PROCEDURE$
LANGUAGE plpgsql;


