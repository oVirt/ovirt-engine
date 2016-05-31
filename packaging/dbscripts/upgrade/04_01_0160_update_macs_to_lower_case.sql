UPDATE mac_pool_ranges
SET from_mac = lower(from_mac),
    to_mac = lower(to_mac);
