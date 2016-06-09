UPDATE vnic_profiles
SET network_filter_id = NULL
WHERE network_filter_id = (
        SELECT filter_id
        FROM network_filter
        WHERE filter_name = 'allow-dhcp-server'
        );

DELETE
FROM network_filter
WHERE filter_name='allow-dhcp-server';

