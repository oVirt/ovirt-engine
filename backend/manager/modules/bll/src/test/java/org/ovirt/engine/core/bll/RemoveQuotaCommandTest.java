package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class RemoveQuotaCommandTest {

    private final Guid generalGuidQuota = Guid.NewGuid();
    private final Guid storagePoolUUID = Guid.NewGuid();

    @Mock
    private DbFacade db;

    @Mock
    private QuotaDAO quotaDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    /**
     * The command under test.
     */
    private RemoveQuotaCommand<QuotaCRUDParameters> command;

    public RemoveQuotaCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
    }

    @Before
    public void testSetup() {
        mockQuotaDAO();
        mockVmDAO();
    }

    private void mockVmDAO() {
        when(db.getVmDAO()).thenReturn(vmDAO);
        when(DbFacade.getInstance()).thenReturn(db);

        // Mock VM Dao getAllVmsRelatedToQuotaId.
        List<VM> newList = new ArrayList<VM>();
        when(vmDAO.getAllVmsRelatedToQuotaId(generalGuidQuota)).thenReturn(newList);
    }

    private void mockQuotaDAO() {
        when(db.getQuotaDAO()).thenReturn(quotaDAO);
        when(quotaDAO.getById(any(Guid.class))).thenReturn(mockGeneralStorageQuota());
        List<Quota> quotaList = new ArrayList<Quota>();
        quotaList.add(new Quota());
        quotaList.add(new Quota());
        when(quotaDAO.getQuotaByStoragePoolGuid(storagePoolUUID)).thenReturn(quotaList);
    }

    private void mockStoragePoolDAO() {
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(mockStoragePool());
    }

    @Test
    public void testExecuteCommand() throws Exception {
        mockStoragePoolDAO();
        RemoveQuotaCommand<QuotaCRUDParameters> removeQuotaCommand = createCommand();
        removeQuotaCommand.executeCommand();
    }

    @Test
    public void testCanDoActionCommand() throws Exception {
        mockStoragePoolDAO();
        RemoveQuotaCommand<QuotaCRUDParameters> removeQuotaCommand = createCommand();
        assertTrue(removeQuotaCommand.canDoAction());
    }

    @Test
    public void testCanRemoveQuotaWithEnforcedDC() throws Exception {
        storage_pool storagePool = mockStoragePool();
        storagePool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(storagePool);
        RemoveQuotaCommand<QuotaCRUDParameters> removeQuotaCommand = createCommand();
        assertTrue(removeQuotaCommand.validDefaultQuota(mockGeneralStorageQuota()));
    }

    @Test
    public void testCanRemoveDefaultQuotaWithEnforcedDC() throws Exception {
        storage_pool storagePool = mockStoragePool();
        storagePool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(storagePool);
        RemoveQuotaCommand<QuotaCRUDParameters> removeQuotaCommand = createCommand();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(true);
        assertTrue(removeQuotaCommand.validDefaultQuota(quota));
    }

    @Test
    public void testCanRemoveDefaultQuotaWithDisabledDC() throws Exception {
        storage_pool storagePool = mockStoragePool();
        storagePool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);
        when(storagePoolDAO.get(any(Guid.class))).thenReturn(storagePool);
        RemoveQuotaCommand<QuotaCRUDParameters> removeQuotaCommand = createCommand();
        Quota quota = mockGeneralStorageQuota();
        quota.setIsDefaultQuota(true);
        assertFalse(removeQuotaCommand.validDefaultQuota(quota));
        assertTrue("canDoAction failed since default quota can not be removed from storage pool with disabled quota enforcement.",
                removeQuotaCommand.getReturnValue()
                        .getCanDoActionMessages()
                        .contains(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_WITH_DEFAULT_INDICATION_CAN_NOT_BE_REMOVED.toString()));
    }

    private RemoveQuotaCommand<QuotaCRUDParameters> createCommand() {
        QuotaCRUDParameters param = new QuotaCRUDParameters();
        param.setQuotaId(generalGuidQuota);
        command = spy(new RemoveQuotaCommand<QuotaCRUDParameters>(param));
        doReturn(storagePoolDAO).when(command).getStoragePoolDAO();
        doReturn(quotaDAO).when(command).getQuotaDAO();
        return command;
    }

    private storage_pool mockStoragePool() {
        storage_pool storagePool = new storage_pool();
        storagePool.setId(storagePoolUUID);
        storagePool.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);
        return storagePool;
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

        generalQuota.setId(generalGuidQuota);
        generalQuota.setStoragePoolId(storagePoolUUID);
        return generalQuota;
    }
}
