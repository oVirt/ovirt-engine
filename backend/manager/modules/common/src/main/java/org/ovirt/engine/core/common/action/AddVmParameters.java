package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.TemplateProvisioningMethod;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class AddVmParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private ArrayList<DiskImage> diskInfoList;
    private TemplateProvisioningMethod templateProvisioningMethod = TemplateProvisioningMethod.THIN;
    private Guid diskOperatorAuthzPrincipalDbId;
    private Guid poolId;

    public AddVmParameters() {
    }

    public AddVmParameters(VmStatic vmStatic) {
        super(vmStatic);
        diskInfoList = new ArrayList<>();
    }

    public AddVmParameters(VM vm) {
        this(vm.getStaticData());
    }

    public ArrayList<DiskImage> getDiskInfoList() {
        return diskInfoList;
    }

    public void setDiskInfoList(ArrayList<DiskImage> diskInfoList) {
        this.diskInfoList = diskInfoList != null ? diskInfoList : new ArrayList<DiskImage>();
    }

    public TemplateProvisioningMethod getTemplateProvisioningMethod() {
        return templateProvisioningMethod;
    }

    public void setTemplateProvisioningMethod(TemplateProvisioningMethod templateProvisioningMethod) {
        this.templateProvisioningMethod = templateProvisioningMethod;
    }

    public Guid getDiskOperatorAuthzPrincipalDbId() {
        return diskOperatorAuthzPrincipalDbId;
    }

    public void setDiskOperatorAuthzPrincipalDbId(Guid diskOperatorAuthzPrincipalDbId) {
        this.diskOperatorAuthzPrincipalDbId = diskOperatorAuthzPrincipalDbId;
    }

    public Guid getPoolId() {
        return poolId;
    }

    public void setPoolId(Guid poolId) {
        this.poolId = poolId;
    }

}
