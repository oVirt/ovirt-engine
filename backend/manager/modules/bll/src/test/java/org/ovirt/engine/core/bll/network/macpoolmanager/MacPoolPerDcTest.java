package org.ovirt.engine.core.bll.network.macpoolmanager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.network.VmNicDao;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolPerDcTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DbFacade dbFacade;

    @Mock
    private StoragePoolDAO storagePoolDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private MacPoolDao macPoolDao;

    @Mock
    private AuditLogDAO auditLogDao;

    private MacPool macPool;
    private StoragePool dataCenter;
    private VmNic vmNic;
    private static final String MAC_FROM = "00:1a:4a:15:c0:00";
    private static final String MAC_TO = "00:1a:4a:15:c0:ff";
    private MacPoolPerDc pool;

    @Before
    public void setUp() throws Exception {
        DbFacadeLocator.setDbFacade(dbFacade);

        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(dbFacade.getVmNicDao()).thenReturn(vmNicDao);
        when(dbFacade.getMacPoolDao()).thenReturn(macPoolDao);
        when(dbFacade.getAuditLogDao()).thenReturn(auditLogDao);

        macPool = createMacPool(MAC_FROM, MAC_TO);
        dataCenter = createStoragePool(macPool);
        vmNic = createVmNic();
        pool = new MacPoolPerDc();
    }

    @Test()
    public void testInitCanBeCalledTwice() throws Exception {
        MacPoolPerDc pool = new MacPoolPerDc();
        pool.initialize();
        Mockito.verify(dbFacade).getMacPoolDao();
        Mockito.verify(macPoolDao).getAll();

        pool.initialize();
        Mockito.verifyNoMoreInteractions(storagePoolDao, vmNicDao, macPoolDao, dbFacade);
    }

    @Test()
    public void testPoolDoesNotExistForGivenDataCenter() throws Exception {
        pool.initialize();
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.INEXISTENT_POOL_EXCEPTION_MESSAGE);
        pool.poolForDataCenter(Guid.newGuid());
    }

    @Test
    public void testPoolOfGivenGuidExist() {
        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        pool.initialize();
        assertThat(pool.poolForDataCenter(dataCenter.getId()), is(notNullValue()));
    }

    @Test
    public void testNicIsCorrectlyAllocatedInScopedPool() throws Exception {
        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        mockAllMacsForStoragePool(dataCenter, vmNic.getMacAddress());

        pool.initialize();
        assertThat("scoped pool for this nic should exist",
                pool.poolForDataCenter(dataCenter.getId()), is(notNullValue()));

        assertThat("provided mac should be used in returned pool",
                pool.poolForDataCenter(dataCenter.getId()).isMacInUse(vmNic.getMacAddress()), is(true));
    }

    @Test
    public void testCreatePool() throws Exception {
        pool.initialize();

        mockStoragePool(dataCenter);
        pool.createPool(macPool);
        assertThat("scoped pool for this data center should exist",
                pool.poolForDataCenter(dataCenter.getId()), is (notNullValue()));
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
        StoragePool dataCenter = createStoragePool(macPool);

        mockStoragePool(dataCenter);
        mockGettingAllMacPools(macPool);
        pool.initialize();

        assertThat(pool.poolForDataCenter(dataCenter.getId()).addMac(MAC_FROM), is(true));
        assertThat(pool.poolForDataCenter(dataCenter.getId()).addMac(MAC_FROM), is(false));

        final String allocatedMac = allocateMac(dataCenter);

        /*needed due to implementation of modifyPool;
        modify assumes, that all allocated macs is used for vmNics. If allocatedMac succeeded it's expected that all
        vmNics were also successfully persisted to db or all allocated macs were returned to the pool. So after
        allocation we need to mock db, otherwise re-init in modifyPool would return improper results.*/
        mockAllMacsForStoragePool(dataCenter, allocatedMac);

        assertThat(allocatedMac, is(macAddress1));
        try {
            allocateMac(dataCenter);
            Assert.fail("this allocation should not succeed.");
        } catch (VdcBLLException e) {
            //ok, this exception should occur.
        }

        macPool.setAllowDuplicateMacAddresses(true);
        final MacRange macRange = new MacRange();
        macRange.setMacFrom(macAddress1);
        macRange.setMacTo(macAddress2);

        macPool.setRanges(Collections.singletonList(macRange));
        pool.modifyPool(macPool);

        assertThat(pool.poolForDataCenter(dataCenter.getId()).addMac(MAC_FROM), is(true));
        assertThat(allocateMac(dataCenter), is(macAddress2));
    }

    protected String allocateMac(StoragePool dataCenter) {
        return pool.poolForDataCenter(dataCenter.getId()).allocateNewMac();
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

        assertThat(pool.poolForDataCenter(dataCenter.getId()), is(notNullValue()));

        pool.removePool(macPool.getId());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.INEXISTENT_POOL_EXCEPTION_MESSAGE);
        pool.poolForDataCenter(dataCenter.getId());
    }

    @Test
    public void testRemoveOfInexistentMacPool() throws Exception {
        pool.initialize();

        try {
            pool.poolForDataCenter(dataCenter.getId());
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
        final MacRange macRange = new MacRange();
        macRange.setMacFrom(macFrom);
        macRange.setMacTo(macTo);

        final MacPool macPool = new MacPool();
        macPool.setId(Guid.newGuid());
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

    private void expectNotInitializedException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerDc.NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    @Test
    public void testPoolForDataCenterMethod() throws Exception {
        expectNotInitializedException();
        new MacPoolPerDc().poolForDataCenter(Guid.newGuid());
    }

    @Test
    public void testCreatePoolMethod() throws Exception {
        expectNotInitializedException();
        new MacPoolPerDc().createPool(new MacPool());
    }

    @Test
    public void testModifyPoolMethod() throws Exception {
        expectNotInitializedException();
        new MacPoolPerDc().modifyPool(new MacPool());
    }

    @Test
    public void testRemovePoolMethod() throws Exception {
        expectNotInitializedException();
        new MacPoolPerDc().removePool(Guid.newGuid());
    }
}
