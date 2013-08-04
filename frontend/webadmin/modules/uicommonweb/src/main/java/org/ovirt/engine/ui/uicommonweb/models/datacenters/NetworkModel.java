package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
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
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public abstract class NetworkModel extends Model
{
    protected static String ENGINE_NETWORK =
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private EntityModel privateName;
    private EntityModel privateDescription;
    private EntityModel export;
    private ListModel externalProviders;
    private EntityModel networkLabel;
    private EntityModel privateComment;
    private EntityModel privateVLanTag;
    private EntityModel privateIsStpEnabled;
    private EntityModel privateHasVLanTag;
    private EntityModel privateHasMtu;
    private EntityModel privateMtu;
    private EntityModel privateIsVmNetwork;
    private EntityModel publicUse;
    private boolean isSupportBridgesReportByVDSM = false;
    private boolean mtuOverrideSupported = false;
    private ListModel privateDataCenters;
    private ListModel profiles;
    private final Network network;
    private final ListModel sourceListModel;

    public NetworkModel(ListModel sourceListModel)
    {
        this(new Network(), sourceListModel);
    }

    public NetworkModel(Network network, ListModel sourceListModel)
    {
        this.network = network;
        this.sourceListModel = sourceListModel;
        setName(new EntityModel());
        setDescription(new EntityModel());
        setComment(new EntityModel());
        setDataCenters(new ListModel());
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                syncWithBackend();
            }
        });
        setExport(new EntityModel(false));
        getExport().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                onExportChanged();
            }
        });

        setExternalProviders(new ListModel());
        setNetworkLabel(new EntityModel());

        EntityModel stpEnabled = new EntityModel();
        stpEnabled.setEntity(false);
        setIsStpEnabled(stpEnabled);

        setVLanTag(new EntityModel());
        EntityModel hasVlanTag = new EntityModel();
        hasVlanTag.setEntity(false);
        setHasVLanTag(hasVlanTag);
        getHasVLanTag().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateVlanTagChangeability();
            }
        });

        setMtu(new EntityModel());
        EntityModel hasMtu = new EntityModel();
        hasMtu.setEntity(false);
        setHasMtu(hasMtu);
        getHasMtu().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateMtuChangeability();
            }
        });

        EntityModel isVmNetwork = new EntityModel();
        isVmNetwork.setEntity(true);
        setIsVmNetwork(isVmNetwork);
        EntityModel publicUse = new EntityModel();
        publicUse.setEntity(true);
        setPublicUse(publicUse);

        setProfiles(new ListModel());

        // Update changeability according to initial values
        onExportChanged();
        updateVlanTagChangeability();
        updateMtuChangeability();
    }

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    public EntityModel getExport() {
        return export;
    }

    private void setExport(EntityModel value) {
        export = value;
    }

    public ListModel getExternalProviders() {
        return externalProviders;
    }

    public void setExternalProviders(ListModel externalProviders) {
        this.externalProviders = externalProviders;
    }

    public EntityModel getNetworkLabel() {
        return networkLabel;
    }

    public void setNetworkLabel(EntityModel networkLabel) {
        this.networkLabel = networkLabel;
    }

    public EntityModel getComment() {
        return privateComment;
    }

    private void setComment(EntityModel value) {
        privateComment = value;
    }

    public EntityModel getVLanTag()
    {
        return privateVLanTag;
    }

    private void setVLanTag(EntityModel value)
    {
        privateVLanTag = value;
    }

    public EntityModel getIsStpEnabled()
    {
        return privateIsStpEnabled;
    }

    private void setIsStpEnabled(EntityModel value)
    {
        privateIsStpEnabled = value;
    }

    public EntityModel getHasVLanTag()
    {
        return privateHasVLanTag;
    }

    private void setHasVLanTag(EntityModel value)
    {
        privateHasVLanTag = value;
    }

    public EntityModel getHasMtu()
    {
        return privateHasMtu;
    }

    private void setHasMtu(EntityModel value)
    {
        privateHasMtu = value;
    }

    public EntityModel getMtu()
    {
        return privateMtu;
    }

    private void setMtu(EntityModel value)
    {
        privateMtu = value;
    }

    public EntityModel getIsVmNetwork()
    {
        return privateIsVmNetwork;
    }

    public void setIsVmNetwork(EntityModel value)
    {
        privateIsVmNetwork = value;
    }

    public EntityModel getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(EntityModel publicUse) {
        this.publicUse = publicUse;
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
        if (!mtuOverrideSupported) {
            getHasMtu().setChangeProhibitionReason(ConstantsManager.getInstance().getMessages()
                    .mtuOverrideNotSupported(getSelectedDc().getcompatibility_version().toString()));
            getHasMtu().setIsChangable(false);
            getMtu().setIsChangable(false);
            getHasMtu().setEntity(false);
            getMtu().setEntity(null);
        } else {
            if (this.mtuOverrideSupported != mtuOverrideSupported) {
                initMtu();
            }
            getHasMtu().setIsChangable(true);
        }
        this.mtuOverrideSupported = mtuOverrideSupported;
    }

    public ListModel getDataCenters()
    {
        return privateDataCenters;
    }

    private void setDataCenters(ListModel value)
    {
        privateDataCenters = value;
    }

    public ListModel getProfiles()
    {
        return profiles;
    }

    private void setProfiles(ListModel value)
    {
        profiles = value;
    }

    public Network getNetwork() {
        return network;
    }

    public ListModel getSourceListModel() {
        return sourceListModel;
    }

    public boolean validate()
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
        if ((Boolean) getHasVLanTag().getEntity())
        {
            IntegerValidation tempVar4 = new IntegerValidation();
            tempVar4.setMinimum(0);
            tempVar4.setMaximum(4094);
            getVLanTag().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
        }

        getMtu().setIsValid(true);
        if ((Boolean) getHasMtu().getEntity())
        {
            IntegerValidation tempVar5 = new IntegerValidation();
            tempVar5.setMinimum(68);
            tempVar5.setMaximum(9000);
            getMtu().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar5 });
        }

        getExternalProviders().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getName().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid()
                && getMtu().getIsValid() && getExternalProviders().getIsValid() && getComment().getIsValid();
    }

    protected boolean firstInit = true;

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

        onExportChanged();

        initProfiles();
    }

    protected void addCommands() {
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        getCommands().add(tempVar3);
    }

    public StoragePool getSelectedDc() {
        return (StoragePool) getDataCenters().getSelectedItem();
    }

    public void flush() {
        network.setDataCenterId(getSelectedDc().getId());
        network.setName((String) getName().getEntity());
        network.setStp((Boolean) getIsStpEnabled().getEntity());
        network.setDescription((String) getDescription().getEntity());
        network.setLabel((String) getNetworkLabel().getEntity());
        network.setComment((String) getComment().getEntity());
        network.setVmNetwork((Boolean) getIsVmNetwork().getEntity());

        network.setMtu(0);
        if ((Boolean) getHasMtu().getEntity())
        {
            network.setMtu(Integer.parseInt(getMtu().getEntity().toString()));
        }

        network.setVlanId(null);
        if ((Boolean) getHasVLanTag().getEntity())
        {
            network.setVlanId(Integer.parseInt(getVLanTag().getEntity().toString()));
        }

        for (VnicProfileModel profileModel : (List<VnicProfileModel>) getProfiles().getItems()) {
            profileModel.flush();
        }
    }

    protected abstract void executeSave();

    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        if (succeeded)
        {
            performProfilesActions(networkGuid);
        }
        stopProgress();
    }

    protected abstract void performProfilesActions(Guid networkGuid);

    protected void performVnicProfileAction(VdcActionType action,
            List<VnicProfileModel> profileModels,
            IFrontendMultipleActionAsyncCallback callback, Guid networkGuid) {
        if (profileModels.isEmpty()) {
            callback.executed(null);
            return;
        }

        networkGuid = networkGuid == null ? getNetwork().getId() : networkGuid;
        ArrayList<VdcActionParametersBase> paramlist = new ArrayList<VdcActionParametersBase>();
        for (VnicProfileModel profileModel : profileModels)
        {
            if (!StringHelper.isNullOrEmpty(profileModel.getProfile().getName())) {
                VnicProfile vnicProfile = profileModel.getProfile();
                vnicProfile.setNetworkId(networkGuid);
                VdcActionParametersBase parameters = new VnicProfileParameters(vnicProfile);
                paramlist.add(parameters);
            }
        }

        Frontend.RunMultipleAction(action, paramlist, callback, null);
    }

    void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    public void onSave()
    {
        if (!validate())
        {
            return;
        }

        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }

    public boolean isManagemet() {
        return StringHelper.stringsEqual(getNetwork().getName(), ENGINE_NETWORK);
    }

    protected abstract void initMtu();

    protected abstract void initIsVm();

    protected abstract void initProfiles();

    protected abstract void onExportChanged();

    private void updateVlanTagChangeability() {
        getVLanTag().setIsChangable((Boolean) getHasVLanTag().getEntity());
    }

    private void updateMtuChangeability() {
        getMtu().setIsChangable((Boolean) getHasMtu().getEntity() && !((Boolean) getExport().getEntity()));
    }
}
