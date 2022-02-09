UPDATE vm_static SET cpu_pinning_policy = 1 WHERE cpu_pinning IS NOT NULL;

SELECT fn_db_create_constraint('vm_static',
                               'vm_static_cpu_pinning_set_for_manual_pinning_policy_only',
                               'CHECK ((cpu_pinning_policy = 1) = (cpu_pinning IS NOT NULL))');