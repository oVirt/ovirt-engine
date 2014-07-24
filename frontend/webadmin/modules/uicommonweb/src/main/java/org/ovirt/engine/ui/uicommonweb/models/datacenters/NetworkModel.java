package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
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

public abstract class NetworkModel extends Model
{
    private static final String CMD_APPROVE = "OnApprove"; //$NON-NLS-1$
    private static final String CMD_ABORT = "OnAbort"; //$NON-NLS-1$
    protected static final String ENGINE_NETWORK =
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private EntityModel<String> privateName;
    private EntityModel<String> privateDescription;
    private EntityModel<Boolean> export;
    private ListModel<Provider> externalProviders;
    private ListModel<String> networkLabel;
    private Collection<String> dcLabels;
    private EntityModel<String> privateComment;
    private EntityModel<Integer> privateVLanTag;
    private EntityModel<Boolean> privateIsStpEnabled;
    private EntityModel<Boolean> privateHasVLanTag;
    private ListModel<MtuSelector> mtuSelector;
    private EntityModel<Integer> mtu;
    private EntityModel<Boolean> privateIsVmNetwork;
    private ListModel<NetworkQoS> qos;
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
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                syncWithBackend();
            }
        });
        setExport(new EntityModel<Boolean>(false));
        getExport().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onExportChanged();
            }
        });

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
        getHasVLanTag().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateVlanTagChangeability();
            }
        });

        ListModel<MtuSelector> mtuSelector = new ListModel<MtuSelector>();
        mtuSelector.setItems(Arrays.asList(MtuSelector.values()));
        setMtuSelector(mtuSelector);
        mtuSelector.getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateMtuSelectorsChangeability();
            }
        });

        setMtu(new EntityModel<Integer>());

        EntityModel<Boolean> isVmNetwork = new EntityModel<Boolean>();
        isVmNetwork.setEntity(true);
        setIsVmNetwork(isVmNetwork);
        isVmNetwork.getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                toggleProfilesAvailability();
            }
        });

        EntityModel<Boolean> publicUse = new EntityModel<Boolean>();
        publicUse.setEntity(true);

        setProfiles(new NetworkProfilesModel());
        List<VnicProfileModel> profiles = new LinkedList<VnicProfileModel>();
        profiles.add(createDefaultProfile());
        getProfiles().setItems(profiles);

        setQos(new ListModel<NetworkQoS>());

        EntityModel<Boolean> createSubnet = new EntityModel<Boolean>(false);
        setCreateSubnet(createSubnet);
        createSubnet.getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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
        final IEventListener networkNameListener = new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                defaultProfile.getName().setEntity(getName().getEntity());
            }
        };
        getName().getEntityChangedEvent().addListener(networkNameListener);

        // if user overrides default name, stop tracking network's name
        defaultProfile.getName().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!defaultProfile.getName().getEntity().equals(getName().getEntity())) {
                    getName().getEntityChangedEvent().removeListener(networkNameListener);
                    defaultProfile.getName().getEntityChangedEvent().removeListener(this);
                }
            }
        });

        return defaultProfile;
    }

    private void initExternalProviderList() {
        startProgress(null);
        AsyncQuery getProvidersQuery = new AsyncQuery();
        getProvidersQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                List<Provider> providers = (List<Provider>) result;
                getExternalProviders().setItems(providers);
                selectExternalProvider();
                stopProgress();
            }
        };
        AsyncDataProvider.getAllNetworkProviders(getProvidersQuery);
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

    public ListModel<NetworkQoS> getQos() {
        return qos;
    }

    private void setQos(ListModel<NetworkQoS> qos) {
        this.qos = qos;
    }

    public boolean isSupportBridgesReportByVDSM() {
        return isSupportBridgesReportByVDSM;
    }

    public void setSupportBridgesReportByVDSM(boolean isSupportBridgesReportByVDSM) {
        if (!isSupportBridgesReportByVDSM) {
            getIsVmNetwork().setEntity(true);
            getIsVmNetwork().setChangeProhibitionReason(ConstantsManager.getInstance().getMessages()
                    .bridlessNetworkNotSupported(getSelectedDc().getcompatibility_version().toString()));
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
        if ((Boolean) getExport().getEntity() && (Boolean) getCreateSubnet().getEntity()) {
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

        setIsGeneralTabValid(getName().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid()
                && getMtu().getIsValid() && getExternalProviders().getIsValid() && getComment().getIsValid()
                && getNetworkLabel().getIsValid());
        setIsVnicProfileTabValid(profilesValid);
        setIsSubnetTabValid(subnetValid);
        return getIsGeneralTabValid() && getIsVnicProfileTabValid() && getIsSubnetTabValid();
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
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.SupportBridgesReportByVDSM,
                        dc.getcompatibility_version().toString());
        setSupportBridgesReportByVDSM(isSupportBridgesReportByVDSM);

        // Get IsMTUOverrideSupported
        boolean isMTUOverrideSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MTUOverrideSupported,
                        dc.getcompatibility_version().toString());

        setMTUOverrideSupported(isMTUOverrideSupported);

        AsyncQuery query = new AsyncQuery();
        query.asyncCallback = new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                Collection<NetworkQoS> qos = (Collection<NetworkQoS>) returnValue;
                getQos().setItems(qos);
                getQos().setSelectedItem(Linq.findNetworkQosById(qos, getNetwork().getQosId()));
            }
        };
        AsyncDataProvider.getAllNetworkQos(dc.getId(), query);

        updateDcLabels();

        onExportChanged();
        getProfiles().updateDcId(dc.getId());
    }

    private void addCommands() {
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
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

        String label = getNetworkLabel().getSelectedItem();
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
            NetworkQoS qos = getQos().getSelectedItem();
            network.setQosId(qos == NetworkQoSModel.EMPTY_QOS ? null : qos.getId());
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
                parameters.setPublicUse((Boolean) profileModel.getPublicUse().getEntity());
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
        NewNetworkQoSModel qosModel = new NewNetworkQoSModel(this, getSelectedDc()) {

            @Override
            protected void postSaveAction(boolean succeeded) {
                if (succeeded) {
                    List<NetworkQoS> qosItems = new ArrayList<NetworkQoS>(getQos().getItems());
                    qosItems.add(1, networkQoS);
                    getQos().setItems(qosItems);
                    getQos().setSelectedItem(networkQoS);
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

        if (isManagemet()) {
            ConfirmationModel confirmationModel = new ConfirmationModel();
            confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().updateManagementNetworkWarning());
            UICommand cmdOk = new UICommand(CMD_APPROVE, this);
            cmdOk.setTitle(ConstantsManager.getInstance().getConstants().ok());
            cmdOk.setIsDefault(true);
            confirmationModel.getCommands().add(cmdOk);
            UICommand cmdCancel = new UICommand(CMD_ABORT, this);
            cmdCancel.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            cmdCancel.setIsCancel(true);
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

    public boolean isManagemet() {
        return ENGINE_NETWORK.equals(getNetwork().getName());
    }

    protected abstract void initMtu();

    protected abstract void initIsVm();

    protected abstract void selectExternalProvider();

    protected void onExportChanged() {
        boolean externalNetwork = getExport().getEntity();

        getQos().setIsChangable(!externalNetwork);
        getAddQosCommand().setIsExecutionAllowed(!externalNetwork);

        String label = getNetworkLabel().getSelectedItem();
        getNetworkLabel().setItems(externalNetwork ? new HashSet<String>() : dcLabels);
        getNetworkLabel().setSelectedItem(label);

        updateMtuSelectorsChangeability();
    }

    private void updateDcLabels() {
        startProgress(null);
        AsyncDataProvider.getNetworkLabelsByDataCenterId(getSelectedDc().getId(),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        dcLabels = (Collection<String>) returnValue;
                        stopProgress();
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
                    .mtuOverrideNotSupported(getSelectedDc().getcompatibility_version().toString()));
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
                .defaultMtu((Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.DefaultMtu))),
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
