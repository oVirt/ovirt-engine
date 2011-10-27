CREATE OR REPLACE FUNCTION upgrade_domain_entries_03_00_0200()
  RETURNS void AS
$BODY$
   DECLARE
   v_domain_name_entry VARCHAR(4000);
   v_password_entry VARCHAR(4000);
   v_username_entry VARCHAR(4000);
   v_user_id_entry VARCHAR(4000);
   v_upgraded_password_entry VARCHAR(4000);
   v_upgraded_username_entry VARCHAR(4000);
   v_upgraded_user_id_entry VARCHAR(4000);
   v_curr_domain text;
   v_first_time boolean;
   v_pos int;
   v_len int;
BEGIN
    v_user_id_entry := option_value FROM vdc_options WHERE option_name='AdUserId';
    v_pos := strpos(v_user_id_entry,':');

    IF (v_pos = 0) THEN
		v_domain_name_entry := option_value FROM vdc_options WHERE option_name='DomainName';
		v_password_entry := option_value FROM vdc_options WHERE option_name='AdUserPassword';
		v_username_entry := option_value FROM vdc_options WHERE option_name='AdUserName';

		v_first_time := TRUE;
		v_upgraded_username_entry := '';
		v_upgraded_password_entry := '';
		v_upgraded_user_id_entry := '';
		FOR v_curr_domain IN (select regexp_split_to_table(v_domain_name_entry,',')) LOOP
			IF (v_first_time IS TRUE) THEN
				v_first_time := FALSE;
			ELSE
				v_upgraded_username_entry := ',' || v_upgraded_username_entry;
				v_upgraded_password_entry := ',' || v_upgraded_password_entry;
				v_upgraded_user_id_entry := ',' || v_upgraded_user_id_entry;
			END IF;
			IF (v_username_entry <> '') THEN
				v_upgraded_username_entry := v_curr_domain || ':' || v_username_entry || v_upgraded_username_entry;
			END IF;
			IF (v_password_entry <> '') THEN
				v_upgraded_password_entry := v_curr_domain || ':' || v_password_entry || v_upgraded_password_entry;
			END IF;
			IF (v_user_id_entry <>  '') THEN
				v_upgraded_user_id_entry := v_curr_domain || ':' || v_user_id_entry || v_upgraded_user_id_entry;
			END IF;
		END LOOP;

		UPDATE vdc_options
		SET option_value = v_upgraded_username_entry
		WHERE option_name = 'AdUserName';

		UPDATE vdc_options
		SET option_value = v_upgraded_password_entry
		WHERE option_name = 'AdUserPassword';

		UPDATE vdc_options
		SET option_value = v_upgraded_user_id_entry
		WHERE option_name = 'AdUserId';
    END IF;

END; $BODY$
LANGUAGE plpgsql;

SELECT upgrade_domain_entries_03_00_0200();
DROP FUNCTION upgrade_domain_entries_03_00_0200();
