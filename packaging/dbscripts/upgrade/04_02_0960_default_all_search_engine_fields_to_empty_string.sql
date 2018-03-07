SELECT fn_db_change_table_string_columns_to_empty_string('ad_groups','{name,domain,distinguishedname}');
SELECT fn_db_change_table_string_columns_to_empty_string('users','{name,surname,department}');
SELECT fn_db_change_table_string_columns_to_empty_string('audit_log','{user_name,vm_name,vm_template_name,vds_name,
                                                         storage_pool_name,storage_domain_name,cluster_name,quota_name,
                                                         gluster_volume_name}');
SELECT fn_db_change_table_string_columns_to_empty_string('cluster','{description,cpu_name,emulated_machine,free_text_comment}');
SELECT fn_db_change_table_string_columns_to_empty_string('vm_device','{alias}');
SELECT fn_db_change_table_string_columns_to_empty_string('network','{name,description,label,free_text_comment}');
SELECT fn_db_change_table_string_columns_to_empty_string('providers','{description}');
SELECT fn_db_change_table_string_columns_to_empty_string('quota','{description}');
SELECT fn_db_change_table_string_columns_to_empty_string('storage_pool','{free_text_comment}');
SELECT fn_db_change_table_string_columns_to_empty_string('vds_static','{free_text_comment}');
SELECT fn_db_change_table_string_columns_to_empty_string('vm_static','{description,free_text_comment}');
