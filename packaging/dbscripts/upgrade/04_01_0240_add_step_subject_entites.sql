CREATE TABLE step_subject_entity (
    step_id UUID NOT NULL REFERENCES step(step_id) ON DELETE CASCADE,
    entity_id UUID NOT NULL,
    entity_type CHARACTER VARYING(32) NOT NULL,
    step_entity_weight SMALLINT,
    PRIMARY KEY (step_id, entity_id, entity_type));

CREATE INDEX idx_step_subject_entity_step ON step_subject_entity(step_id);
