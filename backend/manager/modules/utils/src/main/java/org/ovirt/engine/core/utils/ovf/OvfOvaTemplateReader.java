package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class OvfOvaTemplateReader extends OvfOvaReader {

    private VmTemplate template;

    public OvfOvaTemplateReader(XmlDocument document,
            FullEntityOvfData fullEntityOvfData,
            OsRepository osRepository) {
        super(document, fullEntityOvfData, fullEntityOvfData.getVmTemplate(), osRepository);
        this.template = fullEntityOvfData.getVmTemplate();
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        super.readGeneralData(content);
        consumeReadProperty(content, IS_DISABLED, val -> template.setDisabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TRUSTED_SERVICE, val -> template.setTrustedService(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TEMPLATE_TYPE, val -> template.setTemplateType(VmEntityType.valueOf(val)));
        consumeReadProperty(content, TEMPLATE_IS_SEALED, val -> template.setSealed(Boolean.parseBoolean(val)));

        consumeReadProperty(content,
                BASE_TEMPLATE_ID,
                val -> template.setBaseTemplateId(Guid.createGuidFromString(val)),
                // in case base template is missing, we assume it is a base template
                () -> template.setBaseTemplateId(template.getId()));
        consumeReadProperty(content,
                TEMPLATE_VERSION_NUMBER,
                val -> template.setTemplateVersionNumber(Integer.parseInt(val)));
        consumeReadProperty(content, TEMPLATE_VERSION_NAME, val -> template.setTemplateVersionName(val));
    }

    @Override
    protected void setClusterArch(ArchitectureType arch) {
        template.setClusterArch(arch);
    }
}
