package org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class InstanceTypeGeneralModel extends EntityModel<InstanceType> {

    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!ObjectUtils.objectsEqual(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!ObjectUtils.objectsEqual(description, value)) {
            this.description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }

    }

    public InstanceTypeGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            setName(getEntity().getName());
            setDescription(getEntity().getDescription());
        }
    }
}
