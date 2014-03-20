package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.lock.EngineLock;

/** A test class for the {@link OvfDataUpdater} class */
@RunWith(MockitoJUnitRunner.class)
public class OvfDataUpdaterTest {
    private final int ITEMS_COUNT_PER_UPDATE = 100;
    OvfDataUpdater updater;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Mock
    private VmAndTemplatesGenerationsDAO vmAndTemplatesGenerationsDAO;

    @Mock
    private VmDAO vmDAO;

    @Mock
    private VmStaticDAO vmStaticDAO;

    @Mock
    private SnapshotDao snapshotDAO;

    @Mock
    private VmTemplateDAO vmTemplateDAO;

    @Mock
    private StorageDomainDAO storageDomainDAO;

    @Mock
    private StorageDomainOvfInfoDao storageDomainOvfInfoDao;

    private StoragePool pool1;
    private StoragePool pool2;
    private Map<Guid, VM> vms;
    private Map<Guid, VmTemplate> templates;
    // those members are used to save the executed data during a run of the ovf data updater and are used for comprasion with the expected data needed to be executed
    // in the end of each test.
    private Guid executedUpdateStoragePoolId;
    private Map<Guid, Map<Guid, KeyValuePairCompat<String, List<Guid>>>> executedUpdatedMetadataForStoragePool;
    private Map<Guid, Map<Guid, Long>> executedUpdatedOvfGenerationIdsInDb;
    private Guid executedUpdateStorageDomainId;
    private Map<Guid, List<Guid>> executedRemovedIds;
    private Map<Guid, List<Guid>> executedOvfUpdatedDomainForPool;
    private Map<Guid, Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>>> poolDomainsOvfInfo;
    private Guid executedRemoveStoragePoolId;
    private Guid executedRemoveStorageDomainId;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainOvfStoreCount, 1)
    );


    @Before
    public void setUp() {
        updater = Mockito.spy(OvfDataUpdater.getInstance());
        doReturn(ITEMS_COUNT_PER_UPDATE).when(updater).reloadConfigValue();
        doReturn(new ArrayList<DiskImage>()).when(updater).getAllImageSnapshots(any(DiskImage.class));
        doReturn(false).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        doCallRealMethod().when(updater).ovfUpdate_timer();
        // init members
        initMembers();

        //init daos
        doReturn(storagePoolDAO).when(updater).getStoragePoolDao();
        doReturn(vmAndTemplatesGenerationsDAO).when(updater).getVmAndTemplatesGenerationsDao();
        doReturn(vmDAO).when(updater).getVmDao();
        doReturn(vmStaticDAO).when(updater).getVmStaticDao();
        doReturn(snapshotDAO).when(updater).getSnapshotDao();
        doReturn(vmTemplateDAO).when(updater).getVmTemplateDao();
        doReturn(storageDomainOvfInfoDao).when(updater).getStorageDomainOvfInfoDao();
        doReturn(storageDomainDAO).when(updater).getStorageDomainDao();


        // mock ovf data updater methods
        doNothing().when(updater).loadTemplateData(any(VmTemplate.class));
        doNothing().when(updater).loadVmData(any(VM.class));
        doNothing().when(updater).updateVmDisksFromDb(any(VM.class));
        doNothing().when(updater).addAuditLogError(anyString());
        doNothing().when(updater).updateTemplateDisksFromDb(any(VmTemplate.class));
        doReturn(true).when(updater).acquireLock(any(EngineLock.class));
        doNothing().when(updater).releaseLock(any(EngineLock.class));

        // dao related mocks.
        doReturn(1L).when(vmStaticDAO).getDbGeneration(any(Guid.class));
        List<Snapshot> snapshots = new ArrayList<Snapshot>();
        doReturn(snapshots).when(snapshotDAO).getAllWithConfiguration(any(Guid.class));
        mockAnswers();
    }

    private void initMembers() {
        executedUpdatedMetadataForStoragePool = new HashMap<Guid, Map<Guid, KeyValuePairCompat<String, List<Guid>>>>();
        executedRemovedIds = new HashMap<Guid, List<Guid>>();
        executedUpdatedOvfGenerationIdsInDb = new HashMap<Guid, Map<Guid, Long>>();
        vms = new HashMap<Guid, VM>();
        templates = new HashMap<Guid, VmTemplate>();
        pool1 = new StoragePool();
        pool1.setId(Guid.newGuid());
        pool1.setStatus(StoragePoolStatus.Maintenance);

        pool2 = new StoragePool();
        pool2.setId(Guid.newGuid());
        pool2.setStatus(StoragePoolStatus.Maintenance);
        poolDomainsOvfInfo = new HashMap<>();
        performStoragePoolInitOps(pool1);
        performStoragePoolInitOps(pool2);
    }

    private void performStoragePoolInitOps(StoragePool pool) {
        executedUpdatedMetadataForStoragePool.put(pool.getId(),
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>());
        executedRemovedIds.put(pool.getId(), new LinkedList<Guid>());
        executedOvfUpdatedDomainForPool = new HashMap<>();
        executedUpdatedOvfGenerationIdsInDb.put(pool.getId(), new HashMap<Guid, Long>());

        Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> domainOvfInfos = new HashMap<>();

        for (int i=0; i<2; i++) {
            Guid domainId = Guid.newGuid();
            StorageDomainOvfInfo ovfInfo = new StorageDomainOvfInfo(domainId, null, null, StorageDomainOvfInfoStatus.UPDATED, null);
            StorageDomain domain = new StorageDomain();
            domain.setId(domainId);
            domain.setStoragePoolIsoMapData(new StoragePoolIsoMap(domainId, pool.getId(), StorageDomainStatus.Active));
            domainOvfInfos.put(domainId, new Pair<>(Arrays.asList(ovfInfo), domain));
        }
        poolDomainsOvfInfo.put(pool.getId(), domainOvfInfos);
    }

    private void mockAnswers() {
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                VM vm = (VM) invocation.getArguments()[0];
                return vm.getId().toString();
            }

        }).when(updater).generateVmMetadata(any(VM.class), any(ArrayList.class));

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                VmTemplate template = (VmTemplate) invocation.getArguments()[0];
                return template.getId().toString();
            }

        }).when(updater).generateVmTemplateMetadata(any(VmTemplate.class), anyList());

        doAnswer(new Answer<List<VM>>() {
            @Override
            public List<VM> answer(InvocationOnMock invocation) throws Throwable {
                List<Guid> neededIds = (List<Guid>) invocation.getArguments()[0];
                List<VM> toReturn = new LinkedList<VM>();
                for (Guid id : neededIds) {
                    toReturn.add(vms.get(id));
                }
                return toReturn;
            }

        }).when(vmDAO).getVmsByIds(anyList());

        doAnswer(new Answer<List<VmTemplate>>() {
            @Override
            public List<VmTemplate> answer(InvocationOnMock invocation) throws Throwable {
                List<Guid> neededIds = (List<Guid>) invocation.getArguments()[0];
                List<VmTemplate> toReturn = new LinkedList<VmTemplate>();
                for (Guid id : neededIds) {
                    toReturn.add(templates.get(id));
                }
                return toReturn;
            }

        }).when(vmTemplateDAO).getVmTemplatesByIds(anyList());

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                executedUpdateStoragePoolId = (Guid) invocation.getArguments()[0];
                Map<Guid, KeyValuePairCompat<String, List<Guid>>> updateMap =
                        (Map<Guid, KeyValuePairCompat<String, List<Guid>>>) invocation.getArguments()[1];
                executedUpdatedMetadataForStoragePool.get(executedUpdateStoragePoolId).putAll(updateMap);
                executedUpdateStorageDomainId = (Guid) invocation.getArguments()[2];
                assertTrue("too many ovfs were sent in one vdsm call", updateMap.size() <= ITEMS_COUNT_PER_UPDATE);
                return true;
            }

        }).when(updater).executeUpdateVmInSpmCommand(any(Guid.class), anyMap(), any(Guid.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                executedUpdateStoragePoolId = ((StoragePool) invocation.getArguments()[0]).getId();
                return (Boolean) invocation.callRealMethod();
            }

        }).when(updater).proceedPoolOvfUpdate(any(StoragePool.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Guid poolId = (Guid) invocation.getArguments()[0];
                Guid domainId = (Guid) invocation.getArguments()[1];
                List<Guid> executedDomains = executedOvfUpdatedDomainForPool.get(poolId);
                if (executedDomains == null) {
                    executedDomains = new LinkedList<>();
                    executedOvfUpdatedDomainForPool.put(poolId, executedDomains);
                }
                executedDomains.add(domainId);
                return true;
            }

        }).when(updater).performOvfUpdateForDomain(any(Guid.class), any(Guid.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                executedRemoveStoragePoolId = (Guid) invocation.getArguments()[0];
                executedRemovedIds.get(executedRemoveStoragePoolId).add((Guid) invocation.getArguments()[1]);
                executedRemoveStorageDomainId = (Guid) invocation.getArguments()[2];
                return true;
            }

        }).when(updater).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<Guid> ids = (List<Guid>) invocation.getArguments()[0];
                List<Long> values = (List<Long>) invocation.getArguments()[1];
                assertFalse("update of ovf version in db shouldn't be called with an empty value list",
                        values.isEmpty());
                assertTrue("update of ovf version in db shouldn't be called with more items then MAX_ITEMS_PER_SQL_STATEMENT",
                        values.size() <= StorageConstants.OVF_MAX_ITEMS_PER_SQL_STATEMENT);
                assertEquals("the size of the list of ids for update is not the same as the size of the " +
                        "list with the new ovf values", values.size(), ids.size());
                Guid[] ids_array = ids.toArray(new Guid[ids.size()]);
                Long[] values_array = values.toArray(new Long[values.size()]);
                for (int i = 0; i < ids_array.length; i++) {
                    executedUpdatedOvfGenerationIdsInDb.get(executedUpdateStoragePoolId).put(ids_array[i],
                            values_array[i]);
                }
                return null;
            }

        }).when(vmAndTemplatesGenerationsDAO).updateOvfGenerations(anyList(), anyList(), anyList());

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                StoragePoolStatus desiredStatus = (StoragePoolStatus) invocation.getArguments()[0];
                List<StoragePool> toReturn = new LinkedList<>();
                for (StoragePool pool : buildStoragePoolsList()) {
                    if (desiredStatus.equals(pool.getStatus())) {
                        toReturn.add(pool);
                    }
                }

                return toReturn;
            }

        }).when(storagePoolDAO).getAllByStatus(any(StoragePoolStatus.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Guid poolId = (Guid) invocation.getArguments()[0];
                List<StorageDomain> toReturn = new LinkedList<>();
                Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> domainsMap = poolDomainsOvfInfo.get(poolId);
                for (Pair<List<StorageDomainOvfInfo>, StorageDomain> pair : domainsMap.values()) {
                    toReturn.add(pair.getSecond());
                }

                return toReturn;
            }

        }).when(storageDomainDAO).getAllForStoragePool(any(Guid.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Guid domainId = (Guid) invocation.getArguments()[0];
                for (Map.Entry<Guid, Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>>> entry : poolDomainsOvfInfo.entrySet()) {
                    Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> domainsInfo = entry.getValue();
                    Pair<List<StorageDomainOvfInfo>, StorageDomain> pair = domainsInfo.get(domainId);
                    if (pair != null) {
                        return pair.getFirst();
                    }

                }
                return null;
            }

        }).when(storageDomainOvfInfoDao).getAllForDomain(any(Guid.class));
    }

    private List<StoragePool> buildStoragePoolsList() {
        return Arrays.asList(pool1, pool2);
    }

    private VM createVm(Guid id, VMStatus status) {
        VM vm = new VM();
        vm.setStatus(status);
        vm.setStaticData(createVmStatic());
        vm.setId(id);
        return vm;
    }

    public VmStatic createVmStatic() {
        VmStatic vms = new VmStatic();
        vms.setDbGeneration(1L);
        return vms;
    }

    private VmTemplate createVmTemplate(Guid id, VmTemplateStatus templateStatus) {
        VmTemplate template = new VmTemplate();
        template.setStatus(templateStatus);
        template.setDbGeneration(1L);
        template.setId(id);
        return template;
    }

    private List<Guid> generateGuidList(int size) {
        List<Guid> toReturn = new LinkedList<Guid>();
        for (int i = 0; i < size; i++) {
            toReturn.add(Guid.newGuid());
        }
        return toReturn;
    }

    private Map<Guid, VM> generateVmsMapByGuids(List<Guid> ids,
            int diskCount,
            VMStatus vmStatus,
            ImageStatus diskStatus,
            Guid poolId) {
        Map<Guid, VM> toReturn = new HashMap<Guid, VM>();
        for (Guid id : ids) {
            VM vm = createVm(id, vmStatus);
            for (int i = 0; i < diskCount; i++) {
                DiskImage image = createDiskImage(diskStatus, poolId);
                vm.getDiskMap().put(image.getId(), image);
                vm.getDiskList().add(image);
            }
            toReturn.put(vm.getId(), vm);
        }
        return toReturn;
    }

    private Map<Guid, VmTemplate> generateVmTemplatesMapByGuids(List<Guid> ids,
            int diskCount,
            VmTemplateStatus templateStatus,
            ImageStatus diskStatus, Guid poolId) {
        Map<Guid, VmTemplate> toReturn = new HashMap<Guid, VmTemplate>();
        for (Guid id : ids) {
            VmTemplate template = createVmTemplate(id, templateStatus);
            for (int i = 0; i < diskCount; i++) {
                DiskImage image = createDiskImage(diskStatus, poolId);
                template.getDiskTemplateMap().put(image.getId(), image);
                template.getDiskList().add(image);
            }
            toReturn.put(template.getId(), template);
        }
        return toReturn;
    }

    private DiskImage createDiskImage(ImageStatus status, Guid poolId) {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setImageStatus(status);
        ArrayList<Guid> storageIds = new ArrayList<>();
        Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> domains = poolDomainsOvfInfo.get(poolId);
        storageIds.add(domains.keySet().iterator().next());
        disk.setStorageIds(storageIds);
        return disk;
    }

    private void initTestForPool(StoragePool pool, List<Guid> vmGuids, List<Guid> templatesGuids, List<Guid> removedGuids) {
        Guid poolId = pool.getId();
        doReturn(vmGuids).when(vmAndTemplatesGenerationsDAO).getVmsIdsForOvfUpdate(poolId);
        doReturn(templatesGuids).when(vmAndTemplatesGenerationsDAO).getVmTemplatesIdsForOvfUpdate(poolId);
        doReturn(removedGuids).when(vmAndTemplatesGenerationsDAO).getIdsForOvfDeletion(poolId);
        pool.setStatus(StoragePoolStatus.Up);
    }

    private void verifyCorrectOvfDataUpdaterRun(StoragePool storagePool,
            Collection<Guid> needToBeUpdated,
            Collection<Guid> removedGuids) {
        Guid storagePoolId = storagePool.getId();
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> storagePoolMetadataUpdatedMap =
                executedUpdatedMetadataForStoragePool.get(storagePoolId);
        Map<Guid, Long> storagePoolUpdateOvfGenerationsInDb = executedUpdatedOvfGenerationIdsInDb.get(storagePoolId);
        if (!updater.ovfOnAnyDomainSupported(storagePool)) {
            assertTrue("not all needed vms/templates were updated in vdsm",
                    CollectionUtils.isEqualCollection(storagePoolMetadataUpdatedMap.keySet(),
                            needToBeUpdated));
            for (Map.Entry<Guid, KeyValuePairCompat<String, List<Guid>>> entry : storagePoolMetadataUpdatedMap
                    .entrySet()) {
                assertEquals("wrong ovf data stored in storage for vm/template",
                        entry.getKey().toString(),
                        entry.getValue().getKey());
            }
        }
        assertTrue("not all needed vms/templates were updated in db",
                CollectionUtils.isEqualCollection(storagePoolUpdateOvfGenerationsInDb.keySet(),
                        needToBeUpdated));

        for (Map.Entry<Guid, Long> storagePoolGenerationEntry : storagePoolUpdateOvfGenerationsInDb.entrySet()) {
            boolean isCorrectVersion = false;
            if (vms.get(storagePoolGenerationEntry.getKey()) != null) {
                isCorrectVersion =
                        storagePoolGenerationEntry.getValue()
                                .equals(vms.get(storagePoolGenerationEntry.getKey()).getDbGeneration());
            } else if (templates.get(storagePoolGenerationEntry.getKey()) != null) {
                isCorrectVersion =
                        storagePoolGenerationEntry.getValue()
                                .equals(templates.get(storagePoolGenerationEntry.getKey()).getDbGeneration());
            }
            assertTrue("wrong new ovf version persisted for vm/template", isCorrectVersion);
        }

        if (!updater.ovfOnAnyDomainSupported(storagePool)) {
            assertTrue("not all needed vms/templates were removed from vdsm",
                    CollectionUtils.isEqualCollection(removedGuids, executedRemovedIds.get(storagePoolId)));
        }
    }

    private void addVms(List<Guid> vmGuids, int diskCount, VMStatus vmStatus, ImageStatus vmImageStatus, Guid poolId) {
        vms.putAll(generateVmsMapByGuids(vmGuids, diskCount, vmStatus, vmImageStatus, poolId));

    }

    private void verifyOvfUpdatedForSupportedPools(List<Guid> poolsRequiredUpdate,
            Map<Guid, List<Guid>> domainsRequiredUpdateForPool) {
        assertTrue("wrong number of pools was updated",
                executedOvfUpdatedDomainForPool.keySet().size() == poolsRequiredUpdate.size());
        for (Guid storagePoolId : poolsRequiredUpdate) {
            List<Guid> updatedDomainsForPool = executedOvfUpdatedDomainForPool.get(storagePoolId);
            Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> domainOfPool = poolDomainsOvfInfo.get(storagePoolId);
            for (Guid updatedDomainForPool : updatedDomainsForPool) {
                assertTrue("ovf update for domain has been executed with wrong pool",
                        domainOfPool.containsKey(updatedDomainForPool));
                if (domainsRequiredUpdateForPool.get(storagePoolId) != null) {
                    assertTrue("ovf updated hasn't been executed on needed domain",
                            domainsRequiredUpdateForPool.get(storagePoolId).contains(updatedDomainForPool));
                }
            }
        }
    }

    private void addTemplates(List<Guid> templatesGuids,
            int diskCount,
            VmTemplateStatus templateStatus,
            ImageStatus templateImageStatus, Guid poolId) {
        templates.putAll(generateVmTemplatesMapByGuids(templatesGuids,
                diskCount,
                templateStatus,
                templateImageStatus,
                poolId));
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPoolsOvfOnAnyDomainSupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPools(true);
        verifyOvfUpdatedForSupportedPools(Arrays.asList(pool1.getId(), pool2.getId()),
                Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPoolsOvfOnAnyDomainUnsupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPools(false);
    }

    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPools(boolean ovfOnAnyDomainSupported) {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;

        doReturn(ovfOnAnyDomainSupported).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        List<Guid> vmGuids2 = generateGuidList(size);
        List<Guid> templatesGuids2 = generateGuidList(size);
        List<Guid> removedGuids2 = generateGuidList(size);
        addVms(vmGuids2, 2, VMStatus.Down, ImageStatus.OK, pool2.getId());
        addTemplates(templatesGuids2, 2, VmTemplateStatus.OK, ImageStatus.OK, pool2.getId());
        initTestForPool(pool2, vmGuids2, templatesGuids2, removedGuids2);

        updater.ovfUpdate_timer();

        verify(updater, times(numberOfTimesToBeCalled(size, true) * 2)).performOvfUpdate(any(StoragePool.class),
                anyMap());

        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size * 2)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        List<Guid> idsThatNeededToBeUpdated2 = new LinkedList<Guid>(vmGuids2);
        idsThatNeededToBeUpdated2.addAll(templatesGuids2);

        verifyCorrectOvfDataUpdaterRun(pool1, idsThatNeededToBeUpdated, removedGuids);
        verifyCorrectOvfDataUpdaterRun(pool2, idsThatNeededToBeUpdated2, removedGuids2);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveExceptionInOnePool() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;

        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        List<Guid> vmGuids2 = generateGuidList(size);
        List<Guid> templatesGuids2 = generateGuidList(size);
        List<Guid> removedGuids2 = generateGuidList(size);
        addVms(vmGuids2, 2, VMStatus.Down, ImageStatus.OK, pool2.getId());
        addTemplates(templatesGuids2, 2, VmTemplateStatus.OK, ImageStatus.OK, pool2.getId());
        initTestForPool(pool2, vmGuids2, templatesGuids2, removedGuids2);

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                executedUpdateStoragePoolId = (Guid) invocation.getArguments()[0];
                if (executedUpdateStoragePoolId.equals(pool1.getId())) {
                    throw new MockitoException("testOvfDataUpdaterRunWithUpdateAndRemoveExceptionInOnePool: intendent exception");
                }
                Map<Guid, KeyValuePairCompat<String, List<Guid>>> updateMap =
                        (Map<Guid, KeyValuePairCompat<String, List<Guid>>>) invocation.getArguments()[1];
                executedUpdatedMetadataForStoragePool.get(executedUpdateStoragePoolId).putAll(updateMap);
                executedUpdateStorageDomainId = (Guid) invocation.getArguments()[2];
                assertTrue("too many ovfs were sent in one vdsm call", updateMap.size() <= ITEMS_COUNT_PER_UPDATE);
                return true;
            }

        }).when(updater).executeUpdateVmInSpmCommand(any(Guid.class), anyMap(), any(Guid.class));

        updater.ovfUpdate_timer();

        // + 1 as the exception is thrown during the first call to this method during first update execution for the
        // first pool.
        int numOfExpectedRuns = numberOfTimesToBeCalled(size, true) + 1;
        verify(updater, times(numOfExpectedRuns)).performOvfUpdate(any(StoragePool.class),
                anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        List<Guid> idsToBeUpdated = new LinkedList<Guid>(vmGuids2);
        idsToBeUpdated.addAll(templatesGuids2);
        verifyCorrectOvfDataUpdaterRun(pool1, Collections.<Guid> emptyList(), Collections.<Guid> emptyList());
        verifyCorrectOvfDataUpdaterRun(pool2, idsToBeUpdated, removedGuids2);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePoolOvfOnAnyDomainSupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePool(true);
        verifyOvfUpdatedForSupportedPools(Arrays.asList(pool1.getId()), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePoolOvfOnAnyDomainUnsupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePool(false);
    }

    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePool(boolean ovfOnAnyDomainSupported) {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;
        doReturn(ovfOnAnyDomainSupported).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();

        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(pool1, idsThatNeededToBeUpdated, removedGuids);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCountOvfOnAnyDomainSupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCount(true);
        verifyOvfUpdatedForSupportedPools(Arrays.asList(pool1.getId()), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCountOvfOnAnyDomainUnupported() {
        testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCount(false);
    }

    public void testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCount(boolean ovfOnAnyDomainSupported) {
        int size = ITEMS_COUNT_PER_UPDATE - 1;

        doReturn(ovfOnAnyDomainSupported).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        List<Guid> vmGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());
        List<Guid> removedGuids = generateGuidList(size);

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }
        List<Guid> needToBeUpdated = new LinkedList<Guid>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(pool1, needToBeUpdated, removedGuids);
    }

    @Test
    public void testOvfDataUpdaterAllDisksAreLockedNonToRemoveOvfOnAnyDomainSupported() {
        testOvfDataUpdaterAllDisksAreLockedNonToRemove(true);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterAllDisksAreLockedNonToRemoveOvfOnAnyDomainUnupported() {
        testOvfDataUpdaterAllDisksAreLockedNonToRemove(false);
    }

    public void testOvfDataUpdaterAllDisksAreLockedNonToRemove(boolean ovfOnAnyDomainSupported) {
        int size = ITEMS_COUNT_PER_UPDATE - 1;

        doReturn(ovfOnAnyDomainSupported).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        List<Guid> templatesGuids = generateGuidList(size);

        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.LOCKED, pool1.getId());
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.LOCKED, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, never()).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, never()).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }
        verifyCorrectOvfDataUpdaterRun(pool1, Collections.<Guid> emptyList(), removedGuids);
    }

    @Test
    public void testOvfDataUpdaterPartOfDisksAreLockedOvfOnAnyDomainSupported() {
        testOvfDataUpdaterAllDisksAreLockedNonToRemove(true);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterPartOfDisksAreLockedOvfOnAnyDomainUnupported() {
        testOvfDataUpdaterAllDisksAreLockedNonToRemove(false);
    }

    public void testOvfDataUpdaterPartOfDisksAreLocked(boolean ovfOnAnyDomainSupported) {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        // unlocked vms/templates
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        // locked vms/templates
        List<Guid> lockedVmGuids = generateGuidList(size);
        List<Guid> lockedTemplatesGuids = generateGuidList(size);
        addVms(lockedVmGuids, 2, VMStatus.Down, ImageStatus.LOCKED, pool1.getId());
        addTemplates(lockedTemplatesGuids, 2, VmTemplateStatus.OK, ImageStatus.LOCKED, pool1.getId());
        // ids for removal
        List<Guid> removedGuids = generateGuidList(size);

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }
        // list of ids that should have been updated.
        List<Guid> needToBeUpdated = new LinkedList<Guid>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(pool1, needToBeUpdated, removedGuids);
    }

    private int numberOfTimesToBeCalled(int size, boolean isBothVmAndTemplates) {
        int toReturn = 0;
        if (size % ITEMS_COUNT_PER_UPDATE != 0) {
            toReturn++;
        }
        toReturn += size / ITEMS_COUNT_PER_UPDATE;
        if (isBothVmAndTemplates) {
            toReturn = toReturn * 2;
        }
        return toReturn;
    }

    @Test
    public void testOvfDataUpdaterAllVmsAndTemplatesAreLockedOvfOnAnyDomainSupported() {
        testOvfDataUpdaterAllVmsAndTemplatesAreLocked(true);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterAllVmsAndTemplatesAreLockedOvfOnAnyDomainUnupported() {
        testOvfDataUpdaterAllVmsAndTemplatesAreLocked(false);
    }

    public void testOvfDataUpdaterAllVmsAndTemplatesAreLocked(boolean ovfOnAnyDomainSupported) {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        List<Guid> vmGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.ImageLocked, ImageStatus.OK, pool1.getId());
        List<Guid> removedGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.Locked, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, never()).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }
        verifyCorrectOvfDataUpdaterRun(pool1, Collections.<Guid> emptyList(), removedGuids);
    }

    @Test
    public void testOvfDataUpdaterPartOfVmsAndTemplatesAreLockedOvfOnAnyDomainSupported() {
        testOvfDataUpdaterPartOfVmsAndTemplatesAreLocked(true);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterPartOfVmsAndTemplatesAreLockedOvfOnAnyDomainUnupported() {
        testOvfDataUpdaterPartOfVmsAndTemplatesAreLocked(false);
    }

    public void testOvfDataUpdaterPartOfVmsAndTemplatesAreLocked(boolean ovfOnAnyDomainSupported) {
        int size = ITEMS_COUNT_PER_UPDATE;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);

        addVms(vmGuids, 2, VMStatus.ImageLocked, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.Locked, ImageStatus.OK, pool1.getId());

        List<Guid> vmGuidsUnlocked = generateGuidList(size);
        List<Guid> templatesGuidsUnlocked = generateGuidList(size);

        addVms(vmGuidsUnlocked, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuidsUnlocked, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        vmGuids.addAll(vmGuidsUnlocked);
        templatesGuids.addAll(templatesGuidsUnlocked);
        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();

        List<Guid> neededToBeUpdated = new LinkedList<Guid>(vmGuidsUnlocked);
        neededToBeUpdated.addAll(templatesGuidsUnlocked);
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }
        verifyCorrectOvfDataUpdaterRun(pool1, neededToBeUpdated, removedGuids);
    }

    @Test
    public void testUpdatedDbGenerationOvfOnAnyDomainSupported() {
        testUpdatedDbGeneration(true);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testUpdatedDbGenerationOvfOnAnyDomainUnupported() {
        testUpdatedDbGeneration(false);
    }

    public void testUpdatedDbGeneration(boolean ovfOnAnyDomainSupported) {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        doReturn(2L).when(vmStaticDAO).getDbGeneration(any(Guid.class));

        updater.ovfUpdate_timer();

        verify(updater, never()).performOvfUpdate(any(StoragePool.class), anyMap());
        if (!ovfOnAnyDomainSupported) {
            verify(updater, never()).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        }

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(pool1, Collections.<Guid> emptyList(), removedGuids);
    }

    @Test
    public void testUpdateCalledForUnupdatedDomain() {
        Guid poolId = pool1.getId();
        StorageDomainOvfInfo ovfInfo = poolDomainsOvfInfo.get(poolId).entrySet().iterator().next().getValue().getFirst().get(0);
        ovfInfo.setStatus(StorageDomainOvfInfoStatus.OUTDATED);
        doReturn(true).when(updater).ovfOnAnyDomainSupported(any(StoragePool.class));
        initTestForPool(pool1,
                Collections.<Guid> emptyList(),
                Collections.<Guid> emptyList(),
                Collections.<Guid> emptyList());
        updater.ovfUpdate_timer();
        verify(updater, never()).performOvfUpdate(any(StoragePool.class), anyMap());
        Map<Guid, List<Guid>> domainsRequiredUpdateForPool =
                Collections.singletonMap(poolId, Arrays.asList(ovfInfo.getStorageDomainId()));
        verifyOvfUpdatedForSupportedPools(Arrays.asList(poolId), domainsRequiredUpdateForPool);
    }
}
