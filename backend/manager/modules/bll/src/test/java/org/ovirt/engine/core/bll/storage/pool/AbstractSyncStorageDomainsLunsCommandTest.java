package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class AbstractSyncStorageDomainsLunsCommandTest {

    private StorageDomain blockSd1;
    private StorageDomain blockSd2;
    private Guid blockSd1Id;
    private Guid blockSd2Id;
    private LUNs blockSd1Lun1;
    private LUNs blockSd2Lun1;
    private LUNs blockSd2Lun2;

    private SyncLunsParameters parameters = new SyncLunsParameters();

    @SuppressWarnings("unchecked")
    @InjectMocks
    private AbstractSyncStorageDomainsLunsCommand<SyncLunsParameters> command = mock(
            AbstractSyncStorageDomainsLunsCommand.class,
            withSettings().useConstructor(parameters, null).defaultAnswer(CALLS_REAL_METHODS));

    @BeforeEach
    public void setUp() {
        blockSd1Id = Guid.newGuid();
        String sd1Name = "vg1";
        blockSd1 = new StorageDomain();
        blockSd1.setId(blockSd1Id);
        blockSd1.setStorageType(StorageType.ISCSI);
        blockSd1.setStatus(StorageDomainStatus.Active);
        blockSd1.setStorage(sd1Name);
        blockSd1Lun1 = new LUNs();
        blockSd1Lun1.setId("blockSd1Lun1");
        blockSd1Lun1.setVolumeGroupId(sd1Name);
        blockSd1Lun1.setStorageDomainId(blockSd1Id);

        blockSd2Id = Guid.newGuid();
        String sd2Name = "vg2";
        blockSd2 = new StorageDomain();
        blockSd2.setId(blockSd2Id);
        blockSd2.setStorageType(StorageType.FCP);
        blockSd2.setStatus(StorageDomainStatus.Active);
        blockSd2.setStorage(sd2Name);
        blockSd2Lun1 = new LUNs();
        blockSd2Lun1.setId("blockSd2Lun1");
        blockSd2Lun2 = new LUNs();
        blockSd2Lun2.setId("blockSd2Lun2");
        blockSd2Lun1.setVolumeGroupId(sd2Name);
        blockSd2Lun1.setStorageDomainId(blockSd2Id);
        blockSd2Lun2.setVolumeGroupId(sd2Name);
        blockSd2Lun2.setStorageDomainId(blockSd2Id);
    }

    @Test
    public void getLunsGroupedByStorageDomainId() {
        command.getParameters().setDeviceList(Arrays.asList(
                blockSd1Lun1, blockSd2Lun1, blockSd2Lun2, // Luns which are a part of storage domains.
                new LUNs(), new LUNs())); // Luns which are not a part of storage domains.
        doReturn(Stream.of(blockSd1, blockSd2)).when(command).getStorageDomainsToSync();
        Map<Guid, List<LUNs>> lunsGroupedByStorageDomainId = command.getLunsGroupedByStorageDomainId();

        assertEquals(2, lunsGroupedByStorageDomainId.values().size());
        assertEquals(Collections.singletonList(blockSd1Lun1), lunsGroupedByStorageDomainId.get(blockSd1Id));
        assertEquals(Arrays.asList(blockSd2Lun1, blockSd2Lun2), lunsGroupedByStorageDomainId.get(blockSd2Id));
    }

    @Test
    public void syncStorageDomains() {
        Map<Guid, List<LUNs>> lunsGroupedByStorageDomainId = new HashMap<>();
        List<LUNs> sd1Luns = Collections.singletonList(blockSd1Lun1);
        List<LUNs> sd2Luns = Arrays.asList(blockSd2Lun1, blockSd2Lun2);
        lunsGroupedByStorageDomainId.put(blockSd1Id, sd1Luns);
        lunsGroupedByStorageDomainId.put(blockSd2Id, sd2Luns);
        doReturn(lunsGroupedByStorageDomainId).when(command).getLunsGroupedByStorageDomainId();

        doReturn(true).when(command).runSyncLunsInfoForBlockStorageDomain(blockSd1Id, sd1Luns);
        doReturn(true).when(command).runSyncLunsInfoForBlockStorageDomain(blockSd2Id, sd2Luns);
        assertEquals(Collections.emptyList(), command.syncStorageDomains(),
                "All luns should have been synchronized.");

        doReturn(false).when(command).runSyncLunsInfoForBlockStorageDomain(blockSd1Id, sd1Luns);
        assertEquals(
                Collections.singletonList(blockSd1Id), command.syncStorageDomains(),
                "Storage domain with id " + blockSd1Id + " should have failed to get synchronized.");

        doReturn(false).when(command).runSyncLunsInfoForBlockStorageDomain(blockSd2Id, sd2Luns);
        assertEquals(
                Stream.of(blockSd1Id, blockSd2Id).sorted().collect(Collectors.toList()), command.syncStorageDomains(),
                "Both storage domains should have failed to get synchronized.");
    }

}
