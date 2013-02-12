package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetNewVdsFenceStatusParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.NGuid;
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
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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

    private EntityModel consoleAddress;

    public void setConsoleAddress(EntityModel consoleAddress) {
        this.consoleAddress = consoleAddress;
    }

    public EntityModel getConsoleAddress() {
        return consoleAddress;
    }

    private EntityModel consoleAddressEnabled;

    public EntityModel getConsoleAddressEnabled() {
        return consoleAddressEnabled;
    }

    public void setConsoleAddressEnabled(EntityModel consoleAddressEnabled) {
        this.consoleAddressEnabled = consoleAddressEnabled;
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

    private EntityModel pmSecondaryIp;

    public EntityModel getPmSecondaryIp() {
        return pmSecondaryIp;
    }

    private void setPmSecondaryIp(EntityModel value) {
        pmSecondaryIp = value;
    }

    private EntityModel pmSecondaryPort;

    public EntityModel getPmSecondaryPort() {
        return pmSecondaryPort;
    }

    private void setPmSecondaryPort(EntityModel value) {
        pmSecondaryPort = value;
    }

    private EntityModel pmSecondaryUserName;

    public EntityModel getPmSecondaryUserName() {
        return pmSecondaryUserName;
    }

    private void setPmSecondaryUserName(EntityModel value) {
        pmSecondaryUserName = value;
    }

    private EntityModel pmSecondaryPassword;

    public EntityModel getPmSecondaryPassword() {
        return pmSecondaryPassword;
    }

    private void setPmSecondaryPassword(EntityModel value) {
        pmSecondaryPassword = value;
    }

    private ListModel pmSecondaryType;

    public ListModel getPmSecondaryType() {
        return pmSecondaryType;
    }

    private void setPmSecondaryType(ListModel value) {
        pmSecondaryType = value;
    }

    private EntityModel pmSecondaryOptions;

    public EntityModel getPmSecondaryOptions() {
        return pmSecondaryOptions;
    }

    private void setPmSecondaryOptions(EntityModel value) {
        pmSecondaryOptions = value;
    }

    private EntityModel pmSecondarySecure;

    public EntityModel getPmSecondarySecure() {
        return pmSecondarySecure;
    }

    private void setPmSecondarySecure(EntityModel value) {
        pmSecondarySecure = value;
    }

    public Map<String, String> getPmSecondaryOptionsMap() {

        // For secondary map determine (workarround) if it's was specified
        // by checking secondary PM fields.
        if (!isEntityModelEmpty(getPmSecondaryIp())
            || !isEntityModelEmpty(getPmSecondaryUserName())
            || !isEntityModelEmpty(getPmSecondaryPassword())) {

            return getPmOptionsMapInternal(getPmSecondaryPort(), getPmSecondarySlot(), getPmSecondarySecure(), getPmSecondaryOptions());
        }

        return new HashMap<String, String>();
    }

    public void setPmSecondaryOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmSecondaryPort(), getPmSecondarySlot(), getPmSecondarySecure(), getPmSecondaryOptions());
    }

    private EntityModel pmSecondarySlot;

    public EntityModel getPmSecondarySlot() {
        return pmSecondarySlot;
    }

    private void setPmSecondarySlot(EntityModel value) {
        pmSecondarySlot = value;
    }


    private EntityModel pmSecondaryConcurrent;

    public EntityModel getPmSecondaryConcurrent() {
        return pmSecondaryConcurrent;
    }

    private void setPmSecondaryConcurrent(EntityModel value) {
        pmSecondaryConcurrent = value;
    }

    private ListModel pmVariants;

    public ListModel getPmVariants() {
        return pmVariants;
    }

    private void setPmVariants(ListModel value) {
        pmVariants = value;
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

    public Map<String, String> getPmOptionsMap() {
        return getPmOptionsMapInternal(getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
    }

    public void setPmOptionsMap(Map<String, String> value) {
        setPmOptionsMapInternal(value, getPmPort(), getPmSlot(), getPmSecure(), getPmOptions());
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
        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setCluster(new ListModel());
        getCluster().getSelectedItemChangedEvent().addListener(this);
        setPort(new EntityModel());
        setRootPassword(new EntityModel());
        setOverrideIpTables(new EntityModel());
        getOverrideIpTables().setEntity(false);


        IEventListener pmListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                UpdatePmModels();
            }
        };

        // Initialize primary PM fields.
        setManagementIp(new EntityModel());
        setPmUserName(new EntityModel());
        setPmPassword(new EntityModel());
        setPmType(new ListModel());
        getPmType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmPort(new EntityModel());
        getPmPort().setIsAvailable(false);
        setPmSlot(new EntityModel());
        getPmSlot().setIsAvailable(false);
        setPmOptions(new EntityModel());
        setPmSecure(new EntityModel());
        getPmSecure().setIsAvailable(false);
        getPmSecure().setEntity(false);

        // Initialize secondary PM fields.
        setPmSecondaryIp(new EntityModel());
        setPmSecondaryUserName(new EntityModel());
        setPmSecondaryPassword(new EntityModel());
        setPmSecondaryType(new ListModel());
        getPmSecondaryType().getSelectedItemChangedEvent().addListener(pmListener);
        setPmSecondaryPort(new EntityModel());
        getPmSecondaryPort().setIsAvailable(false);
        setPmSecondarySlot(new EntityModel());
        getPmSecondarySlot().setIsAvailable(false);
        setPmSecondaryOptions(new EntityModel());
        setPmSecondarySecure(new EntityModel());
        getPmSecondarySecure().setIsAvailable(false);
        getPmSecondarySecure().setEntity(false);

        // Initialize other PM fields.
        setPmSecondaryConcurrent(new EntityModel());
        getPmSecondaryConcurrent().setEntity(false);

        setPmVariants(new ListModel());
        List<String> pmVariants = new ArrayList<String>();
        pmVariants.add("Primary");      //$NON-NLS-1$
        pmVariants.add("Secondary");    //$NON-NLS-1$
        getPmVariants().setItems(pmVariants);
        getPmVariants().setSelectedItem(pmVariants.get(0));

        setPmProxyPreferencesList(new ListModel());
        getPmProxyPreferencesList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
               UpdatePmModels();
            }
        });

        setConsoleAddress(new EntityModel());
        getConsoleAddress().setEntity(null);
        setConsoleAddressEnabled(new EntityModel());
        getConsoleAddressEnabled().setEntity(false);
        getConsoleAddressEnabled().getEntityChangedEvent().addListener(this);

        setIsPm(new EntityModel());
        getIsPm().getEntityChangedEvent().addListener(pmListener);
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
        } else if (sender == getConsoleAddressEnabled()) {
            consoleAddressChanged();
        }
    }

    private void consoleAddressChanged() {
        boolean enabled = (Boolean) getConsoleAddressEnabled().getEntity();
        getConsoleAddress().setIsChangable(enabled);
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
            AsyncDataProvider.GetPmTypeList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {

                    ArrayList<String> pmTypes = (ArrayList<String>) returnValue;
                    updatePmTypeList(pmTypes, getPmType());
                    updatePmTypeList(pmTypes, getPmSecondaryType());
                }
            }), cluster.getcompatibility_version());
        }
    }

    private void updatePmTypeList(List<String> pmTypes, ListModel model) {

        String pmType = (String) model.getSelectedItem();

        model.setItems(pmTypes);

        if (pmTypes.contains(pmType)) {
            model.setSelectedItem(pmType);
        }
    }

    private void setPmOptionsMapInternal(Map<String,String> value, EntityModel port, EntityModel slot, EntityModel secure, EntityModel options) {

        StringBuilder pmOptions = new StringBuilder();

        for (Map.Entry<String, String> pair : value.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();

            if (StringHelper.stringsEqual(k, PmPortKey)) {
                port.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (StringHelper.stringsEqual(k, PmSlotKey)) {
                slot.setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k)); //$NON-NLS-1$

            } else if (StringHelper.stringsEqual(k, PmSecureKey)) {
                secure.setEntity(Boolean.parseBoolean(value.get(k)));

            } else {
                // Compose custom string from unknown pm options.
                if (StringHelper.isNullOrEmpty(v)) {
                    pmOptions.append(k).append(","); //$NON-NLS-1$
                } else {
                    pmOptions.append(k).append("=").append(v).append(","); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        String pmOptionsValue = pmOptions.toString();
        if (!StringHelper.isNullOrEmpty(pmOptionsValue)) {
            options.setEntity(pmOptionsValue.substring(0, pmOptionsValue.length() - 1));
        }
    }

    private Map<String,String> getPmOptionsMapInternal(EntityModel port, EntityModel slot, EntityModel secure, EntityModel options) {

        Map<String, String> dict = new HashMap<String, String>();

        if ((Boolean) getIsPm().getEntity()) {
            // Add well known pm options.
            if (port.getIsAvailable() && port.getEntity() != null) {
                dict.put(PmPortKey, (String) port.getEntity());
            }
            if (slot.getIsAvailable() && slot.getEntity() != null) {
                dict.put(PmSlotKey, (String) slot.getEntity());
            }
            if (secure.getIsAvailable()) {
                dict.put(PmSecureKey, secure.getEntity().toString());
            }

            // Add unknown pm options.
            // Assume Validate method was called before this getter.
            String pmOptions = (String) options.getEntity();
            if (!StringHelper.isNullOrEmpty(pmOptions)) {
                for (String pair : pmOptions.split("[,]", -1)) //$NON-NLS-1$
                {
                    String[] array = pair.split("[=]", -1); //$NON-NLS-1$
                    if (array.length == 2) {
                        dict.put(array[0], array[1]);
                    } else if (array.length == 1) {
                        dict.put(array[0], ""); //$NON-NLS-1$
                    }
                }
            }
        }

        return dict;
    }

    private void UpdatePmModels()
    {
        boolean isPm = (Boolean) getIsPm().getEntity();

        // Update primary PM fields.
        getManagementIp().setIsChangable(isPm);
        getManagementIp().setIsValid(true);
        getPmUserName().setIsChangable(isPm);
        getPmUserName().setIsValid(true);
        getPmPassword().setIsChangable(isPm);
        getPmPassword().setIsValid(true);
        getPmType().setIsChangable(isPm);
        getPmType().setIsValid(true);
        getPmPort().setIsChangable(isPm);
        getPmPort().setIsValid(true);
        getPmProxyPreferencesList().setIsChangable((Boolean) getIsPm().getEntity());
        Object proxySelectedItem = getPmProxyPreferencesList().getSelectedItem();
        getTestCommand().setIsExecutionAllowed(isPm);
        getProxyUpCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getProxyDownCommand().setIsExecutionAllowed(isPm && proxySelectedItem != null);
        getPmSlot().setIsChangable(isPm);
        getPmOptions().setIsChangable(isPm);
        getPmOptions().setIsValid(true);
        getPmSecure().setIsChangable(isPm);

        String pmType = (String) getPmType().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(pmType)) {
            AsyncDataProvider.GetPmOptions(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {

                    List<String> pmOptions = (ArrayList<String>) returnValue;

                    getPmPort().setIsAvailable(pmOptions.contains(PmPortKey));
                    getPmSlot().setIsAvailable(pmOptions.contains(PmSlotKey));
                    getPmSecure().setIsAvailable(pmOptions.contains(PmSecureKey));
                }
            }), pmType);
        } else {
            getPmPort().setIsAvailable(false);
            getPmSlot().setIsAvailable(false);
            getPmSecure().setIsAvailable(false);
        }


        // Update secondary PM fields.
        getPmSecondaryIp().setIsChangable(isPm);
        getPmSecondaryIp().setIsValid(true);
        getPmSecondaryUserName().setIsChangable(isPm);
        getPmSecondaryUserName().setIsValid(true);
        getPmSecondaryPassword().setIsChangable(isPm);
        getPmSecondaryPassword().setIsValid(true);
        getPmSecondaryType().setIsChangable(isPm);
        getPmSecondaryType().setIsValid(true);
        getPmSecondaryPort().setIsChangable(isPm);
        getPmSecondaryPort().setIsValid(true);
        getPmSecondarySlot().setIsChangable(isPm);
        getPmSecondaryOptions().setIsChangable(isPm);
        getPmSecondaryOptions().setIsValid(true);
        getPmSecondarySecure().setIsChangable(isPm);

        String pmSecondaryType = (String) getPmSecondaryType().getSelectedItem();
        if (!StringHelper.isNullOrEmpty(pmSecondaryType)) {
            AsyncDataProvider.GetPmOptions(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {

                    List<String> pmOptions = (ArrayList<String>) returnValue;

                    getPmSecondaryPort().setIsAvailable(pmOptions.contains(PmPortKey));
                    getPmSecondarySlot().setIsAvailable(pmOptions.contains(PmSlotKey));
                    getPmSecondarySecure().setIsAvailable(pmOptions.contains(PmSecureKey));
                }
            }), pmSecondaryType);
        } else {
            getPmSecondaryPort().setIsAvailable(false);
            getPmSecondarySlot().setIsAvailable(false);
            getPmSecondarySecure().setIsAvailable(false);
        }


        // Update other PM fields.
        getPmVariants().setIsChangable(isPm);
        getPmSecondaryConcurrent().setIsChangable(isPm);
        getTestCommand().setIsExecutionAllowed(isPm);
    }

    private boolean isPmPrimarySelected() {

        List items = (List) getPmVariants().getItems();
        Object selectedItem = getPmVariants().getSelectedItem();

        return items.indexOf(selectedItem) == 0;
    }

        // Validate user input.
    public void Test()
        {
        Boolean isPmEnabled = (Boolean) getIsPm().getEntity();
        boolean isPrimary = isPmPrimarySelected();
            getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
            ValidatePmModels(isPrimary);

        if (isPrimary && (!getManagementIp().getIsValid()
            || !getPmUserName().getIsValid()
            || !getPmPassword().getIsValid()
            || !getPmType().getIsValid()
            || !getPmPort().getIsValid()
            || !getPmOptions().getIsValid())) {
            return;
        }

        if (!isPrimary && (!getPmSecondaryIp().getIsValid()
            || !getPmSecondaryUserName().getIsValid()
            || !getPmSecondaryPassword().getIsValid()
            || !getPmSecondaryType().getIsValid()
            || !getPmSecondaryPort().getIsValid()
            || !getPmSecondaryOptions().getIsValid())) {
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
        param.setOrder(isPrimary ? FenceAgentOrder.Primary : FenceAgentOrder.Secondary);
        param.setManagementIp(isPrimary ? (String) getManagementIp().getEntity() : (String) getPmSecondaryIp().getEntity());
        param.setPmType(isPrimary ? (String) getPmType().getSelectedItem() : (String) getPmSecondaryType().getSelectedItem());
        param.setUser(isPrimary ? (String) getPmUserName().getEntity() : (String) getPmSecondaryUserName().getEntity());
        param.setPassword(isPrimary ? (String) getPmPassword().getEntity() : (String) getPmSecondaryPassword().getEntity());
        param.setStoragePoolId(cluster.getStoragePoolId().getValue() != null ? cluster.getStoragePoolId()
            .getValue()
            .getValue() : NGuid.Empty);
        param.setFencingOptions(new ValueObjectMap(getPmOptionsMap(), false));
        param.setPmProxyPreferences(getPmProxyPreferences());

        Frontend.RunQuery(VdcQueryType.GetNewVdsFenceStatus, param, new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response == null || !response.getSucceeded()) {
                    String message;
                    if (response != null && response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                (FenceStatusReturnValue) response.getReturnValue();
                        message = fenceStatusReturnValue.toString();
                    } else {
                        message = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
                    }
                    setMessage(message);
                    getTestCommand().setIsExecutionAllowed(true);
                } else {

                    if (response != null && response.getReturnValue() != null) {
                        FenceStatusReturnValue fenceStatusReturnValue =
                                (FenceStatusReturnValue) response.getReturnValue();
                        String message = fenceStatusReturnValue.toString();
                        setMessage(message);
                        getTestCommand().setIsExecutionAllowed(true);
                    }

                }
            }
        }
                , true));
    }

    private void ValidatePmModels(boolean primary)
    {
        EntityModel ip = primary ? getManagementIp() : getPmSecondaryIp();
        EntityModel userName = primary ? getPmUserName() : getPmSecondaryUserName();
        EntityModel password = primary ? getPmPassword() : getPmSecondaryPassword();
        ListModel type = primary ? getPmType() : getPmSecondaryType();
        EntityModel port = primary ? getPmPort() : getPmSecondaryPort();
        EntityModel options = primary ? getPmOptions() : getPmSecondaryOptions();

        ip.ValidateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        userName.ValidateEntity(new IValidation[] {new NotEmptyValidation()});
        password.ValidateEntity(new IValidation[] {new NotEmptyValidation()});
        type.ValidateSelectedItem(new IValidation[] {new NotEmptyValidation()});
        port.ValidateEntity(new IValidation[] {new IntegerValidation(1, 65535)});
        options.ValidateEntity(new IValidation[] {new KeyValuePairValidation(true)});
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

        getPort().ValidateEntity(new IValidation[] {new NotEmptyValidation(), new IntegerValidation(1, 65535)});

        if ((Boolean) getConsoleAddressEnabled().getEntity()) {
            getConsoleAddress().ValidateEntity(new IValidation[] {new NotEmptyValidation(), new HostAddressValidation()});
        } else {
            // the console address is ignored so can not be invalid
            getConsoleAddress().setIsValid(true);
        }

        getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        if ((Boolean) getIsPm().getEntity())
        {
            // If PM enabled primary fencing options must be specified, ensure that.
            ValidatePmModels(true);

            // Secondary fencing options aren't mandatory, only ensure there was set
            // if one of the related fields was filled.
            if (!isEntityModelEmpty(getPmSecondaryIp())
                || !isEntityModelEmpty(getPmSecondaryUserName())
                || !isEntityModelEmpty(getPmSecondaryPassword())
                || !isEntityModelEmpty(getPmSecondaryPort())
                || !isEntityModelEmpty(getPmSecondarySlot())
                || !isEntityModelEmpty(getPmSecondaryOptions())) {

                getPmSecondaryIp().setIsValid(true);
                getPmSecondaryUserName().setIsValid(true);
                getPmSecondaryPassword().setIsValid(true);
                getPmSecondaryPort().setIsValid(true);
                getPmSecondarySlot().setIsValid(true);
                getPmSecondaryOptions().setIsValid(true);

                ValidatePmModels(false);
            }
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
                && getPmOptions().getIsValid()

                && getPmSecondaryIp().getIsValid()
                && getPmSecondaryUserName().getIsValid()
                && getPmSecondaryPassword().getIsValid()
                && getPmSecondaryType().getIsValid()
                && getPmSecondaryPort().getIsValid()
                && getPmSecondaryOptions().getIsValid());
        return getIsGeneralTabValid()
            && getIsPowerManagementTabValid() && getConsoleAddress().getIsValid();
    }

    private boolean isEntityModelEmpty(EntityModel model) {
        return !(model.getEntity() != null && model.getEntity() != "");
    }
}
