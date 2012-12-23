package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.BaseI18NValidation;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.KeyValuePairValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendQueryAsyncCallback;

@SuppressWarnings("unused")
public class HostModel extends Model
{

    public static final int HostNameMaxLength = 255;
    public static final String PmSecureKey = "secure"; //$NON-NLS-1$
    public static final String PmPortKey = "port"; //$NON-NLS-1$
    public static final String PmSlotKey = "slot"; //$NON-NLS-1$
    public static final String BeginTestStage = "BeginTest"; //$NON-NLS-1$
    public static final String EndTestStage = "EndTest"; //$NON-NLS-1$

    private UICommand privateTestCommand;
    Constants constants = ConstantsManager.getInstance().getConstants();

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
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isPowerManagementTabSelected;

    public boolean getIsPowerManagementTabSelected()
    {
        return isPowerManagementTabSelected;
    }

    public void setIsPowerManagementTabSelected(boolean value)
    {
        if (isPowerManagementTabSelected != value)
        {
            isPowerManagementTabSelected = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabSelected")); //$NON-NLS-1$
        }
    }

    public HashMap<String, String> getPmOptionsMap()
    {
        HashMap<String, String> dict = new HashMap<String, String>();

        // Add well known pm options.
        if (getPmPort().getIsAvailable())
        {
            dict.put(PmPortKey, getPmPort().getEntity() == null ? "" : (String) getPmPort().getEntity()); //$NON-NLS-1$
        }
        if (getPmSlot().getIsAvailable())
        {
            dict.put(PmSlotKey, getPmSlot().getEntity() == null ? "" : (String) getPmSlot().getEntity()); //$NON-NLS-1$
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
            for (String pair : pmOptions.split("[,]", -1)) //$NON-NLS-1$
            {
                String[] array = pair.split("[=]", -1); //$NON-NLS-1$
                if (array.length == 2)
                {
                    dict.put(array[0], array[1]);
                }
                else if (array.length == 1)
                {
                    dict.put(array[0], ""); //$NON-NLS-1$
                }
            }
        }

        return dict;
    }

    public void setPmOptionsMap(HashMap<String, String> value)
    {
        String pmOptions = ""; //$NON-NLS-1$

        for (Map.Entry<String, String> pair : value.entrySet())
        {
            String k = pair.getKey();
            String v = pair.getValue();

            if (StringHelper.stringsEqual(k, PmPortKey))
            {
                getPmPort().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            }
            else if (StringHelper.stringsEqual(k, PmSlotKey))
            {
                getPmSlot().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            }
            else if (StringHelper.stringsEqual(k, PmSecureKey))
            {
                getPmSecure().setEntity(Boolean.parseBoolean(value.get(k)));

            }
            else
            {
                // Compose custom string from unknown pm options.
                if (StringHelper.isNullOrEmpty(v))
                {
                    pmOptions += StringFormat.format("%1$s,", k); //$NON-NLS-1$
                }
                else
                {
                    pmOptions += StringFormat.format("%1$s=%2$s,", k, v); //$NON-NLS-1$
                }
            }
        }

        if (!StringHelper.isNullOrEmpty(pmOptions))
        {
            getPmOptions().setEntity(pmOptions.substring(0, pmOptions.length() - 1));
        }
    }

    public String getPmProxyPreferences() {
        // Return null if power management is not enabled.
        if (!(Boolean) getIsPm().getEntity()) {
            return null;
        }

        // Pack back proxy items to the comma delimited string.
        StringBuilder builder = new StringBuilder();

        if (getPmProxyPreferencesList().getItems() != null) {
            List items = (List) getPmProxyPreferencesList().getItems();
            for (Object item : items) {

                builder.append(item);

                if (items.indexOf(item) < items.size() - 1) {
                    builder.append(",");    //$NON-NLS-1$
                }
            }
        }

        return builder.toString();
    }

    public void setPmProxyPreferences(String value) {
        // Create list from the provided comma delimited string.
        String[] array = value.split(",");    //$NON-NLS-1$
        List<String> list = new ArrayList<String>();

        for (String item : array) {
            list.add(item);
        }

        getPmProxyPreferencesList().setItems(list);
    }

    private ListModel pmProxyPreferencesList;

    public ListModel getPmProxyPreferencesList() {
        return pmProxyPreferencesList;
    }

    private void setPmProxyPreferencesList(ListModel value) {
        pmProxyPreferencesList = value;
    }

    private UICommand proxyUpCommand;

    public UICommand getProxyUpCommand() {
        return proxyUpCommand;
    }

    private void setProxyUpCommand(UICommand value) {
        proxyUpCommand = value;
    }

    private UICommand proxyDownCommand;

    public UICommand getProxyDownCommand() {
        return proxyDownCommand;
    }

    private void setProxyDownCommand(UICommand value) {
        proxyDownCommand = value;
    }

