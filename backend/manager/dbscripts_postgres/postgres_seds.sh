#!/bin/sh
sed -i "s~RETURN_VALUE~SWV_RefCur~g" /install/engine/modules/dal/src/main/java/com/redhat/engine/dal/dbbroker/DbFacade.java
sed -i "s~addValue(\"~addValue(\"v_~g" /install/engine/modules/dal/src/main/java/com/redhat/engine/dal/dbbroker/DbFacade.java
sed -i "s~dbResults.get(\"~dbResults.get(\"v_~" /install/engine/modules/dal/src/main/java/com/redhat/engine/dal/dbbroker/DbFacade.java
sed -i "s~return \"SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;\" + query;~return query;~" /install/engine/modules/bll/src/main/java/com/redhat/engine/bll/queryData2.java
sed -i "s~dbFacade  = new DbFacade(datasource);~dbFacade  = new DbFacade(new PGHack(datasource));~" /install/engine/modules/dal/src/main/java/com/redhat/engine/dal/dbbroker/DbFacadeLocator.java