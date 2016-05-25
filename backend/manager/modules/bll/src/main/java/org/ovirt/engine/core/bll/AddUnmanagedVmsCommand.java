package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AddUnmanagedVmsCommand<T extends AddUnmanagedVmsParameters> extends CommandBase<T> {
    private static final String EXTERNAL_VM_NAME_FORMAT = "external-%1$s";

    @Inject
    private Instance<HostedEngineImporter> hostedEngineImporterProvider;

    @Inject
    private VmDevicesMonitoring vmDevicesMonitoring;

    private Set<String> graphicsDeviceTypes = new HashSet<>(Arrays.asList(
            GraphicsType.SPICE.toString().toLowerCase(),
            GraphicsType.VNC.toString().toLowerCase()
    ));

    public AddUnmanagedVmsCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void init() {
        super.init();
        setVdsId(getParameters().getVdsId());
        // this command is called internally within lock so the host surely exists
        setClusterId(getVds().getClusterId());
    }

    @Override
    protected void executeCommand() {
        if (!getParameters().getVmIds().isEmpty()) {
            int defaultOsId = getDefaultOsId(getCluster().getArchitecture());
            DisplayType defaultDisplayType = getDefaultDisplayType(defaultOsId, getCluster().getCompatibilityVersion());

            // Query VDSM for VMs info, and creating a proper VMStatic to be used when importing them
            long fetchTime = System.nanoTime();
            Map<String, Object>[] vmsInfo = getVmsInfo();
            for (Map<String, Object> vmInfo : vmsInfo) {
                convertVm(defaultOsId, defaultDisplayType, fetchTime, vmInfo);
            }
        }
        setSucceeded(true);
    }

    // Visible for testing
    protected void convertVm(int defaultOsId, DisplayType defaultDisplayType, long fetchTime, Map<String, Object>
            vmInfo) {
        Guid vmId = Guid.createGuidFromString((String) vmInfo.get(VdsProperties.vm_guid));
        String vmNameOnHost = (String) vmInfo.get(VdsProperties.vm_name);

        if (isHostedEngineVm(vmId, vmNameOnHost)) {
            // its a hosted engine VM -> import it
            importHostedEngineVm(vmInfo);
            return;
        }
        VmStatic vmStatic = new VmStatic();
        vmStatic.setId(vmId);
        vmStatic.setCreationDate(new Date());
        vmStatic.setClusterId(getClusterId());
        vmStatic.setName(String.format(EXTERNAL_VM_NAME_FORMAT, vmNameOnHost));
        vmStatic.setOrigin(OriginType.EXTERNAL);
        vmStatic.setNumOfSockets(VdsBrokerObjectsBuilder.parseIntVdsProperty(vmInfo.get(VdsProperties.num_of_cpus)));
        vmStatic.setMemSizeMb(VdsBrokerObjectsBuilder.parseIntVdsProperty(vmInfo.get(VdsProperties.mem_size_mb)));
        vmStatic.setSingleQxlPci(false);

        setOsId(vmStatic, (String) vmInfo.get(VdsProperties.guest_os), defaultOsId);
        setDisplayType(vmStatic, (String) vmInfo.get(VdsProperties.displayType), defaultDisplayType);

        addExternallyManagedVm(vmStatic);
        addDevices(vmInfo, fetchTime);
        log.info("Importing VM '{}' as '{}', as it is running on the on Host, but does not exist in the engine.", vmNameOnHost, vmStatic.getName());
    }

    private List<GraphicsDevice> extractGraphicsDevices(Guid vmId, Object[] devices) {
        List<GraphicsDevice> graphicsDevices = new ArrayList<>();
        for (Object o : devices) {
            Map device = (Map<String, Object>) o;
            String deviceName = (String) device.get(VdsProperties.Device);
            if (graphicsDeviceTypes.contains(deviceName)) {
                GraphicsDevice graphicsDevice = new GraphicsDevice(VmDeviceType.valueOf(deviceName.toUpperCase()));
                graphicsDevice.setVmId(vmId);
                graphicsDevice.setDeviceId(Guid.newGuid());
                graphicsDevice.setIsManaged(true);
                graphicsDevices.add(graphicsDevice);
            }
        }
        return graphicsDevices;
    }

    private boolean isHostedEngineVm(Guid vmId, String vmNameOnHost) {
        VmStatic dbVm = getVmStaticDao().get(vmId);
        return dbVm == null ?
                Objects.equals(vmNameOnHost, Config.<String>getValue(ConfigValues.HostedEngineVmName))
                : dbVm.getOrigin() == OriginType.HOSTED_ENGINE;
    }

    /**
     * Gets VM full information for the given list of VMs.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object>[] getVmsInfo() {
        List<String> vmsToUpdate = getParameters().getVmIds().stream().map(Guid::toString).collect(Collectors.toList());
        // TODO refactor commands to use vdsId only - the whole vds object here is useless
        VDS vds = new VDS();
        vds.setId(getVdsId());
        Map<String, Object>[] result = new Map[0];
        VDSReturnValue vdsReturnValue = runVdsCommand(
                VDSCommandType.FullList,
                new FullListVDSCommandParameters(vds, vmsToUpdate));
        if (vdsReturnValue.getSucceeded()) {
            result = (Map<String, Object>[]) vdsReturnValue.getReturnValue();
        }
        return result;
    }

    // Visible for testing
    protected void importHostedEngineVm(Map<String, Object> vmStruct) {
        VM vm = VdsBrokerObjectsBuilder.buildVmsDataFromExternalProvider(vmStruct);
        if (vm != null) {
            vm.setImages(VdsBrokerObjectsBuilder.buildDiskImagesFromDevices(vmStruct, vm.getId()));
            vm.setInterfaces(VdsBrokerObjectsBuilder.buildVmNetworkInterfacesFromDevices(vmStruct));
            for (DiskImage diskImage : vm.getImages()) {
                vm.getDiskMap().put(diskImage.getId(), diskImage);
            }
            vm.setClusterId(getClusterId());
            vm.setRunOnVds(getVdsId());
            List<GraphicsDevice> graphicsDevices = extractGraphicsDevices(vm.getId(),
                    (Object[]) vmStruct.get(VdsProperties.Devices));
            if (graphicsDevices.size() == 1
                    && VmDeviceType.valueOf(graphicsDevices.get(0).getDevice().toUpperCase()) == VmDeviceType.VNC) {
                vm.setDefaultDisplayType(DisplayType.vga);
            } else {
                vm.setDefaultDisplayType(DisplayType.qxl);
            }
            // HE VM does not support that feature, disable it
            vm.setSingleQxlPci(false);
            for (GraphicsDevice graphicsDevice : graphicsDevices) {
                vm.getManagedVmDeviceMap().put(graphicsDevice.getDeviceId(), graphicsDevice);
            }
            importHostedEngineVm(vm);
        }
    }

    public void importHostedEngineVm(final VM vm) {
        ThreadPoolUtil.execute(() -> hostedEngineImporterProvider.get().doImport(vm));
    }

    // Visible for testing
    protected void addExternallyManagedVm(VmStatic vmStatic) {
        VdcReturnValueBase returnValue =
                runInternalAction(VdcActionType.AddVmFromScratch,
                        new AddVmParameters(vmStatic),
                        createAddExternalVmContext(vmStatic));
        if (!returnValue.getSucceeded()) {
            log.debug("Failed adding Externally managed VM '{}'", vmStatic.getName());
        }
    }

    // Visible for testing
    protected void addDevices(Map<String, Object> vmInfo, long fetchTime) {
        VmDevicesMonitoring.Change change = vmDevicesMonitoring.createChange(fetchTime);
        change.updateVmFromFullList(vmInfo);
        change.flush();
    }

    protected CommandContext createAddExternalVmContext(VmStatic vmStatic) {
        ExecutionContext ctx = new ExecutionContext();
        try {
            Step step = ExecutionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.ADD_VM,
                    ExecutionMessageDirector.resolveStepMessage(
                            StepEnum.ADD_VM,
                            Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), vmStatic.getName())));
            ctx.setJob(getExecutionContext().getJob());
            ctx.setStep(step);
            ctx.setMonitored(true);
        } catch (RuntimeException e) {
            log.error("Failed to create ExecutionContext for AddVmFromScratch", e);
        }
        return cloneContextAndDetachFromParent().withExecutionContext(ctx);
    }

    protected void setOsId(VmStatic vmStatic, String guestOsNameFromVdsm, int defaultArchOsId) {
        if (StringUtils.isEmpty(guestOsNameFromVdsm)) {
            log.debug("VM '{}': setting default OS ID: '{}'", vmStatic.getName(), defaultArchOsId);
            vmStatic.setOsId(defaultArchOsId);
        }
    }

    protected void setDisplayType(VmStatic vmStatic, String displayTypeFromVdsm, DisplayType defaultDisplayType) {
        if (StringUtils.isEmpty(displayTypeFromVdsm)) {
            log.debug("VM '{}': setting default display type: '{}'", vmStatic.getName(), defaultDisplayType.getValue());
            vmStatic.setDefaultDisplayType(defaultDisplayType);
        }
    }

    private int getDefaultOsId(ArchitectureType architecture) {
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        Integer defaultArchOsId = osRepository.getDefaultOSes().get(architecture);
        return (defaultArchOsId == null) ? 0 : defaultArchOsId;
    }

    private DisplayType getDefaultDisplayType(int osId, Version clusterVersion) {
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        List<Pair<GraphicsType, DisplayType>> pairs = osRepository.getGraphicsAndDisplays(osId, clusterVersion);

        if (!pairs.isEmpty()) {
            Pair<GraphicsType, DisplayType> graphicsDisplayPair = pairs.get(0);
            return graphicsDisplayPair.getSecond();
        }

        return DisplayType.qxl;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }
}
