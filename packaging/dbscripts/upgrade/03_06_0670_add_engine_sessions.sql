CREATE SEQUENCE engine_session_seq INCREMENT BY 1 START WITH 1;

CREATE TABLE engine_sessions (
    id bigint DEFAULT nextval('engine_session_seq'::regclass) NOT NULL,
    engine_session_id text NOT NULL,
    user_id uuid NOT NULL,
    user_name character varying(255) NOT NULL,
    group_ids text,
    role_ids text,
    CONSTRAINT pk_engine_session PRIMARY KEY (id)
);
CREATE INDEX idx_engine_session_session_id ON engine_sessions USING btree (engine_session_id);
