package org.ovirt.engine.core.utils.ovf;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class OvfOvaVmReader extends OvfOvaReader {

    private VM vm;

    public OvfOvaVmReader(XmlDocument document, FullEntityOvfData fullEntityOvfData, VM vm, OsRepository osRepository) {
        super(document, fullEntityOvfData, vm.getStaticData(), osRepository);
        this.vm = vm;
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        super.readGeneralData(content);
        consumeReadProperty(content, CPU_PINNING, vm::setCpuPinning);
        if (!StringUtils.isBlank(vm.getCpuPinning())) {
            vm.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        }
    }

    @Override
    protected void setClusterArch(ArchitectureType arch) {
        vm.setClusterArch(arch);
    }
}
