UPDATE cluster SET cpu_name = 'Intel Nehalem IBRS SSBD Family'
WHERE cpu_name = 'Intel Nehalem IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Westmere IBRS SSBD Family'
WHERE cpu_name = 'Intel Westmere IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel SandyBridge IBRS SSBD Family'
WHERE cpu_name = 'Intel SandyBridge IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Haswell-noTSX IBRS SSBD Family'
WHERE cpu_name = 'Intel Haswell-noTSX IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Haswell IBRS SSBD Family'
WHERE cpu_name = 'Intel Haswell IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Broadwell-noTSX IBRS SSBD Family'
WHERE cpu_name = 'Intel Broadwell-noTSX IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Broadwell IBRS SSBD Family'
WHERE cpu_name = 'Intel Broadwell IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Skylake Client IBRS SSBD Family'
WHERE cpu_name = 'Intel Skylake Client IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'Intel Skylake Server IBRS SSBD Family'
WHERE cpu_name = 'Intel Skylake Server IBRS Family' AND compatibility_version >= '4.3';

UPDATE cluster SET cpu_name = 'AMD EPYC IBPB SSBD'
WHERE cpu_name = 'AMD EPYC IBPB' AND compatibility_version >= '4.3';


