UPDATE cluster
    SET cpu_name =
        (CASE
            WHEN (cpu_name = 'Intel Nehalem IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Nehalem Family'
            WHEN (cpu_name = 'Intel Nehalem IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Nehalem Family'
            WHEN (cpu_name = 'Intel Westmere IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Westmere Family'
            WHEN (cpu_name = 'Intel Westmere IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Westmere Family'
            WHEN (cpu_name = 'Intel SandyBridge IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel SandyBridge Family'
            WHEN (cpu_name = 'Intel SandyBridge IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel SandyBridge Family'
            WHEN (cpu_name = 'Intel Haswell-noTSX IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Haswell-noTSX Family'
            WHEN (cpu_name = 'Intel Haswell-noTSX IBRS SSBD MDS Family' AND compatibility_version >= '4. 4') THEN 'Secure Intel Haswell-noTSX Family'
            WHEN (cpu_name = 'Intel Haswell IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Haswell Family'
            WHEN (cpu_name = 'Intel Haswell IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Haswell Family'
            WHEN (cpu_name = 'Intel Broadwell-noTSX IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Broadwell-noTSX Family'
            WHEN (cpu_name = 'Intel Broadwell-noTSX IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Broadwell-noTSX Family'
            WHEN (cpu_name = 'Intel Broadwell IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Broadwell Family'
            WHEN (cpu_name = 'Intel Broadwell IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Broadwell Family'
            WHEN (cpu_name = 'Intel Skylake Client IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Skylake Client Family'
            WHEN (cpu_name = 'Intel Skylake Client IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Skylake Client Family'
            WHEN (cpu_name = 'Intel Skylake Server IBRS SSBD Family' AND compatibility_version >= '4.4') THEN 'Intel Skylake Server Family'
            WHEN (cpu_name = 'Intel Skylake Server IBRS SSBD MDS Family' AND compatibility_version >= '4.4') THEN 'Secure Intel Skylake Server Family'
            WHEN (cpu_name = 'AMD EPYC IBPB SSBD' AND compatibility_version >= '4.4') THEN 'Secure AMD EPYC'
         ELSE cpu_name
         END);
