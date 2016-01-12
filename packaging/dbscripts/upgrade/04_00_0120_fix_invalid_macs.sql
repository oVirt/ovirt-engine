UPDATE
  mac_pool_ranges
SET
  from_mac = ( SELECT
                substr(
                  regexp_replace(
                    lpad(replace(from_mac, ':', ''),
                      12,
                      '0'),
                    '(..)',
                    E'\\1:',
                    'g'),
                1,
                17)
              ),
  to_mac = (SELECT
              substr(
                regexp_replace(
                  lpad(replace(to_mac, ':', ''),
                    12,
                    '0'),
                  '(..)',
                  E'\\1:',
                  'g'),
              1,
              17)
            )
;
