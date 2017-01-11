package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

public class StorageDomainCommandBaseTest extends BaseCommandTest {
    private static final Guid[] GUIDS = new Guid[] {
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222")
    };
    private final Guid HE_SD_ID = new Guid("33333333-3333-3333-3333-333333333333");
    private final Guid SHARED_SD_ID = new Guid("44444444-4444-4444-4444-444444444444");
    private final Guid LOCAL_SD_ID = new Guid("55555555-5555-5555-5555-555555555555");

    private static final String HE_STORAGE_DOMAIN_NAME = "heStorageDomain";

    @Mock
    private LunDao lunDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private VmDao vmDao;

    @Spy
    @InjectMocks
    public StorageDomainCommandBase<StorageDomainParametersBase> cmd = new TestStorageCommandBase(new StorageDomainParametersBase());

    @Test
    public void statusMatches() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.Inactive));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusIsNull() {
        setStorageDomainStatus(null);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Inactive));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotMatch() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Active));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusInList() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertTrue(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.Inactive,
                StorageDomainStatus.Unknown));
        assertFalse(commandHasInvalidStatusMessage());
    }

    @Test
    public void statusNotInList() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        assertFalse(cmd.checkStorageDomainStatus(StorageDomainStatus.Locked, StorageDomainStatus.Active,
                StorageDomainStatus.Unknown));
        assertTrue(commandHasInvalidStatusMessage());
    }

    @Test
    public void canDetachInactiveDomain() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        storagePoolExists();
        masterDomainIsUp();
        canDetachDomain();
        assertTrue(cmd.canDetachDomain(false, false));
    }

    @Test
    public void canDetachMaintenanceDomain() {
        setStorageDomainStatus(StorageDomainStatus.Inactive);
        storagePoolExists();
        masterDomainIsUp();
        canDetachDomain();
        assertTrue(cmd.canDetachDomain(false, false));
    }

    @Test
    public void checkCinderStorageDomainContainDisks() {
        setCinderStorageDomainStatus(StorageDomainStatus.Inactive);
        storagePoolExists();
        cinderStorageDomainContainsDisks(false);
        masterDomainIsUp();
        canDetachDomain();
        assertFalse(cmd.canDetachDomain(false, false));
    }

    @Test
    public void checkStorageDomainNotEqualWithStatusActive() {
        setStorageDomainStatus(StorageDomainStatus.Active);
        assertFalse(cmd.checkStorageDomainStatusNotEqual(StorageDomainStatus.Active));
        List<String> messages = cmd.getReturnValue().getValidationMessages();
        assertEquals(2, messages.size());
        assertEquals(messages.get(0), EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", StorageDomainStatus.Active));
    }

    @Test
    public void checkStorageDomainNotEqualWithNonActiveStatus() {
        setStorageDomainStatus(StorageDomainStatus.Maintenance);
        assertTrue(cmd.checkStorageDomainStatusNotEqual(StorageDomainStatus.Active));
    }

    @Test
    public void lunAlreadyPartOfStorageDomains() {
        LUNs lun1 = new LUNs();
        lun1.setLUNId(GUIDS[0].toString());
        lun1.setStorageDomainId(Guid.newGuid());
        LUNs lun2 = new LUNs();
        lun2.setLUNId(GUIDS[1].toString());
        lun2.setStorageDomainId(Guid.newGuid());

        when(lunDao.getAll()).thenReturn(Arrays.asList(lun1, lun2));
        List<String> specifiedLunIds = Collections.singletonList(GUIDS[0].toString());

        assertTrue(cmd.isLunsAlreadyInUse(specifiedLunIds));
        List<String> messages = cmd.getReturnValue().getValidationMessages();
        assertEquals(2, messages.size());
        assertEquals(messages.get(0), EngineMessage.ACTION_TYPE_FAILED_LUNS_ALREADY_PART_OF_STORAGE_DOMAINS.toString());
        assertEquals(messages.get(1), String.format("$lunIds %1$s", cmd.getFormattedLunId(lun1, lun1.getStorageDomainName())));
    }

    @Test
    public void shouldElectActiveDataDomain() {
        final StorageDomain domain = prepareStorageDomainForElection(StorageDomainStatus.Active,
                "not he domain name", false);
        assertEquals(domain, cmd.electNewMaster());
    }

    @Test
    public void shouldNotElectActiveHostedEngineDomain() {
        prepareStorageDomainForElection(StorageDomainStatus.Active, HE_STORAGE_DOMAIN_NAME, true);
        assertNull(cmd.electNewMaster());
    }

    @Test
    public void shouldNotElectUnknownHostedEngineDomain() {
        prepareStorageDomainForElection(StorageDomainStatus.Unknown, HE_STORAGE_DOMAIN_NAME, true);
        assertNull(cmd.electNewMaster());
    }

    @Test
    public void shouldNotElectInactiveHostedEngineDomain() {
        prepareStorageDomainForElection(StorageDomainStatus.Inactive, HE_STORAGE_DOMAIN_NAME, true);
        assertNull(cmd.electNewMaster(false, true, false));
    }

    @Test
    public void shouldElectActiveSharedDataDomain() {
        final StorageDomain domain =
                prepareSharedStorageDomainForElection(StorageDomainStatus.Active, "shared domain name");
        assertEquals(domain, cmd.electNewMaster());
    }

    @Test
    public void shouldElectActiveLocalDataDomain() {
        StorageDomain domain = prepareLocalStorageDomainForElection(StorageDomainStatus.Active, "local domain name");
        assertEquals(domain, cmd.electNewMaster());
    }

    @Test
    public void shouldElectActiveSharedBeforeLocalDataDomain() {
        StorageDomain localDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "local domain name", LOCAL_SD_ID);
        localDomain.setStorageType(StorageType.LOCALFS);
        StorageDomain sharedDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "shared domain name", SHARED_SD_ID);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Arrays.asList(localDomain, sharedDomain));
        cmd.setStoragePool(new StoragePool());
        assertEquals(sharedDomain, cmd.electNewMaster());
    }

    @Test
    public void shouldElectActiveSharedBeforeLocalDataDomain2() {
        StorageDomain localDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "local domain name", LOCAL_SD_ID);
        localDomain.setStorageType(StorageType.LOCALFS);
        StorageDomain sharedDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "shared domain name", SHARED_SD_ID);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Arrays.asList(sharedDomain, localDomain));
        cmd.setStoragePool(new StoragePool());
        assertEquals(sharedDomain, cmd.electNewMaster());
    }

    @Test
    public void shouldElectStorageTypeAfterLastUsedAsMasterDataDomain() {
        StorageDomain localDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "local domain name", LOCAL_SD_ID);
        localDomain.setLastTimeUsedAsMaster(System.currentTimeMillis() - 1000);
        localDomain.setStorageType(StorageType.LOCALFS);
        StorageDomain sharedDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "shared domain name", SHARED_SD_ID);
        sharedDomain.setLastTimeUsedAsMaster(System.currentTimeMillis());
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Arrays.asList(sharedDomain, localDomain));
        cmd.setStoragePool(new StoragePool());
        assertEquals(localDomain, cmd.electNewMaster());
    }

    @Test
    public void shouldElectStorageTypeAfterLastUsedAsMasterDataDomain2() {
        StorageDomain localDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "local domain name", LOCAL_SD_ID);
        localDomain.setLastTimeUsedAsMaster(System.currentTimeMillis());
        localDomain.setStorageType(StorageType.LOCALFS);
        StorageDomain sharedDomain =
                createDataStorageDomain(StorageDomainStatus.Active, "shared domain name", SHARED_SD_ID);
        sharedDomain.setLastTimeUsedAsMaster(System.currentTimeMillis() - 1000);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Arrays.asList(sharedDomain, localDomain));
        cmd.setStoragePool(new StoragePool());
        assertEquals(sharedDomain, cmd.electNewMaster());
    }

    private StorageDomain prepareLocalStorageDomainForElection(StorageDomainStatus status, String name) {
        final StorageDomain localDomain = createDataStorageDomain(status, name, LOCAL_SD_ID);
        localDomain.setStorageType(StorageType.LOCALFS);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(localDomain));
        cmd.setStoragePool(new StoragePool());
        return localDomain;
    }

    private StorageDomain prepareSharedStorageDomainForElection(StorageDomainStatus status, String name) {
        final StorageDomain sharedDomain = createDataStorageDomain(status, name, SHARED_SD_ID);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(sharedDomain));
        cmd.setStoragePool(new StoragePool());
        return sharedDomain;
    }

    private StorageDomain prepareStorageDomainForElection(StorageDomainStatus status,
            String name,
            boolean isHostedEngine) {
        final StorageDomain domain = createDataStorageDomain(status, name, HE_SD_ID);
        domain.setHostedEngineStorage(isHostedEngine);
        when(storageDomainDao.getAllForStoragePool(any(Guid.class))).thenReturn(Collections.singletonList(domain));
        cmd.setStoragePool(new StoragePool());
        return domain;
    }

    private StorageDomain createDataStorageDomain(StorageDomainStatus status, String name, Guid sdId) {
        final StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStorageName(name);
        storageDomain.setStatus(status);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        storageDomain.setId(sdId);
        storageDomain.setHostedEngineStorage(false);
        return storageDomain;
    }

    private void storagePoolExists() {
        when(cmd.checkStoragePool()).thenReturn(true);
    }

    private void masterDomainIsUp() {
        doReturn(true).when(cmd).checkMasterDomainIsUp();
    }

    private void cinderStorageDomainContainsDisks(boolean isCinderContainsDisks) {
        doReturn(isCinderContainsDisks).when(cmd).isCinderStorageHasNoDisks();
    }

    private void canDetachDomain() {
        doReturn(true).when(cmd).isDetachAllowed(anyBoolean());
    }

    private boolean commandHasInvalidStatusMessage() {
        return cmd.getReturnValue().getValidationMessages().contains(
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
    }

    private void setStorageDomainStatus(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
        when(cmd.getStorageDomain()).thenReturn(domain);
    }

    private void setCinderStorageDomainStatus(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
        domain.setStorageType(StorageType.CINDER);
        when(cmd.getStorageDomain()).thenReturn(domain);
    }

    private static class TestStorageCommandBase extends StorageDomainCommandBase<StorageDomainParametersBase> {

        public TestStorageCommandBase(StorageDomainParametersBase parameters) {
            super(parameters, null);
        }

        @Override
        protected void executeCommand() {

        }
    }
}
