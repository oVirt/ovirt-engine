-- rest blank template cpu_profile since it doesn't reside in any cluster (although it referenced to default cluster)
UPDATE vm_static SET cpu_profile_id = NULL where vm_guid = '00000000-0000-0000-0000-000000000000';
