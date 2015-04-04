CREATE TABLE engine_backup_log(
    db_name VARCHAR(64),
    done_at TIMESTAMP WITH TIME ZONE,
    is_passed BOOLEAN,
    output_message TEXT,
    PRIMARY KEY (db_name, done_at)
);

CREATE UNIQUE INDEX IDX_engine_backup_log ON engine_backup_log(db_name, done_at DESC);


