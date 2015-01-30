CREATE TABLE command_assoc_entities (
    command_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    entity_type character varying(128),
    PRIMARY KEY (command_id, entity_id)
);

SELECT fn_db_create_constraint('command_assoc_entities', 'fk_coco_command_assoc_entity', 'FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE');
