package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;

public class OvfManager {
    /**
     * EINAV TODO: DateTimeFormat is currently not in use. Need to find a way the DateTime.Parse/TryParse will surely
     * work.
     */
    public static String DateTimeFormat = "dd/MM/yyy HH:mm:ss";

    public String ExportVm(VM vm, ArrayList<DiskImage> images) {
        XmlDocument document = new XmlDocument();
        OvfWriter ovf = new OvfVmWriter(document, vm, images);
        try {
            BuildOvf(ovf);
        } finally {
            ovf.dispose();
        }
        // document.outerxml will be valid only out of the using block
        // because the Dispose closing the document
        return document.OuterXml;
    }

    public String ExportTemplate(VmTemplate vmTemplate, List<DiskImage> images) {
        XmlDocument document = new XmlDocument();
        OvfWriter ovf = new OvfTemplateWriter(document, vmTemplate, images);
        try {
            BuildOvf(ovf);
        } finally {
            ovf.dispose();
        }
        // document.outerxml will be valid only out of the using block
        // because the Dispose closing the document
        return document.OuterXml;
    }

    public void ImportVm(String ovfstring,
            VM vm,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {
        XmlDocument document = new XmlDocument();
        document.LoadXml(ovfstring);


        OvfReader ovf = null;
        try {
            ovf = new OvfVmReader(document, vm, images, interfaces);
            BuildOvf(ovf);
        } catch (Exception ex) {
            String name = (ovf == null) ? OvfVmReader.EmptyName : ovf.getName();
            throw new OvfReaderException("Error parsing OVF:\r\n\r\n" + ovfstring, ex, name);
        }
        Guid id = vm.getStaticData().getId();
        for (VmNetworkInterface iface : interfaces) {
            iface.setVmId(id);
        }
    }

    public void ImportTemplate(String ovfstring, VmTemplate vmTemplate,
            ArrayList<DiskImage> images, ArrayList<VmNetworkInterface> interfaces)
            throws OvfReaderException {
        XmlDocument document = new XmlDocument();
        document.LoadXml(ovfstring);

        OvfReader ovf = null;
        try {
            ovf = new OvfTemplateReader(document, vmTemplate, images, interfaces);
            BuildOvf(ovf);
        } catch (Exception ex) {
            String name = (ovf == null) ? OvfVmReader.EmptyName : ovf.getName();
            throw new OvfReaderException("Error parsing OVF:\r\n\r\n" + ovfstring, ex, name);
        }
    }

    public boolean IsOvfTemplate(String ovfstring) {
        return new OvfParser(ovfstring).IsTemplate();
    }

    private void BuildOvf(IOvfBuilder builder) {
        builder.buildReference();
        builder.buildNetwork();
        builder.buildDisk();
        builder.buildVirtualSystem();
    }
}
