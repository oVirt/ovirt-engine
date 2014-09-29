-- update the VMs based on that instance type
update vm_static set num_of_sockets = 2 where
    instance_type_id in (
        select vm_guid from vm_static where entity_type = 'INSTANCE_TYPE' and
            vm_name = 'Medium' and
            description = 'Medium instance type' and
            mem_size_mb = 4096 and
            num_of_sockets = 1 and
            cpu_per_socket = 1
    )
    or
    vm_guid in (
        select vm_guid from vm_static
            where entity_type = 'INSTANCE_TYPE' and
            vm_name = 'Medium' and
            description = 'Medium instance type' and
            mem_size_mb = 4096 and
            num_of_sockets = 1 and
            cpu_per_socket = 1
    );
