package org.ovirt.engine.core.common.action;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmTemplateParameters")
public class AddVmTemplateParameters extends VmTemplateParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 2114985552063499069L;

    public AddVmTemplateParameters() {
    }

    private VmStatic _masterVm;

    @XmlElement(name = "DestinationStorageDomainId")
    private Guid destinationStorageDomainId;

    @XmlElement(name = "Name")
    @Size(max = 40, message = "VALIDATION.VM_TEMPLATE.NAME.MAX", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _name;

    @XmlElement
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _description;

    @XmlElement(name = "PublicUse")
    private boolean publicUse = false;

    public AddVmTemplateParameters(VmStatic masterVm, String name, String description) {
        _masterVm = masterVm;
        _name = name;
        _description = description;
    }

    public AddVmTemplateParameters(VM vm, String name, String description) {
        _masterVm = vm.getStaticData();
        _name = name;
        _description = description;
    }

    public VmStatic getMasterVm() {
        return _masterVm;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    @XmlElement(name = "VmTemplateID")
    private Guid privateVmTemplateID = new Guid();

    @Override
    public Guid getVmTemplateId() {
        return privateVmTemplateID;
    }

    public void setVmTemplateID(Guid value) {
        privateVmTemplateID = value;
    }

    @XmlElement(name = "vm")
    public VM getVm() {
        VM vm = new VM();
        vm.setStaticData(_masterVm);
        return vm;
    }

    public void setVm(VM value) {
        _masterVm = value.getStaticData();
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public void setDestinationStorageDomainId(Guid destinationStorageDomainId) {
        this.destinationStorageDomainId = destinationStorageDomainId;
    }

    public Guid getDestinationStorageDomainId() {
        return destinationStorageDomainId;
    }
}
