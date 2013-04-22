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
    private ListModel keyList;
    private EntityModel selectedKey;
    private EntityModel value;
    private EntityModel description;
    private Boolean isNew;

    public VolumeParameterModel() {
        setKeyList(new ListModel());
        setSelectedKey(new EntityModel());
        setValue(new EntityModel());
        setDescription(new EntityModel());

        setIsNew(true);

        getKeyList().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                keyItemChanged();
            }
        });

        getSelectedKey().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                selectedKeyChanged();
            }
        });
    }

    public ListModel getKeyList() {
        return keyList;
    }

    public void setKeyList(ListModel keyList) {
        this.keyList = keyList;
    }

    public EntityModel getSelectedKey()
    {
        return selectedKey;
    }

    public void setSelectedKey(EntityModel value)
    {
        this.selectedKey = value;
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

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    private void keyItemChanged() {
        if (getIsNew() && getKeyList().getSelectedItem() != null)
        {
            getSelectedKey().setEntity(((GlusterVolumeOptionInfo) getKeyList().getSelectedItem()).getKey());
        }
    }

    private void selectedKeyChanged() {
        String key = (String) getSelectedKey().getEntity();
        List<GlusterVolumeOptionInfo> options = (List<GlusterVolumeOptionInfo>) getKeyList().getItems();
        GlusterVolumeOptionInfo selectedOption = null;
        for (GlusterVolumeOptionInfo option : options)
        {
            if (option.getKey().equals(key.trim()))
            {
                selectedOption = option;
                break;
            }
        }

        if (selectedOption != null)
        {
            if (selectedOption.getDescription() == null || selectedOption.getDescription().equals(NULL_CONST))
            {
                getDescription().setEntity(null);
            }
            else
            {
                getDescription().setEntity(selectedOption.getDescription());
            }

            if (getIsNew())
            {
                if (selectedOption.getDefaultValue() == null || selectedOption.getDefaultValue().equals(NULL_CONST))
                {
                    getValue().setEntity(null);
                }
                else
                {
                    getValue().setEntity(selectedOption.getDefaultValue());
                }
            }
        }
        else if (getIsNew())
        {
            getDescription().setEntity(null);
            getValue().setEntity(null);
        }
    }

    public boolean Validate() {
        NotEmptyValidation valueValidation = new NotEmptyValidation();
        getSelectedKey().validateEntity(new IValidation[] { valueValidation });
        getValue().validateEntity(new IValidation[] { valueValidation });

        return getSelectedKey().getIsValid() && getValue().getIsValid();
    }

}
