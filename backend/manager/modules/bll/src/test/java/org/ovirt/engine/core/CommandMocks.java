package org.ovirt.engine.core;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import static org.mockito.Mockito.doReturn;

public class CommandMocks {

    public static void mockDbFacade(CommandBase cmdMock, DbFacade dbFacade) {
        doReturn(dbFacade).when(cmdMock).getDbFacade();
    }

}
