package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolPerDcTest extends DbDependentTestBase {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private MacPoolFactory macPoolFactory;

    @Mock
    private DecoratedMacPoolFactory decoratedMacPoolFactory;

    @Mock
    private org.ovirt.engine.core.bll.network.macpool.MacPool macPoolMock;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private MacPoolDao macPoolDao;

    @Mock
    private AuditLogDao auditLogDao;

    private MacPool macPool;
    private StoragePool dataCenter;
    private VmNic vmNic;
    private static final String MAC_FROM = "00:1a:4a:15:c0:00";
    private static final String MAC_TO = "00:1a:4a:15:c0:ff";
    private MacPoolPerDc pool;

    @Before
    public void setUp() throws Exception {
        when(DbFacade.getInstance().getStoragePoolDao()).thenReturn(storagePoolDao);
        when(DbFacade.getInstance().getVmNicDao()).thenReturn(vmNicDao);
        when(DbFacade.getInstance().getMacPoolDao()).thenReturn(macPoolDao);
        when(DbFacade.getInstance().getAuditLogDao()).thenReturn(auditLogDao);

        macPool = createMacPool(MAC_FROM, MAC_TO);

        when(macPoolFactory.createMacPool(eq(macPool))).thenReturn(macPoolMock);

        Mockito.doAnswer(invocation -> invocation.getArguments()[1])
                .when(decoratedMacPoolFactory)
                .createDecoratedPool(any(Guid.class),
                        any(org.ovirt.engine.core.bll.network.macpool.MacPool.class),
                        any(List.class));


        dataCenter = createStoragePool(macPool);
        vmNic = createVmNic();
        pool = new MacPoolPerDc(macPoolDao, macPoolFactory, decoratedMacPoolFactory);
    }

    @Test()
    public void testPoolDoesNotExistForGivenDataCenter() throws Exception {
        pool.initialize();
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.INEXISTENT_POOL_EXCEPTION_MESSAGE);
        pool.getMacPoolForDataCenter(Guid.newGuid());
    }

    @Test
    public void testPoolOfGivenGuidExist() {
        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        pool.initialize();
        assertThat(pool.getMacPoolForDataCenter(dataCenter.getId()), is(notNullValue()));
    }

    @Test
    public void testNicIsCorrectlyAllocatedInScopedPool() throws Exception {
        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        mockAllMacsForStoragePool(dataCenter, vmNic.getMacAddress());

        pool.initialize();
        assertThat("scoped pool for this nic should exist",
                pool.getMacPoolForDataCenter(dataCenter.getId()), is(notNullValue()));

        //provided mac should be force-added into relevant pool"
        verify(macPoolMock).forceAddMac(vmNic.getMacAddress());
        verifyNoMoreInteractions(macPoolMock);
    }

    @Test
    public void testCreatePool() throws Exception {
        pool.initialize();

        mockStoragePool(dataCenter);
        pool.createPool(macPool);
        assertThat("scoped pool for this data center should exist",
                pool.getMacPoolForDataCenter(dataCenter.getId()), is (notNullValue()));
    }

    @Test
    public void testCreatePoolWhichExists() throws Exception {
        mockGettingAllMacPools(macPool);
        pool.initialize();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST);
        pool.createPool(macPool);
    }

    @Test
    public void testModifyOfExistingMacPool() throws Exception {
        final String macAddress1 = "00:00:00:00:00:01";
        final String macAddress2 = "00:00:00:00:00:02";

        MacPool macPool = createMacPool(macAddress1, macAddress1);
        when(macPoolFactory.createMacPool(eq(macPool))).thenReturn(macPoolMock);
        StoragePool dataCenter = createStoragePool(macPool);

        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        pool.initialize();

        verifyNoMoreInteractions(macPoolMock);
        pool.getMacPoolForDataCenter(dataCenter.getId()).addMac(MAC_FROM);
        verify(macPoolMock).addMac(MAC_FROM);


        MacPool alteredMacPool = createMacPool(macAddress1, macAddress2, macPool.getId());
        alteredMacPool.setAllowDuplicateMacAddresses(true);

        org.ovirt.engine.core.bll.network.macpool.MacPool differentMacPoolMock =
                Mockito.mock(org.ovirt.engine.core.bll.network.macpool.MacPool.class);
        when(macPoolFactory.createMacPool(eq(alteredMacPool))).thenReturn(differentMacPoolMock);
        pool.modifyPool(alteredMacPool);

        pool.getMacPoolForDataCenter(dataCenter.getId()).addMac(MAC_FROM);
        verify(differentMacPoolMock).addMac(MAC_FROM);

        verifyNoMoreInteractions(macPoolMock);
        verifyNoMoreInteractions(differentMacPoolMock);
    }

    protected String allocateMac(StoragePool dataCenter) {
        return pool.getMacPoolForDataCenter(dataCenter.getId()).allocateNewMac();
    }

    @Test
    public void testModifyOfNotExistingMacPool() throws Exception {
        pool.initialize();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.INEXISTENT_POOL_EXCEPTION_MESSAGE);
        pool.modifyPool(createMacPool(null, null));
    }

    @Test
    public void testRemoveOfMacPool() throws Exception {
        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        pool.initialize();

        assertThat(pool.getMacPoolForDataCenter(dataCenter.getId()), is(notNullValue()));

        pool.removePool(macPool.getId());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.INEXISTENT_POOL_EXCEPTION_MESSAGE);
        pool.getMacPoolForDataCenter(dataCenter.getId());
    }

    @Test
    public void testRemoveOfInexistentMacPool() throws Exception {
        pool.initialize();

        try {
            pool.getMacPoolForDataCenter(dataCenter.getId());
            Assert.fail("pool for given data center should not exist");
        } catch (IllegalStateException e) {
            //ignore this exception.
        }

        pool.removePool(macPool.getId());
        //nothing to test, should not fail.
    }

    private StoragePool createStoragePool(MacPool macPool) {

        //mock existing data centers.
        final StoragePool storagePool = new StoragePool();
        storagePool.setId(Guid.newGuid());
        storagePool.setMacPoolId(macPool.getId());
        return storagePool;
    }

    private MacPool createMacPool(String macFrom, String macTo) {
        return createMacPool(macFrom, macTo, Guid.newGuid());
    }

    private MacPool createMacPool(String macFrom, String macTo, Guid id) {
        final MacRange macRange = new MacRange();
        macRange.setMacFrom(macFrom);
        macRange.setMacTo(macTo);

        final MacPool macPool = new MacPool();
        macPool.setId(id);
        macPool.setRanges(Collections.singletonList(macRange));
        return macPool;
    }

    protected void mockAllMacsForStoragePool(StoragePool storagePool, String... macAddress) {
        when(macPoolDao.getAllMacsForMacPool(eq(storagePool.getMacPoolId()))).thenReturn(Arrays.asList(macAddress));
    }

    protected void mockGettingAllMacPools(MacPool... macPool) {
        when(macPoolDao.getAll()).thenReturn(Arrays.asList(macPool));
    }

    protected VmNic createVmNic() {
        final VmNic vmNic = new VmNic();
        vmNic.setMacAddress("00:1a:4a:15:c0:fe");
        return vmNic;
    }

    protected void mockStoragePool(StoragePool storagePool) {
        when(storagePoolDao.get(eq(storagePool.getId()))).thenReturn(storagePool);
    }
}
