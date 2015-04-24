select fn_db_drop_column('gluster_georep_session_details', 'files_synced');
select fn_db_drop_column('gluster_georep_session_details', 'files_pending');
select fn_db_drop_column('gluster_georep_session_details', 'bytes_pending');
select fn_db_drop_column('gluster_georep_session_details', 'deletes_pending');
select fn_db_drop_column('gluster_georep_session_details', 'files_skipped');

select fn_db_add_column('gluster_georep_session_details', 'data_pending', 'BIGINT NULL');
select fn_db_add_column('gluster_georep_session_details', 'entry_pending', 'BIGINT NULL');
select fn_db_add_column('gluster_georep_session_details', 'meta_pending', 'BIGINT NULL');
select fn_db_add_column('gluster_georep_session_details', 'failures', 'BIGINT NULL');
select fn_db_add_column('gluster_georep_session_details', 'last_synced_at', 'TIMESTAMP WITH TIME ZONE NULL');
select fn_db_add_column('gluster_georep_session_details', 'checkpoint_time', 'TIMESTAMP WITH TIME ZONE NULL');
select fn_db_add_column('gluster_georep_session_details', 'checkpoint_completed_time', 'TIMESTAMP WITH TIME ZONE NULL');
select fn_db_add_column('gluster_georep_session_details', 'is_checkpoint_completed', 'BOOLEAN DEFAULT FALSE');


