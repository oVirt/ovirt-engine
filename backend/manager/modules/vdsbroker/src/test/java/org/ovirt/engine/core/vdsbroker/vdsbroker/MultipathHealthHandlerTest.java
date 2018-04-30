package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@ExtendWith(MockitoExtension.class)
public class MultipathHealthHandlerTest {

    @InjectMocks
    private MultipathHealthHandler multipathHealthHandler;
    @Mock
    private AuditLogDirector auditLogDirector;
    @Captor
    private ArgumentCaptor<AuditLogType> logTypeCaptor;

    @Test
    public void testEmptyHealthReport() {
        VDS vds = new VDS();
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.MULTIPATH_HEALTH, new HashMap<>());
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);

        verify(auditLogDirector, times(1)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.NO_FAULTY_MULTIPATHS_ON_HOST));
    }

    @Test
    public void testEmptyHealthReportSeveral() {
        VDS vds = new VDS();
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.MULTIPATH_HEALTH, new HashMap<>());
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);

        verify(auditLogDirector, times(1)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.NO_FAULTY_MULTIPATHS_ON_HOST));
    }

    @Test
    public void testNoHealthReport() {
        VDS vds = new VDS();
        Map<String, Object> struct = new HashMap<>();
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);

        verify(auditLogDirector, never()).log(any(), logTypeCaptor.capture());
    }

    private Map<String, Object> generateFaultyMultipathReport() {
        Map<String, Object> struct = new HashMap<>();
        Map<String, Object> report = new HashMap<>();
        struct.put(VdsProperties.MULTIPATH_HEALTH, report);
        Map<String, Object> guid1Report = new HashMap<>();
        report.put("guid-1", guid1Report);
        guid1Report.put(VdsProperties.MULTIPATH_VALID_PATHS, 1);
        return struct;
    }

    @Test
    public void testFaultyMultipathReport() {
        VDS vds = new VDS();
        multipathHealthHandler.handleMultipathHealthReport(vds, generateFaultyMultipathReport());

        verify(auditLogDirector, times(1)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.FAULTY_MULTIPATHS_ON_HOST));
    }

    @Test
    public void testFaultyMultipathSeveralReport() {
        VDS vds = new VDS();
        multipathHealthHandler.handleMultipathHealthReport(vds, generateFaultyMultipathReport());
        multipathHealthHandler.handleMultipathHealthReport(vds, generateFaultyMultipathReport());
        multipathHealthHandler.handleMultipathHealthReport(vds, generateFaultyMultipathReport());

        verify(auditLogDirector, times(1)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.FAULTY_MULTIPATHS_ON_HOST));
    }

    @Test
    public void testMultipathNoValidPathReport() {
        VDS vds = new VDS();
        Map<String, Object> struct = new HashMap<>();
        Map<String, Object> report = new HashMap<>();
        struct.put(VdsProperties.MULTIPATH_HEALTH, report);
        Map<String, Object> guid1Report = new HashMap<>();
        report.put("guid-1", guid1Report);
        guid1Report.put(VdsProperties.MULTIPATH_VALID_PATHS, 0);
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);

        verify(auditLogDirector, times(1)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST));
    }

    @Test
    public void testMultipathMixedReport() {
        VDS vds = new VDS();
        Map<String, Object> struct = new HashMap<>();
        Map<String, Object> report = new HashMap<>();
        struct.put(VdsProperties.MULTIPATH_HEALTH, report);
        Map<String, Object> guid1Report = new HashMap<>();
        report.put("guid-1", guid1Report);
        guid1Report.put(VdsProperties.MULTIPATH_VALID_PATHS, 0);
        Map<String, Object> guid2Report = new HashMap<>();
        report.put("guid-2", guid2Report);
        guid2Report.put(VdsProperties.MULTIPATH_VALID_PATHS, 2);
        multipathHealthHandler.handleMultipathHealthReport(vds, struct);

        verify(auditLogDirector, times(2)).log(any(), logTypeCaptor.capture());
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.MULTIPATH_DEVICES_WITHOUT_VALID_PATHS_ON_HOST));
        assertTrue(logTypeCaptor.getAllValues().contains(AuditLogType.FAULTY_MULTIPATHS_ON_HOST));
    }

}
