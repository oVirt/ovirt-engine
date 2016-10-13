-- Previously option 'ClusterRequiredRngSourcesDefault' contained csv of 'RANDOM' and 'HWRNG'.
-- Following query removes 'RANDOM' since this rng source is implicit from now on.
UPDATE vdc_options
SET option_value = CASE WHEN option_value ILIKE '%HWRNG%' THEN 'HWRNG' ELSE '' END
WHERE option_name ILIKE 'ClusterRequiredRngSourcesDefault';
