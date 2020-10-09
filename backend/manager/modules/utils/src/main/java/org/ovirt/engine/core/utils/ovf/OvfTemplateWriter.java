package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Version;

public class OvfTemplateWriter extends OvfOvirtWriter {
    protected VmTemplate vmTemplate;

    public OvfTemplateWriter(FullEntityOvfData fullEntityOvfData, Version version, OsRepository osRepository) {
        super(fullEntityOvfData, version, osRepository);
        this.vmTemplate = (VmTemplate) fullEntityOvfData.getVmBase();
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(TEMPLATE_ID, vmTemplate.getId().toString());
        OriginType originType = vmTemplate.getOrigin();
        _writer.writeElement(ORIGIN, originType == null ? "" : String.valueOf(originType.getValue()));
        _writer.writeElement(TEMPLATE_DEFAULT_DISPLAY_TYPE,
                String.valueOf(vmTemplate.getDefaultDisplayType().getValue()));
        _writer.writeElement(IS_DISABLED, String.valueOf(vmTemplate.isDisabled()));
        _writer.writeElement(TRUSTED_SERVICE, String.valueOf(vmTemplate.isTrustedService()));
        _writer.writeElement(TEMPLATE_TYPE, vmTemplate.getTemplateType().name());
        _writer.writeElement(BASE_TEMPLATE_ID, vmTemplate.getBaseTemplateId().toString());
        _writer.writeElement(TEMPLATE_VERSION_NUMBER, String.valueOf(vmTemplate.getTemplateVersionNumber()));
        _writer.writeElement(TEMPLATE_VERSION_NAME, vmTemplate.getTemplateVersionName());
        _writer.writeElement(TEMPLATE_IS_SEALED, String.valueOf(vmTemplate.isSealed()));
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(vmTemplate, getVersion());
    }

    @Override
    protected void writeAppList() {
    }

    @Override
    protected void writeMacAddress(VmNetworkInterface iface) {
    }
}
