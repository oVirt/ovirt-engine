UPDATE vm_static
SET is_stateless = true
WHERE vm_guid IN (
    SELECT vm_guid
    FROM vm_pool_map
)
