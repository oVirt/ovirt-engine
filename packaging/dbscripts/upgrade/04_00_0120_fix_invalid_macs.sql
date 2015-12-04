UPDATE
  mac_pool_ranges
SET
  from_mac = (
              SELECT
                string_agg(a, ':')
              FROM (
                    SELECT
                      regexp_replace(regexp_split_to_table(from_mac, ':'), '^(.)$', '0\1') AS a
                    ) AS b
              ),
  to_mac = (
              SELECT
                string_agg(a, ':')
              FROM (
                    SELECT
                      regexp_replace(regexp_split_to_table(to_mac, ':'), '^(.)$', '0\1') AS a
                    ) AS b
              )
;
