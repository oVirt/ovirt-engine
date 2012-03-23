package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, QuotaHelper.class })
public class AddQuotaCommandTest {
    @Mock
    private DbFacade db;

    @Mock
    private QuotaHelper quotaHelper;

    @Mock
    private QuotaDAO quotaDAO;

    /**
     * The command under test.
     */
    private AddQuotaCommand<QuotaCRUDParameters> command;

    private Guid storagePoolUUID = Guid.NewGuid();
    storage_pool storagePool = new storage_pool();
    storage_domains firstStorageDomains = new storage_domains();
    storage_domains secondStorageDomains = new storage_domains();

    public AddQuotaCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(QuotaHelper.class);
    }

    @Before
    public void testSetup() {
        mockDbFacade();
        mockQuotaHelper();
    }

    @SuppressWarnings("unchecked")
    private void mockQuotaHelper() {
        mockStatic(QuotaHelper.class);
        Mockito.when(QuotaHelper.getInstance()).thenReturn(quotaHelper);
        when(quotaHelper.checkQuotaValidationForAddEdit(Matchers.any(Quota.class), Matchers.any(ArrayList.class))).thenReturn(true);
    }

    private void mockDbFacade() {
        mockStatic(DbFacade.class);
        Mockito.when(DbFacade.getInstance()).thenReturn(db);
        when(db.getQuotaDAO()).thenReturn(quotaDAO);
        when(quotaDAO.getById(any(Guid.class))).thenReturn(mockGeneralStorageQuota());
    }

    @Test
    public void testExecuteCommand() throws Exception {
        AddQuotaCommand<QuotaCRUDParameters> addQuotaCommand = createCommand();
        addQuotaCommand.executeCommand();
    }

    @Test
    public void testCanDoiActionCommand() throws Exception {
        AddQuotaCommand<QuotaCRUDParameters> addQuotaCommand = createCommand();
        addQuotaCommand.canDoAction();
    }

    private AddQuotaCommand<QuotaCRUDParameters> createCommand() {
        QuotaCRUDParameters param = new QuotaCRUDParameters(mockGeneralStorageQuota());
        command = new AddQuotaCommand<QuotaCRUDParameters>(param);
        return spy(command);
    }

    private Quota mockGeneralStorageQuota() {
        Quota generalQuota = new Quota();
        generalQuota.setDescription("New Quota to create");
        generalQuota.setQuotaName("New Quota Name");
        QuotaStorage storageQuota = new QuotaStorage();
        storageQuota.setStorageSizeGB(100l);
        storageQuota.setStorageSizeGBUsage(0d);
        generalQuota.setGlobalQuotaStorage(storageQuota);

        QuotaVdsGroup vdsGroupQuota = new QuotaVdsGroup();
        vdsGroupQuota.setVirtualCpu(0);
        vdsGroupQuota.setVirtualCpuUsage(0);
        vdsGroupQuota.setMemSizeMB(0l);
        vdsGroupQuota.setMemSizeMBUsage(0l);
        generalQuota.setGlobalQuotaVdsGroup(vdsGroupQuota);

        generalQuota.setId(Guid.NewGuid());
        generalQuota.setStoragePoolId(storagePoolUUID);
        return generalQuota;
    }
}
