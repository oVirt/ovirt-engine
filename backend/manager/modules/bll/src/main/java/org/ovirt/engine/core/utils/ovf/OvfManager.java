package org.ovirt.engine.core.utils.ovf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OvfManager {

    private Logger log = LoggerFactory.getLogger(OvfManager.class);

    @Inject
    private OvfVmIconDefaultsProvider iconDefaultsProvider;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private OsRepository osRepository;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    DiskDao diskDao;

    public String exportVm(VM vm, FullEntityOvfData fullEntityOvfData, Version version) {
        updateBootOrderOnDevices(vm.getStaticData(), false);
        final OvfVmWriter vmWriter;
        if (vm.isHostedEngine()) {
            Cluster cluster = clusterDao.get(vm.getClusterId());
            String cpuVerb = cluster.getCpuVerb();
            String emulatedMachine = ClusterEmulatedMachines.forChipset(
                    cluster.getEmulatedMachine(), vm.getBiosType().getChipsetType());
            vmWriter = new HostedEngineOvfWriter(vm,
                    fullEntityOvfData,
                    version,
                    emulatedMachine,
                    cpuVerb,
                    getOsRepository(),
                    generateEngineXml(vm, cpuVerb, emulatedMachine));
        } else {
            vmWriter = new OvfVmWriter(vm, fullEntityOvfData, version, getOsRepository(), getMemoryDiskForSnapshots(vm));
        }
        return vmWriter.build().getStringRepresentation();
    }

    private Map<Guid, DiskImage> getMemoryDiskForSnapshots(VM vm) {
        return MemoryUtils.getMemoryDiskIdsFromSnapshots(vm.getSnapshots()).stream()
                .collect(Collectors.toMap(Function.identity(), d -> (DiskImage) diskDao.get(d)));
    }

    public String exportTemplate(FullEntityOvfData fullEntityOvfData, Version version) {
        updateBootOrderOnDevices(fullEntityOvfData.getVmBase(), true);
        return new OvfTemplateWriter(fullEntityOvfData, version, getOsRepository()).build().getStringRepresentation();
    }

    public String exportOva(VM vm, FullEntityOvfData fullEntityOvfData, Version version) {
        return new OvfOvaVmWriter(vm, fullEntityOvfData, version, getOsRepository()).build().getStringRepresentation();
    }

    public String exportOva(VmTemplate template, FullEntityOvfData fullEntityOvfData, Version version) {
        return new OvfOvaTemplateWriter(template, fullEntityOvfData, version, getOsRepository()).build().getStringRepresentation();
    }

    public void importVm(String ovfstring,
            VM vm,
            FullEntityOvfData fullEntityOvfData)
            throws OvfReaderException {
        OvfReader ovf = null;
        try {
            ovf = new OvfVmReader(new XmlDocument(ovfstring), vm, fullEntityOvfData, getOsRepository());
            ovf.build();
            initIcons(vm.getStaticData());
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
        Guid id = vm.getStaticData().getId();
        fullEntityOvfData.getInterfaces().forEach(iface -> iface.setVmId(id));
    }

    public void importVmFromOva(String ovfstring,
            VM vm,
            FullEntityOvfData fullEntityOvfData)
            throws OvfReaderException {
        OvfReader ovf = null;
        try {
            ovf = new OvfOvaVmReader(new XmlDocument(ovfstring), fullEntityOvfData, vm, getOsRepository());
            ovf.build();
            initIcons(vm.getStaticData());
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
        Guid id = vm.getStaticData().getId();
        fullEntityOvfData.getInterfaces().forEach(iface -> iface.setVmId(id));
    }

    public void importTemplate(String ovfstring, FullEntityOvfData fullEntityOvfData)
            throws OvfReaderException {
        OvfReader ovf = null;
        try {
            ovf = new OvfTemplateReader(new XmlDocument(ovfstring), fullEntityOvfData, getOsRepository());
            ovf.build();
            initIcons(fullEntityOvfData.getVmBase());
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
    }

    public void importTemplateFromOva(String ovfstring, FullEntityOvfData fullEntityOvfData)
            throws OvfReaderException {
        OvfReader ovf = null;
        try {
            ovf = new OvfOvaTemplateReader(new XmlDocument(ovfstring), fullEntityOvfData, getOsRepository());
            ovf.build();
            initIcons(fullEntityOvfData.getVmBase());
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
    }

    private String generateOvfReaderErrorMessage(OvfReader ovf, Exception ex) {
        StringBuilder message = new StringBuilder();
        if (ovf == null) {
            message.append("Error loading ovf, message ")
                .append(ex.getMessage());
        } else {
            message.append("OVF error: ")
                    .append(ovf.getName())
                    .append(": cannot read '")
                    .append(ovf.getLastReadEntry())
                    .append("' with value: ")
                    .append(ex.getMessage());
        }
        return message.toString();
    }

    private void initIcons(VmBase vmBase) {
        final int osId = vmBase.getOsId();
        final int fallbackOsId = OsRepository.DEFAULT_X86_OS;
        final Map<Integer, VmIconIdSizePair> vmIconDefaults = getIconDefaultsProvider().getVmIconDefaults();
        final VmIconIdSizePair iconPair = vmIconDefaults.containsKey(osId)
                ? vmIconDefaults.get(osId)
                : vmIconDefaults.get(fallbackOsId);
        vmBase.setSmallIconId(iconPair.getSmall());
        vmBase.setLargeIconId(iconPair.getLarge());
    }

    private void logOvfLoadError(String message, String ovfstring) {
        log.error("Error parsing OVF due to {}", message);
        log.debug("Error parsing OVF {}\n", ovfstring);
    }

    public boolean isOvfTemplate(String ovfstring) throws OvfReaderException {
        return new OvfParser(ovfstring).isTemplate();
    }

    /**
     * For backward compatibility we need to set the boot order on each device
     * in the OVF. This can be dropped as soon as we drop support for 4.0.
     * Note that this method is made public for visibility for tests outside of its package.
     */
    public void updateBootOrderOnDevices(VmBase vmBase, boolean template) {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        // Reset current boot order
        devices.forEach(device -> device.setBootOrder(0));
        Map<VmDeviceId, DiskVmElement> diskVmElements = diskVmElementDao.getAllForVm(vmBase.getId())
                .stream()
                .collect(Collectors.toMap(DiskVmElement::getId, element -> element));
        List<VmNetworkInterface> interfaces = template ?
                vmNetworkInterfaceDao.getAllForTemplate(vmBase.getId())
                : vmNetworkInterfaceDao.getAllForVm(vmBase.getId());
        VmDeviceCommonUtils.updateVmDevicesBootOrder(
                vmBase.getDefaultBootSequence(),
                devices,
                interfaces,
                diskVmElements);
    }

    private String generateEngineXml(VM vm, String cpuId, String emulatedMachine) {
        vm.setCpuName(cpuId);
        vm.setEmulatedMachine(emulatedMachine);
        return new LibvirtVmXmlBuilder(vm, vmInfoBuildUtils).buildCreateVm();
    }

    // Method exposed for mocking in unit tests
    public OsRepository getOsRepository() {
        return osRepository;
    }

    // Method exposed for mocking in unit tests
    public OvfVmIconDefaultsProvider getIconDefaultsProvider() {
        return iconDefaultsProvider;
    }
}
