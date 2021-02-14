package org.ovirt.engine.core.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveVdsCommandTest extends BaseCommandTest {

    private static final AnsibleReturnValue ANSIBLE_RETURN_OK = new AnsibleReturnValue(AnsibleReturnCode.OK);

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VmStaticDao vmStaticDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private Cluster cluster;

    @Mock
    private GlusterBrickDao glusterBrickDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private GlusterUtil glusterUtils;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterHooksDao hooksDao;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    public AuditLogDirector auditLogDirector;

    @Mock
    private AnsibleExecutor ansibleExecutor;

    @Mock
    private VdsStaticDao vdsStaticDao;

    @Mock
    private VdsDynamicDao vdsDynamicDao;

    @Mock
    private VdsStatisticsDao vdsStatisticsDao;

    @Mock
    private TagDao tagDao;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private RemoveVdsCommand<RemoveVdsParameters> command =
            new RemoveVdsCommand<>(new RemoveVdsParameters(Guid.newGuid(), false), null);

    private Guid clusterId;

    private static Stream<Arguments> removeWithAnsiblePlaybookScenarios() {
        return Stream.of(
                Arguments.of(AnsibleReturnCode.OK,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FINISHED),
                Arguments.of(AnsibleReturnCode.UNREACHABLE,
                        VDSStatus.Down,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FINISHED),
                Arguments.of(AnsibleReturnCode.PARSE_ERROR, //https://github.com/ansible/ansible/issues/19720
                        VDSStatus.Down,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FINISHED),
                Arguments.of(AnsibleReturnCode.UNREACHABLE,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.BAD_OPTIONS,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.ERROR,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.FAIL,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.PARSE_ERROR,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.UNEXPECTED_ERROR,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED),
                Arguments.of(AnsibleReturnCode.USER_INTERRUPTED,
                        VDSStatus.Maintenance,
                        AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED)
        );
    }

    private static Stream<Arguments> validVDSStatusesForRemoval() {
        return Stream.of(
                Arguments.of(VDSStatus.NonResponsive),
                Arguments.of(VDSStatus.Maintenance),
                Arguments.of(VDSStatus.Unassigned),
                Arguments.of(VDSStatus.InstallFailed),
                Arguments.of(VDSStatus.PendingApproval),
                Arguments.of(VDSStatus.NonOperational),
                Arguments.of(VDSStatus.InstallingOS),
                Arguments.of(VDSStatus.Down));
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> invalidVDSStatusesForRemoval() {
        Set<VDSStatus> validStatuses = validVDSStatusesForRemoval()
                .map(arg -> (VDSStatus) arg.get()[0])
                .collect(Collectors.toSet());
        return Stream.of(VDSStatus.values())
                .filter(status -> !validStatuses.contains(status))
                .map(Arguments::of);
    }

    @BeforeEach
    public void setUp() {
        clusterId = Guid.newGuid();
        doReturn(cluster).when(clusterDao).get(any());
        when(ansibleExecutor.runCommand(any(AnsibleCommandConfig.class))).thenReturn(ANSIBLE_RETURN_OK);
        when(glusterUtils.getUpServer(clusterId)).thenReturn(getVds(VDSStatus.Up));
    }

    private void mockHasMultipleClusters(Boolean isMultiple) {
        when(command.getClusterId()).thenReturn(clusterId);
        when(clusterUtils.hasMultipleServers(clusterId)).thenReturn(isMultiple);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    @Test
    public void validateSucceeds() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailsWhenGlusterHostHasVolumes() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void validateFailsWhenGlusterMultipleHostHasVolumesWithForce() {
        command.getParameters().setForceAction(true);
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockHasMultipleClusters(true);
        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void validateSucceedsWithForceOption() {
        command.getParameters().setForceAction(true);
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @ParameterizedTest(name = "Status {0} is valid for removal")
    @MethodSource("validVDSStatusesForRemoval")
    public void validateHostStatusSucceeds(VDSStatus vdsStatus) {
        mockVdsWithStatus(vdsStatus);
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @ParameterizedTest(name = "Status {0} is invalid for removal")
    @MethodSource("invalidVDSStatusesForRemoval")
    public void validateHostStatusFails(VDSStatus vdsStatus) {
        mockVdsWithStatus(vdsStatus);
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL);
        List<String> validationMessages = command.getReturnValue().getValidationMessages();
        assertThat(validationMessages).containsExactly(EngineMessage.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL.name());
    }

    @Test
    public void validateFailsWhenVMsPinnedToHost() {
        mockVdsWithStatus(VDSStatus.Maintenance);

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        VM vm = new VM();
        vm.setName("abc");
        vm.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        mockVmsPinnedToHost(Collections.singletonList(vm));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS);

        boolean foundMessage = false;
        for (String message : command.getReturnValue().getValidationMessages()) {
            foundMessage |= message.contains(vm.getName());
        }

        assertTrue(foundMessage, "Can't find VM name in validate messages");
    }

    @Test
    public void validateFailsWhenVMsAssingedWithHostDevices() {
        mockVdsWithStatus(VDSStatus.Maintenance);

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setName("abc");
        vm.setMigrationSupport(MigrationSupport.MIGRATABLE);
        mockVmsPinnedToHost(Collections.singletonList(vm));
        mockVmsAssignedWithHostDevice(vm.getId(), Collections.singletonList(null));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DETECTED_ASSIGNED_HOST_DEVICES);

        boolean foundMessage = false;
        for (String message : command.getReturnValue().getValidationMessages()) {
            foundMessage |= message.contains(vm.getName());
        }

        assertTrue(foundMessage, "Can't find VM name in validate messages");
    }

    @Test
    public void removeWhenMultipleHosts() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(true);
        mockVmsPinnedToHost(Collections.emptyList());

        command.executeCommand();

        assertHostRemoved(true);
    }

    @Test
    public void removeLastHost() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(false);
        mockVmsPinnedToHost(Collections.emptyList());

        command.executeCommand();

        assertHostRemoved(false);
    }

    @ParameterizedTest(name = "Ansible remove playbook execution completed with {0} for vds with status {1} should be audited as {2}")
    @MethodSource("removeWithAnsiblePlaybookScenarios")
    public void auditAnsibleRemoveVdsPlaybookExecution(AnsibleReturnCode ansibleReturnCode,
                                                       VDSStatus vdsStatus,
                                                       AuditLogType  expectedAuditLogType) {
        mockVdsWithStatus(vdsStatus);
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(false);
        mockVmsPinnedToHost(Collections.emptyList());
        AnsibleReturnValue ansibleReturnValue = new AnsibleReturnValue(ansibleReturnCode);
        ansibleReturnValue.setLogFile(Paths.get("ansible_unit_test.log"));
        when(ansibleExecutor.runCommand(any(AnsibleCommandConfig.class))).thenReturn(ansibleReturnValue);

        command.executeCommand();

        verify(auditLogDirector).log(any(AuditLogable.class), eq(expectedAuditLogType));
        assertHostRemoved(false);
    }

    private void assertHostRemoved(boolean multipleHosts) {
        assertEquals(AuditLogType.USER_REMOVE_VDS, command.getAuditLogTypeValue());
        verify(vdsStaticDao).remove(command.getParameters().getVdsId());
        verify(vdsDynamicDao).remove(command.getParameters().getVdsId());
        verify(vdsStatisticsDao).remove(command.getParameters().getVdsId());
        verify(tagDao).detachVdsFromAllTags(command.getParameters().getVdsId());
        VerificationMode multipleHostsRemovedVerificationMode = multipleHosts ? never() : times(1);
        verify(volumeDao, multipleHostsRemovedVerificationMode).removeByClusterId(any());
        verify(hooksDao, multipleHostsRemovedVerificationMode).removeAllInCluster(any());
    }

    private void mockVmsAssignedWithHostDevice(Guid vmId, List<VmDevice> devices) {
        when(vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.HOSTDEV)).thenReturn(devices);
    }

    /**
     * Mocks that the given VMs are pinned to the host (List can be empty, but by the API contract can't be
     * <code>null</code>).
     *
     * @param emptyList
     *            The list of VM names.
     */
    private void mockVmsPinnedToHost(List<VM> vms) {
        when(vmDao.getAllPinnedToHost(command.getParameters().getVdsId())).thenReturn(vms);
    }

    /**
     * Mocks that a {@link VDS} with the given status is returned.
     *
     * @param status
     *            The status of the VDS.
     */
    private void mockVdsWithStatus(VDSStatus status) {
        VDS vds = new VDS();
        vds.setStatus(status);
        vds.setId(command.getParameters().getVdsId());
        when(vdsDao.get(command.getParameters().getVdsId())).thenReturn(vds);
    }

    /**
     * Mock that {@link org.ovirt.engine.core.common.businessentities.Cluster} with the given glusterservice status is returned
     *
     * @param glusterService
     *              The Cluster with the given glusterservice status
     */
    private void mockIsGlusterEnabled(boolean glusterService) {
        when(cluster.supportsGlusterService()).thenReturn(glusterService);
    }

    /**
     * Mock that whether the VDS configured with gluster volume. This will return the given volume count
     */
    private void mockHasVolumeOnServer(boolean isBricksRequired) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        if (isBricksRequired) {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setVolumeId(Guid.newGuid());
            brick.setServerId(command.getVdsId());
            bricks.add(brick);
        }
        when(glusterBrickDao.getGlusterVolumeBricksByServerId(command.getVdsId())).thenReturn(bricks);
    }
}
