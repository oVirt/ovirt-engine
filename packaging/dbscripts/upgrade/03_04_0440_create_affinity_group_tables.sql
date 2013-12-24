-- create affinity_groups table
CREATE TABLE affinity_groups
(
    id UUID NOT NULL CONSTRAINT affinity_group_pk PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(4000),
    cluster_id UUID NOT NULL CONSTRAINT affinity_group_cluster_id_fk REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE,
    positive BOOLEAN NOT NULL DEFAULT TRUE,
    enforcing BOOLEAN NOT NULL DEFAULT TRUE,
    _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE default NULL
) WITH OIDS;
-- create index for cluster id
CREATE INDEX IDX_affinity_group_cluster_id ON affinity_groups(cluster_id);
-- create affinity_groups members table
CREATE TABLE affinity_group_members
(
    affinity_group_id UUID NOT NULL CONSTRAINT affinity_group_member_affinity_id_fk REFERENCES affinity_groups(id) ON DELETE CASCADE,
    vm_id UUID NOT NULL CONSTRAINT affinity_group_member_vm_id_fk REFERENCES vm_static(vm_guid) ON DELETE CASCADE
);

