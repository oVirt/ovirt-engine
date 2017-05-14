UPDATE vds_static
    SET vds_type = 1 WHERE vds_id IN
       (SELECT vds_id FROM vds_dynamic
           WHERE vds_type = 0
               AND pretty_name LIKE 'Red Hat Virtualization Host%'
       );
