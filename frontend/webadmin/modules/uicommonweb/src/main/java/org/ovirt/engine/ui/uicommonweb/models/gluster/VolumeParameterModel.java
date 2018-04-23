package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class VolumeParameterModel extends EntityModel {

    private static final String NULL_CONST = "(null)"; //$NON-NLS-1$
    private Map<String, GlusterVolumeOptionInfo> optionsMap;
    private ListModel<String> keyList;
    private Boolean isNew;
    private EntityModel<String> value;
    private EntityModel<String> description;

    public VolumeParameterModel() {
        setKeyList(new ListModel<String>());
        setValue(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setIsNew(true);

        getKeyList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> selectedKeyChanged());
    }

    public ListModel<String> getKeyList() {
        return keyList;
    }

    public void setKeyList(ListModel<String> keyList) {
        this.keyList = keyList;
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

    private void selectedKeyChanged() {
        String key = getKeyList().getSelectedItem();
        GlusterVolumeOptionInfo selectedOption = optionsMap.get(key);

        if (selectedOption != null) {
            if (selectedOption.getDescription() == null || selectedOption.getDescription().equals(NULL_CONST)) {
                getDescription().setEntity(null);
            } else {
                getDescription().setEntity(selectedOption.getDescription());
            }

            if (getIsNew()) {
                if (selectedOption.getDefaultValue() == null || selectedOption.getDefaultValue().equals(NULL_CONST)) {
                    getValue().setEntity(null);
                } else {
                    getValue().setEntity(selectedOption.getDefaultValue());
                }
            }
        } else if (getIsNew()) {
            getDescription().setEntity(null);
            getValue().setEntity(null);
        }
    }

    public boolean validate() {
        NotEmptyValidation valueValidation = new NotEmptyValidation();
        getValue().validateEntity(new IValidation[] { valueValidation });
        getKeyList().validateSelectedItem(new IValidation[] { valueValidation });
        return getKeyList().getIsValid() && getValue().getIsValid();
    }

    public Map<String, GlusterVolumeOptionInfo> getOptionsMap() {
        return optionsMap;
    }

    public void setOptionsMap(Map<String, GlusterVolumeOptionInfo> optionsMap) {
        this.optionsMap = optionsMap;
        getKeyList().setItems(new ArrayList<String>(getOptionsMap().keySet()), getKeyList().getSelectedItem());
    }

}
