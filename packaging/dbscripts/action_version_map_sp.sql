

----------------------------------------------------------------
-- [action_version_map] Table
--
CREATE OR REPLACE FUNCTION GetAllFromaction_version_map ()
RETURNS SETOF action_version_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM action_version_map;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getaction_version_mapByaction_type (v_action_type INT)
RETURNS SETOF action_version_map STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM action_version_map
    WHERE action_type = v_action_type;
END;$PROCEDURE$
LANGUAGE plpgsql;
