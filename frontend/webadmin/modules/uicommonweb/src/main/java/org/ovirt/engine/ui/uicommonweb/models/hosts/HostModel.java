package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.KeyValuePairValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.FrontendQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendQueryAsyncCallback;

@SuppressWarnings("unused")
public class HostModel extends Model
{

    public static final int HostNameMaxLength = 255;
    public static final String PmSecureKey = "secure";
    public static final String PmPortKey = "port";
    public static final String PmSlotKey = "slot";
    public static final String BeginTestStage = "BeginTest";
    public static final String EndTestStage = "EndTest";

    private UICommand privateTestCommand;

    public UICommand getTestCommand()
    {
        return privateTestCommand;
    }

    private void setTestCommand(UICommand value)
    {
        privateTestCommand = value;
    }

    public boolean getIsNew()
    {
        return getHostId() == null;
    }

    private NGuid privateHostId;

    public NGuid getHostId()
    {
        return privateHostId;
    }

    public void setHostId(NGuid value)
    {
        privateHostId = value;
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

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateHost;

    public EntityModel getHost()
    {
        return privateHost;
    }

    private void setHost(EntityModel value)
    {
        privateHost = value;
    }

    private EntityModel privateManagementIp;

    public EntityModel getManagementIp()
    {
        return privateManagementIp;
    }

    private void setManagementIp(EntityModel value)
    {
        privateManagementIp = value;
    }

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
    }

    private ListModel privateCluster;

    public ListModel getCluster()
    {
        return privateCluster;
    }

    private void setCluster(ListModel value)
    {
        privateCluster = value;
    }

    private EntityModel privatePort;

    public EntityModel getPort()
    {
        return privatePort;
    }

    private void setPort(EntityModel value)
    {
        privatePort = value;
    }

    private EntityModel privateRootPassword;

    public EntityModel getRootPassword()
    {
        return privateRootPassword;
    }

    private void setRootPassword(EntityModel value)
    {
        privateRootPassword = value;
    }

    private EntityModel privateOverrideIpTables;

    public EntityModel getOverrideIpTables()
    {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel value)
    {
        privateOverrideIpTables = value;
    }

    private EntityModel privateIsPm;

    public EntityModel getIsPm()
    {
        return privateIsPm;
    }

    private void setIsPm(EntityModel value)
    {
        privateIsPm = value;
    }

    private EntityModel privatePmUserName;

    public EntityModel getPmUserName()
    {
        return privatePmUserName;
    }

    private void setPmUserName(EntityModel value)
    {
        privatePmUserName = value;
    }

    private EntityModel privatePmPassword;

    public EntityModel getPmPassword()
    {
        return privatePmPassword;
    }

    private void setPmPassword(EntityModel value)
    {
        privatePmPassword = value;
    }

    private ListModel privatePmType;

    public ListModel getPmType()
    {
        return privatePmType;
    }

    private void setPmType(ListModel value)
    {
        privatePmType = value;
    }

    private EntityModel privatePmSecure;

    public EntityModel getPmSecure()
    {
        return privatePmSecure;
    }

    private void setPmSecure(EntityModel value)
    {
        privatePmSecure = value;
    }

    private EntityModel privatePmPort;

    public EntityModel getPmPort()
    {
        return privatePmPort;
    }

    private void setPmPort(EntityModel value)
    {
        privatePmPort = value;
    }

    private EntityModel privatePmSlot;

    public EntityModel getPmSlot()
    {
        return privatePmSlot;
    }

    private void setPmSlot(EntityModel value)
    {
        privatePmSlot = value;
    }

    private EntityModel privatePmOptions;

    public EntityModel getPmOptions()
    {
        return privatePmOptions;
    }

