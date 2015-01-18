package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.NewHostNetworkQosModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NetworkProfilesModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ExternalSubnetModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class NetworkModel extends Model implements HasValidatedTabs
{
    private static final String CMD_APPROVE = "OnApprove"; //$NON-NLS-1$
    private static final String CMD_ABORT = "OnAbort"; //$NON-NLS-1$

    public final static HostNetworkQos EMPTY_HOST_NETWORK_QOS = createEmptyHostNetworkQos();

    public static HostNetworkQos createEmptyHostNetworkQos() {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
        qos.setId(Guid.Empty);
        return qos;
    }

    private EntityModel<String> privateName;
    private EntityModel<String> privateDescription;
    private EntityModel<Boolean> export;
    private ListModel<Provider> externalProviders;
    private ListModel<String> networkLabel;
    private EntityModel<String> neutronPhysicalNetwork;
    private EntityModel<String> privateComment;
    private EntityModel<Integer> privateVLanTag;
    private EntityModel<Boolean> privateIsStpEnabled;
    private EntityModel<Boolean> privateHasVLanTag;
    private ListModel<MtuSelector> mtuSelector;
    private EntityModel<Integer> mtu;
    private EntityModel<Boolean> privateIsVmNetwork;
    private ListModel<HostNetworkQos> qos;
    private boolean isSupportBridgesReportByVDSM = false;
    private boolean mtuOverrideSupported = false;
    private ListModel<StoragePool> privateDataCenters;
    private NetworkProfilesModel profiles;
    private EntityModel<Boolean> createSubnet;
    private ExternalSubnetModel subnetModel;
    private UICommand addQosCommand;
    private final Network network;
    private final ListModel sourceListModel;
    private boolean isGeneralTabValid;
    private boolean isVnicProfileTabValid;
    private boolean isSubnetTabValid;

    public NetworkModel(ListModel sourceListModel)
    {
        this(new Network(), sourceListModel);
    }

    public NetworkModel(Network network, ListModel sourceListModel)
    {
        addCommands();
        this.network = network;
        this.sourceListModel = sourceListModel;
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setDataCenters(new ListModel<StoragePool>());
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                syncWithBackend();
            }
        });
        setExport(new EntityModel<Boolean>(false));
        getExport().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                onExportChanged();
            }
        });
        setNeutronPhysicalNetwork(new EntityModel<String>());

        setNetworkLabel(new ListModel<String>());
        setExternalProviders(new ListModel<Provider>());
        initExternalProviderList();

        EntityModel<Boolean> stpEnabled = new EntityModel<Boolean>();
        stpEnabled.setEntity(false);
        setIsStpEnabled(stpEnabled);

        setVLanTag(new EntityModel<Integer>());
        EntityModel<Boolean> hasVlanTag = new EntityModel<Boolean>();
        hasVlanTag.setEntity(false);
        setHasVLanTag(hasVlanTag);
        getHasVLanTag().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateVlanTagChangeability();
            }
        });

        ListModel<MtuSelector> mtuSelector = new ListModel<MtuSelector>();
        mtuSelector.setItems(Arrays.asList(MtuSelector.values()));
        setMtuSelector(mtuSelector);
        mtuSelector.getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateMtuSelectorsChangeability();
            }
        });

        setMtu(new EntityModel<Integer>());

        EntityModel<Boolean> isVmNetwork = new EntityModel<Boolean>();
        isVmNetwork.setEntity(true);
        setIsVmNetwork(isVmNetwork);
        isVmNetwork.getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                toggleProfilesAvailability();
            }
        });

        EntityModel<Boolean> publicUse = new EntityModel<Boolean>();
        publicUse.setEntity(true);

        setProfiles(new NetworkProfilesModel());
        List<VnicProfileModel> profiles = new LinkedList<VnicProfileModel>();
        profiles.add(createDefaultProfile());
        getProfiles().setItems(profiles);

        setQos(new ListModel<HostNetworkQos>());

        EntityModel<Boolean> createSubnet = new EntityModel<Boolean>(false);
        setCreateSubnet(createSubnet);
        createSubnet.getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateSubnetChangeability();
            }
        });

        setSubnetModel(new ExternalSubnetModel());

        // Update changeability according to initial values
        updateVlanTagChangeability();
        updateSubnetChangeability();

        setIsGeneralTabValid(true);
        setIsVnicProfileTabValid(true);
    }

    private VnicProfileModel createDefaultProfile() {
        final VnicProfileModel defaultProfile = new NewVnicProfileModel();

        // make sure default profile's name is in sync with network's name
        defaultProfile.getName().setEntity(getName().getEntity());
        final IEventListener<EventArgs> networkNameListener = new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                defaultProfile.getName().setEntity(getName().getEntity());
            }
        };
        getName().getEntityChangedEvent().addListener(networkNameListener);

        // if user overrides default name, stop tracking network's name
        defaultProfile.getName().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (!defaultProfile.getName().getEntity().equals(getName().getEntity())) {
                    getName().getEntityChangedEvent().removeListener(networkNameListener);
                    defaultProfile.getName().getEntityChangedEvent().removeListener(this);
                }
            }
        });

        return defaultProfile;
    }

    private void initExternalProviderList() {
        AsyncQuery getProvidersQuery = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<Provider> providers = (List<Provider>) result;
                getExternalProviders().setItems(providers);
                selectExternalProvider();
            }
        });
        AsyncDataProvider.getInstance().getAllNetworkProviders(getProvidersQuery);
    }

    public EntityModel<String> getName()
    {
        return privateName;
    }

    private void setName(EntityModel<String> value)
    {
        privateName = value;
    }

    public EntityModel<String> getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel<String> value)
    {
        privateDescription = value;
    }

    public EntityModel<Boolean> getExport() {
        return export;
    }

    private void setExport(EntityModel<Boolean> value) {
        export = value;
    }

    public ListModel<Provider> getExternalProviders() {
        return externalProviders;
    }

    public void setExternalProviders(ListModel<Provider> externalProviders) {
        this.externalProviders = externalProviders;
    }

    public ListModel<String> getNetworkLabel() {
        return networkLabel;
    }

    public void setNetworkLabel(ListModel<String> networkLabel) {
        this.networkLabel = networkLabel;
    }

    public EntityModel<String> getNeutronPhysicalNetwork() {
        return neutronPhysicalNetwork;
    }

    private void setNeutronPhysicalNetwork(EntityModel<String> neutronPhysicalNetwork) {
        this.neutronPhysicalNetwork = neutronPhysicalNetwork;
    }

    public EntityModel<String> getComment() {
        return privateComment;
    }

    private void setComment(EntityModel<String> value) {
        privateComment = value;
    }

    public EntityModel<Integer> getVLanTag()
    {
        return privateVLanTag;
    }

    private void setVLanTag(EntityModel<Integer> value)
    {
        privateVLanTag = value;
    }

    public EntityModel<Boolean> getIsStpEnabled()
    {
        return privateIsStpEnabled;
    }

    private void setIsStpEnabled(EntityModel<Boolean> value)
    {
        privateIsStpEnabled = value;
    }

    public EntityModel<Boolean> getHasVLanTag()
    {
        return privateHasVLanTag;
    }

    private void setHasVLanTag(EntityModel<Boolean> value)
    {
        privateHasVLanTag = value;
    }

    public ListModel<MtuSelector> getMtuSelector()
    {
        return mtuSelector;
    }

    private void setMtuSelector(ListModel<MtuSelector> value)
    {
        mtuSelector = value;
    }

    public EntityModel<Integer> getMtu()
    {
        return mtu;
    }

    private void setMtu(EntityModel<Integer> value)
    {
        mtu = value;
    }

    public EntityModel<Boolean> getIsVmNetwork()
    {
        return privateIsVmNetwork;
    }

    public void setIsVmNetwork(EntityModel<Boolean> value)
    {
        privateIsVmNetwork = value;
    }

    public ListModel<HostNetworkQos> getQos() {
        return qos;
    }

    private void setQos(ListModel<HostNetworkQos> qos) {
        this.qos = qos;
    }

    public boolean isSupportBridgesReportByVDSM() {
        return isSupportBridgesReportByVDSM;
    }

    public void setSupportBridgesReportByVDSM(boolean isSupportBridgesReportByVDSM) {
        if (!isSupportBridgesReportByVDSM) {
            getIsVmNetwork().setEntity(true);
            getIsVmNetwork().setChangeProhibitionReason(ConstantsManager.getInstance().getMessages()
                    .bridlessNetworkNotSupported(getSelectedDc().getCompatibilityVersion().toString()));
            getIsVmNetwork().setIsChangable(false);
        } else {
            if (this.isSupportBridgesReportByVDSM != isSupportBridgesReportByVDSM) {
                initIsVm();
            }
            getIsVmNetwork().setIsChangable(true);
        }
        this.isSupportBridgesReportByVDSM = isSupportBridgesReportByVDSM;
    }

    public boolean isMTUOverrideSupported() {
        return mtuOverrideSupported;
    }

    public void setMTUOverrideSupported(boolean mtuOverrideSupported) {
        this.mtuOverrideSupported = mtuOverrideSupported;
        updateMtuSelectorsChangeability();
    }

    public ListModel<StoragePool> getDataCenters()
    {
        return privateDataCenters;
    }

    private void setDataCenters(ListModel<StoragePool> value)
    {
        privateDataCenters = value;
    }

    public NetworkProfilesModel getProfiles()
    {
        return profiles;
    }

    private void setProfiles(NetworkProfilesModel value)
    {
        profiles = value;
    }

    public Network getNetwork() {
        return network;
    }

    public ListModel getSourceListModel() {
        return sourceListModel;
    }

    public EntityModel<Boolean> getCreateSubnet() {
        return createSubnet;
    }

    private void setCreateSubnet(EntityModel<Boolean> createSubnet) {
        this.createSubnet = createSubnet;
    }

    public ExternalSubnetModel getSubnetModel() {
        return subnetModel;
    }

    private void setSubnetModel(ExternalSubnetModel subnetModel) {
        this.subnetModel = subnetModel;
    }

    public UICommand getAddQosCommand() {
        return addQosCommand;
    }

    public boolean getIsGeneralTabValid() {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value) {
        if (isGeneralTabValid != value) {
            isGeneralTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
        }
    }

    public boolean getIsVnicProfileTabValid() {
        return isVnicProfileTabValid;
    }

    public void setIsVnicProfileTabValid(boolean value) {
        if (isVnicProfileTabValid != value) {
            isVnicProfileTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsVnicProfileTabValid")); //$NON-NLS-1$
        }
    }

    public boolean getIsSubnetTabValid() {
        return isSubnetTabValid;
    }

    public void setIsSubnetTabValid(boolean value) {
        if (isSubnetTabValid != value) {
            isSubnetTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSubnetTabValid")); //$NON-NLS-1$
        }
    }

    private boolean validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^[A-Za-z0-9_]{1,15}$"); //$NON-NLS-1$
        tempVar.setMessage(ConstantsManager.getInstance().getConstants().nameMustContainAlphanumericMaxLenMsg());
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setIsNegate(true);
        tempVar2.setExpression("^(bond)"); //$NON-NLS-1$
        tempVar2.setMessage(ConstantsManager.getInstance().getConstants().networkNameStartMsg());
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(40);
        getDescription().validateEntity(new IValidation[] { tempVar3 });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        getVLanTag().setIsValid(true);
        if (getHasVLanTag().getEntity())
        {
            IntegerValidation tempVar4 = new IntegerValidation();
            tempVar4.setMinimum(0);
            tempVar4.setMaximum(4094);
            getVLanTag().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
        }

        IntegerValidation tempVar5 = new IntegerValidation();
        tempVar5.setMinimum(68);
        getMtu().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar5 });

        getExternalProviders().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        boolean subnetValid = true;
        if (getExport().getEntity() && getCreateSubnet().getEntity()) {
            subnetValid = getSubnetModel().validate();
        }

        boolean profilesValid = true;
        Iterable<VnicProfileModel> profiles = getProfiles().getItems();
        for (VnicProfileModel profileModel : profiles) {
            if (!profileModel.validate()) {
                profilesValid = false;
            }
        }

        getNetworkLabel().validateSelectedItem(new IValidation[] { new AsciiNameValidation() });

        setValidTab(TabName.GENERAL_TAB, getName().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid()
                && getMtu().getIsValid() && getExternalProviders().getIsValid() && getComment().getIsValid()
                && getNetworkLabel().getIsValid());
        setValidTab(TabName.SUBNET_TAB, subnetValid);
        setValidTab(TabName.PROFILES_TAB, profilesValid);

        ValidationCompleteEvent.fire(getEventBus(), this);
        return allTabsValid();
    }

    protected boolean isCustomMtu() {
        return MtuSelector.customMtu == getMtuSelector().getSelectedItem();
    }

    public void syncWithBackend() {
        final StoragePool dc = getSelectedDc();
        if (dc == null) {
            return;
        }

        // Get IsSupportBridgesReportByVDSM
        boolean isSupportBridgesReportByVDSM =
                (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.SupportBridgesReportByVDSM,
                        dc.getCompatibilityVersion().toString());
        setSupportBridgesReportByVDSM(isSupportBridgesReportByVDSM);

        // Get IsMTUOverrideSupported
        boolean isMTUOverrideSupported =
                (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MTUOverrideSupported,
                        dc.getCompatibilityVersion().toString());

        setMTUOverrideSupported(isMTUOverrideSupported);

        AsyncQuery query = new AsyncQuery();
        query.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                Collection<HostNetworkQos> qos = (Collection<HostNetworkQos>) returnValue;
                getQos().setItems(qos);
                getQos().setSelectedItem(Linq.findHostNetworkQosById(qos, getNetwork().getQosId()));
            }
        };
        AsyncDataProvider.getInstance().getAllHostNetworkQos(dc.getId(), query);

        updateDcLabels();

        onExportChanged();
        getProfiles().updateDcId(dc.getId());
    }

    private void addCommands() {
        UICommand tempVar2 = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
        UICommand tempVar3 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar3);
        addQosCommand = new UICommand("AddQos", this); //$NON-NLS-1$
        addQosCommand.setTitle(ConstantsManager.getInstance().getConstants().newNetworkQosButton());
    }

    public StoragePool getSelectedDc() {
        return getDataCenters().getSelectedItem();
    }

    public void flush() {
        network.setDataCenterId(getSelectedDc().getId());
        network.setName(getName().getEntity());
        network.setStp(getIsStpEnabled().getEntity());
        network.setDescription(getDescription().getEntity());
        network.setComment(getComment().getEntity());
        network.setVmNetwork(getIsVmNetwork().getEntity());

        String label = getExport().getEntity() ?
                getNeutronPhysicalNetwork().getEntity() : getNetworkLabel().getSelectedItem();
        network.setLabel(label == null || !label.isEmpty() ? label : null);

        network.setMtu(0);
        if (getMtu().getIsChangable())
        {
            network.setMtu(Integer.parseInt(getMtu().getEntity().toString()));
        }

        network.setVlanId(null);
        if (getHasVLanTag().getEntity())
        {
            network.setVlanId(Integer.parseInt(getVLanTag().getEntity().toString()));
        }

        for (VnicProfileModel profileModel : getProfiles().getItems()) {
            profileModel.flush();
        }

        if (getQos().getIsChangable()) {
            HostNetworkQos qos = getQos().getSelectedItem();
            network.setQosId(qos == EMPTY_HOST_NETWORK_QOS ? null : qos.getId());
        }
    }

    protected abstract void executeSave();

    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        if (succeeded) {
            performProfilesActions(networkGuid);
            stopProgress();
            cancel();
        }
    }

    private void performProfilesActions(Guid networkGuid) {
        List<VnicProfileModel> profileModels = (List<VnicProfileModel>) getProfiles().getItems();
        if (profileModels.isEmpty() || !getProfiles().getIsAvailable()) {
            return;
        }

        networkGuid = networkGuid == null ? getNetwork().getId() : networkGuid;
        ArrayList<VdcActionParametersBase> paramlist = new ArrayList<VdcActionParametersBase>();
        for (VnicProfileModel profileModel : profileModels)
        {
            if (!StringHelper.isNullOrEmpty(profileModel.getProfile().getName())) {
                VnicProfile vnicProfile = profileModel.getProfile();
                vnicProfile.setNetworkId(networkGuid);
                VnicProfileParameters parameters = new VnicProfileParameters(vnicProfile);
                parameters.setPublicUse(profileModel.getPublicUse().getEntity());
                paramlist.add(parameters);
            }
        }
        Frontend.getInstance().runMultipleActions(VdcActionType.AddVnicProfile,
                paramlist,
                (IFrontendActionAsyncCallback) null); // cast is required to avoid overload ambiguity
    }

    protected void toggleProfilesAvailability() {
        getProfiles().setIsAvailable(getIsVmNetwork().getEntity());
    }

    void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    private void addQos() {
        NewHostNetworkQosModel qosModel = new NewHostNetworkQosModel(this, getSelectedDc()) {

            @Override
            protected void postSaveAction(boolean succeeded) {
                if (succeeded) {
                    List<HostNetworkQos> qosItems =
                            new ArrayList<HostNetworkQos>(NetworkModel.this.getQos().getItems());
                    qosItems.add(1, getQos());
                    NetworkModel.this.getQos().setItems(qosItems);
                    NetworkModel.this.getQos().setSelectedItem(getQos());
                }
                super.postSaveAction(succeeded);
            }

            @Override
            protected void cancel() {
                sourceListModel.setConfirmWindow(null);
            }
        };
        qosModel.getDataCenters().setIsChangable(false);
        sourceListModel.setConfirmWindow(qosModel);
    }

    public void onSave()
    {
        if (!validate())
        {
            return;
        }

        if (isManagement()) {
            ConfirmationModel confirmationModel = new ConfirmationModel();
            confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().updateManagementNetworkWarning());
            UICommand cmdOk = UICommand.createDefaultOkUiCommand(CMD_APPROVE, this);
            confirmationModel.getCommands().add(cmdOk);
            UICommand cmdCancel = UICommand.createCancelUiCommand(CMD_ABORT, this); //$NON-NLS-1$
            confirmationModel.getCommands().add(cmdCancel);
            sourceListModel.setConfirmWindow(confirmationModel);
        } else {
            onApprove();
        }
    }

    private void onApprove() {
        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    private void onAbort() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getAddQosCommand()) {
            addQos();
        } else if (CMD_APPROVE.equals(command.getName())) {
            onAbort();
            onApprove();
        } else if (CMD_ABORT.equals(command.getName())) {
            onAbort();
        }
    }

    protected abstract boolean isManagement();

    protected abstract void initMtu();

    protected abstract void initIsVm();

    protected abstract void selectExternalProvider();

    protected void onExportChanged() {
        boolean externalNetwork = getExport().getEntity();

        getNetworkLabel().setIsChangable(!externalNetwork);
        getNeutronPhysicalNetwork().setIsChangable(externalNetwork);
        getQos().setIsChangable(!externalNetwork);
        getAddQosCommand().setIsExecutionAllowed(!externalNetwork);

        updateMtuSelectorsChangeability();
    }

    private void updateDcLabels() {
        AsyncDataProvider.getInstance().getNetworkLabelsByDataCenterId(getSelectedDc().getId(),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        String label = getNetworkLabel().getSelectedItem();
                        getNetworkLabel().setItems((Collection<String>) returnValue);
                        getNetworkLabel().setSelectedItem(label);
                        onExportChanged();
                    }
                }));
    }

    private void updateVlanTagChangeability() {
        getVLanTag().setIsChangable(getHasVLanTag().getEntity());
    }

    private void setMtuSelectorsChangeability(boolean isChangeable, String prohibitionReason) {
        if (!isChangeable) {
            getMtuSelector().setChangeProhibitionReason(prohibitionReason);
            getMtu().setChangeProhibitionReason(prohibitionReason);
        }

        getMtuSelector().setIsChangable(isChangeable);
        getMtu().setIsChangable(isChangeable && isCustomMtu());
    }

    protected void updateMtuSelectorsChangeability() {

        if (getSelectedDc() != null && !isMTUOverrideSupported()) {
            setMtuSelectorsChangeability(false, ConstantsManager.getInstance().getMessages()
                    .mtuOverrideNotSupported(getSelectedDc().getCompatibilityVersion().toString()));
            return;
        }

        if (getExport().getEntity()) {
            setMtuSelectorsChangeability(false, null);
            return;
        }

        setMtuSelectorsChangeability(true, null);
    }

    private void updateSubnetChangeability() {
        getSubnetModel().toggleChangeability(getCreateSubnet().getEntity());
    }

    public enum MtuSelector {
        defaultMtu(ConstantsManager.getInstance()
                .getMessages()
                .defaultMtu((Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.DefaultMTU))),
        customMtu(ConstantsManager.getInstance().getConstants().customMtu());

        private String description;

        private MtuSelector(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
