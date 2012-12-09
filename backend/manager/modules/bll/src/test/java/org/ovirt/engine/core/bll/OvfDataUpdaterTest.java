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
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

/** A test class for the {@link OvfDataUpdater} class */
@RunWith(MockitoJUnitRunner.class)
public class OvfDataUpdaterTest {
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
    private VmTemplateDAO vmTemplateDAO;

    private storage_pool pool1;

    private storage_pool pool2;

    private Map<Guid, VM> vms;

    private Map<Guid, VmTemplate> templates;

    // those members are used to save the executed data during a run of the ovf data updater and are used for comprasion with the expected data needed to be executed
    // in the end of each test.
    private Guid executedUpdateStoragePoolId;

    private Map<Guid, Map<Guid, KeyValuePairCompat<String, List<Guid>>>> executedUpdatedMetadataForStoragePool;

    private Map<Guid, Map<Guid, Long>> executedUpdatedOvfGenerationIdsInDb;

    private Guid executedUpdateStorageDomainId;

    private Map<Guid, List<Guid>> executedRemovedIds;

    private Guid executedRemoveStoragePoolId;

    private Guid executedRemoveStorageDomainId;

    private final int ITEMS_COUNT_PER_UPDATE = 100;

    @Before
    public void setUp() {
        updater = Mockito.spy(OvfDataUpdater.getInstance());
        doReturn(ITEMS_COUNT_PER_UPDATE).when(updater).reloadConfigValue();
        doReturn(new ArrayList<DiskImage>()).when(updater).getAllImageSnapshots(any(DiskImage.class));
        doCallRealMethod().when(updater).ovfUpdate_timer();
        // init members
        initMembers();

        //init daos
        doReturn(storagePoolDAO).when(updater).getStoragePoolDao();
        doReturn(vmAndTemplatesGenerationsDAO).when(updater).getVmAndTemplatesGenerationsDao();
        doReturn(vmDAO).when(updater).getVmDao();
        doReturn(vmStaticDAO).when(updater).getVmStaticDao();
        doReturn(vmTemplateDAO).when(updater).getVmTemplateDao();

        // mock ovf data updater methods
        doNothing().when(updater).loadTemplateData(any(VmTemplate.class));
        doNothing().when(updater).loadVmData(any(VM.class));
        doNothing().when(updater).updateVmDisksFromDb(any(VM.class));
        doNothing().when(updater).addAuditLogError(anyString());
        doNothing().when(updater).updateTemplateDisksFromDb(any(VmTemplate.class));

        // dao related mocks.
        doReturn(buildStoragePoolsList()).when(storagePoolDAO).getAllByStatus(any(StoragePoolStatus.class));
        doReturn(1L).when(vmStaticDAO).getDbGeneration(any(Guid.class));

        mockAnswers();
    }

    private void initMembers() {
        executedUpdatedMetadataForStoragePool = new HashMap<Guid, Map<Guid, KeyValuePairCompat<String, List<Guid>>>>();
        executedRemovedIds = new HashMap<Guid, List<Guid>>();
        executedUpdatedOvfGenerationIdsInDb = new HashMap<Guid, Map<Guid, Long>>();
        vms = new HashMap<Guid, VM>();
        templates = new HashMap<Guid, VmTemplate>();
        pool1 =
                new storage_pool("first sp",
                        Guid.NewGuid(),
                        "storage_pool1",
                        StorageType.NFS.getValue(),
                        StoragePoolStatus.Up.getValue());
        pool2 =
                new storage_pool("second sp",
                        Guid.NewGuid(),
                        "storage_pool2",
                        StorageType.NFS.getValue(),
                        StoragePoolStatus.Up.getValue());

        performStoragePoolInitOps(pool1);
        performStoragePoolInitOps(pool2);
    }