    private void setPmOptions(EntityModel value)
    {
        privatePmOptions = value;
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
        }
    }

    private boolean isPowerManagementTabValid;

    public boolean getIsPowerManagementTabValid()
    {
        return isPowerManagementTabValid;
    }

    public void setIsPowerManagementTabValid(boolean value)
    {
        if (isPowerManagementTabValid != value)
        {
            isPowerManagementTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabValid"));
        }
    }

    private boolean isPowerManagementSelected;

    public boolean getIsPowerManagementSelected()
    {
        return isPowerManagementSelected;
    }

    public void setIsPowerManagementSelected(boolean value)
    {
        if (isPowerManagementSelected != value)
        {
            isPowerManagementSelected = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementSelected"));
        }
    }

    public java.util.HashMap<String, String> getPmOptionsMap()
    {
        java.util.HashMap<String, String> dict = new java.util.HashMap<String, String>();

        // Add well known pm options.
        if (getPmPort().getIsAvailable())
        {
            dict.put(PmPortKey, getPmPort().getEntity() == null ? "" : (String) getPmPort().getEntity());
        }
        if (getPmSlot().getIsAvailable())
        {
            dict.put(PmSlotKey, getPmSlot().getEntity() == null ? "" : (String) getPmSlot().getEntity());
        }
        if (getPmSecure().getIsAvailable())
        {
            dict.put(PmSecureKey, getPmSecure().getEntity().toString());
        }

        // Add unknown pm options.
        // Assume Validate method was called before this getter.
        String pmOptions = (String) getPmOptions().getEntity();
        if (!StringHelper.isNullOrEmpty(pmOptions))
        {
            for (String pair : pmOptions.split("[,]", -1))
            {
                String[] array = pair.split("[=]", -1);
                if (array.length == 2)
                {
                    dict.put(array[0], array[1]);
                }
                else if (array.length == 1)
                {
                    dict.put(array[0], "");
                }
            }
        }

        return dict;
    }

    public void setPmOptionsMap(java.util.HashMap<String, String> value)
    {
        String pmOptions = "";

        for (java.util.Map.Entry<String, String> pair : value.entrySet())
        {
            String k = pair.getKey();
            String v = pair.getValue();

            // C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java
            // 'if-else' logic:
            // switch (k)
            // Handle well known pm options.
            // ORIGINAL LINE: case PmPortKey:
            if (StringHelper.stringsEqual(k, PmPortKey))
            {
                getPmPort().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k));

            }
            // ORIGINAL LINE: case PmSlotKey:
            else if (StringHelper.stringsEqual(k, PmSlotKey))
            {
                getPmSlot().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k));

            }
            // ORIGINAL LINE: case PmSecureKey:
            else if (StringHelper.stringsEqual(k, PmSecureKey))
            {
                getPmSecure().setEntity(Boolean.parseBoolean(value.get(k)));

            }
            else
            {
                // Compose custom string from unknown pm options.
                if (StringHelper.isNullOrEmpty(v))
                {
                    pmOptions += StringFormat.format("%1$s,", k);
                }
                else
                {
                    pmOptions += StringFormat.format("%1$s=%2$s,", k, v);
                }
            }
        }

        if (!StringHelper.isNullOrEmpty(pmOptions))
        {
            getPmOptions().setEntity(pmOptions.substring(0, pmOptions.length() - 1));
        }
    }

    public HostModel()
    {
        setTestCommand(new UICommand("Test", this));

        setName(new EntityModel());
        setHost(new EntityModel());
        setManagementIp(new EntityModel());
        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);
        setPort(new EntityModel());
        setRootPassword(new EntityModel());
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setOverrideIpTables(tempVar);
        setPmUserName(new EntityModel());
        setPmPassword(new EntityModel());
        setPmType(new ListModel());
        getPmType().getSelectedItemChangedEvent().addListener(this);
        setPmSecure(new EntityModel());
        getPmSecure().setIsAvailable(false);
        getPmSecure().setEntity(false);
        setPmPort(new EntityModel());
        getPmPort().setIsAvailable(false);
        setPmSlot(new EntityModel());
        getPmSlot().setIsAvailable(false);
        setPmOptions(new EntityModel());

        setIsPm(new EntityModel());
        getIsPm().getEntityChangedEvent().addListener(this);
        getIsPm().setEntity(false);

        setIsPowerManagementTabValid(true);
        setIsGeneralTabValid(getIsPowerManagementTabValid());
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            DataCenter_SelectedItemChanged();
        }
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getCluster())
        {
            Cluster_SelectedItemChanged();
        }
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getPmType())
        {
            PmType_SelectedItemChanged();
        }
        else if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getIsPm())
        {
            IsPm_EntityChanged();
        }
    }

    private void IsPm_EntityChanged()
    {
        UpdatePmModels();
    }

    private void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (dataCenter != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    HostModel hostModel = (HostModel) model;
                    java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) result;
                    VDSGroup oldCluster = (VDSGroup) hostModel.getCluster().getSelectedItem();
                    storage_pool selectedDataCenter = (storage_pool) getDataCenter().getSelectedItem();

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getstorage_pool_id()
                                    .getValue()
                                    .equals(selectedDataCenter.getId().getValue()))
                    {
                        hostModel.getCluster().setItems(clusters);

                        if (oldCluster != null)
                        {
                            VDSGroup newSelectedItem =
                                    Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getID()));
                            if (newSelectedItem != null)
                            {
                                hostModel.getCluster().setSelectedItem(newSelectedItem);
                            }
                        }

                        if (hostModel.getCluster().getSelectedItem() == null)
                        {
                            hostModel.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
                        }
                    }
                }
            };

            AsyncDataProvider.GetClusterList(_asyncQuery, dataCenter.getId());
        }
    }

    private void Cluster_SelectedItemChanged()
    {
        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();
        if (cluster != null)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    HostModel hostModel = (HostModel) model;
                    java.util.ArrayList<String> pmTypeList = (java.util.ArrayList<String>) result;
                    String pmType = (String) hostModel.getPmType().getSelectedItem();

                    hostModel.getPmType().setItems(pmTypeList);
                    if (pmTypeList.contains(pmType))
                    {
                        hostModel.getPmType().setSelectedItem(pmType);
                    }
                    else
                    {
                        hostModel.getPmType().setSelectedItem(null);
                    }
                }
            };
            AsyncDataProvider.GetPmTypeList(_asyncQuery, cluster.getcompatibility_version());
        }
    }

    private void PmType_SelectedItemChanged()
    {
        UpdatePmModels();
    }

    private void UpdatePmModels()
    {
        String pmType = (String) getPmType().getSelectedItem();
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                HostModel hostModel = (HostModel) model;
                hostModel.postGetPmOptions((java.util.ArrayList<String>) result);

            }
        };
        if (!StringHelper.isNullOrEmpty(pmType))
        {
            AsyncDataProvider.GetPmOptions(_asyncQuery, pmType);
        }
        else
        {
            postGetPmOptions(new java.util.ArrayList<String>());
        }
    }

    public void postGetPmOptions(java.util.ArrayList<String> pmOptions)
    {
        getPmPort().setIsAvailable(pmOptions.contains(PmPortKey));
        getPmSlot().setIsAvailable(pmOptions.contains(PmSlotKey));
        getPmSecure().setIsAvailable(pmOptions.contains(PmSecureKey));

        boolean isPm = (Boolean) getIsPm().getEntity();

        getTestCommand().setIsExecutionAllowed(isPm);

        getManagementIp().setIsChangable((Boolean) getIsPm().getEntity());
        getManagementIp().setIsValid(true);
        getPmUserName().setIsChangable((Boolean) getIsPm().getEntity());
        getPmUserName().setIsValid(true);
        getPmPassword().setIsChangable((Boolean) getIsPm().getEntity());
        getPmPassword().setIsValid(true);
        getPmType().setIsChangable((Boolean) getIsPm().getEntity());
        getPmType().setIsValid(true);
        getPmOptions().setIsChangable((Boolean) getIsPm().getEntity());
        getPmSecure().setIsChangable((Boolean) getIsPm().getEntity());
        getPmPort().setIsChangable((Boolean) getIsPm().getEntity());
        getPmPort().setIsValid(true);
        getPmSlot().setIsChangable((Boolean) getIsPm().getEntity());
    }

    public void Test()
    {
        // Validate user input.
        if ((Boolean) getIsPm().getEntity())
        {
            getCluster().setIsValid(true);
            getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            ValidatePmModels();
        }

        if (!getManagementIp().getIsValid() || !getPmUserName().getIsValid() || !getPmPassword().getIsValid()
                || !getPmType().getIsValid() || !getPmPort().getIsValid() || !getPmOptions().getIsValid())
        {
            return;
        }

        setMessage("Testing in progress. It will take a few seconds. Please wait...");
        getTestCommand().setIsExecutionAllowed(false);

        VDSGroup cluster = (VDSGroup) getCluster().getSelectedItem();

        GetNewVdsFenceStatusParameters param = new GetNewVdsFenceStatusParameters();
        if (getHostId() != null)
        {
            param.setVdsId(getHostId().getValue());
        }
        param.setManagementIp((String) getManagementIp().getEntity());
        param.setPmType((String) getPmType().getSelectedItem());
        param.setUser((String) getPmUserName().getEntity());
        param.setPassword((String) getPmPassword().getEntity());
        param.setStoragePoolId(cluster.getstorage_pool_id().getValue() != null ? cluster.getstorage_pool_id()
                .getValue()
                .getValue() : NGuid.Empty);
        param.setFencingOptions(new ValueObjectMap(getPmOptionsMap(), false));

        Frontend.RunQuery(VdcQueryType.GetNewVdsFenceStatus, param, new IFrontendQueryAsyncCallback() {

            @Override
            public void OnSuccess(FrontendQueryAsyncResult result) {
                if (result != null && result.getReturnValue() != null
                        && result.getReturnValue().getReturnValue() != null) {
                    FenceStatusReturnValue fenceStatusReturnValue =
                            (FenceStatusReturnValue) result.getReturnValue().getReturnValue();
                    String message = fenceStatusReturnValue.toString();
                    setMessage(message);
                    getTestCommand().setIsExecutionAllowed(true);
                }
            }

            @Override
            public void OnFailure(FrontendQueryAsyncResult result) {
                String message = "Test Failed (unknown error).";
                setMessage(message);
                getTestCommand().setIsExecutionAllowed(true);

            }
        });
    }

    private void ValidatePmModels()
    {
        getManagementIp().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new HostAddressValidation() });
        getPmUserName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        getPmPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        getPmType().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(65535);
        getPmPort().ValidateEntity(new IValidation[] { tempVar });
        getPmOptions().ValidateEntity(new IValidation[] { new KeyValuePairValidation(true) });
    }

    public boolean Validate()
    {
        String hostNameRegex = StringFormat.format("^[0-9a-zA-Z-_\\.]{1,%1$s}$", HostNameMaxLength);
        String hostNameMessage =
                StringFormat.format("This field can't contain blanks or special characters, must "
                        + "be at least one character long, legal values are 0-9, a-z, '_', '.' "
                        + "and a length of up to %1$s characters.", HostNameMaxLength);

        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(255);
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setExpression(hostNameRegex);
        tempVar2.setMessage(hostNameMessage);
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(255);
        getHost().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar3, new HostAddressValidation() });

        IntegerValidation tempVar4 = new IntegerValidation();
        tempVar4.setMinimum(1);
        tempVar4.setMaximum(65535);
        getPort().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });

        getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        // TODO: async validation.
        // string name = (string)Name.Entity;

        // //Check name unicitate.
        // if (String.Compare(name, OriginalName, true) != 0 && !DataProvider.IsHostNameUnique(name))
        // {
        // Name.IsValid = false;
        // Name.InvalidityReasons.Add("Name must be unique.");
        // }

        if ((Boolean) getIsPm().getEntity())
        {
            ValidatePmModels();
        }

        setIsGeneralTabValid(getName().getIsValid() && getHost().getIsValid() && getPort().getIsValid()
                && getCluster().getIsValid());

        setIsPowerManagementTabValid(getManagementIp().getIsValid() && getPmUserName().getIsValid()
                && getPmPassword().getIsValid() && getPmType().getIsValid() && getPmPort().getIsValid()
                && getPmOptions().getIsValid());

        return getName().getIsValid() && getHost().getIsValid() && getPort().getIsValid() && getCluster().getIsValid()
                && getManagementIp().getIsValid() && getPmUserName().getIsValid() && getPmPassword().getIsValid()
                && getPmType().getIsValid() && getPmPort().getIsValid() && getPmOptions().getIsValid();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getTestCommand())
        {
            Test();
        }
    }
}
