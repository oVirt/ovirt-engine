package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Version;

public class OvfOvaTemplateWriter extends OvfOvaWriter {

    private VmTemplate template;

    public OvfOvaTemplateWriter(VmTemplate template, FullEntityOvfData fullEntityOvfData, Version version, OsRepository osRepository) {
        super(template, fullEntityOvfData.getDiskImages(), version, template.getVmExternalData(), osRepository);
        this.template = template;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(TEMPLATE_ID, template.getId().toString());
        OriginType originType = template.getOrigin();
        _writer.writeElement(ORIGIN, originType == null ? "" : String.valueOf(originType.getValue()));
        _writer.writeElement(VM_DEFAULT_DISPLAY_TYPE,
                String.valueOf(template.getDefaultDisplayType().getValue()));
        _writer.writeElement(IS_DISABLED, String.valueOf(template.isDisabled()));
        _writer.writeElement(TRUSTED_SERVICE, String.valueOf(template.isTrustedService()));
        _writer.writeElement(TEMPLATE_TYPE, template.getTemplateType().name());
        _writer.writeElement(BASE_TEMPLATE_ID, template.getBaseTemplateId().toString());
        _writer.writeElement(TEMPLATE_VERSION_NUMBER, String.valueOf(template.getTemplateVersionNumber()));
        _writer.writeElement(TEMPLATE_VERSION_NAME, template.getTemplateVersionName());
        _writer.writeElement(TEMPLATE_IS_SEALED, String.valueOf(template.isSealed()));
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(template, getVersion());
    }

}
