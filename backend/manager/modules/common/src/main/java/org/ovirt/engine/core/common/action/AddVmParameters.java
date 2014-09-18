package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.TemplateProvisioningMethod;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;

public class AddVmParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private ArrayList<DiskImage> diskInfoList;
    private TemplateProvisioningMethod templateProvisioningMethod = TemplateProvisioningMethod.THIN;

    public AddVmParameters() {
    }

    public AddVmParameters(VmStatic vmStatic) {
        super(vmStatic);
        diskInfoList = new ArrayList<DiskImage>();
    }

    public AddVmParameters(VM vm) {
        this(vm.getStaticData());
    }

    public ArrayList<DiskImage> getDiskInfoList() {
        return diskInfoList;
    }

    public void setDiskInfoList(ArrayList<DiskImage> diskInfoList) {
        this.diskInfoList = diskInfoList;
    }

    public TemplateProvisioningMethod getTemplateProvisioningMethod() {
        return templateProvisioningMethod;
    }

    public void setTemplateProvisioningMethod(TemplateProvisioningMethod templateProvisioningMethod) {
        this.templateProvisioningMethod = templateProvisioningMethod;
    }
}
