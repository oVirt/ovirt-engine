------------------------------------
--     Changed values
------------------------------------
--Set version and supported clusters
select fn_db_update_config_value('VdcVersion','3.1.0.0','general');
select fn_db_update_config_value('SupportedClusterLevels','2.2,3.0,3.1','general');
------------------------------------
--     Added values
------------------------------------
--Handling User defined VM properties
select fn_db_add_config_value('UserDefinedVMProperties','','3.1');
--Handling Predefined VM properties
select fn_db_add_config_value('PredefinedVMProperties','sap_agent=^(true|false)$;sndbuf=^[0-9]+$;vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;viodiskcache=^(none|writeback|writethrough)$','3.1');
select fn_db_add_config_value('UseRtl8139_pv','false','3.1');
select fn_db_add_config_value('EmulatedMachine','pc-0.14','3.1');
select fn_db_add_config_value('LimitNumberOfNetworkInterfaces','false','3.1');
select fn_db_add_config_value('DesktopAudioDeviceType','WindowsXP,ac97,RHEL4,ac97,RHEL3,ac97,Windows2003x64,ac97,RHEL4x64,ac97,RHEL3x64,ac97,OtherLinux,ac97,Other,ac97,default,ich6','3.1');
--Handling CPU flags syntax: {id:name:flags,..,:vdsm-command};{id:...}
select fn_db_add_config_value('ServerCPUList','3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3;','3.1');
select fn_db_add_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ipmilan,rsa,rsb,wti,cisco_ucs','3.1');
--Handling Total Numbers of Virtual Machine CPUs
select fn_db_add_config_value('MaxNumOfVmCpus','64','3.1');
--Handling Max Number of Socket per Virtual Machine
select fn_db_add_config_value('MaxNumOfVmSockets','16','3.1');
--Handling Max Number of CPU per socket
select fn_db_add_config_value('MaxNumOfCpuPerSocket','16','3.1');
select fn_db_add_config_value('LocalStorageEnabled','true','3.1');
select fn_db_add_config_value('SupportCustomProperties','true','3.1');
select fn_db_add_config_value('SupportGetDevicesVisibility','true','3.1');
select fn_db_add_config_value('SupportStorageFormat','true','3.1');
select fn_db_add_config_value('SupportedStorageFormats','0,2','3.1');



