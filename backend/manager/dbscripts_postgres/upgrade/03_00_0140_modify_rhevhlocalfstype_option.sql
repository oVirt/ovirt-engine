-- Adjusts the RhevhLocalFSPath vdc_options value
select fn_db_update_config_value('RhevhLocalFSPath','/data/images/rhev','general');

