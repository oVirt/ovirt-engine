Create or replace FUNCTION __temp_add_gluster_services_tables() returns void
AS $procedure$
BEGIN
    -- Service Types
    CREATE TABLE gluster_service_types
    (
        service_type VARCHAR(100) NOT NULL,
        CONSTRAINT pk_gluster_service_types PRIMARY KEY(service_type)
    ) WITH OIDS;

    -- Services ( There can be multiple services under a given service type )
    CREATE TABLE gluster_services
    (
        id UUID NOT NULL,
        service_type VARCHAR(100) NOT NULL references gluster_service_types(service_type) ON DELETE CASCADE,
        service_name VARCHAR(100) NOT NULL,
        CONSTRAINT pk_gluster_services PRIMARY KEY(id),
        CONSTRAINT unique_gluster_services_type_name UNIQUE (service_type, service_name)
    ) WITH OIDS;

    -- Cluster-Services
    CREATE TABLE gluster_cluster_services
    (
        cluster_id UUID NOT NULL references vds_groups(vds_group_id) ON DELETE CASCADE,
        service_type VARCHAR(100) NOT NULL references gluster_service_types(service_type) ON DELETE CASCADE,
        status VARCHAR(32) NOT NULL,
        _create_date TIMESTAMP WITH TIME ZONE NOT NULL default LOCALTIMESTAMP,
        _update_date TIMESTAMP WITH TIME ZONE,
        CONSTRAINT pk_gluster_cluster_services PRIMARY KEY(cluster_id, service_type)
    ) WITH OIDS;

    -- Create partial index for fetching services of a cluster
    CREATE INDEX IDX_gluster_cluster_services_cluster_id ON gluster_cluster_services(cluster_id);

    -- Server-Services
    CREATE TABLE gluster_server_services
    (
        id UUID NOT NULL,
        server_id UUID NOT NULL references vds_static(vds_id) ON DELETE CASCADE,
        service_id UUID NOT NULL references gluster_services(id) ON DELETE CASCADE,
        pid INTEGER,
        status VARCHAR(32) NOT NULL,
        message VARCHAR(1000),
        _create_date TIMESTAMP WITH TIME ZONE NOT NULL default LOCALTIMESTAMP,
        _update_date TIMESTAMP WITH TIME ZONE,
        CONSTRAINT pk_gluster_server_services PRIMARY KEY(id),
        CONSTRAINT unique_gluster_server_services_server_service UNIQUE (server_id, service_id)
    ) WITH OIDS;

    -- Create partial index for fetching services of a server
    CREATE INDEX IDX_gluster_server_services_server_id ON gluster_server_services(server_id);

END; $procedure$
LANGUAGE plpgsql;

select __temp_add_gluster_services_tables();
drop function __temp_add_gluster_services_tables();

-- Insert services in to the services master table
Create or replace FUNCTION __temp_insert_services() RETURNS VOID
AS $procedure$
BEGIN

-- Service Types
INSERT INTO gluster_service_types(service_type) values ('GLUSTER');
INSERT INTO gluster_service_types(service_type) values ('GLUSTER_SWIFT');
INSERT INTO gluster_service_types(service_type) values ('SMB');

-- Services
-- glusterd
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER', 'glusterd';

-- gluster-swift-proxy
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER_SWIFT', 'gluster-swift-proxy';

-- gluster-swift-container
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER_SWIFT', 'gluster-swift-container';

-- gluster-swift-proxy
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER_SWIFT', 'gluster-swift-object';

-- gluster-swift-proxy
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER_SWIFT', 'gluster-swift-account';

-- memcached
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'GLUSTER_SWIFT', 'memcached';

-- smb
INSERT INTO gluster_services(id, service_type, service_name)
    SELECT uuid_generate_v1(), 'SMB', 'smb';

END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_services();
DROP function __temp_insert_services();
