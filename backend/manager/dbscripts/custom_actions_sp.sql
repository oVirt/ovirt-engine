

----------------------------------------------------------------
-- [custom_actions] Table
--




Create or replace FUNCTION Insertcustom_actions(INOUT v_action_id INTEGER ,
	v_action_name VARCHAR(50),
	v_path VARCHAR(300),
	v_tab INTEGER ,
	v_description VARCHAR(4000))
   AS $procedure$
BEGIN
INSERT INTO custom_actions(action_name, path, tab, description)
	VALUES(v_action_name, v_path, v_tab, v_description);

      v_action_id := CURRVAL('custom_actions_seq');
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatecustom_actions(v_action_id INTEGER,
	v_action_name VARCHAR(50),
	v_path VARCHAR(300),
	v_tab INTEGER ,
	v_description VARCHAR(4000))
RETURNS VOID

	--The [custom_actions] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE custom_actions
      SET action_name = v_action_name,path = v_path,tab = v_tab,description = v_description
      WHERE action_id = v_action_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletecustom_actions(v_action_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM custom_actions
      WHERE action_id = v_action_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromcustom_actions() RETURNS SETOF custom_actions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM custom_actions;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getcustom_actionsByaction_id(v_action_id INTEGER)
RETURNS SETOF custom_actions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM custom_actions
      WHERE action_id = v_action_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getcustom_actionsByTab_id(v_tab INTEGER) RETURNS SETOF custom_actions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM custom_actions
      WHERE tab = v_tab;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getcustom_actionsByNameAndTab(v_action_name VARCHAR(50),
	v_tab INTEGER) RETURNS SETOF custom_actions
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM custom_actions
      WHERE tab = v_tab and action_name = v_action_name;
END; $procedure$
LANGUAGE plpgsql;


