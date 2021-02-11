package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.XmlUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateBrokerVDSCommand<P extends CreateVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateBrokerVDSCommand.class);

    protected VM vm;

    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    private SysprepHandler sysprepHandler;
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;
    @Inject
    protected OsRepository osRepository;

    public CreateBrokerVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmReturn = getBroker().create(createInfo());
        proceedProxyReturnValue();
        vdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                vmReturn.vm, getVds());
    }

    private Map<String, Object> createInfo() {
        Map<String, Object> createInfo = new HashMap<>();
        createInfo.put(VdsProperties.vm_guid, vm.getId().toString()); // deprecated: can be removed once we stop support 4.3
        createInfo.put(VdsProperties.engineXml, generateDomainXml());

        if (getParameters().getMemoryDumpImage() != null) {
            DiskImage memoryDump = getParameters().getMemoryDumpImage();
            Map<String, String> memoryDumpPDIV = PDIVMapBuilder.create()
                    .setPoolId(memoryDump.getStoragePoolId())
                    .setDomainId(memoryDump.getStorageIds().get(0))
                    .setImageGroupId(memoryDump.getId())
                    .setVolumeId(memoryDump.getImageId())
                    .build();

            DiskImage memoryConf = getParameters().getMemoryConfImage();
            Map<String, String> memoryConfPDIV = PDIVMapBuilder.create()
                    .setPoolId(memoryConf.getStoragePoolId())
                    .setDomainId(memoryConf.getStorageIds().get(0))
                    .setImageGroupId(memoryConf.getId())
                    .setVolumeId(memoryConf.getImageId())
                    .build();

            createInfo.put("memoryDumpVolume", memoryDumpPDIV);
            createInfo.put("memoryConfVolume", memoryConfPDIV);
        }

        String tpmData = vmInfoBuildUtils.tpmData(vm.getId());
        if (tpmData != null) {
            createInfo.put(VdsProperties.tpmData, tpmData);
        }
        if (vm.getBiosType() == BiosType.Q35_SECURE_BOOT) {
            String nvramData = vmInfoBuildUtils.nvramData(vm.getId());
            if (nvramData != null) {
                createInfo.put(VdsProperties.nvramData, nvramData);
            }
        }

        return createInfo;
    }

    private String generateDomainXml() {
        LibvirtVmXmlBuilder builder = new LibvirtVmXmlBuilder(
                vm,
                getVds().getId(),
                getPayload(),
                getVds().getCpuThreads(),
                getParameters().isVolatileRun(),
                getParameters().getPassthroughVnicToVfMap(),
                vmInfoBuildUtils);
        String libvirtXml = builder.buildCreateVm();
        String prettyLibvirtXml = XmlUtils.prettify(libvirtXml);
        if (prettyLibvirtXml != null) {
            log.info("VM {}", prettyLibvirtXml);
        }
        return libvirtXml;
    }

    private VmDevice getPayload() {
        switch (getParameters().getInitializationType()) {
        case Sysprep:
            String sysPrepContent = sysprepHandler.getSysPrep(
                    getParameters().getVm(),
                    getParameters().getSysPrepParams());

            return (!"".equals(sysPrepContent)) ?
                    vmInfoBuildUtils.createSysprepPayloadDevice(sysPrepContent, getParameters().getVm())
                    : null;

        case CloudInit:
            Map<String, byte[]> cloudInitContent;
            try {
                cloudInitContent = vmInfoBuildUtils.buildPayloadCloudInit(getParameters().getVm().getVmInit());
            } catch (Exception e) {
                throw new RuntimeException("Failed to build cloud-init data:", e);
            }

            return (cloudInitContent != null && !cloudInitContent.isEmpty()) ?
                    vmInfoBuildUtils.createCloudInitPayloadDevice(cloudInitContent, getParameters().getVm())
                    : null;

        case Ignition:
            Map<String, byte[]> ignitionContent;
            try {
                String[] version = osRepository.getVmInitMap().get(getParameters().getVm().getVmOsId()).split("_");
                Version ver = version.length <= 1 ? null : new Version(version[1]);
                ignitionContent = vmInfoBuildUtils.buildPayloadIgnition(getParameters().getVm().getVmInit(), ver);
            } catch (Exception e) {
                throw new RuntimeException("Failed to build ignition data:", e);
            }

            return (ignitionContent != null && !ignitionContent.isEmpty()) ?
                    vmInfoBuildUtils.createCloudInitPayloadDevice(ignitionContent, getParameters().getVm())
                    : null;

        case None:
        default:
            return getParameters().getVmPayload();
        }
    }
}
