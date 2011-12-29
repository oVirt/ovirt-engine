-- Change Minimal ETL version back to 3.0.0, since upstream version is still 3.0.0
select fn_db_update_config_value('MinimalETLVersion','3.0.0','general');

