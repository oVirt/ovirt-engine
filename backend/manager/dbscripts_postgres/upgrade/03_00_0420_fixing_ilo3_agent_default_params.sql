-- fixing default parameters for ilo3
select fn_db_update_config_value('FenceAgentDefaultParams','ilo3:lanplus,power_wait=4','general');
