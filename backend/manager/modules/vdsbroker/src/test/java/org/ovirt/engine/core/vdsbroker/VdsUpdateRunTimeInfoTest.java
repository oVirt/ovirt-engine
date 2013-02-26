package org.ovirt.engine.core.vdsbroker;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

@RunWith(MockitoJUnitRunner.class)
public class VdsUpdateRunTimeInfoTest {

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    private VDS vds;
    XmlRpcStruct[] vmInfo;

    VdsUpdateRunTimeInfo updater;

    @Mock
    VdsGroupDAO groupDAO;

    @Mock
    VmDAO vmDAO;

    @Mock
    DbFacade dbFacade;

    @Mock
    VDSGroup cluster;

    @Mock
    VmDeviceDAO vmDeviceDAO;

    AuditLogDAO mockAuditLogDao = new AuditLogDaoMocker();

    @Before
    public void setup() {
        initVds();
        initConditions();
        updater = new VdsUpdateRunTimeInfo(null, vds, mock(MonitoringStrategy.class)) {

            @Override
            public DbFacade getDbFacade() {
                return dbFacade;
            }

            @Override
            protected void auditLog(AuditLogableBase auditLogable, AuditLogType logType) {
                AuditLog al = new AuditLog();
                al.setlog_type(logType);
                mockAuditLogDao.save(al);
            }

            @Override
            protected XmlRpcStruct[] getVmInfo(List<String> vmsToUpdate) {
                return vmInfo;
            }

        };
    }

    @Test
    public void updateVmDevicesNull() {
        updater.updateVmDevices(Collections.singletonList(""));

        assertEquals("wrong number of new devices", 0, updater.getNewVmDevices().size());
        assertEquals("wrong number of removed devices", 0, updater.getRemovedVmDevices().size());
    }

    @Test
    public void updateVmDevicesNotNull() {
        Guid vmGuid = Guid.NewGuid();
        when(vmDeviceDAO.getVmDeviceByVmId(vmGuid)).thenReturn(Collections.<VmDevice> emptyList());

        XmlRpcStruct vm = new XmlRpcStruct();
        vm.add(VdsProperties.vm_guid, vmGuid.toString());

        Map<String, Object> deviceProperties = new HashMap<String, Object>();
        Guid deviceID = Guid.NewGuid();
        deviceProperties.put(VdsProperties.DeviceId, deviceID.toString());
        deviceProperties.put(VdsProperties.Address, Collections.emptyMap());
        deviceProperties.put(VdsProperties.Device, "aDevice");
        deviceProperties.put(VdsProperties.Type, "aType");

        vm.add(VdsProperties.Devices, new XmlRpcStruct[] { new XmlRpcStruct(deviceProperties) });
        vmInfo = new XmlRpcStruct[] { vm };

        updater.updateVmDevices(Collections.singletonList(vmGuid.toString()));

        assertEquals("wrong number of new devices", 1, updater.getNewVmDevices().size());
        assertEquals("wrong number of removed devices", 0, updater.getRemovedVmDevices().size());
    }

    private void initConditions() {
        when(dbFacade.getVdsGroupDao()).thenReturn(groupDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        when(dbFacade.getAuditLogDao()).thenReturn(mockAuditLogDao);
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDAO);
        when(groupDAO.get((Guid) any())).thenReturn(cluster);
        Map<Guid, VM> emptyMap = Collections.emptyMap();
        when(vmDAO.getAllRunningByVds(vds.getId())).thenReturn(emptyMap);
    }

    private void initVds() {
        vds = new VDS();
        vds.setId(Guid.createGuidFromString("00000000-0000-0000-0000-000000000012"));

    }
}
