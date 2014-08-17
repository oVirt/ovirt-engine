-- add cpu profile id field to vm_static
SELECT fn_db_add_column('vm_static', 'cpu_profile_id', 'UUID NULL');

-- Create index for cpu profile
DROP INDEX IF EXISTS IDX_vm_static_cpu_profile_id;
CREATE INDEX IDX_vm_static_cpu_profile_id ON vm_static(cpu_profile_id);

-- Add FK and handle cascade
ALTER TABLE vm_static ADD CONSTRAINT FK_vm_static_cpu_profile_id FOREIGN KEY(cpu_profile_id)
REFERENCES cpu_profiles(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE SET NULL;

--- Create cpu profile for every cluster greater than 3.5
INSERT INTO cpu_profiles(id, name, cluster_id)
  SELECT uuid_generate_v1(),
    vds_groups.name,
    vds_groups.vds_group_id
  FROM vds_groups
  WHERE cast(compatibility_version as float) >= 3.5;

--- Add correct profile id foreach VM/Template.
UPDATE vm_static
  SET cpu_profile_id = cpu_profiles.id
  FROM cpu_profiles
  WHERE vm_static.vds_group_id = cpu_profiles.cluster_id;

