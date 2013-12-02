select fn_db_add_column('vds_statistics','anonymous_hugepages', 'INTEGER NULL');
UPDATE vds_statistics vs SET anonymous_hugepages = (SELECT anonymous_hugepages FROM vds_dynamic vd WHERE vs.vds_id = vd.vds_id);
select fn_db_drop_column ('vds_dynamic', 'anonymous_hugepages');
