package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

public class NetworkFilterParameterModel extends EntityModel<VmNicFilterParameter> {

    private EntityModel<String> name;
    private EntityModel<String> value;

    public NetworkFilterParameterModel(VmNicFilterParameter parameter) {
        setEntity(parameter);
        name = new EntityModel<>(parameter.getName());
        value = new EntityModel<>(parameter.getValue());
    }

    public NetworkFilterParameterModel() {
        setEntity(new VmNicFilterParameter());
        name = new EntityModel<>();
        value = new EntityModel<>();
    }

    public VmNicFilterParameter flush() {
        getEntity().setName(name.getEntity());
        getEntity().setValue(value.getEntity());
        if (getEntity().getVmInterfaceId() == null) {
            getEntity().setVmInterfaceId(Guid.Empty);
        }
        return getEntity();
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getValue() {
        return value;
    }

    public void setValue(EntityModel<String> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (getEntity() == null && !(o instanceof NetworkFilterParameterModel)) {
            return false;
        }
        return getEntity().getId().equals(((NetworkFilterParameterModel) o).getEntity().getId())
                && getEntity().getVmInterfaceId().equals(((NetworkFilterParameterModel) o).getEntity().getVmInterfaceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    public void validate() {
        RegexValidation nameValidation = new RegexValidation();
        nameValidation.setExpression("^[a-zA-Z0-9_]+$");  //$NON-NLS-1$

        RegexValidation valueValidation = new RegexValidation();
        valueValidation.setExpression("^[a-zA-Z0-9_\\.:]+$");  //$NON-NLS-1$

        name.validateEntity(new IValidation[]{new NotEmptyValidation(), nameValidation});
        value.validateEntity(new IValidation[]{new NotEmptyValidation(), valueValidation});

        setIsValid(name.getIsValid() && value.getIsValid());
    }
}
