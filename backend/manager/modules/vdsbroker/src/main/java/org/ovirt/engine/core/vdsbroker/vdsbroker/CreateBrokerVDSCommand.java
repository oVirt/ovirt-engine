package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateBrokerVDSCommand<P extends CreateVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateBrokerVDSCommand.class);

    protected VM vm;
    protected Map<String, Object> createInfo;
    protected VmInfoBuilder builder;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    public CreateBrokerVDSCommand(P parameters) {
        super(parameters, parameters.getVm().getId());
        vm = parameters.getVm();
        createInfo = new HashMap<>();
        builder = createBuilder();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildVmData();
        log.info("VM {}", createInfo);
        if ((boolean) Config.getValue(ConfigValues.DomainXML)) {
            LibvirtVmXmlBuilder builder = Injector.injectMembers(new LibvirtVmXmlBuilder(
                    createInfo,
                    vm,
                    getRunOncePayload()));
            String libvirtXml = builder.build();
            String prettyLibvirtXml = prettify(libvirtXml);
            if (prettyLibvirtXml != null) {
                log.info("VM {}", prettyLibvirtXml);
            }
            createInfo.put("xml", libvirtXml);
        }
        vmReturn = getBroker().create(createInfo);
        proceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vm.getDynamicData(),
                vmReturn.vm, getVds());
    }

    public static String prettify(String input) {
        Source xmlInput = new StreamSource(new StringReader(input));
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, new StreamResult(stringWriter));
            return stringWriter.toString().replace("\r\n", "\n");
        } catch (Exception ex) {
            log.error("Failed to produce pretty-print of {}", input);
            log.error("Exception:", ex);
            return null;
        }
    }

    private VmInfoBuilder createBuilder() {
        final VmInfoBuilderFactory vmInfoBuilderFactory = Injector.get(VmInfoBuilderFactory.class);
        return vmInfoBuilderFactory.createVmInfoBuilder(vm, getParameters().getVdsId(), createInfo);
    }

    private void buildVmData() {
        builder.buildVmProperties(getParameters().getHibernationVolHandle());
        builder.buildVmVideoCards();
        builder.buildVmGraphicsDevices();
        builder.buildVmCD(getParameters().getVmPayload());
        builder.buildVmFloppy(getParameters().getVmPayload());
        builder.buildVmDrives();
        builder.buildVmNetworkInterfaces(getParameters().getPassthroughVnicToVfMap());
        builder.buildVmNetworkCluster();
        builder.buildVmBootSequence();
        builder.buildVmBootOptions();
        builder.buildVmSoundDevices();
        builder.buildVmConsoleDevice();
        builder.buildVmTimeZone();
        builder.buildVmUsbDevices();
        builder.buildVmMemoryBalloon();
        builder.buildVmWatchdog();
        builder.buildVmVirtioScsi();
        builder.buildVmVirtioSerial();
        builder.buildVmRngDevice();
        builder.buildUnmanagedDevices();
        builder.buildVmSerialNumber();
        builder.buildVmNumaProperties();
        builder.buildVmHostDevices();

        switch (getParameters().getInitializationType()) {
        case Sysprep:
            String sysPrepContent = SysprepHandler.getSysPrep(
                    getParameters().getVm(),
                    getParameters().getSysPrepParams());

            if (!"".equals(sysPrepContent)) {
                builder.buildSysprepVmPayload(sysPrepContent);
            }
            break;

        case CloudInit:
            CloudInitHandler cloudInitHandler = new CloudInitHandler(getParameters().getVm().getVmInit());
            Map<String, byte[]> cloudInitContent;
            try {
                cloudInitContent = cloudInitHandler.getFileData();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build cloud-init data:", e);
            }

            if (cloudInitContent != null && !cloudInitContent.isEmpty()) {
                builder.buildCloudInitVmPayload(cloudInitContent);
            }
            break;

        case None:
        }
    }

    private VmDevice getRunOncePayload() {
        switch (getParameters().getInitializationType()) {
        case Sysprep:
            String sysPrepContent = SysprepHandler.getSysPrep(
                    getParameters().getVm(),
                    getParameters().getSysPrepParams());

            return (!"".equals(sysPrepContent)) ?
                    vmInfoBuildUtils.createSysprepPayloadDevice(sysPrepContent, getParameters().getVm())
                    : null;

        case CloudInit:
            CloudInitHandler cloudInitHandler = new CloudInitHandler(getParameters().getVm().getVmInit());
            Map<String, byte[]> cloudInitContent;
            try {
                cloudInitContent = cloudInitHandler.getFileData();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build cloud-init data:", e);
            }

            return (cloudInitContent != null && !cloudInitContent.isEmpty()) ?
                    vmInfoBuildUtils.createCloudInitPayloadDevice(cloudInitContent, getParameters().getVm())
                    : null;

        case None:
        default:
            return null;
        }
    }
}
