CREATE TABLE vm_icons (
    id             UUID           NOT NULL,
    data_url       VARCHAR(32768) NOT NULL -- 1024 * 32
);
-- visual table viewer http://jsfiddle.net/jniederm/u1bf0767/
-- it allows to visually browse joined vm_icons and vm_icon_defaults tables
-- - because dataurl images are difficult to recognize

SELECT fn_db_create_constraint('vm_icons',
                               'pk_vm_icons',
                               'PRIMARY KEY (id)');
-- this works like: ALTER TABLE vm_icons ADD CONSTRAINT vm_icons_data_url_unique UNIQUE (data_url);
-- The catch is that index (on which UNIQUE constraint is based) can't work on long strings (more than about 9000 chars)
-- and simple UNIQUE constraint can't be used on column expressions, only pure columns are allowed
CREATE UNIQUE INDEX vm_icons_data_url_unique_index ON vm_icons( md5(data_url) );

CREATE TABLE vm_icon_defaults (
    id UUID NOT NULL,
    os_id INTEGER NOT NULL,
    small_icon_id UUID NOT NULL,
    large_icon_id UUID NOT NULL
);

SELECT fn_db_create_constraint('vm_icon_defaults',
                               'pk_vm_icon_defaults',
                               'PRIMARY KEY (id)');
SELECT fn_db_create_constraint('vm_icon_defaults',
                               'fk_vm_icon_defaults_small_icon_id_vm_icons_id',
                               'FOREIGN KEY (small_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT');
SELECT fn_db_create_constraint('vm_icon_defaults',
                               'fk_vm_icon_defaults_large_icon_id_vm_icons_id',
                               'FOREIGN KEY (large_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT');
SELECT fn_db_create_constraint('vm_icon_defaults',
                               'unique_vm_icon_defaults_record',
                               'UNIQUE (os_id)');


SELECT fn_db_add_column('vm_static', 'small_icon_id', 'UUID NULL');
SELECT fn_db_add_column('vm_static', 'large_icon_id', 'UUID NULL');

SELECT fn_db_create_constraint('vm_static',
                               'fk_vm_static_small_icon_id_vm_icons_id',
                               'FOREIGN KEY (small_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT');
SELECT fn_db_create_constraint('vm_static',
                               'fk_vm_static_large_icon_id_vm_icons_id',
                               'FOREIGN KEY (large_icon_id) REFERENCES vm_icons(id) ON UPDATE RESTRICT ON DELETE RESTRICT');