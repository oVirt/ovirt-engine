package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;

public class OvfManager {
    /**
     * EINAV TODO: DateTimeFormat is currently not in use. Need to find a way the DateTime.Parse/TryParse will surely
     * work.
     */
    public static String DateTimeFormat = "dd/MM/yyy HH:mm:ss";

    public void ExportVm(RefObject<String> ovfstring, VM vm, java.util.ArrayList<DiskImage> images) {
        XmlDocument document = null;
        RefObject<XmlDocument> tempRefObject = new RefObject<XmlDocument>(document);
        OvfWriter ovf = new OvfVmWriter(tempRefObject, vm, images);
        document = tempRefObject.argvalue;
        try {
            BuildOvf(ovf);
        } finally {
            ovf.dispose();
        }
        // document.outerxml will be valid only out of the using block
        // because the Dispose closing the document
        ovfstring.argvalue = document.OuterXml;
    }

    public void ExportTemplate(RefObject<String> ovfstring, VmTemplate vmTemplate, List<DiskImage> images) {
        XmlDocument document = new XmlDocument();
        RefObject<XmlDocument> tempRefObject = new RefObject<XmlDocument>(document);
        OvfWriter ovf = new OvfTemplateWriter(tempRefObject, vmTemplate, images);
        document = tempRefObject.argvalue;
        try {
            BuildOvf(ovf);
        } finally {
            ovf.dispose();
        }
        // document.outerxml will be valid only out of the using block
        // because the Dispose closing the document
        ovfstring.argvalue = document.OuterXml;
    }

    public void ImportVm(String ovfstring, RefObject<VM> vm, RefObject<java.util.ArrayList<DiskImage>> images)
            throws OvfReaderException {
        XmlDocument document = new XmlDocument();
        document.LoadXml(ovfstring);

        vm.argvalue = new VM();
        images.argvalue = new java.util.ArrayList<DiskImage>();

        OvfReader ovf = null;
        try {
            ovf = new OvfVmReader(document, vm.argvalue, images.argvalue);
            BuildOvf(ovf);
        } catch (Exception ex) {
            String name = (ovf == null) ? OvfVmReader.EmptyName : ovf.getName();
            throw new OvfReaderException("Error parsing OVF:\r\n\r\n" + ovfstring, ex, name);
        }

        // this is static data for all images:
        for (DiskImage image : images.argvalue) {
            image.setvm_guid(vm.argvalue.getStaticData().getId());
        }
    }

    public void ImportTemplate(String ovfstring, RefObject<VmTemplate> vmTemplate,
            RefObject<java.util.ArrayList<DiskImage>> images) throws OvfReaderException {
        XmlDocument document = new XmlDocument();
        document.LoadXml(ovfstring);

        vmTemplate.argvalue = new VmTemplate();
        images.argvalue = new java.util.ArrayList<DiskImage>();

        OvfReader ovf = null;
        try {
            ovf = new OvfTemplateReader(document, vmTemplate.argvalue, images.argvalue);
            BuildOvf(ovf);
        } catch (Exception ex) {
            String name = (ovf == null) ? OvfVmReader.EmptyName : ovf.getName();
            throw new OvfReaderException("Error parsing OVF:\r\n\r\n" + ovfstring, ex, name);
        }

        // this is static data for all images:
        for (DiskImage image : images.argvalue) {
            image.setcontainer_guid(vmTemplate.argvalue.getId());
        }
    }

    public boolean IsOvfTemplate(String ovfstring) {
        return new OvfParser(ovfstring).IsTemplate();
    }

    private void BuildOvf(IOvfBuilder builder) {
        builder.BuildReference();
        builder.BuildNetwork();
        builder.BuildDisk();
        builder.BuildVirtualSystem();
    }
}
