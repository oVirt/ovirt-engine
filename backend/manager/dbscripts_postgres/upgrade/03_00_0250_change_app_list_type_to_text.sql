-- change app_list length of vm_dynamic to unlimited
select fn_db_change_column_type('images','app_list','varchar','text');
select fn_db_change_column_type('vm_dynamic','app_list','varchar','text');
