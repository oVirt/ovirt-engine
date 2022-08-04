


----------------------------------------------------------------
-- [custom_actions] Table
--
CREATE OR REPLACE FUNCTION Insertcustom_actions (
    INOUT v_action_id INT,
    v_action_name VARCHAR(50),
    v_path VARCHAR(300),
    v_tab INT,
    v_description VARCHAR(4000)
    ) AS $FUNCTION$
BEGIN
    INSERT INTO custom_actions (
        action_name,
        path,
        tab,
        description
        )
    VALUES (
        v_action_name,
        v_path,
        v_tab,
        v_description
        );

    v_action_id := CURRVAL('custom_actions_seq');
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updatecustom_actions (
    v_action_id INT,
    v_action_name VARCHAR(50),
    v_path VARCHAR(300),
    v_tab INT,
    v_description VARCHAR(4000)
    )
RETURNS VOID
    --The [custom_actions] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $FUNCTION$
BEGIN
    UPDATE custom_actions
    SET action_name = v_action_name,
        path = v_path,
        tab = v_tab,
        description = v_description
    WHERE action_id = v_action_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deletecustom_actions (v_action_id INT)
RETURNS VOID AS $FUNCTION$
BEGIN
    DELETE
    FROM custom_actions
    WHERE action_id = v_action_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromcustom_actions ()
RETURNS SETOF custom_actions STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM custom_actions;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getcustom_actionsByaction_id (v_action_id INT)
RETURNS SETOF custom_actions STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM custom_actions
    WHERE action_id = v_action_id;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getcustom_actionsByTab_id (v_tab INT)
RETURNS SETOF custom_actions STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM custom_actions
    WHERE tab = v_tab;
END;$FUNCTION$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getcustom_actionsByNameAndTab (
    v_action_name VARCHAR(50),
    v_tab INT
    )
RETURNS SETOF custom_actions STABLE AS $FUNCTION$
BEGIN
    RETURN QUERY

    SELECT *
    FROM custom_actions
    WHERE tab = v_tab
        AND action_name = v_action_name;
END;$FUNCTION$
LANGUAGE plpgsql;


