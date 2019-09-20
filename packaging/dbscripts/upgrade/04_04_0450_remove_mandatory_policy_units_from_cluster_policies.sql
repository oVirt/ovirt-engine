DELETE
FROM cluster_policy_units
WHERE policy_unit_id IN (
    '3e4a7d54-9e7f-11e5-8994-feff819cdc9f',  -- ID of CompatibilityVersionFilterPolicyUnit
    '438b052c-90ab-40e8-9be0-a22560202ea6',  -- ID of CpuLevelFilterPolicyUnit
    '6d636bf6-a35c-4f9d-b68d-0731f731cddc',  -- ID of CpuPinningPolicyUnit
    '728a21f1-f97e-4d32-bc3e-b3cc49756abb',  -- ID of HostDeviceFilterPolicyUnit
    '12262ab6-9690-4bc3-a2b3-35573b172d54',  -- ID of PinToHostPolicyUnit
    'b454ae40-f767-45b1-949a-7e5bd04d83ab'); -- ID of VmLeasesReadyFilterPolicyUnit
