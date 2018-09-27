
UPDATE cluster SET cpu_name = CASE
    WHEN cpu_name = 'Intel Nehalem Family-IBRS' THEN 'Intel Nehalem IBRS Family' -- fix wrong 4.1.9 name
    WHEN cpu_name = 'Intel Nehalem-IBRS Family' THEN 'Intel Nehalem IBRS Family' -- align -IBRS
    WHEN cpu_name = 'Intel Westmere-IBRS Family' THEN 'Intel Westmere IBRS Family'
    WHEN cpu_name = 'Intel SandyBridge-IBRS Family' THEN 'Intel SandyBridge IBRS Family'
    WHEN cpu_name = 'Intel Haswell Family-IBRS' THEN 'Intel Haswell IBRS Family' -- fix wrong 3.6 name
    WHEN cpu_name = 'Intel Haswell-noTSX-IBRS Family' THEN 'Intel Haswell-noTSX IBRS Family'
    WHEN cpu_name = 'Intel Haswell-IBRS Family' THEN 'Intel Haswell IBRS Family'
    WHEN cpu_name = 'Intel Broadwell-noTSX-IBRS Family' THEN 'Intel Broadwell-noTSX IBRS Family'
    WHEN cpu_name = 'Intel Broadwell-IBRS Family' THEN 'Intel Broadwell IBRS Family'
    WHEN cpu_name = 'Intel Skylake Family' THEN 'Intel Skylake Client Family' -- rename Skylake
    WHEN cpu_name = 'Intel Skylake-IBRS Family' THEN 'Intel Skylake Client IBRS Family'
    ELSE cpu_name
END;
