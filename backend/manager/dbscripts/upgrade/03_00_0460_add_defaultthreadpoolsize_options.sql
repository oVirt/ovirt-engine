-- Added a new configuration option: DefaultMinThreadPoolSize, DefaultMaxThreadPoolSize, DefaultMaxSizeOfWaitingTasks
select fn_db_add_config_value('DefaultMinThreadPoolSize','10','general');
select fn_db_add_config_value('DefaultMaxThreadPoolSize','200','general');
select fn_db_add_config_value('DefaultMaxSizeOfWaitingTasks','1500','general');
