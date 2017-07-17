package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Version;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate vmTemplate;

    public OvfTemplateWriter(VmTemplate vmTemplate, List<DiskImage> images, Version version, OsRepository osRepository) {
        super(vmTemplate, images, version, osRepository);
        this.vmTemplate = vmTemplate;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(OvfProperties.TEMPLATE_ID, vmTemplate.getId().toString());
        OriginType originType = vmTemplate.getOrigin();
        _writer.writeElement(OvfProperties.ORIGIN, originType == null ? "" : String.valueOf(originType.getValue()));
        _writer.writeElement(OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE,
                String.valueOf(vmTemplate.getDefaultDisplayType().getValue()));
        _writer.writeElement(OvfProperties.IS_DISABLED, String.valueOf(vmTemplate.isDisabled()));
        _writer.writeElement(OvfProperties.TRUSTED_SERVICE, String.valueOf(vmTemplate.isTrustedService()));
        _writer.writeElement(OvfProperties.TEMPLATE_TYPE, vmTemplate.getTemplateType().name());
        _writer.writeElement(OvfProperties.BASE_TEMPLATE_ID, vmTemplate.getBaseTemplateId().toString());
        _writer.writeElement(OvfProperties.TEMPLATE_VERSION_NUMBER,
                String.valueOf(vmTemplate.getTemplateVersionNumber()));
        _writer.writeElement(OvfProperties.TEMPLATE_VERSION_NAME, vmTemplate.getTemplateVersionName());
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