    private void performStoragePoolInitOps(storage_pool pool) {
        executedUpdatedMetadataForStoragePool.put(pool.getId(),
                new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>());
        executedRemovedIds.put(pool.getId(), new LinkedList<Guid>());
        executedUpdatedOvfGenerationIdsInDb.put(pool.getId(), new HashMap<Guid, Long>());
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
                        values.size() <= OvfDataUpdater.MAX_ITEMS_PER_SQL_STATEMENT);
                assertEquals("the size of the list of ids for update is not the same as the size of the " +
                        "list with the new ovf values", values.size(), ids.size());
                Guid[] ids_array = ids.toArray(new Guid[ids.size()]);
                Long[] values_array = values.toArray(new Long[values.size()]);
                for (int i = 0; i < ids_array.length; i++) {
                    executedUpdatedOvfGenerationIdsInDb.get(executedUpdateStoragePoolId).put(ids_array[i], values_array[i]);
                }
                return null;
            }

        }).when(vmAndTemplatesGenerationsDAO).updateOvfGenerations(anyList(), anyList());
    }

    private List<storage_pool> buildStoragePoolsList() {
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
        template.setstatus(templateStatus);
        template.setDbGeneration(1L);
        template.setId(id);
        return template;
    }

    private List<Guid> generateGuidList(int size) {
        List<Guid> toReturn = new LinkedList<Guid>();
        for (int i = 0; i < size; i++) {
            toReturn.add(Guid.NewGuid());
        }
        return toReturn;
    }

    private Map<Guid, VM> generateVmsMapByGuids(List<Guid> ids, int diskCount, VMStatus vmStatus, ImageStatus diskStatus) {
        Map<Guid, VM> toReturn = new HashMap<Guid, VM>();
        for (Guid id : ids) {
            VM vm = createVm(id, vmStatus);
            for (int i = 0; i < diskCount; i++) {
                DiskImage image = createDiskImage(diskStatus);
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
            ImageStatus diskStatus) {
        Map<Guid, VmTemplate> toReturn = new HashMap<Guid, VmTemplate>();
        for (Guid id : ids) {
            VmTemplate template = createVmTemplate(id, templateStatus);
            for (int i = 0; i < diskCount; i++) {
                DiskImage image = createDiskImage(diskStatus);
                template.getDiskMap().put(image.getId(), image);
                template.getDiskList().add(image);
            }
            toReturn.put(template.getId(), template);
        }
        return toReturn;
    }

    private DiskImage createDiskImage(ImageStatus status) {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.NewGuid());
        disk.setimageStatus(status);
        return disk;
    }

    private void initTestForPool(Guid poolId, List<Guid> vmGuids, List<Guid> templatesGuids, List<Guid> removedGuids) {
        doReturn(vmGuids).when(vmAndTemplatesGenerationsDAO).getVmsIdsForOvfUpdate(poolId);
        doReturn(templatesGuids).when(vmAndTemplatesGenerationsDAO).getVmTemplatesIdsForOvfUpdate(poolId);
        doReturn(removedGuids).when(vmAndTemplatesGenerationsDAO).getIdsForOvfDeletion(poolId);
    }

    private void verifyCorrectOvfDataUpdaterRun(Guid storagePoolId,
            Collection<Guid> needToBeUpdated,
            Collection<Guid> removedGuids) {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> storagePoolMetadataUpdatedMap = executedUpdatedMetadataForStoragePool.get(storagePoolId);
        Map<Guid, Long> storagePoolUpdateOvfGenerationsInDb = executedUpdatedOvfGenerationIdsInDb.get(storagePoolId);
        assertTrue("not all needed vms/templates were updated in vdsm",
                CollectionUtils.isEqualCollection(storagePoolMetadataUpdatedMap.keySet(),
                        needToBeUpdated));
        assertTrue("not all needed vms/templates were updated in db",
                CollectionUtils.isEqualCollection(storagePoolUpdateOvfGenerationsInDb.keySet(),
                        needToBeUpdated));

        for (Map.Entry<Guid, KeyValuePairCompat<String, List<Guid>>> entry : storagePoolMetadataUpdatedMap
                .entrySet()) {
            assertEquals("wrong ovf data stored in storage for vm/template",
                    entry.getKey().toString(),
                    entry.getValue().getKey());
        }

        for (Guid id : storagePoolUpdateOvfGenerationsInDb.keySet()) {
            boolean isCorrectVersion = false;
            if (vms.get(id) != null) {
                isCorrectVersion =
                        storagePoolUpdateOvfGenerationsInDb
                                .get(id)
                                .equals(vms.get(id).getDbGeneration());
            } else if (templates.get(id) != null) {
                isCorrectVersion =
                        storagePoolUpdateOvfGenerationsInDb
                                .get(id)
                                .equals(templates.get(id).getDbGeneration());
            }
            assertTrue("wrong new ovf version persisted for vm/template", isCorrectVersion);
        }

        assertTrue("not all needed vms/templates were removed from vdsm",
                CollectionUtils.isEqualCollection(removedGuids, executedRemovedIds.get(storagePoolId)));
    }

    private void addVms(List<Guid> vmGuids, int diskCount, VMStatus vmStatus, ImageStatus vmImageStatus) {
        vms.putAll(generateVmsMapByGuids(vmGuids, diskCount, vmStatus, vmImageStatus));

    }

    private void addTemplates(List<Guid> templatesGuids,
            int diskCount,
            VmTemplateStatus templateStatus,
            ImageStatus templateImageStatus) {
        templates.putAll(generateVmTemplatesMapByGuids(templatesGuids, diskCount, templateStatus, templateImageStatus));
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountTwoPools() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;

        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);

        List<Guid> vmGuids2 = generateGuidList(size);
        List<Guid> templatesGuids2 = generateGuidList(size);
        List<Guid> removedGuids2 = generateGuidList(size);
        addVms(vmGuids2, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids2, 2, VmTemplateStatus.OK, ImageStatus.OK);
        initTestForPool(pool2.getId(), vmGuids2, templatesGuids2, removedGuids2);

        updater.ovfUpdate_timer();

        verify(updater, times(numberOfTimesToBeCalled(size, true) * 2)).performOvfUpdate(any(Guid.class),
                anyMap());
        verify(updater, times(size * 2)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        List<Guid> idsThatNeededToBeUpdated2 = new LinkedList<Guid>(vmGuids2);
        idsThatNeededToBeUpdated2.addAll(templatesGuids2);

        verifyCorrectOvfDataUpdaterRun(pool1.getId(), idsThatNeededToBeUpdated, removedGuids);
        verifyCorrectOvfDataUpdaterRun(pool2.getId(), idsThatNeededToBeUpdated2, removedGuids2);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveExceptionInOnePool() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;

        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);

        List<Guid> vmGuids2 = generateGuidList(size);
        List<Guid> templatesGuids2 = generateGuidList(size);
        List<Guid> removedGuids2 = generateGuidList(size);
        addVms(vmGuids2, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids2, 2, VmTemplateStatus.OK, ImageStatus.OK);
        initTestForPool(pool2.getId(), vmGuids2, templatesGuids2, removedGuids2);

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

        // + 1 as the exception is thrown during the first call to this method during first update execution for the first pool.
        int numOfExpectedRuns = numberOfTimesToBeCalled(size, true) + 1;
        verify(updater, times(numOfExpectedRuns)).performOvfUpdate(any(Guid.class),
                anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        List<Guid> idsToBeUpdated = new LinkedList<Guid>(vmGuids2);
        idsToBeUpdated.addAll(templatesGuids2);
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), Collections.<Guid>emptyList(), Collections.<Guid>emptyList());
        verifyCorrectOvfDataUpdaterRun(pool2.getId(), idsToBeUpdated, removedGuids2);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveHigherThanCountOnePool() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();

        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(pool1.getId(), idsThatNeededToBeUpdated, removedGuids);
    }

    @Test
    public void testOvfDataUpdaterRunWithUpdateAndRemoveLowerThanCount() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        List<Guid> vmGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);
        List<Guid> removedGuids = generateGuidList(size);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        List<Guid> needToBeUpdated = new LinkedList<Guid>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), needToBeUpdated, removedGuids);
    }

    @Test
    public void testOvfDataUpdaterAllDisksAreLockedNonToRemove() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        List<Guid> templatesGuids = generateGuidList(size);

        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.LOCKED);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.LOCKED);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, never()).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, never()).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), Collections.<Guid> emptyList(), removedGuids);
    }

    @Test
    public void testOvfDataUpdaterPartOfDisksAreLocked() {
        int size = ITEMS_COUNT_PER_UPDATE - 1;
        // unlocked vms/templates
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);

        // locked vms/templates
        List<Guid> lockedVmGuids = generateGuidList(size);
        List<Guid> lockedTemplatesGuids = generateGuidList(size);
        addVms(lockedVmGuids, 2, VMStatus.Down, ImageStatus.LOCKED);
        addTemplates(lockedTemplatesGuids, 2, VmTemplateStatus.OK, ImageStatus.LOCKED);
        // ids for removal
        List<Guid> removedGuids = generateGuidList(size);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        // list of ids that should have been updated.
        List<Guid> needToBeUpdated = new LinkedList<Guid>(vmGuids);
        needToBeUpdated.addAll(templatesGuids);
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), needToBeUpdated, removedGuids);
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
        addVms(vmGuids, 2, VMStatus.ImageLocked, ImageStatus.OK);
        List<Guid> removedGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        addTemplates(templatesGuids, 2, VmTemplateStatus.Locked, ImageStatus.OK);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();
        verify(updater, never()).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), Collections.<Guid> emptyList(), removedGuids);
    }

    @Test
    public void testOvfDataUpdaterPartOfVmsAndTemplatesAreLocked() {
        int size = ITEMS_COUNT_PER_UPDATE;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> removedGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);

        addVms(vmGuids, 2, VMStatus.ImageLocked, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.Locked, ImageStatus.OK);

        List<Guid> vmGuidsUnlocked = generateGuidList(size);
        List<Guid> templatesGuidsUnlocked = generateGuidList(size);

        addVms(vmGuidsUnlocked, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuidsUnlocked, 2, VmTemplateStatus.OK, ImageStatus.OK);

        vmGuids.addAll(vmGuidsUnlocked);
        templatesGuids.addAll(templatesGuidsUnlocked);
        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        updater.ovfUpdate_timer();

        List<Guid> neededToBeUpdated = new LinkedList<Guid>(vmGuidsUnlocked);
        neededToBeUpdated.addAll(templatesGuidsUnlocked);
        verify(updater, times(numberOfTimesToBeCalled(size, true))).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, times(size)).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));
        verifyCorrectOvfDataUpdaterRun(pool1.getId(), neededToBeUpdated, removedGuids);
    }

    @Test
    public void testUpdatedDbGeneration() {
        int size = 3 * ITEMS_COUNT_PER_UPDATE + 10;
        List<Guid> vmGuids = generateGuidList(size);
        List<Guid> templatesGuids = generateGuidList(size);
        List<Guid> removedGuids = Collections.emptyList();
        addVms(vmGuids, 2, VMStatus.Down, ImageStatus.OK);
        addTemplates(templatesGuids, 2, VmTemplateStatus.OK, ImageStatus.OK);

        initTestForPool(pool1.getId(), vmGuids, templatesGuids, removedGuids);

        doReturn(2L).when(vmStaticDAO).getDbGeneration(any(Guid.class));

        updater.ovfUpdate_timer();

        verify(updater, never()).performOvfUpdate(any(Guid.class), anyMap());
        verify(updater, never()).executeRemoveVmInSpm(any(Guid.class), any(Guid.class), any(Guid.class));

        List<Guid> idsThatNeededToBeUpdated = new LinkedList<Guid>(vmGuids);
        idsThatNeededToBeUpdated.addAll(templatesGuids);

        verifyCorrectOvfDataUpdaterRun(pool1.getId(), Collections.<Guid> emptyList(), removedGuids);
    }
}
