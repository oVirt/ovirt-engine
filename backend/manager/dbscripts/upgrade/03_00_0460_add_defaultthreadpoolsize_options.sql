-- Added a new configuration option: DefaultMinThreadPoolSize, DefaultMaxThreadPoolSize
select fn_db_add_config_value('DefaultMinThreadPoolSize','50','general');
select fn_db_add_config_value('DefaultMaxThreadPoolSize','500','general');
