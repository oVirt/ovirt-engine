-- Add In cluster upgrade filter module
INSERT INTO policy_units (
    id,
    name,
    is_internal,
    custom_properties_regex,
    type,
    enabled,
    description
    )
VALUES (
    '84e6ddee-ab0d-42dd-82f0-c298889db567',
    'InClusterUpgrade',
    TRUE,
    NULL,
    0,
    true,
    'Filter out all hosts which run an older OS version than the vm is currently running on.'
    );

-- Add In cluster upgrade weight module
INSERT INTO policy_units (
    id,
    name,
    is_internal,
    custom_properties_regex,
    type,
    enabled,
    description
    )
VALUES (
    '84e6ddee-ab0d-42dd-82f0-c298889db568',
    'InClusterUpgrade',
    TRUE,
    NULL,
    1,
    true,
    'Penalize hosts with older OS version more than hosts with the sam OS version where a vm is currently running on. Newer OS versions are not penalized'
    );
