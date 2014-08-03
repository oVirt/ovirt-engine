package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class OvfManager {

    private Log log = LogFactory.getLog(OvfManager.class);

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
        } catch (Exception ex) {
            logOvfLoadError(ex.getMessage(), ovfstring);
            throw new OvfReaderException(ex, ovf != null ? ovf.getName() : null);
        }
    }

    private void logOvfLoadError(String message, String ovfstring) {
        log.errorFormat("Error parsing OVF due to {0}", message);
        log.debugFormat("Error parsing OVF {0}\n", ovfstring);
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
