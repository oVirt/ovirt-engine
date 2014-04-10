-- Add new tables for numa feature
Create or replace FUNCTION __temp_add_numa_tables() returns void
AS $procedure$
BEGIN
    -- Numa nodes table
    CREATE TABLE numa_node
    (
        numa_node_id UUID NOT NULL,
        vds_id UUID,
        vm_id UUID,
        numa_node_index SMALLINT,
        mem_total BIGINT,
        cpu_count SMALLINT,
        mem_free BIGINT,
        usage_mem_percent INTEGER,
        cpu_sys numeric(5,2),
        cpu_user numeric(5,2),
        cpu_idle numeric(5,2),
        usage_cpu_percent INTEGER,
        distance text,
        CONSTRAINT pk_numa_node PRIMARY KEY(numa_node_id),
        CONSTRAINT fk_numa_node_vds FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE,
        CONSTRAINT fk_numa_node_vm FOREIGN KEY(vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE
    );

    -- Create partial index for numa nodes
    CREATE INDEX IDX_numa_node_vds_id ON numa_node(vds_id);
    CREATE INDEX IDX_numa_node_vm_id ON numa_node(vm_id);

    -- Vds cpu statistics table
    CREATE TABLE vds_cpu_statistics
    (
        vds_cpu_id UUID NOT NULL,
        vds_id UUID NOT NULL,
        cpu_core_id SMALLINT,
        cpu_sys numeric(5,2),
        cpu_user numeric(5,2),
        cpu_idle numeric(5,2),
        usage_cpu_percent INTEGER,
        CONSTRAINT pk_vds_cpu_statistics PRIMARY KEY(vds_cpu_id),
        CONSTRAINT fk_vds_cpu_statistics_vds FOREIGN KEY(vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE
    );

    -- Create partial index for vds cpu statistics
    CREATE INDEX IDX_vds_cpu_statistics_vds_id ON vds_cpu_statistics(vds_id);

    -- Numa nodes internal connection table
    CREATE TABLE vm_vds_numa_node_map
    (
        id UUID NOT NULL,
        vm_numa_node_id UUID NOT NULL,
        vds_numa_node_id UUID,
        vds_numa_node_index SMALLINT,
        is_pinned BOOLEAN DEFAULT false NOT NULL,
        CONSTRAINT pk_vm_vds_numa_node_map PRIMARY KEY(id),
        CONSTRAINT fk_vm_vds_numa_node_map_vds_numa_node FOREIGN KEY(vds_numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE SET NULL,
        CONSTRAINT fk_vm_vds_numa_node_map_vm_numa_node FOREIGN KEY(vm_numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE CASCADE
    );

    -- Create partial index for numa node map
    CREATE INDEX IDX_vm_vds_numa_node_map_vm_numa_node_id ON vm_vds_numa_node_map(vm_numa_node_id);
    CREATE INDEX IDX_vm_vds_numa_node_map_vds_numa_node_id ON vm_vds_numa_node_map(vds_numa_node_id);

    -- Numa node cpus table
    CREATE TABLE numa_node_cpu_map
    (
        id UUID NOT NULL,
        numa_node_id UUID NOT NULL,
        cpu_core_id INTEGER,
        CONSTRAINT pk_numa_node_cpu_map PRIMARY KEY(id),
        CONSTRAINT fk_numa_node_cpu_map_numa_node FOREIGN KEY(numa_node_id) REFERENCES numa_node(numa_node_id) ON DELETE CASCADE
    );

    -- Create partial index for numa node cpu map
    CREATE INDEX IDX_numa_node_cpu_map_numa_node_id ON numa_node_cpu_map(numa_node_id);

END; $procedure$
LANGUAGE plpgsql;

select __temp_add_numa_tables();
drop function __temp_add_numa_tables();


-- Add new columns for numa feature
Create or replace FUNCTION __temp_add_numa_columns() returns void
AS $procedure$
BEGIN
    -- Add columns in table vm_static
    PERFORM fn_db_add_column('vm_static', 'numatune_mode', 'varchar(20)');

    -- Add columns in table vds_dynamic
    PERFORM fn_db_add_column('vds_dynamic', 'auto_numa_balancing', 'smallint');
    PERFORM fn_db_add_column('vds_dynamic', 'is_numa_supported', 'boolean');

END; $procedure$
LANGUAGE plpgsql;

select __temp_add_numa_columns();
drop function __temp_add_numa_columns();
