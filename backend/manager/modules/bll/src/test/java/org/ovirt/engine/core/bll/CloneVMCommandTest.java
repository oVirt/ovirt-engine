package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CloneVMCommandTest extends BaseCommandTest {

    @Mock
    private VmDao vmDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private VmTemplateDao vmTemplateDao;
    @Mock
    private DiskDao diskDao;
    @Mock
    private StorageDomainDao storageDomainDao;
    @Mock
    private VmDeviceUtils vmDeviceUtils;
    @Mock
    private VmHandler vmHandler;
    @Mock
    private VmTemplateHandler vmTemplateHandler;
    @Mock
    private BackendInternal backendInternal;

    DiskImage disk = new DiskImage();

    VM vm = new VM();

    CommandContext context = CommandContext.createContext("");

    @Spy
    @InjectMocks
    protected CloneVmCommand<CloneVmParameters> cmd = new CloneVmCommand<>(createParameters(), context);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                        new HashSet<>(Collections.singletonList(Version.v4_4)))
        );
    }

    @BeforeEach
    public void setUp() {
        VmBase vmBase = new VmBase();
        vmBase.setBiosType(BiosType.Q35_SEA_BIOS);
        doReturn(vmBase).when(cmd).getVmBase(any());
    }

    @Test
    public void testValidateIllegalDisk() {
        vm.setStatus(VMStatus.Down);
        disk.setActive(true);
        disk.setImageStatus(ImageStatus.ILLEGAL);
        initializeCommand();
        ValidateTestUtils.runAndAssertValidateFailure(cmd, EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    private void initializeCommand() {
        when(vmDao.get(any())).thenReturn(vm);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_4);
        when(clusterDao.get(any())).thenReturn(cluster);
        VmTemplate vmTemplate = new VmTemplate();
        when(vmTemplateDao.get(any())).thenReturn(vmTemplate);
        disk.setStorageIds(Collections.singletonList(Guid.newGuid()));
        List<Disk> disks = Collections.singletonList(disk);
        QueryReturnValue disksByVmId = new QueryReturnValue();
        disksByVmId.setSucceeded(true);
        disksByVmId.setReturnValue(disks);
        when(backendInternal.runInternalQuery(eq(QueryType.GetAllDisksByVmId), any(), any()))
                .thenReturn(disksByVmId);
        when(diskDao.getAllForVm(any())).thenReturn(disks);
        cmd.init();
    }

    private static CloneVmParameters createParameters() {
        CloneVmParameters parameters = new CloneVmParameters();
        parameters.setVmId(Guid.newGuid());
        parameters.setVmStaticData(new VmStatic());
        parameters.setEdited(true);
        return parameters;
    }
}
