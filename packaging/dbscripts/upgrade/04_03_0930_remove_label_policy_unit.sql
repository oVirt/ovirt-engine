-- Delete the LabelFilterPolicyUnit only from cluster policies that contain VmToHostAffinityFilterPolicyUnit
DELETE
FROM cluster_policy_units
WHERE policy_unit_id = '27846536-f653-11e5-9ce9-5e5517507c66' -- ID of LabelFilterPolicyUnit
    AND cluster_policy_id IN (
        select cluster_policy_id
        from cluster_policy_units
        where policy_unit_id = 'e69808a9-8a41-40f1-94ba-dd5d385d82d8' -- ID of VmToHostAffinityFilterPolicyUnit
    );


-- For others, change the LabelFilterPolicyUnit to VmToHostAffinityFilterPolicyUnit
UPDATE cluster_policy_units
SET policy_unit_id = 'e69808a9-8a41-40f1-94ba-dd5d385d82d8'
WHERE policy_unit_id = '27846536-f653-11e5-9ce9-5e5517507c66';
