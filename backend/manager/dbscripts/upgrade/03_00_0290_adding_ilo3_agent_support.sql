--adding ilo3 to current agent list and setting options
select fn_db_update_config_value('VdsFenceOptionMapping','alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port;ilo3:','general');
select fn_db_update_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ilo3,ipmilan,rsa,rsb,wti,cisco_ucs','3.0');
--adding agent mapping and  default parameters
select fn_db_add_config_value('FenceAgentMapping','ilo3=ipmilan','general');
select fn_db_add_config_value('FenceAgentDefaultParams','ilo3:lanplus,timeout=4','general');

