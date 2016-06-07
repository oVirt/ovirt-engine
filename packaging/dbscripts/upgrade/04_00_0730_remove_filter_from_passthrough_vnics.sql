UPDATE vnic_profiles
SET network_filter_id = NULL
WHERE passthrough = TRUE;

