package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
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

@SuppressWarnings("unused")
public class DataCenterModel extends Model
{

    private StoragePool privateEntity;

    public StoragePool getEntity()
    {
        return privateEntity;
    }

    public void setEntity(StoragePool value)
    {
        privateEntity = value;
    }

    private Guid privateDataCenterId;

    public Guid getDataCenterId()
    {
        return privateDataCenterId;
    }

    public void setDataCenterId(Guid value)
    {
        privateDataCenterId = value;
    }

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private String privateOriginalName;

    public String getOriginalName()
    {
        return privateOriginalName;
    }

    public void setOriginalName(String value)
    {
        privateOriginalName = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    public void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel privateComment;

    public EntityModel getComment()
    {
        return privateComment;
    }

    public void setComment(EntityModel value)
    {
        privateComment = value;
    }

    private ListModel privateStorageTypeList;

    public ListModel getStorageTypeList()
    {
        return privateStorageTypeList;
    }

    public void setStorageTypeList(ListModel value)
    {
        privateStorageTypeList = value;
    }

    private ListModel privateVersion;

    public ListModel getVersion()
    {
        return privateVersion;
    }

    public void setVersion(ListModel value)
    {
        privateVersion = value;
    }

    private int privateMaxNameLength;

    public int getMaxNameLength()
    {
        return privateMaxNameLength;
    }

    public void setMaxNameLength(int value)
    {
        privateMaxNameLength = value;
    }

    ListModel quotaEnforceTypeListModel;

    public ListModel getQuotaEnforceTypeListModel() {
        return quotaEnforceTypeListModel;
    }

    public void setQuotaEnforceTypeListModel(ListModel quotaEnforceTypeListModel) {
        this.quotaEnforceTypeListModel = quotaEnforceTypeListModel;
    }

    public DataCenterModel()
    {
        setName(new EntityModel());
        setDescription(new EntityModel());
        setComment(new EntityModel());
        setVersion(new ListModel());

        setStorageTypeList(new ListModel());
        getStorageTypeList().getSelectedItemChangedEvent().addListener(this);
        getStorageTypeList().setItems(AsyncDataProvider.getStoragePoolTypeList());

        setQuotaEnforceTypeListModel(new ListModel());
        ArrayList<QuotaEnforcementTypeEnum> list = AsyncDataProvider.getQuotaEnforcmentTypes();
        getQuotaEnforceTypeListModel().setItems(list);
        getQuotaEnforceTypeListModel().setSelectedItem(list.get(0));

        setMaxNameLength(1);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                DataCenterModel dataCenterModel = (DataCenterModel) model;
                dataCenterModel.setMaxNameLength((Integer) result);
            }
        };
        AsyncDataProvider.getDataCenterMaxNameLength(_asyncQuery);

    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getStorageTypeList())
        {
            storageType_SelectedItemChanged();
        }
    }

    private void storageType_SelectedItemChanged()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                DataCenterModel dataCenterModel = (DataCenterModel) model;
                ArrayList<Version> versions = (ArrayList<Version>) result;

                // Rebuild version items.
                ArrayList<Version> list = new ArrayList<Version>();
                StorageType type = (StorageType) dataCenterModel.getStorageTypeList().getSelectedItem();

                for (Version item : versions)
                {
                    if (AsyncDataProvider.isVersionMatchStorageType(item, type))
                    {
                        list.add(item);
                    }
                }

                if (type == StorageType.LOCALFS)
                {
                    ArrayList<Version> tempList = new ArrayList<Version>();
                    for (Version version : list)
                    {
                        Version version3_0 = new Version(3, 0);
                        if (version.compareTo(version3_0) >= 0)
                        {
                            tempList.add(version);
                        }
                    }
                    list = tempList;
                }

                Version selectedVersion = null;
                if (dataCenterModel.getVersion().getSelectedItem() != null)
                {
                    selectedVersion = (Version) dataCenterModel.getVersion().getSelectedItem();
                    boolean hasSelectedVersion = false;
                    for (Version version : list)
                    {
                        if (selectedVersion.equals(version))
                        {
                            selectedVersion = version;
                            hasSelectedVersion = true;
                            break;
                        }
                    }
                    if (!hasSelectedVersion)
                    {
                        selectedVersion = null;
                    }
                }

                dataCenterModel.getVersion().setItems(list);

                if (selectedVersion == null)
                {
                    dataCenterModel.getVersion().setSelectedItem(Linq.selectHighestVersion(list));
                    if (getEntity() != null)
                    {
                        initVersion();
                    }
                }
                else
                {
                    dataCenterModel.getVersion().setSelectedItem(selectedVersion);
                }

            }
        };
        AsyncDataProvider.getDataCenterVersions(_asyncQuery, getDataCenterId());
    }

    private boolean isVersionInit = false;

    private void initVersion()
    {
        if (!isVersionInit)
        {
            isVersionInit = true;
            for (Object a : getVersion().getItems())
            {
                Version item = (Version) a;
                if (Version.OpEquality(item, getEntity().getcompatibility_version()))
                {
                    getVersion().setSelectedItem(item);
                    break;
                }
            }
        }
    }

    public boolean validate()
    {
        getName().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new LengthValidation(getMaxNameLength()),
                new AsciiNameValidation() });

        getStorageTypeList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        // TODO: add this code to async validate.
        // string name = (string)Name.Entity;
        // if (String.Compare(name, OriginalName, true) != 0 && !DataProvider.IsDataCenterNameUnique(name))
        // {
        // Name.IsValid = false;
        // Name.InvalidityReasons.Add("Name must be unique.");
        // }

        return getName().getIsValid() && getDescription().getIsValid() && getComment().getIsValid()
                && getStorageTypeList().getIsValid()
                && getVersion().getIsValid();
    }

}
