-- Add command_entities table
CREATE TABLE command_entities
(
    command_id UUID NOT NULL,
    command_type integer NOT NULL,
    root_command_id UUID DEFAULT NULL,
    action_parameters text,
    action_parameters_class varchar(256),
    created_at TIMESTAMP WITH TIME ZONE,
    status varchar(20) DEFAULT NULL,
    CONSTRAINT pk_command_entities PRIMARY KEY(command_id)
);
CREATE INDEX idx_root_command_id ON command_entities(root_command_id) WHERE root_command_id IS NOT NULL;
