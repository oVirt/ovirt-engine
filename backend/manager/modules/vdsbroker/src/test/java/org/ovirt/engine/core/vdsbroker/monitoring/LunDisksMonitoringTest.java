package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LunDisksMonitoringTest {

    @Spy
    private LunDisksMonitoring lunDisksMonitoring;

    private static final Guid VM_ID_1 = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504c");
    private static final Guid VM_ID_2 = new Guid("b7dfe5e6-5667-4e40-8ecb-6d97c8df504d");
    private static final String LUN_ID_1 = "lun1";
    private static final String LUN_ID_2 = "lun2";
    private static final String LUN_ID_3 = "lun3";
    private static final String LUN_ID_4 = "lun4";

    @Captor
    private ArgumentCaptor<List<LUNs>> lunsCaptor;
    @Captor
    private ArgumentCaptor<Integer> deviceSizeCaptor;
    @Mock
    private LUNs lun1FromDb;
    @Mock
    private LUNs lun2FromDb;
    @Mock
    private LUNs lun3FromDb;
    @Mock
    private LUNs lun4FromDb;
    @Mock
    private LUNs lun1FromVdsm;
    @Mock
    private LUNs lun2FromVdsm;
    @Mock
    private LUNs lun3FromVdsm;
    @Mock
    private LUNs lun4FromVdsm;

    @BeforeEach
    public void before() {
        when(lun1FromDb.getId()).thenReturn(LUN_ID_1);
        when(lun2FromDb.getId()).thenReturn(LUN_ID_2);
        when(lun3FromDb.getId()).thenReturn(LUN_ID_3);
        when(lun4FromDb.getId()).thenReturn(LUN_ID_4);
        doReturn(Arrays.asList(lun1FromDb, lun2FromDb)).when(lunDisksMonitoring).getVmPluggedLunsFromDb(VM_ID_1);
        doReturn(Arrays.asList(lun3FromDb, lun4FromDb)).when(lunDisksMonitoring).getVmPluggedLunsFromDb(VM_ID_2);
        doNothing().when(lunDisksMonitoring).saveVmLunDisks(any());
    }

    @Test
    public void noVms() {
        lunDisksMonitoring.process(Collections.emptyMap());
        verify(lunDisksMonitoring, never()).getVmLunDisksToSave(any(), any());
        verify(lunDisksMonitoring, never()).saveVmLunDisks(any());
    }

    @Test
    public void vmWithNoLuns() {
        lunDisksMonitoring.process(Collections.singletonMap(VM_ID_1, Collections.emptyMap()));
        verify(lunDisksMonitoring, never()).getVmPluggedLunsFromDb(any());
        verify(lunDisksMonitoring, times(1)).saveVmLunDisks(lunsCaptor.capture());
        assertTrue(lunsCaptor.getValue().isEmpty());
    }

    @Test
    public void noLunChanged() {
        doReturn(0).when(lun1FromDb).getDeviceSize();
        doReturn(0).when(lun1FromVdsm).getDeviceSize();
        doReturn(20).when(lun2FromDb).getDeviceSize();
        doReturn(20).when(lun2FromVdsm).getDeviceSize();
        doReturn(30).when(lun3FromDb).getDeviceSize();
        doReturn(30).when(lun3FromVdsm).getDeviceSize();
        doReturn(40).when(lun4FromDb).getDeviceSize();
        doReturn(0).when(lun4FromVdsm).getDeviceSize();

        lunDisksMonitoring.process(initInputFromVdsm());
        verify(lunDisksMonitoring, times(1)).saveVmLunDisks(lunsCaptor.capture());
        assertTrue(lunsCaptor.getValue().isEmpty());
    }

    @Test
    public void someLunsChanged() {
        doReturn(0).when(lun1FromDb).getDeviceSize();
        doReturn(0).when(lun1FromVdsm).getDeviceSize();
        doReturn(20).when(lun2FromDb).getDeviceSize();
        doReturn(25).when(lun2FromVdsm).getDeviceSize();
        doReturn(30).when(lun3FromDb).getDeviceSize();
        doReturn(35).when(lun3FromVdsm).getDeviceSize();
        doReturn(40).when(lun4FromDb).getDeviceSize();
        doReturn(0).when(lun4FromVdsm).getDeviceSize();

        lunDisksMonitoring.process(initInputFromVdsm());
        verify(lunDisksMonitoring, times(1)).saveVmLunDisks(lunsCaptor.capture());
        assertEquals(2, lunsCaptor.getValue().size());
        assertNotNull(findLunById(LUN_ID_2, lunsCaptor.getValue()));
        verify(lun2FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(25, (int) deviceSizeCaptor.getValue());
        assertNotNull(findLunById(LUN_ID_3, lunsCaptor.getValue()));
        verify(lun3FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(35, (int) deviceSizeCaptor.getValue());
    }

    @Test
    public void allLunsChanged() {
        doReturn(10).when(lun1FromDb).getDeviceSize();
        doReturn(15).when(lun1FromVdsm).getDeviceSize();
        doReturn(20).when(lun2FromDb).getDeviceSize();
        doReturn(25).when(lun2FromVdsm).getDeviceSize();
        doReturn(30).when(lun3FromDb).getDeviceSize();
        doReturn(35).when(lun3FromVdsm).getDeviceSize();
        doReturn(40).when(lun4FromDb).getDeviceSize();
        doReturn(45).when(lun4FromVdsm).getDeviceSize();

        lunDisksMonitoring.process(initInputFromVdsm());
        verify(lunDisksMonitoring, times(1)).saveVmLunDisks(lunsCaptor.capture());
        assertEquals(4, lunsCaptor.getValue().size());
        verify(lun1FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(15, (int) deviceSizeCaptor.getValue());
        verify(lun2FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(25, (int) deviceSizeCaptor.getValue());
        verify(lun3FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(35, (int) deviceSizeCaptor.getValue());
        verify(lun4FromDb, times(1)).setDeviceSize(deviceSizeCaptor.capture());
        assertEquals(45, (int) deviceSizeCaptor.getValue());
    }

    private LUNs findLunById(String lunId, List<LUNs> luns) {
        return luns.stream().filter(lun -> lun.getId().equals(lunId)).findFirst().orElse(null);
    }

    @SuppressWarnings("serial")
    private Map<Guid, Map<String, LUNs>> initInputFromVdsm() {
        return new HashMap<Guid, Map<String, LUNs>>() {{
            put(VM_ID_1, new HashMap<String, LUNs>() {{
                put(LUN_ID_1, lun1FromVdsm);
                put(LUN_ID_2, lun2FromVdsm);
            }});
            put(VM_ID_2, new HashMap<String, LUNs>() {{
                put(LUN_ID_3, lun3FromVdsm);
                put(LUN_ID_4, lun4FromVdsm);
            }});
        }};
    }
}
