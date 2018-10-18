package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmJobDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmJobsMonitoringTest {

    @Spy
    private VmJobsMonitoring vmJobsMonitoring;

    private static final Guid VM_ID_1 = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504c");
    private static final Guid VM_ID_2 = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504d");
    private static final Guid JOB_ID_1 = new Guid("b7dfe5e6-5667-4e40-8ecb-000000000001");
    private static final Guid JOB_ID_2 = new Guid("b7dfe5e6-5667-4e40-8ecb-000000000002");
    private static final Guid JOB_ID_3 = new Guid("b7dfe5e6-5667-4e40-8ecb-000000000003");
    private static final Guid JOB_ID_4 = new Guid("b7dfe5e6-5667-4e40-8ecb-000000000004");
    private static final Guid JOB_ID_5 = new Guid("b7dfe5e6-5667-4e40-8ecb-000000000005");

    @Captor
    private ArgumentCaptor<Collection<VmJob>> vmJobsToUpdateCaptor;
    @Captor
    private ArgumentCaptor<List<VmJob>> vmJobsToRemoveCaptor;

    @Mock
    private VmJob job1FromDb;
    @Mock
    private VmJob job2FromDb;
    @Mock
    private VmJob job3FromDb;
    @Mock
    private VmJob job4FromDb;

    private VmJob job1FromVdsm;
    @Mock
    private VmJob job2FromVdsm;
    @Mock
    private VmJob job3FromVdsm;
    @Mock
    private VmJob job5FromVdsm;
    @Mock
    private VmJobDao vmJobDao;

    public static final long DEFAULT_FETCH_TIME_FOR_VMS = 10L;

    @BeforeEach
    public void before() {
        when(job1FromDb.getId()).thenReturn(JOB_ID_1);
        when(job2FromDb.getId()).thenReturn(JOB_ID_2);
        when(job3FromDb.getId()).thenReturn(JOB_ID_3);
        when(job4FromDb.getId()).thenReturn(JOB_ID_4);

        when(job2FromVdsm.getId()).thenReturn(JOB_ID_2);
        when(job3FromVdsm.getId()).thenReturn(JOB_ID_3);
        when(job5FromVdsm.getId()).thenReturn(JOB_ID_5);

        when(job1FromDb.getVmId()).thenReturn(VM_ID_1);
        when(job2FromDb.getVmId()).thenReturn(VM_ID_1);
        when(job2FromVdsm.getVmId()).thenReturn(VM_ID_1);

        when(job3FromDb.getVmId()).thenReturn(VM_ID_2);
        when(job4FromDb.getVmId()).thenReturn(VM_ID_2);
        when(job3FromVdsm.getVmId()).thenReturn(VM_ID_2);
        when(job5FromVdsm.getVmId()).thenReturn(VM_ID_2);

        when(vmJobDao.getAll()).thenReturn(Arrays.asList(job1FromDb, job4FromDb, job2FromDb, job3FromDb));
        doReturn(vmJobDao).when(vmJobsMonitoring).getVmJobDao();
        doReturn(Collections.emptyList()).when(vmJobsMonitoring).getIdsOfDownVms();
        doNothing().when(vmJobsMonitoring).removeJobsByVmIds(any());

        vmJobsMonitoring.init();
    }

    @Test
    public void noVms() {
        vmJobsMonitoring.process(Collections.emptyMap(), DEFAULT_FETCH_TIME_FOR_VMS);
        verify(vmJobsMonitoring, never()).getExistingJobsForVm(any());
        verify(vmJobsMonitoring, never()).updateJobs(any());
        verify(vmJobsMonitoring, never()).removeJobs(any());
    }

    @Test
    public void vmWithNoJobs() {
        vmJobsMonitoring.process(Collections.singletonMap(VM_ID_1, null), DEFAULT_FETCH_TIME_FOR_VMS);
        verify(vmJobsMonitoring, never()).getExistingJobsForVm(any());
        verify(vmJobsMonitoring, times(1)).updateJobs(vmJobsToUpdateCaptor.capture());
        assertTrue(vmJobsToUpdateCaptor.getValue().isEmpty());
        verify(vmJobsMonitoring, times(1)).removeJobs(vmJobsToRemoveCaptor.capture());
        assertTrue(vmJobsToRemoveCaptor.getValue().isEmpty());
    }

    @Test
    public void noChangeInJobs() {
        vmJobsMonitoring.process(Collections.singletonMap(VM_ID_1, Arrays.asList(job1FromDb, job2FromDb)), DEFAULT_FETCH_TIME_FOR_VMS);
        verify(vmJobsMonitoring, times(1)).updateJobs(vmJobsToUpdateCaptor.capture());
        assertTrue(vmJobsToUpdateCaptor.getValue().isEmpty());
        verify(vmJobsMonitoring, times(1)).removeJobs(vmJobsToRemoveCaptor.capture());
        assertTrue(vmJobsToRemoveCaptor.getValue().isEmpty());
    }

    @Test
    public void vmsWithJobs() {
        vmJobsMonitoring.process(initJobsFromVdsm(), DEFAULT_FETCH_TIME_FOR_VMS);
        List<VmJob> vm1Jobs = vmJobsMonitoring.getExistingJobsForVm(VM_ID_1);
        List<VmJob> vm2Jobs = vmJobsMonitoring.getExistingJobsForVm(VM_ID_2);
        assertEquals(2, vm1Jobs.size());
        assertTrue(vm1Jobs.contains(job2FromVdsm));
        assertFalse(vm1Jobs.contains(job2FromDb));
        assertEquals(1, vm2Jobs.size());
        assertTrue(vm2Jobs.contains(job3FromVdsm));
    }

    @Test
    public void loadFromDatabase() {
        assertEquals(2, vmJobsMonitoring.getExistingJobsForVm(VM_ID_1).size());
        assertEquals(2, vmJobsMonitoring.getExistingJobsForVm(VM_ID_2).size());
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_1).contains(job1FromDb));
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_1).contains(job2FromDb));
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_2).contains(job3FromDb));
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_2).contains(job4FromDb));
    }

    @Test
    public void finishedJobsAreRemovedFromMemory() {
        vmJobsMonitoring.process(Collections.singletonMap(VM_ID_1, Collections.emptyList()), DEFAULT_FETCH_TIME_FOR_VMS);
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_1).isEmpty());
        assertEquals(2, vmJobsMonitoring.getExistingJobsForVm(VM_ID_2).size());
    }

    @Test
    public void jobsAreRemovedFromMemoryOnRemoveVm() {
        vmJobsMonitoring.onVmDelete(VM_ID_1);
        assertTrue(vmJobsMonitoring.getExistingJobsForVm(VM_ID_1).isEmpty());
        assertEquals(2, vmJobsMonitoring.getExistingJobsForVm(VM_ID_2).size());
    }

    @Test
    public void jobsAreNotUpdatedWhenVmDataNotCurrent() {
        initJobsFromVdsm();
        when(job2FromDb.getStartTime()).thenReturn(5L);
        vmJobsMonitoring.process(Collections.singletonMap(VM_ID_1, Arrays.asList(job1FromVdsm, job2FromVdsm)), 3L);
        verify(vmJobsMonitoring, times(1)).updateJobs(vmJobsToUpdateCaptor.capture());
        verify(vmJobsMonitoring, times(1)).updateJobs(vmJobsToRemoveCaptor.capture());

        // The start time of the job is later than the fetch time so no update should have been updated or removed
        assertTrue(vmJobsToUpdateCaptor.getValue().isEmpty());
        assertTrue(vmJobsToRemoveCaptor.getValue().isEmpty());
    }

    @SuppressWarnings("serial")
    public Map<Guid, List<VmJob>> initJobsFromVdsm() {
        job1FromVdsm = job1FromDb;
        return new HashMap<Guid, List<VmJob>>() {{
            put(VM_ID_1, Arrays.asList(job1FromVdsm, job2FromVdsm));
            put(VM_ID_2, Arrays.asList(job3FromVdsm, job5FromVdsm));
        }};
    }
}
