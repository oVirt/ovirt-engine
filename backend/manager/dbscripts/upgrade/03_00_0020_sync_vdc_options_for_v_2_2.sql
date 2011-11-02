-- update keys from internal version 2.3 to official 3.0`
update vdc_options set version = '3.0' where version = '2.3';
-- new keys
select fn_db_add_config_value('VdcVersion','3.0.0.0','general');
select fn_db_add_config_value('AutoRepoDomainRefreshTime','60','general');
select fn_db_add_config_value('DesktopAudioDeviceType','default,ac97','2.2');
select fn_db_add_config_value('DesktopAudioDeviceType','WindowsXP,ac97,RHEL4,ac97,RHEL3,ac97,Windows2003x64,ac97,RHEL4x64,ac97,RHEL3x64,ac97,OtherLinux,ac97,Other,ac97,default,ich6','3.0');
select fn_db_add_config_value('FindFenceProxyDelayBetweenRetriesInSec','30','general');
select fn_db_add_config_value('FindFenceProxyRetries','3','general');
select fn_db_add_config_value('LdapQueryPageSize','1000','general');
select fn_db_add_config_value('LDAPQueryTimeout','30','general');
select fn_db_add_config_value('MaxLDAPQueryPartsNumber','100','general');
select fn_db_add_config_value('MaxStorageVdsDelayCheckSec','5','general');
select fn_db_add_config_value('NumberOfVmsForTopSizeVms','10','general');
select fn_db_add_config_value('PredefinedVMProperties','','2.2');
select fn_db_add_config_value('PredefinedVMProperties','sap_agent=^(true|false)$;sndbuf=^[0-9]+$;vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;viodiskcache=^(none|writeback|writethrough)$','3.0');
select fn_db_add_config_value('UserDefinedVMProperties','','2.2');
select fn_db_add_config_value('UserDefinedVMProperties','','3.0');
select fn_db_add_config_value('RhevhLocalFSPath','/data/images/','general');
select fn_db_add_config_value('ENGINEEARLib','%JBOSS_HOME%\server\engine-slimmed\deploy\engine.ear','general');
select fn_db_add_config_value('SASL_QOP','auth-conf','general');
select fn_db_add_config_value('StoragePoolNonOperationalResetTimeoutInMin','3','general');
select fn_db_add_config_value('SupportCustomProperties','false','2.2');
select fn_db_add_config_value('SupportCustomProperties','true','3.0');
select fn_db_add_config_value('SupportedStorageFormats','0','2.2');
select fn_db_add_config_value('SupportedStorageFormats','0,2','3.0');
select fn_db_add_config_value('SupportGetDevicesVisibility','false','2.2');
select fn_db_add_config_value('SupportGetDevicesVisibility','true','3.0');
select fn_db_add_config_value('SupportStorageFormat','false','2.2');
select fn_db_add_config_value('SupportStorageFormat','true','3.0');
select fn_db_add_config_value('UknownTaskPrePollingLapse','60000','general');
select fn_db_add_config_value('UseRtl8139_pv','true','2.2');
select fn_db_add_config_value('UseRtl8139_pv','false','3.0');

-- modified keys
select fn_db_update_config_value('DBEngine','Postgres','general');
select fn_db_update_config_value('DebugSearchLogging','false','general');
select fn_db_update_config_value('DefaultTimeZone','(GMT) GMT Standard Time','general');
select fn_db_update_config_value('DisableFenceAtStartupInSec','300','general');
select fn_db_update_config_value('IsMultilevelAdministrationOn','true','general');
select fn_db_update_config_value('LogPhysicalMemoryThresholdInMB','1024','general');
select fn_db_update_config_value('PostgresPagingSyntax','OFFSET (%1$s -1) LIMIT %2$s','general');
select fn_db_update_config_value('PostgresSearchTemplate','SELECT * FROM (%2$s) %1$s) as T1 %3$s','general');
select fn_db_update_config_value('ServerCPUList','2:Intel Xeon w/o XD/NX:vmx,sse2:qemu64,-nx,+sse2; 3:Intel Xeon:vmx,sse2,nx:qemu64,+sse2; 4:Intel Conroe Family:vmx,sse2,nx,cx16,ssse3:qemu64,+sse2,+cx16,+ssse3; 5:Intel Penryn Family:vmx,sse2,nx,cx16,ssse3,sse4_1:qemu64,+sse2,+cx16,+ssse3,+sse4.1; 6:Intel Nehalem Family:vmx,sse2,nx,cx16,ssse3,sse4_1,sse4_2,popcnt:qemu64,+sse2,+cx16,+ssse3,+sse4.1,+sse4.2,+popcnt; 2:AMD Opteron G1 w/o NX:svm,sse2:qemu64,-nx,+sse2; 3:AMD Opteron G1:svm,sse2,nx:qemu64,+sse2; 4:AMD Opteron G2:svm,sse2,nx,cx16:qemu64,+sse2,+cx16; 5:AMD Opteron G3:svm,sse2,nx,cx16,sse4a,misalignsse,popcnt,abm:qemu64,+sse2,+cx16,+sse4a,+misalignsse,+popcnt,+abm;','2.2');
select fn_db_update_config_value('ServerCPUList','3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3;','3.0');
select fn_db_update_config_value('VdsFenceOptionMapping','alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port','general');
select fn_db_update_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ipmilan,rsa,rsb,wti,cisco_ucs','2.2');
select fn_db_update_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ipmilan,rsa,rsb,wti,cisco_ucs','3.0');

-- deprecated keys
select fn_db_delete_config_value('ENMailEnableSsl','general');
select fn_db_delete_config_value('ENMailHost','general');
select fn_db_delete_config_value('ENMailPassword','general');
select fn_db_delete_config_value('ENMailPort','general');
select fn_db_delete_config_value('ENMailUser','general');
select fn_db_delete_config_value('FreeSpaceCriticalLow','general');
select fn_db_delete_config_value('PredefinedVMProperties','general');
select fn_db_delete_config_value('RpmsRepositoryUrl','general');
select fn_db_delete_config_value('SysPrep3.0Path','general');
select fn_db_delete_config_value('UseENGINERepositoryRPMs','general');
select fn_db_delete_config_value('VdsErrorsFileName','general');

