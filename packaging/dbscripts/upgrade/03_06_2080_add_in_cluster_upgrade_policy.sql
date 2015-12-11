-- Add Upgrade policy
INSERT INTO cluster_policies (
    id,
    name,
    description,
    is_locked,
    is_default
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    'InClusterUpgrade',
    'Allow upgrade hosts to newer major OS versions',
    true,
    false
    );

-- InClusterUpgradeWeightPolicyUnit
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '84e6ddee-ab0d-42dd-82f0-c298889db568',
    0,
    1
    );

-- InClusterUpgradeFilterPolicyUnit
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '84e6ddee-ab0d-42dd-82f0-c298889db567',
    0,
    0
    );

-- EmulatedMachineFilter
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    (
        SELECT id
        FROM policy_units
        WHERE name = 'Emulated-Machine' limit 1
        ),
    0,
    0
    );

-- Network
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '72163d1c-9468-4480-99d9-0888664eb143',
    0,
    0
    );

-- Migration
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    'e659c871-0bf1-4ccc-b748-f28f5d08ddda',
    0,
    0
    );

-- Memory
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    'c9ddbb34-0e1d-4061-a8d7-b0893fa80932',
    0,
    0
    );

-- CPU
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '6d636bf6-a35c-4f9d-b68d-0731f720cddc',
    0,
    0
    );

-- CPU Level
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '438b052c-90ab-40e8-9be0-a22560202ea6',
    0,
    0
    );

-- No load balancer
INSERT INTO cluster_policy_units (
    cluster_policy_id,
    policy_unit_id,
    filter_sequence,
    factor
    )
VALUES (
    '8d5d7bec-68de-4a67-b53e-0ac54686d586',
    '38440000-8cf0-14bd-c43e-10b96e4ef00a',
    0,
    0
    );