    private Integer postponedSpmPriority;

    public void setSpmPriorityValue(Integer value) {
        if (spmInitialized) {
            UpdateSpmPriority(value);
        } else {
            postponedSpmPriority = value;
        }
    }

    public int getSpmPriorityValue() {

        EntityModel selectedItem = (EntityModel) getSpmPriority().getSelectedItem();
        if (selectedItem != null) {
            return (Integer) selectedItem.getEntity();
        }

        return 0;
    }

    private ListModel spmPriority;

    public ListModel getSpmPriority() {
        return spmPriority;
    }

    private void setSpmPriority(ListModel value) {
        spmPriority = value;
    }

    public HostModel()
    {
        setTestCommand(new UICommand("Test", new ICommandTarget() { //$NON-NLS-1$
            @Override
            public void ExecuteCommand(UICommand command) {
                Test();
            }

            @Override
            public void ExecuteCommand(UICommand uiCommand, Object... parameters) {
                Test();
            }
        }));
        setProxyUpCommand(new UICommand("Up", new ICommandTarget() {    //$NON-NLS-1$
            @Override
            public void ExecuteCommand(UICommand command) {
                ProxyUp();
            }

            @Override
            public void ExecuteCommand(UICommand uiCommand, Object... parameters) {
                ProxyUp();
            }
        }));
        setProxyDownCommand(new UICommand("Down", new ICommandTarget() {    //$NON-NLS-1$
            @Override
            public void ExecuteCommand(UICommand command) {
                ProxyDown();
            }

            @Override
            public void ExecuteCommand(UICommand uiCommand, Object... parameters) {
                ProxyDown();
            }
        }));

        setName(new EntityModel());
        setHost(new EntityModel());
        setManagementIp(new EntityModel());
        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
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

        setPmProxyPreferencesList(new ListModel());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                UpdatePmCommandAvailability();
            }
        });

        setIsPm(new EntityModel());
        getIsPm().getEntityChangedEvent().addListener(this);
        getIsPm().setEntity(false);

        setIsPowerManagementTabValid(true);
        setIsGeneralTabValid(getIsPowerManagementTabValid());

        setSpmPriority(new ListModel());

        InitSpmPriorities();
    }

    private void ProxyUp() {
        if (getPmProxyPreferencesList().getItems() == null) {
            return;
        }

        List list = new ArrayList((List) getPmProxyPreferencesList().getItems());
        Object selectedItem = getPmProxyPreferencesList().getSelectedItem();
        int selectedItemIndex = list.indexOf(selectedItem);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex > 0) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex - 1, selectedItem);

            getPmProxyPreferencesList().setItems(list);
            getPmProxyPreferencesList().setSelectedItem(selectedItem);
        }
    }

    private void ProxyDown() {
        if (getPmProxyPreferencesList().getItems() == null) {
            return;
        }

        List list = new ArrayList((List) getPmProxyPreferencesList().getItems());
        Object selectedItem = getPmProxyPreferencesList().getSelectedItem();
        int selectedItemIndex = list.indexOf(selectedItem);

        // Check whether the selected item is first in the list.
        if (selectedItemIndex < list.size()) {
            list.remove(selectedItemIndex);
            list.add(selectedItemIndex + 1, selectedItem);

            getPmProxyPreferencesList().setItems(list);
            getPmProxyPreferencesList().setSelectedItem(selectedItem);
        }
    }

    boolean spmInitialized;
    int maxSpmPriority;
    int defaultSpmPriority;

    private void InitSpmPriorities() {

        AsyncDataProvider.GetMaxSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                HostModel model = (HostModel) target;

                model.maxSpmPriority = (Integer) returnValue;
                InitSpmPriorities1();
            }
        }));
    }

    private void InitSpmPriorities1() {

        AsyncDataProvider.GetDefaultSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                HostModel model = (HostModel) target;

                model.defaultSpmPriority = (Integer) returnValue;

                if (postponedSpmPriority != null) {
                    UpdateSpmPriority(postponedSpmPriority);
                }

                spmInitialized = true;
            }
        }));
    }

    private void UpdateSpmPriority(Integer value) {

        List<EntityModel> items = new ArrayList<EntityModel>();

        if (value == null) {
            value = defaultSpmPriority;
        }

        int neverValue = -1;
        EntityModel neverItem = new EntityModel(constants.neverTitle(), neverValue);
        items.add(neverItem);
        int lowValue = defaultSpmPriority / 2;
        items.add(new EntityModel(constants.lowTitle(), lowValue));
        items.add(new EntityModel(constants.normalTitle(), defaultSpmPriority));
        int highValue = defaultSpmPriority + (maxSpmPriority - defaultSpmPriority) / 2;
        items.add(new EntityModel(constants.highTitle(), highValue));

        // Determine whether to set custom SPM priority, and where.
        EntityModel selectedItem = null;

        int[] values = new int[] { neverValue, lowValue, defaultSpmPriority, highValue, maxSpmPriority + 1 };
        Integer prevValue = null;

        for (int i = 0; i < values.length; i++) {

            int currentValue = values[i];

            if (value == currentValue) {
                selectedItem = items.get(i);
                break;
            } else if (prevValue != null && value > prevValue && value < currentValue) {
                EntityModel customItem = new EntityModel("Custom (" + value + ")", value);//$NON-NLS-1$ //$NON-NLS-2$

                items.add(i, customItem);
                selectedItem = customItem;
                break;
            }

            prevValue = currentValue;
        }

        // Delete 'never' item if it's not selected.
        if (selectedItem != neverItem) {
            items.remove(neverItem);
        }

        getSpmPriority().setItems(items);
        getSpmPriority().setSelectedItem(selectedItem);
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
                    ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) result;
                    VDSGroup oldCluster = (VDSGroup) hostModel.getCluster().getSelectedItem();
                    storage_pool selectedDataCenter = (storage_pool) getDataCenter().getSelectedItem();

                    // Update selected cluster only if the returned cluster list is indeed the selected datacenter's
                    // clusters
                    if (clusters.isEmpty()
                            || clusters.size() > 0
                            && clusters.get(0)
                                    .getStoragePoolId()
                                    .getValue()
                                    .equals(selectedDataCenter.getId().getValue()))
                    {
                        hostModel.getCluster().setItems(clusters);

                        if (oldCluster != null)
                        {
                            VDSGroup newSelectedItem =
                                    Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getId()));
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
                    ArrayList<String> pmTypeList = (ArrayList<String>) result;
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
                hostModel.postGetPmOptions((ArrayList<String>) result);

            }
        };
        if (!StringHelper.isNullOrEmpty(pmType))
        {
            AsyncDataProvider.GetPmOptions(_asyncQuery, pmType);
        }
        else
        {
            postGetPmOptions(new ArrayList<String>());
        }
    }

    public void postGetPmOptions(ArrayList<String> pmOptions)
    {
        getPmPort().setIsAvailable(pmOptions.contains(PmPortKey));
        getPmSlot().setIsAvailable(pmOptions.contains(PmSlotKey));
        getPmSecure().setIsAvailable(pmOptions.contains(PmSecureKey));

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
        getPmProxyPreferencesList().setIsChangable((Boolean) getIsPm().getEntity());

        UpdatePmCommandAvailability();
    }

    private void UpdatePmCommandAvailability() {
        boolean isPm = (Boolean) getIsPm().getEntity();
        Object proxySelectedItem = getPmProxyPreferencesList().getSelectedItem();

        getTestCommand().setIsExecutionAllowed(isPm);
        getProxyUpCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getProxyDownCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
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

        setMessage(ConstantsManager.getInstance().getConstants().testingInProgressItWillTakeFewSecondsPleaseWaitMsg());
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
        param.setStoragePoolId(cluster.getStoragePoolId().getValue() != null ? cluster.getStoragePoolId()
                .getValue()
                .getValue() : NGuid.Empty);
        param.setFencingOptions(new ValueObjectMap(getPmOptionsMap(), false));
        param.setPmProxyPreferences(getPmProxyPreferences());

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
                String message;
                if (result != null && result.getReturnValue() != null
                        && result.getReturnValue().getReturnValue() != null) {
                    FenceStatusReturnValue fenceStatusReturnValue =
                            (FenceStatusReturnValue) result.getReturnValue().getReturnValue();
                    message = fenceStatusReturnValue.toString();
                } else {
                    message = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
                }
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
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new LengthValidation(255),
                new BaseI18NValidation() {
            @Override
            protected String composeRegex() {
                return "^[-_\\.0-9a-zA-Z]*$"; //$NON-NLS-1$
            }

            @Override
            protected String composeMessage() {
                return ConstantsManager.getInstance().getConstants().hostNameValidationMsg();
            }
        } });

        getHost().ValidateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(255),
                new HostAddressValidation() });

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

        setIsGeneralTabValid(getName().getIsValid()
                && getHost().getIsValid()
                && getPort().getIsValid()
                && getCluster().getIsValid());

        setIsPowerManagementTabValid(getManagementIp().getIsValid()
                && getPmUserName().getIsValid()
                && getPmPassword().getIsValid()
                && getPmType().getIsValid()
                && getPmPort().getIsValid()
                && getPmOptions().getIsValid());

        return getName().getIsValid()
                && getHost().getIsValid()
                && getPort().getIsValid()
                && getCluster().getIsValid()
                && getManagementIp().getIsValid()
                && getPmUserName().getIsValid()
                && getPmPassword().getIsValid()
                && getPmType().getIsValid()
                && getPmPort().getIsValid()
                && getPmOptions().getIsValid();
    }
}
