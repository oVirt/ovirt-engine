package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class DataCenterModel extends Model implements HasValidatedTabs {
    private StoragePool privateEntity;

    public StoragePool getEntity() {
        return privateEntity;
    }

    public void setEntity(StoragePool value) {
        privateEntity = value;
    }

    private Guid privateDataCenterId;

    public Guid getDataCenterId() {
        return privateDataCenterId;
    }

    public void setDataCenterId(Guid value) {
        privateDataCenterId = value;
    }

    private boolean privateIsNew;

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    private String privateOriginalName;

    public String getOriginalName() {
        return privateOriginalName;
    }

    public void setOriginalName(String value) {
        privateOriginalName = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    public void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    public void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    private EntityModel<String> privateComment;

    public EntityModel<String> getComment() {
        return privateComment;
    }

    public void setComment(EntityModel<String> value) {
        privateComment = value;
    }

    private ListModel<Boolean> storagePoolType;

    public ListModel<Boolean> getStoragePoolType() {
        return storagePoolType;
    }

    public void setStoragePoolType(ListModel<Boolean> value) {
        this.storagePoolType = value;
    }

    private ListModel<Version> privateVersion;

    private boolean isEdit;

    public void setIsEdit(boolean value) {
        this.isEdit = value;
    }

    public boolean getIsEdit() {
        return this.isEdit;
    }

    public ListModel<Version> getVersion() {
        return privateVersion;
    }

    public void setVersion(ListModel<Version> value) {
        privateVersion = value;
    }

    private int privateMaxNameLength;

    public int getMaxNameLength() {
        return privateMaxNameLength;
    }

    public void setMaxNameLength(int value) {
        privateMaxNameLength = value;
    }

    ListModel<QuotaEnforcementTypeEnum> quotaEnforceTypeListModel;

    public ListModel<QuotaEnforcementTypeEnum> getQuotaEnforceTypeListModel() {
        return quotaEnforceTypeListModel;
    }

    public void setQuotaEnforceTypeListModel(ListModel<QuotaEnforcementTypeEnum> quotaEnforceTypeListModel) {
        this.quotaEnforceTypeListModel = quotaEnforceTypeListModel;
    }



    public DataCenterModel() {
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setVersion(new ListModel<Version>());

        setStoragePoolType(new ListModel<Boolean>());
        getStoragePoolType().getSelectedItemChangedEvent().addListener(this);
        getStoragePoolType().setItems(Arrays.asList(Boolean.FALSE, Boolean.TRUE));

        setQuotaEnforceTypeListModel(new ListModel<QuotaEnforcementTypeEnum>());
        List<QuotaEnforcementTypeEnum> list = AsyncDataProvider.getInstance().getQuotaEnforcmentTypes();
        getQuotaEnforceTypeListModel().setItems(list);
        getQuotaEnforceTypeListModel().setSelectedItem(list.get(0));

        setMaxNameLength(1);
        AsyncDataProvider.getInstance().getDataCenterMaxNameLength(new AsyncQuery<>(result -> setMaxNameLength(result)));

    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getStoragePoolType()) {
            storagePoolType_SelectedItemChanged();
        }
    }

    private void storagePoolType_SelectedItemChanged() {
        AsyncDataProvider.getInstance().getDataCenterVersions(new AsyncQuery<>(versions -> {
            Version selectedVersion = null;
            if (getVersion().getSelectedItem() != null) {
                selectedVersion = getVersion().getSelectedItem();
                boolean hasSelectedVersion = false;
                for (Version version : versions) {
                    if (selectedVersion.equals(version)) {
                        selectedVersion = version;
                        hasSelectedVersion = true;
                        break;
                    }
                }
                if (!hasSelectedVersion) {
                    selectedVersion = null;
                }
            }

            if (getIsEdit() && getEntity() != null) {
                Version compatibilityVersion = getEntity().getCompatibilityVersion();
                List<Version> filteredVersions = versions.stream()
                        .filter(version -> version.compareTo(compatibilityVersion) >= 0)
                        .collect(Collectors.toList());
                getVersion().setItems(filteredVersions);
            } else {
                getVersion().setItems(versions);
            }

            if (selectedVersion == null) {
                getVersion().setSelectedItem(versions.stream().max(Comparator.naturalOrder()).orElse(null));
                if (getEntity() != null) {
                    initVersion();
                }
            } else {
                getVersion().setSelectedItem(selectedVersion);
            }

        }), getDataCenterId());
    }

    private boolean isVersionInit = false;

    private void initVersion() {
        if (!isVersionInit) {
            isVersionInit = true;
            for (Version item : getVersion().getItems()) {
                if (item.equals(getEntity().getCompatibilityVersion())) {
                    getVersion().setSelectedItem(item);
                    break;
                }
            }
        }
    }


    public boolean validate() {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new LengthValidation(getMaxNameLength()),
                new AsciiNameValidation() });

        getVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        boolean validationResult = getName().getIsValid()
                && getDescription().getIsValid()
                && getComment().getIsValid()
                && getVersion().getIsValid();

        return validationResult;
    }

}
