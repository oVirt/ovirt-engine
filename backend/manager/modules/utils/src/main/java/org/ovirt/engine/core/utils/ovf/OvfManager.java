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
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvfManager {

    private Logger log = LoggerFactory.getLogger(OvfManager.class);
    private OvfVmIconDefaultsProvider iconDefaultsProvider = SimpleDependecyInjector.getInstance().get(
            OvfVmIconDefaultsProvider.class);

    public String ExportVm(VM vm, ArrayList<DiskImage> images, Version version) {
        OvfWriter ovf = new OvfVmWriter(vm, images, version);
        BuildOvf(ovf);

        return ovf.getStringRepresentation();
    }

    public String ExportTemplate(VmTemplate vmTemplate, List<DiskImage> images, Version version) {
        OvfWriter ovf = new OvfTemplateWriter(vmTemplate, images, version);
        BuildOvf(ovf);

        return ovf.getStringRepresentation();
    }

    public void ImportVm(String ovfstring,
            VM vm,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {

        OvfReader ovf = null;
        try {
            ovf = new OvfVmReader(new XmlDocument(ovfstring), vm, images, interfaces);
            BuildOvf(ovf);
            initIcons(vm.getStaticData());
        } catch (Exception ex) {
            logOvfLoadError(ex.getMessage(), ovfstring);
            throw new OvfReaderException( ex, ovf != null ? ovf.getName() : null);
        }
        Guid id = vm.getStaticData().getId();
        for (VmNetworkInterface iface : interfaces) {
            iface.setVmId(id);
        }
    }

    public void ImportTemplate(String ovfstring, VmTemplate vmTemplate,
            ArrayList<DiskImage> images, ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {

        OvfReader ovf = null;
        try {
            ovf = new OvfTemplateReader(new XmlDocument(ovfstring), vmTemplate, images, interfaces);
            BuildOvf(ovf);
            initIcons(vmTemplate);
        } catch (Exception ex) {
            logOvfLoadError(ex.getMessage(), ovfstring);
            throw new OvfReaderException(ex, ovf != null ? ovf.getName() : null);
        }
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

    public boolean IsOvfTemplate(String ovfstring) throws OvfReaderException {
        return new OvfParser(ovfstring).IsTemplate();
    }

    private void BuildOvf(IOvfBuilder builder) {
        builder.buildReference();
        builder.buildNetwork();
        builder.buildDisk();
        builder.buildVirtualSystem();
    }
}
