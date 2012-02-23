package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class })
public class AutoRecoveryManagerTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(Backend.class);
        final BackendInternal backendMock = Mockito.mock(BackendInternal.class);
        Mockito.when(Backend.getInstance()).thenReturn(backendMock);
        PowerMockito.mockStatic(DbFacade.class);

        final DbFacade dbFacadeMock = Mockito.mock(DbFacade.class);
        final VdsDAO vdsDaoMock = Mockito.mock(VdsDAO.class);
        final ArrayList<VDS> vdss = new ArrayList<VDS>();
        final VDS vds = new VDS();
        vdss.add(vds);
        Mockito.when(vdsDaoMock.listFailedAutorecoverables()).thenReturn(vdss);
        Mockito.when(dbFacadeMock.getVdsDAO()).thenReturn(vdsDaoMock);
        Mockito.when(DbFacade.getInstance()).thenReturn(dbFacadeMock);

        final StorageDomainDAO storageDomainDaoMock = Mockito.mock(StorageDomainDAO.class);
        final ArrayList<storage_domains> storageDomains = new ArrayList<storage_domains>();
        Mockito.when(storageDomainDaoMock.listFailedAutorecoverables()).thenReturn(storageDomains);
        Mockito.when(dbFacadeMock.getStorageDomainDAO()).thenReturn(storageDomainDaoMock);

        final IConfigUtilsInterface configMock = Mockito.mock(IConfigUtilsInterface.class);
        Config.setConfigUtils(configMock);

    }

    @Test
    public void onTimer() {
        AutoRecoveryManager.getInstance().onTimer();
    }

}
