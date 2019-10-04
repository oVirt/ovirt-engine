package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
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
        String engineXml = generateDomainXml();
        if (getParameters().getMemoryDumpImage() == null) {
            return Collections.singletonMap(VdsProperties.engineXml, engineXml);
        } else {
            Map<String, Object> createInfo = new HashMap<>(4);
            createInfo.put(VdsProperties.engineXml, engineXml);

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
            return createInfo;
        }
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
                cloudInitContent = vmInfoBuildUtils.buildPayload(getParameters().getVm().getVmInit());
            } catch (Exception e) {
                throw new RuntimeException("Failed to build cloud-init data:", e);
            }

            return (cloudInitContent != null && !cloudInitContent.isEmpty()) ?
                    vmInfoBuildUtils.createCloudInitPayloadDevice(cloudInitContent, getParameters().getVm())
                    : null;

        case None:
        default:
            return getParameters().getVmPayload();
        }
    }
}
