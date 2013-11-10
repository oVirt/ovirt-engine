-- ----------------------------------------------------------------------
-- Add table "vm_init"
-- ----------------------------------------------------------------------
CREATE TABLE vm_init
(
    vm_id UUID NOT NULL,
    host_name TEXT DEFAULT NULL,
    domain TEXT DEFAULT NULL,
    authorized_keys TEXT DEFAULT NULL,
    regenerate_keys BOOLEAN DEFAULT FALSE,
    time_zone VARCHAR(40) DEFAULT NULL,
    dns_servers TEXT DEFAULT NULL,
    dns_search_domains TEXT DEFAULT NULL,
    networks TEXT DEFAULT NULL,
    password TEXT DEFAULT NULL,
    winkey VARCHAR(30) DEFAULT NULL,
    custom_script TEXT DEFAULT NULL,
    CONSTRAINT pk_vm_init PRIMARY KEY (vm_id),
    CONSTRAINT vm_static_vm_init FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE
) WITH OIDS;


-- ----------------------------------------------------------------------
-- copy the domain and time_zone to the new vm_init table
-- ----------------------------------------------------------------------
INSERT into vm_init (vm_id, domain, time_zone) SELECT
vm_guid, domain, time_zone from vm_static
WHERE domain is NOT null OR time_zone is NOT null;


-- ----------------------------------------------------------------------
-- drop the domain from vm_static
-- ----------------------------------------------------------------------
ALTER TABLE vm_static DROP COLUMN domain CASCADE;
