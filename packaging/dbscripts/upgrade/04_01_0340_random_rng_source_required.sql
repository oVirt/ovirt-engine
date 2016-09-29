SELECT fn_db_rename_column('cluster', 'required_rng_sources', 'additional_rng_sources');

-- Previously column `additional_rng_sources` contained csv of 'RANDOM' and 'HWRNG'.
-- Following query removes 'RANDOM' since this rng source is implicit from now on.
UPDATE cluster
SET additional_rng_sources = CASE WHEN additional_rng_sources LIKE '%HWRNG%' THEN 'HWRNG' ELSE '' END;
