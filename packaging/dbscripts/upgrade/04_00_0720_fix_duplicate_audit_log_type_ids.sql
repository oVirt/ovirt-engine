CREATE INDEX idx_audit_log_type_name ON audit_log(log_type, log_type_name);

UPDATE audit_log
  SET log_type = 27
  WHERE log_type = 20
    AND log_type_name = 'VDS_STATUS_CHANGE_FAILED_DUE_TO_STOP_SPM_FAILURE';

UPDATE audit_log
  SET log_type = 28
  WHERE log_type = 21
    AND log_type_name = 'VDS_PROVISION';

UPDATE audit_log
  SET log_type = 90
  WHERE log_type = 517
    AND log_type_name = 'VDS_FAILED_TO_GET_HOST_HARDWARE_INFO';

UPDATE audit_log
  SET log_type = 187
  WHERE log_type = 533
    AND log_type_name = 'VDS_STORAGE_CONNECTION_FAILED_BUT_LAST_VDS';

UPDATE audit_log
  SET log_type = 188
  WHERE log_type = 535
    AND log_type_name = 'VDS_STORAGES_CONNECTION_FAILED';

UPDATE audit_log
  SET log_type = 189
  WHERE log_type = 534
    AND log_type_name = 'VDS_STORAGE_VDS_STATS_FAILED';

UPDATE audit_log
  SET log_type = 179
  WHERE log_type = 156
    AND log_type_name = 'USER_INITIATED_RUN_VM_AND_PAUSE';

UPDATE audit_log
  SET log_type = 130
  WHERE log_type = 532
    AND log_type_name = 'USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN';

UPDATE audit_log
  SET log_type = 4095
  WHERE log_type = 4089
    AND log_type_name = 'GLUSTER_HOST_UUID_ALREADY_EXISTS';

UPDATE audit_log
  SET log_type = 29
  WHERE log_type = 53
    AND log_type_name = 'USER_ADD_VM_TEMPLATE_SUCCESS';

UPDATE audit_log
  SET log_type = 36
  WHERE log_type = 54
    AND log_type_name = 'USER_ADD_VM_TEMPLATE_FAILURE';

UPDATE audit_log
  SET log_type = 174
  WHERE log_type = 148
    AND log_type_name = 'VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY';

UPDATE audit_log
  SET log_type = 175
  WHERE log_type = 149
    AND log_type_name = 'VM_IMPORT_FROM_CONFIGURATION_ATTACH_DISKS_FAILED';

UPDATE audit_log
  SET log_type = 176
  WHERE log_type = 149
    AND log_type_name = 'VM_BALLOON_DRIVER_ERROR';

UPDATE audit_log
  SET log_type = 177
  WHERE log_type = 150
    AND log_type_name = 'VM_BALLOON_DRIVER_UNCONTROLLED';

UPDATE audit_log
  SET log_type = 178
  WHERE log_type = 151
    AND log_type_name = 'VM_MEMORY_NOT_IN_RECOMMENDED_RANGE';

UPDATE audit_log
  SET log_type = 183
  WHERE log_type = 456
    AND log_type_name = 'USER_ATTACH_TAG_TO_TEMPLATE';

UPDATE audit_log
  SET log_type = 184
  WHERE log_type = 457
    AND log_type_name = 'USER_ATTACH_TAG_TO_TEMPLATE_FAILED';

UPDATE audit_log
  SET log_type = 185
  WHERE log_type = 458
    AND log_type_name = 'USER_DETACH_TEMPLATE_FROM_TAG';

UPDATE audit_log
  SET log_type = 186
  WHERE log_type = 459
    AND log_type_name = 'USER_DETACH_TEMPLATE_FROM_TAG_FAILED';

UPDATE audit_log
  SET log_type = 200
  WHERE log_type = 1162
    AND log_type_name = 'IMPORTEXPORT_GET_VMS_INFO_FAILED';

UPDATE audit_log
  SET log_type = 190
  WHERE log_type = 1012
    AND log_type_name = 'UPDATE_OVF_FOR_STORAGE_DOMAIN_FAILED';

UPDATE audit_log
  SET log_type = 191
  WHERE log_type = 1013
    AND log_type_name = 'CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED';

UPDATE audit_log
  SET log_type = 192
  WHERE log_type = 1014
    AND log_type_name = 'CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_INITIATE_FAILED';

UPDATE audit_log
  SET log_type = 193
  WHERE log_type = 1015
    AND log_type_name = 'DELETE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED';

UPDATE audit_log
  SET log_type = 160
  WHERE log_type = 1100
    AND log_type_name = 'USER_ACCOUNT_DISABLED_OR_LOCKED';

UPDATE audit_log
  SET log_type = 205
  WHERE log_type = 1150
    AND log_type_name = 'PROVIDER_ADDED';

UPDATE audit_log
  SET log_type = 206
  WHERE log_type = 1151
    AND log_type_name = 'PROVIDER_ADDITION_FAILED';

UPDATE audit_log
  SET log_type = 207
  WHERE log_type = 1152
    AND log_type_name = 'PROVIDER_UPDATED';

UPDATE audit_log
  SET log_type = 208
  WHERE log_type = 1153
    AND log_type_name = 'PROVIDER_UPDATE_FAILED';

UPDATE audit_log
  SET log_type = 209
  WHERE log_type = 1154
    AND log_type_name = 'PROVIDER_REMOVED';

UPDATE audit_log
  SET log_type = 210
  WHERE log_type = 1155
    AND log_type_name = 'PROVIDER_REMOVAL_FAILED';

UPDATE audit_log
  SET log_type = 211
  WHERE log_type = 1156
    AND log_type_name = 'PROVIDER_CERTIFICATE_CHAIN_IMPORTED';

UPDATE audit_log
  SET log_type = 212
  WHERE log_type = 1157
    AND log_type_name = 'PROVIDER_CERTIFICATE_CHAIN_IMPORT_FAILED';

