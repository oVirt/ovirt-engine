UPDATE vm_static SET cpu_pinning = NULL where length(trim(cpu_pinning)) = 0;
