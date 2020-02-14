package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class OvfTemplateReader extends OvfOvirtReader {
    protected VmTemplate _vmTemplate;

    public OvfTemplateReader(XmlDocument document,
                             FullEntityOvfData fullEntityOvfData,
                             OsRepository osRepository) {
        super(document, fullEntityOvfData, osRepository);
        _vmTemplate = (VmTemplate) fullEntityOvfData.getVmBase();
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        super.readGeneralData(content);
        consumeReadProperty(content, TEMPLATE_ID, val -> _vmTemplate.setId(new Guid(val)));
        consumeReadProperty(content, IS_DISABLED, val -> _vmTemplate.setDisabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TRUSTED_SERVICE, val -> _vmTemplate.setTrustedService(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TEMPLATE_TYPE, val -> _vmTemplate.setTemplateType(VmEntityType.valueOf(val)));
        consumeReadProperty(content, TEMPLATE_IS_SEALED, val -> _vmTemplate.setSealed(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                BASE_TEMPLATE_ID,
                val -> _vmTemplate.setBaseTemplateId(Guid.createGuidFromString(val)),
                () -> {
                    // in case base template is missing, we assume it is a base template
                    _vmTemplate.setBaseTemplateId(_vmTemplate.getId());
                });
        consumeReadProperty(content,
                TEMPLATE_VERSION_NUMBER,
                val -> _vmTemplate.setTemplateVersionNumber(Integer.parseInt(val)));
        consumeReadProperty(content, TEMPLATE_VERSION_NAME, val -> _vmTemplate.setTemplateVersionName(val));
        consumeReadProperty(content, AUTO_STARTUP, val -> _vmTemplate.setAutoStartup(Boolean.parseBoolean(val)));
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return TEMPLATE_DEFAULT_DISPLAY_TYPE;
    }

    protected void setClusterArch(ArchitectureType arch) {
        _vmTemplate.setClusterArch(arch);
    }
}
