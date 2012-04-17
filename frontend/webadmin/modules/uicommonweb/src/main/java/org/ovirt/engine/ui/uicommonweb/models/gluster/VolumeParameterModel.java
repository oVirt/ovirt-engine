package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class VolumeParameterModel extends EntityModel {

    private ListModel keyList;
    private EntityModel value;
    private EntityModel description;

    public VolumeParameterModel() {
        setKeyList(new ListModel());
        setValue(new EntityModel());
        setDescription(new EntityModel());

        getKeyList().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                keySelectedItemChanged();
            }
        });
    }

    public ListModel getKeyList() {
        return keyList;
    }

    public void setKeyList(ListModel keyList) {
        this.keyList = keyList;
    }

    public EntityModel getValue() {
        return value;
    }

    public void setValue(EntityModel value) {
        this.value = value;
    }

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    private void keySelectedItemChanged() {
        getDescription().setEntity(((GlusterVolumeOptionInfo) getKeyList().getSelectedItem()).getDescription());
    }

    public boolean Validate() {
        NotEmptyValidation valueValidation = new NotEmptyValidation();
        getValue().ValidateEntity(new IValidation[] { valueValidation });

        return getKeyList().getIsValid() && getValue().getIsValid();
    }

}
