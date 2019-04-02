SELECT fn_db_create_constraint('image_transfers',
                               'fk_image_transfers_command_enitites',
                               'FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE');

