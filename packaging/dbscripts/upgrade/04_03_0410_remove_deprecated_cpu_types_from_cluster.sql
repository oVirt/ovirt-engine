DO $$
-- remove deprecated CPUs from the cluster table for version 4.3 and greater
  DECLARE

    cpu_cluster_name CURSOR FOR SELECT cpu_name FROM cluster WHERE compatibility_version >= '4.3';
    cpu_name_row RECORD;
    cpu_found boolean := FALSE;
    cpu_information_array TEXT[];
    cpu_information TEXT;

  BEGIN

    SELECT regexp_split_to_array(option_value, ';') INTO cpu_information_array FROM
                                 vdc_options WHERE option_name = 'ServerCPUList'
                                 AND version >= '4.3';
    OPEN cpu_cluster_name;
    LOOP
    FETCH cpu_cluster_name INTO cpu_name_row;

        EXIT WHEN NOT FOUND OR cpu_name_row IS NULL;

        FOREACH cpu_information IN ARRAY cpu_information_array LOOP
            if cpu_name_row.cpu_name = split_part(cpu_information, ':', 2) THEN
                cpu_found := TRUE;
                exit;
            END if;
        END loop;

        if NOT cpu_found THEN
            FOREACH cpu_information IN ARRAY cpu_information_array LOOP
                if split_part(cpu_name_row.cpu_name, ' ', 1) =
                    split_part(split_part(cpu_information, ':', 2), ' ', 1) THEN
                    UPDATE cluster SET cpu_name = split_part(cpu_information, ':', 2)
                        WHERE cpu_name = cpu_name_row.cpu_name AND compatibility_version >= '4.3';
                    exit;
                END if;
            END loop;
        END if;

        cpu_found := FALSE;

    END LOOP;
    CLOSE cpu_cluster_name;
  END;
$$;
