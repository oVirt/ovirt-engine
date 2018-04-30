package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    private VmSlaPolicyParameters parameters = new VmSlaPolicyParameters(VM_ID);

    @Spy
    @InjectMocks
    private VmSlaPolicyCommand<VmSlaPolicyParameters> command = new VmSlaPolicyCommand<>(parameters, null);

    private Function<UpdateVmPolicyVDSParams, Boolean> vdsFunction;

    @BeforeEach
    public void setUp() {
        doAnswer(invocation -> {
            VDSCommandType commandType = (VDSCommandType) invocation.getArguments()[0];
            assertEquals(VDSCommandType.UpdateVmPolicy, commandType);

            UpdateVmPolicyVDSParams params = (UpdateVmPolicyVDSParams) invocation.getArguments()[1];
            VDSReturnValue retVal = new VDSReturnValue();
            retVal.setSucceeded(vdsFunction.apply(params));
            return retVal;
        }).when(command).runVdsCommand(any(), any());

        vm = new VM();
        vm.setId(VM_ID);
        vm.setStatus(VMStatus.Up);

        when(vmDao.get(VM_ID)).thenReturn(vm);

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
            assertEquals(50, params.getCpuLimit().intValue());
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
            assertEquals(100, params.getCpuLimit().intValue());
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
