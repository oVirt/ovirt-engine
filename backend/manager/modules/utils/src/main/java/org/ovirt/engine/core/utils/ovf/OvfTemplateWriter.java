package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Version;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate _vmTemplate;

    public OvfTemplateWriter(VmTemplate vmTemplate, List<DiskImage> images, Version version) {
        super(vmTemplate, images, version);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeStartElement(OvfProperties.NAME);
        _writer.writeRaw(_vmTemplate.getName());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_ID);
        _writer.writeRaw(_vmTemplate.getId().toString());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.ORIGIN);
        _writer.writeRaw(_vmTemplate.getOrigin() == null ? "" : String.valueOf(_vmTemplate.getOrigin().getValue()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE);
        _writer.writeRaw(String.valueOf(_vmTemplate.getDefaultDisplayType().getValue()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.IS_DISABLED);
        _writer.writeRaw(String.valueOf(_vmTemplate.isDisabled()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TRUSTED_SERVICE);
        _writer.writeRaw(String.valueOf(_vmTemplate.isTrustedService()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_TYPE);
        _writer.writeRaw(_vmTemplate.getTemplateType().name());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.BASE_TEMPLATE_ID);
        _writer.writeRaw(_vmTemplate.getBaseTemplateId().toString());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_VERSION_NUMBER);
        _writer.writeRaw(String.valueOf(_vmTemplate.getTemplateVersionNumber()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_VERSION_NAME);
        _writer.writeRaw(_vmTemplate.getTemplateVersionName());
        _writer.writeEndElement();
        _writer.writeStartElement("AutoStartup"); // aka highly available
        _writer.writeRaw(String.valueOf(_vmTemplate.isAutoStartup()));
        _writer.writeEndElement();
    }

    @Override
    protected void writeAppList() {
    }

    @Override
    protected void writeMacAddress(VmNetworkInterface iface) {
    }
}
