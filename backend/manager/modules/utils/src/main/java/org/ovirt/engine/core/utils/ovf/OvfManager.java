package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvfManager {

    private Logger log = LoggerFactory.getLogger(OvfManager.class);
    private OvfVmIconDefaultsProvider iconDefaultsProvider = SimpleDependencyInjector.getInstance().get(
            OvfVmIconDefaultsProvider.class);

    public String exportVm(VM vm, ArrayList<DiskImage> images, Version version) {
        OvfWriter ovf = new OvfVmWriter(vm, images, version);
        buildOvf(ovf);

        return ovf.getStringRepresentation();
    }

    public String exportTemplate(VmTemplate vmTemplate, List<DiskImage> images, Version version) {
        OvfWriter ovf = new OvfTemplateWriter(vmTemplate, images, version);
        buildOvf(ovf);

        return ovf.getStringRepresentation();
    }

    public void importVm(String ovfstring,
            VM vm,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {

        OvfReader ovf = null;
        try {
            ovf = new OvfVmReader(new XmlDocument(ovfstring), vm, images, interfaces);
            buildOvf(ovf);
            initIcons(vm.getStaticData());
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
        Guid id = vm.getStaticData().getId();
        for (VmNetworkInterface iface : interfaces) {
            iface.setVmId(id);
        }
    }

    public void importTemplate(String ovfstring, VmTemplate vmTemplate,
            ArrayList<DiskImage> images, ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {

        OvfReader ovf = null;
        try {
            ovf = new OvfTemplateReader(new XmlDocument(ovfstring), vmTemplate, images, interfaces);
            buildOvf(ovf);
            initIcons(vmTemplate);
        } catch (Exception ex) {
            String message = generateOvfReaderErrorMessage(ovf, ex);
            logOvfLoadError(message, ovfstring);
            throw new OvfReaderException(message);
        }
    }

    private String generateOvfReaderErrorMessage(OvfReader ovf, Exception ex) {
        StringBuilder message = new StringBuilder();
        if (ovf == null) {
            message.append("Error loading ovf, message")
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
        final Map<Integer, VmIconIdSizePair> vmIconDefaults = iconDefaultsProvider.getVmIconDefaults();
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

    private void buildOvf(IOvfBuilder builder) {
        builder.buildReference();
        builder.buildNetwork();
        builder.buildDisk();
        builder.buildVirtualSystem();
    }
}
