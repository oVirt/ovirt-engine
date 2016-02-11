package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class VmSlaPolicyCommandTest {

    static final Guid VM_ID = Guid.newGuid();

    static final Guid DISK_DOMAIN_ID = Guid.newGuid();
    static final Guid DISK_POOL_ID = Guid.newGuid();
    static final Guid DISK_IMAGE_ID = Guid.newGuid();
    static final Guid DISK_VOLUME_ID = Guid.newGuid();

    @Mock
    private VmDao vmDao;

    @Mock
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    private VM vm;

    private DiskImage diskImage;

    private VmSlaPolicyParameters parameters;
    private VmSlaPolicyCommand<VmSlaPolicyParameters> command;

    private Function<UpdateVmPolicyVDSParams, Boolean> vdsFunction;

    @Before
    public void setUp() {
        parameters = new VmSlaPolicyParameters(VM_ID);
        command = spy(new VmSlaPolicyCommand<>(parameters, null));

        doReturn(vmDao).when(command).getVmDao();
        doReturn(vmNetworkInterfaceDao).when(command).getVmNetworkInterfaceDao();

        Answer<?> answer = invocation -> {
            VDSCommandType commandType = (VDSCommandType) invocation.getArguments()[0];
            assertEquals(commandType, VDSCommandType.UpdateVmPolicy);

            UpdateVmPolicyVDSParams params = (UpdateVmPolicyVDSParams) invocation.getArguments()[1];
            VDSReturnValue retVal = new VDSReturnValue();
            retVal.setSucceeded(vdsFunction.apply(params));
            return retVal;
        };

        doAnswer(answer).when(command).runVdsCommand(any(VDSCommandType.class), any(VDSParametersBase.class));

        vm = new VM();
        vm.setId(VM_ID);
        vm.setStatus(VMStatus.Up);

        when(vmDao.get(VM_ID)).thenReturn(vm);
        when(vmNetworkInterfaceDao.getAllForVm(any(Guid.class))).thenReturn(Collections.emptyList());

        diskImage = new DiskImage();
        diskImage.setStorageIds(new ArrayList<>());
        diskImage.getStorageIds().add(DISK_DOMAIN_ID);
        diskImage.setStoragePoolId(DISK_POOL_ID);
        diskImage.setId(DISK_IMAGE_ID);
        diskImage.setImageId(DISK_VOLUME_ID);
    }

    @Test
    public void testVmDown() {
        vm.setStatus(VMStatus.Down);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
    }

    @Test
    public void testEmptyParams() {
        parameters.setCpuQos(null);
        parameters.setStorageQos(Collections.emptyMap());
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VM_SLA_POLICY_UNCHANGED);
    }

    @Test
    public void testCpuQos() {
        CpuQos cpuQos = new CpuQos();
        cpuQos.setCpuLimit(50);

        parameters.setCpuQos(cpuQos);

        vdsFunction = params -> {
            assertEquals(params.getCpuLimit().intValue(), 50);
            return true;
        };

        assertTrue(command.validate());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testCpuQosUnlimited() {
        CpuQos cpuQos = new CpuQos();
        cpuQos.setCpuLimit(null);

        parameters.setCpuQos(cpuQos);

        vdsFunction = params -> {
            assertEquals(params.getCpuLimit().intValue(), 100);
            return true;
        };

        assertTrue(command.validate());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    void assertIoTune(UpdateVmPolicyVDSParams.IoTuneParams ioTuneParams,
                      long totalBytesSec, long readBytesSec, long writeBytesSec,
                      long totalIopsSec, long readIopsSec, long writeIopsSec) {
        assertEquals(ioTuneParams.getDomainId(), DISK_DOMAIN_ID.toString());
        assertEquals(ioTuneParams.getPoolId(), DISK_POOL_ID.toString());
        assertEquals(ioTuneParams.getImageId(), DISK_IMAGE_ID.toString());
        assertEquals(ioTuneParams.getVolumeId(), DISK_VOLUME_ID.toString());

        Map<String, Long> ioTune = ioTuneParams.getIoTune();

        assertEquals(ioTune.get(VdsProperties.TotalBytesSec).longValue(), totalBytesSec);
        assertEquals(ioTune.get(VdsProperties.ReadBytesSec).longValue(), readBytesSec);
        assertEquals(ioTune.get(VdsProperties.WriteBytesSec).longValue(), writeBytesSec);

        assertEquals(ioTune.get(VdsProperties.TotalIopsSec).longValue(), totalIopsSec);
        assertEquals(ioTune.get(VdsProperties.ReadIopsSec).longValue(), readIopsSec);
        assertEquals(ioTune.get(VdsProperties.WriteIopsSec).longValue(), writeIopsSec);
    }

    @Test
    public void testStorageQos() {
        StorageQos storageQos = new StorageQos();
        storageQos.setMaxThroughput(100);
        storageQos.setMaxIops(60000);

        parameters.getStorageQos().put(diskImage, storageQos);

        vdsFunction = params -> {
            assertIoTune(params.getIoTuneList().get(0), 100L * 1024L * 1024L, 0, 0, 60000, 0, 0);
            return true;
        };

        assertTrue(command.validate());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }

    @Test
    public void testStorageQosUnlimited() {
        parameters.getStorageQos().put(diskImage, new StorageQos());

        vdsFunction = params -> {
            assertIoTune(params.getIoTuneList().get(0), 0, 0, 0, 0, 0, 0);
            return true;
        };

        assertTrue(command.validate());
        command.executeCommand();
        assertTrue(command.getReturnValue().getSucceeded());
    }
}
