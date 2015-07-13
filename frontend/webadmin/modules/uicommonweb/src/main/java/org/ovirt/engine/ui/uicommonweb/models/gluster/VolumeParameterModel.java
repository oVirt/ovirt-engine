package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class VolumeParameterModel extends EntityModel {

    private static final String NULL_CONST = "(null)"; //$NON-NLS-1$
    private ListModel<GlusterVolumeOptionInfo> keyList;
    private EntityModel<String> selectedKey;
    private EntityModel<String> value;
    private EntityModel<String> description;
    private Boolean isNew;

    public VolumeParameterModel() {
        setKeyList(new ListModel<GlusterVolumeOptionInfo>());
        setSelectedKey(new EntityModel<String>());
        setValue(new EntityModel<String>());
        setDescription(new EntityModel<String>());

        setIsNew(true);

        getKeyList().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                keyItemChanged();
            }
        });

        getSelectedKey().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                selectedKeyChanged();
            }
        });
    }

    public ListModel<GlusterVolumeOptionInfo> getKeyList() {
        return keyList;
    }

    public void setKeyList(ListModel<GlusterVolumeOptionInfo> keyList) {
        this.keyList = keyList;
    }

    public EntityModel<String> getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(EntityModel<String> value) {
        this.selectedKey = value;
    }

    public EntityModel<String> getValue() {
        return value;
    }

    public void setValue(EntityModel<String> value) {
        this.value = value;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    private void keyItemChanged() {
        if (getIsNew() && getKeyList().getSelectedItem() != null) {
            getSelectedKey().setEntity(getKeyList().getSelectedItem().getKey());
        }
    }

    private void selectedKeyChanged() {
        String key = getSelectedKey().getEntity();
        List<GlusterVolumeOptionInfo> options = (List<GlusterVolumeOptionInfo>) getKeyList().getItems();
        GlusterVolumeOptionInfo selectedOption = null;
        for (GlusterVolumeOptionInfo option : options) {
            if (option.getKey().equals(key.trim())) {
                selectedOption = option;
                break;
            }
        }

        if (selectedOption != null) {
            if (selectedOption.getDescription() == null || selectedOption.getDescription().equals(NULL_CONST)) {
                getDescription().setEntity(null);
            }
            else {
                getDescription().setEntity(selectedOption.getDescription());
            }

            if (getIsNew()) {
                if (selectedOption.getDefaultValue() == null || selectedOption.getDefaultValue().equals(NULL_CONST)) {
                    getValue().setEntity(null);
                }
                else {
                    getValue().setEntity(selectedOption.getDefaultValue());
                }
            }
        }
        else if (getIsNew()) {
            getDescription().setEntity(null);
            getValue().setEntity(null);
        }
    }

    public boolean validate() {
        NotEmptyValidation valueValidation = new NotEmptyValidation();
        getSelectedKey().validateEntity(new IValidation[] { valueValidation });
        getValue().validateEntity(new IValidation[] { valueValidation });

        return getSelectedKey().getIsValid() && getValue().getIsValid();
    }

}
