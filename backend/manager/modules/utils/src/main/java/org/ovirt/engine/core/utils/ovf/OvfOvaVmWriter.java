package org.ovirt.engine.core.utils.ovf;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class OvfOvaVmWriter extends OvfOvaWriter {

    private VM vm;

    public OvfOvaVmWriter(VM vm, FullEntityOvfData fullEntityOvfData, Version version, OsRepository osRepository) {
        super(vm.getStaticData(), fullEntityOvfData.getDiskImages(), version, vm.getVmExternalData(), osRepository);
        this.vm = vm;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(TEMPLATE_ID, Guid.Empty.toString());
        if (vm.getInstanceTypeId() != null ) {
            _writer.writeElement(INSTANCE_TYPE_ID, vm.getInstanceTypeId().toString());
        }
        if (vm.getImageTypeId() != null ) {
            _writer.writeElement(IMAGE_TYPE_ID, vm.getImageTypeId().toString());
        }
        _writer.writeElement(IS_INITIALIZED, String.valueOf(vm.isInitialized()));
        _writer.writeElement(ORIGIN, String.valueOf(vm.getOrigin().getValue()));
        if (!StringUtils.isBlank(vm.getAppList())) {
            _writer.writeElement(APPLICATIONS_LIST, vm.getAppList());
        }
        if (vm.getQuotaId() != null) {
            _writer.writeElement(QUOTA_ID, vm.getQuotaId().toString());
        }
        _writer.writeElement(VM_DEFAULT_DISPLAY_TYPE, String.valueOf(vm.getDefaultDisplayType().getValue()));
        _writer.writeElement(TRUSTED_SERVICE, String.valueOf(vm.isTrustedService()));

        if (vm.getOriginalTemplateGuid() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_ID, vm.getOriginalTemplateGuid().toString());
        }

        if (vm.getOriginalTemplateName() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_NAME, vm.getOriginalTemplateName());
        }

        _writer.writeElement(USE_LATEST_VERSION, String.valueOf(vm.isUseLatestVersion()));

        if (vm.getLastStopTime() != null) {
            _writer.writeElement(STOP_TIME, OvfParser.localDateToUtcDateString(vm.getLastStopTime()));
        }

        if (vm.getBootTime() != null) {
            _writer.writeElement(BOOT_TIME, OvfParser.localDateToUtcDateString(vm.getBootTime()));
            _writer.writeElement(DOWNTIME, String.valueOf(vm.getDowntime()));
        }

        if (vm.getCpuPinning() != null) {
            _writer.writeElement(CPU_PINNING, vm.getCpuPinning());
        }
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(vm, getVersion());
    }

}
