select fn_db_delete_config_value('VM64BitMaxMemorySizeInMB','general');
select fn_db_add_config_value('VM64BitMaxMemorySizeInMB','524288','3.0');
select fn_db_add_config_value('VM64BitMaxMemorySizeInMB','262144','2.2');
