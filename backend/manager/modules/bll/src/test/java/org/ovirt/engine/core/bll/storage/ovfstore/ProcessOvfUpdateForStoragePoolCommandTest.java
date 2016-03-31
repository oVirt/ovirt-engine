package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStoragePoolParameters;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class ProcessOvfUpdateForStoragePoolCommandTest extends BaseCommandTest {
    private static final int ITEMS_COUNT_PER_UPDATE = 100;
    private ProcessOvfUpdateForStoragePoolCommand<ProcessOvfUpdateForStoragePoolParameters> command;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VmAndTemplatesGenerationsDao vmAndTemplatesGenerationsDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private VmStaticDao vmStaticDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private VmTemplateDao vmTemplateDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private StorageDomainOvfInfoDao storageDomainOvfInfoDao;

    private OvfUpdateProcessHelper ovfUpdateProcessHelper;

    private StoragePool pool1;
    private Map<Guid, VM> vms;
    private Map<Guid, VmTemplate> templates;
    private Map<Guid, KeyValuePairCompat<String, List<Guid>>> executedUpdatedMetadataForStoragePool;
    private Map<Guid, Long> executedUpdatedOvfGenerationIdsInDb;
    private Set<Guid> executedRemovedIds;
    private Set<Guid> executedOvfUpdatedDomains;
    private Map<Guid, Pair<List<StorageDomainOvfInfo>, StorageDomain>> poolDomainsOvfInfo;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.StorageDomainOvfStoreCount, 1)
    );


    @Before
    public void setUp() {
        command = Mockito.spy(new ProcessOvfUpdateForStoragePoolCommand<>(
                new ProcessOvfUpdateForStoragePoolParameters(), null));
        ovfUpdateProcessHelper = Mockito.spy(new OvfUpdateProcessHelper());
        doReturn(ITEMS_COUNT_PER_UPDATE).when(command).loadConfigValue();
        doReturn(new ArrayList<DiskImage>()).when(ovfUpdateProcessHelper).getAllImageSnapshots(any(DiskImage.class));
        doCallRealMethod().when(command).executeCommand();
        // init members
        initMembers();

        //init daos
        doReturn(storagePoolDao).when(command).getStoragePoolDao();
        doReturn(vmAndTemplatesGenerationsDao).when(command).getVmAndTemplatesGenerationsDao();
        doReturn(vmDao).when(command).getVmDao();
        doReturn(vmStaticDao).when(command).getVmStaticDao();
        doReturn(snapshotDao).when(command).getSnapshotDao();
        doReturn(vmTemplateDao).when(command).getVmTemplateDao();
        doReturn(storageDomainOvfInfoDao).when(command).getStorageDomainOvfInfoDao();
        doReturn(storageDomainDao).when(command).getStorageDomainDao();

        doReturn(ovfUpdateProcessHelper).when(command).getOvfUpdateProcessHelper();


        // mock ovf data updater methods
        doNothing().when(ovfUpdateProcessHelper).loadTemplateData(any(VmTemplate.class));
        doNothing().when(ovfUpdateProcessHelper).loadVmData(any(VM.class));
        doNothing().when(command).updateVmDisksFromDb(any(VM.class));
        doNothing().when(command).updateTemplateDisksFromDb(any(VmTemplate.class));

        // dao related mocks.
        doReturn(1L).when(vmStaticDao).getDbGeneration(any(Guid.class));
        doReturn(pool1).when(command).getStoragePool();
        List<Snapshot> snapshots = new ArrayList<>();
        doReturn(snapshots).when(snapshotDao).getAllWithConfiguration(any(Guid.class));
        mockAnswers();
    }

    private void initMembers() {
        executedUpdatedMetadataForStoragePool = new HashMap<>();
        executedRemovedIds = new HashSet<>();
        executedUpdatedOvfGenerationIdsInDb = new HashMap<>();
        poolDomainsOvfInfo = new HashMap<>();
        vms = new HashMap<>();
        templates = new HashMap<>();
        pool1 = new StoragePool();
        pool1.setId(Guid.newGuid());
        pool1.setStatus(StoragePoolStatus.Maintenance);

        performStoragePoolInitOps(pool1);
    }

    private void performStoragePoolInitOps(StoragePool pool) {
        executedUpdatedMetadataForStoragePool = new HashMap<>();
        executedRemovedIds = new HashSet<>();
        executedUpdatedOvfGenerationIdsInDb = new HashMap<>();

        for (int i = 0; i < 2; i++) {
            Guid domainId = Guid.newGuid();
            StorageDomainOvfInfo ovfInfo = new StorageDomainOvfInfo(domainId, null, null, StorageDomainOvfInfoStatus.UPDATED, null);
            StorageDomain domain = new StorageDomain();
            domain.setId(domainId);
            domain.setStoragePoolIsoMapData(new StoragePoolIsoMap(domainId, pool.getId(), StorageDomainStatus.Active));
            poolDomainsOvfInfo.put(domainId, new Pair<>(Arrays.asList(ovfInfo), domain));
        }
    }

    private void mockAnswers() {
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                VM vm = (VM) invocation.getArguments()[0];
                return vm.getId().toString();
            }

        }).when(ovfUpdateProcessHelper).generateVmMetadata(any(VM.class), any(ArrayList.class));

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                VmTemplate template = (VmTemplate) invocation.getArguments()[0];
                return template.getId().toString();
            }

        }).when(ovfUpdateProcessHelper).generateVmTemplateMetadata(any(VmTemplate.class), anyList());

        doAnswer(new Answer<List<VM>>() {
            @Override
            public List<VM> answer(InvocationOnMock invocation) throws Throwable {
                List<Guid> neededIds = (List<Guid>) invocation.getArguments()[0];
                List<VM> toReturn = new LinkedList<>();
                for (Guid id : neededIds) {
                    toReturn.add(vms.get(id));
                }
                return toReturn;
            }

        }).when(vmDao).getVmsByIds(anyList());

        doAnswer(new Answer<List<VmTemplate>>() {
            @Override
            public List<VmTemplate> answer(InvocationOnMock invocation) throws Throwable {
                List<Guid> neededIds = (List<Guid>) invocation.getArguments()[0];
                List<VmTemplate> toReturn = new LinkedList<>();
                for (Guid id : neededIds) {
                    toReturn.add(templates.get(id));
                }
                return toReturn;
            }

        }).when(vmTemplateDao).getVmTemplatesByIds(anyList());

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Map<Guid, KeyValuePairCompat<String, List<Guid>>> updateMap =
                        (Map<Guid, KeyValuePairCompat<String, List<Guid>>>) invocation.getArguments()[1];
                executedUpdatedMetadataForStoragePool.putAll(updateMap);
                assertTrue("too many ovfs were sent in one vdsm call", updateMap.size() <= ITEMS_COUNT_PER_UPDATE);
                return true;
            }

        }).when(ovfUpdateProcessHelper).executeUpdateVmInSpmCommand(any(Guid.class), anyMap(), any(Guid.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                executedRemovedIds.add((Guid) invocation.getArguments()[1]);
                return true;
            }

        }).when(ovfUpdateProcessHelper).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

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
                    executedUpdatedOvfGenerationIdsInDb.put(ids_array[i],
                            values_array[i]);
                }
                return null;
            }

        }).when(vmAndTemplatesGenerationsDao).updateOvfGenerations(anyList(), anyList(), anyList());

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

        }).when(storagePoolDao).getAllByStatus(any(StoragePoolStatus.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Guid poolId = (Guid) invocation.getArguments()[0];
                List<StorageDomain> toReturn = new LinkedList<>();
                for (Pair<List<StorageDomainOvfInfo>, StorageDomain> pair : poolDomainsOvfInfo.values()) {
                    toReturn.add(pair.getSecond());
                }

                return toReturn;
            }

        }).when(storageDomainDao).getAllForStoragePool(any(Guid.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Guid domainId = (Guid) invocation.getArguments()[0];
                Pair<List<StorageDomainOvfInfo>, StorageDomain> pair = poolDomainsOvfInfo.get(domainId);
                if (pair != null) {
                    return pair.getFirst();
                }
                return null;
            }

        }).when(storageDomainOvfInfoDao).getAllForDomain(any(Guid.class));
    }

    private List<StoragePool> buildStoragePoolsList() {
        return Arrays.asList(pool1);
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
        List<Guid> toReturn = new LinkedList<>();
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
        Map<Guid, VM> toReturn = new HashMap<>();
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
        Map<Guid, VmTemplate> toReturn = new HashMap<>();
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
        storageIds.add(poolDomainsOvfInfo.keySet().iterator().next());
        disk.setStorageIds(storageIds);
        return disk;
    }

    private void initTestForPool(StoragePool pool, List<Guid> vmGuids, List<Guid> templatesGuids, List<Guid> removedGuids) {
        Guid poolId = pool.getId();
        doReturn(vmGuids).when(vmAndTemplatesGenerationsDao).getVmsIdsForOvfUpdate(poolId);
        doReturn(templatesGuids).when(vmAndTemplatesGenerationsDao).getVmTemplatesIdsForOvfUpdate(poolId);
        doReturn(removedGuids).when(vmAndTemplatesGenerationsDao).getIdsForOvfDeletion(poolId);
        pool.setStatus(StoragePoolStatus.Up);
    }

    private void verifyCorrectOvfDataUpdaterRun(Collection<Guid> needToBeUpdated) {

        assertTrue("not all needed vms/templates were updated in db",
                CollectionUtils.isEqualCollection(executedUpdatedOvfGenerationIdsInDb.keySet(),
                        needToBeUpdated));

        for (Map.Entry<Guid, Long> storagePoolGenerationEntry : executedUpdatedOvfGenerationIdsInDb.entrySet()) {
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
    }

    private void addVms(List<Guid> vmGuids, int diskCount, VMStatus vmStatus, ImageStatus vmImageStatus, Guid poolId) {
        vms.putAll(generateVmsMapByGuids(vmGuids, diskCount, vmStatus, vmImageStatus, poolId));

    }

    private void verifyOvfUpdatedForSupportedPools(List<Guid> poolsRequiredUpdate,
            Map<Guid, List<Guid>> domainsRequiredUpdateForPool) {
        for (Guid storagePoolId : poolsRequiredUpdate) {
            for (Guid updatedDomainForPool : executedOvfUpdatedDomains) {
                assertTrue("ovf update for domain has been executed with wrong pool",
                        poolDomainsOvfInfo.containsKey(updatedDomainForPool));
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
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePool() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;

        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);
        executeCommand();
        verify(command, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(anyMap());

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(idsThatNeededToBeUpdated);
        verifyOvfUpdatedForSupportedPools(Arrays.asList(pool1.getId()), Collections.<Guid, List<Guid>> emptyMap());
    }

    private void executeCommand() {
        command.executeCommand();
        executedOvfUpdatedDomains = (Set<Guid>)command.getActionReturnValue();
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCount() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;

        List<Guid> vmGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());
        List<Guid> removedGuids = generateGuidList(size);

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        executeCommand();
        verify(command, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(anyMap());

        List<Guid> needToBeUpdated = new LinkedList<>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(needToBeUpdated);

        verifyOvfUpdatedForSupportedPools(Arrays.asList(pool1.getId()), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterAllDisksAreLockedNonToRemove() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;

        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        List<Guid> templatesGuids = generateGuidList(size);

        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.LOCKED, pool1.getId());
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.LOCKED, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        executeCommand();
        verify(command, never()).performOvfUpdate(anyMap());
        verifyCorrectOvfDataUpdaterRun(Collections.<Guid> emptyList());
    }

    @Test
    public void testOvfDataUpdaterPartOfDisksAreLocked() {
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

        executeCommand();
        verify(command, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(anyMap());
        // list of ids that should have been updated.
        List<Guid> needToBeUpdated = new LinkedList<>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(needToBeUpdated);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
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
    public void testOvfDataUpdaterAllVmsAndTemplatesAreLocked() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        List<Guid> vmGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.ImageLocked, ImageStatus.OK, pool1.getId());
        List<Guid> removedGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.Locked, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        command.executeCommand();
        verify(command, never()).performOvfUpdate(anyMap());
        verifyCorrectOvfDataUpdaterRun(Collections.<Guid> emptyList());
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testOvfDataUpdaterPartOfVmsAndTemplatesAreLocked() {
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

        executeCommand();

        List<Guid> neededToBeUpdated = new LinkedList<>(vmGuidsUnlocked);
        neededToBeUpdated.addAll(templatesGuidsUnlocked);
        verify(command, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(anyMap());
        verifyCorrectOvfDataUpdaterRun(neededToBeUpdated);
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testUpdatedDbGeneration() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK, pool1.getId());
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK, pool1.getId());

        initTestForPool(pool1, vmGuids, templatesGuids, removedGuids);

        doReturn(2L).when(vmStaticDao).getDbGeneration(any(Guid.class));

        executeCommand();

        verify(command, never()).performOvfUpdate(anyMap());
        List<Guid> idsThatNeededToBeUpdated = new LinkedList<>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(Collections.<Guid> emptyList());
        verifyOvfUpdatedForSupportedPools(Collections.<Guid> emptyList(), Collections.<Guid, List<Guid>> emptyMap());
    }

    @Test
    public void testUpdateCalledForUnupdatedDomain() {
        Guid poolId = pool1.getId();
        StorageDomainOvfInfo ovfInfo = poolDomainsOvfInfo.entrySet().iterator().next().getValue().getFirst().get(0);
        ovfInfo.setStatus(StorageDomainOvfInfoStatus.OUTDATED);
        initTestForPool(pool1,
                Collections.<Guid> emptyList(),
                Collections.<Guid> emptyList(),
                Collections.<Guid> emptyList());
        executeCommand();
        verify(command, never()).performOvfUpdate(anyMap());
        Map<Guid, List<Guid>> domainsRequiredUpdateForPool =
                Collections.singletonMap(poolId, Arrays.asList(ovfInfo.getStorageDomainId()));
        verifyOvfUpdatedForSupportedPools(Arrays.asList(poolId), domainsRequiredUpdateForPool);
    }
}
