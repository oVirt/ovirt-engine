-- Since we will have more than one optimization type, defining an integer
-- for defining cluster optimization:
-- 0 is none.
-- 1 is optimize for speed.
select fn_db_add_column('vds_groups', 'optimization_type', 'smallint default 0');
