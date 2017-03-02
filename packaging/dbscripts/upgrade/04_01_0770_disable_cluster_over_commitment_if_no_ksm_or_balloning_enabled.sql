UPDATE cluster
SET max_vds_memory_over_commit = 100
WHERE max_vds_memory_over_commit > 100
      AND enable_ksm = FALSE
      AND enable_balloon = FALSE;