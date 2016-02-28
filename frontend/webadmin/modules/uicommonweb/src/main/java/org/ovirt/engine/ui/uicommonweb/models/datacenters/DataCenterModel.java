package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
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
        initSelectedMacPool();
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

    private ListModel<MacPool> macPoolListModel;

    public ListModel<MacPool> getMacPoolListModel() {
        return macPoolListModel;
    }

    private void setMacPoolListModel(ListModel<MacPool> macPoolListModel) {
        this.macPoolListModel = macPoolListModel;
    }

    private MacPoolModel macPoolModel;

    public MacPoolModel getMacPoolModel() {
        return macPoolModel;
    }

    private void setMacPoolModel(MacPoolModel macPoolModel) {
        this.macPoolModel = macPoolModel;
    }

    private UICommand addMacPoolCommand;

    public UICommand getAddMacPoolCommand() {
        return addMacPoolCommand;
    }

    public void setAddMacPoolCommand(UICommand addMacPoolCommand) {
        this.addMacPoolCommand = addMacPoolCommand;
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

        setMacPoolListModel(new SortedListModel<>(new Linq.SharedMacPoolComparator()));
        setMacPoolModel(new MacPoolModel());
        getMacPoolModel().setIsChangeable(false);
        getMacPoolListModel().getItemsChangedEvent().addListener(this);
        getMacPoolListModel().getSelectedItemChangedEvent().addListener(this);
        startProgress();
        Frontend.getInstance().runQuery(VdcQueryType.GetAllMacPools,
                new VdcQueryParametersBase(),
                new AsyncQuery(new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        getMacPoolListModel().setItems((Collection<MacPool>) ((VdcQueryReturnValue) returnValue).getReturnValue());
                        stopProgress();
                    }
                }));

        setMaxNameLength(1);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                DataCenterModel dataCenterModel = (DataCenterModel) model;
                dataCenterModel.setMaxNameLength((Integer) result);
            }
        };
        AsyncDataProvider.getInstance().getDataCenterMaxNameLength(_asyncQuery);

    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getStoragePoolType()) {
            storagePoolType_SelectedItemChanged();
        } else if (sender == getMacPoolListModel()) {
            if (ev.matchesDefinition(ListModel.itemsChangedEventDefinition)) {
                initSelectedMacPool();
            } else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
                getMacPoolModel().setEntity(getMacPoolListModel().getSelectedItem());
            }
        }
    }

    private void storagePoolType_SelectedItemChanged() {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                DataCenterModel dataCenterModel = (DataCenterModel) model;
                ArrayList<Version> versions = (ArrayList<Version>) result;

                Version selectedVersion = null;
                if (dataCenterModel.getVersion().getSelectedItem() != null) {
                    selectedVersion = dataCenterModel.getVersion().getSelectedItem();
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

                dataCenterModel.getVersion().setItems(versions);

                if (selectedVersion == null) {
                    dataCenterModel.getVersion().setSelectedItem(Linq.selectHighestVersion(versions));
                    if (getEntity() != null) {
                        initVersion();
                    }
                }
                else {
                    dataCenterModel.getVersion().setSelectedItem(selectedVersion);
                }

            }
        };
        AsyncDataProvider.getInstance().getDataCenterVersions(_asyncQuery, getDataCenterId());
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

    private void initSelectedMacPool() {
        Collection<MacPool> allMacPools = getMacPoolListModel().getItems();
        StoragePool dc = getEntity();
        if (allMacPools != null && dc != null) {
            Guid macPoolId = dc.getMacPoolId();
            for (MacPool macPool : allMacPools) {
                if (macPool.getId().equals(macPoolId)) {
                    getMacPoolListModel().setSelectedItem(macPool);
                    break;
                }
            }
        }
    }

    public boolean validate() {
        boolean generalTabValid = isGeneralTabValid();

        getMacPoolModel().validate();

        setValidTab(TabName.GENERAL_TAB, generalTabValid);
        boolean macPoolTabValid = getMacPoolModel().getIsValid();
        setValidTab(TabName.MAC_POOL_TAB, macPoolTabValid);
        ValidationCompleteEvent.fire(getEventBus(), this);
        return generalTabValid && macPoolTabValid;
    }

    public boolean isGeneralTabValid() {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new LengthValidation(getMaxNameLength()),
                new AsciiNameValidation() });

        getVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        return getName().getIsValid() && getDescription().getIsValid() && getComment().getIsValid()
                && getVersion().getIsValid();
    }

}
