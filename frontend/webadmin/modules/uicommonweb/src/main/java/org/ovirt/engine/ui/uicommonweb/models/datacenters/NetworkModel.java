package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class NetworkModel extends Model
{
    protected static String ENGINE_NETWORK =
            (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private EntityModel privateName;
    private EntityModel privateDescription;
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
        setDataCenters(new ListModel());
        getDataCenters().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                syncWithBackend();
            }
        });
        setVLanTag(new EntityModel());
        EntityModel stpEnabled = new EntityModel();
        stpEnabled.setEntity(false);
        setIsStpEnabled(stpEnabled);
        EntityModel hasVlanTag = new EntityModel();
        hasVlanTag.setEntity(false);
        setHasVLanTag(hasVlanTag);
        setMtu(new EntityModel());
        EntityModel hasMtu = new EntityModel();
        hasMtu.setEntity(false);
        setHasMtu(hasMtu);
        EntityModel isVmNetwork = new EntityModel();
        isVmNetwork.setEntity(true);
        setIsVmNetwork(isVmNetwork);
        EntityModel publicUse = new EntityModel();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
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
            getIsVmNetwork().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
                    .getMessages()
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
            getHasMtu().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
                    .getMessages()
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

    public Network getNetwork() {
        return network;
    }

    public ListModel getSourceListModel() {
        return sourceListModel;
    }

    public boolean Validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^[A-Za-z0-9_]{1,15}$"); //$NON-NLS-1$
        tempVar.setMessage(ConstantsManager.getInstance().getConstants().nameMustContainAlphanumericMaxLenMsg());
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setIsNegate(true);
        tempVar2.setExpression("^(bond)"); //$NON-NLS-1$
        tempVar2.setMessage(ConstantsManager.getInstance().getConstants().networkNameStartMsg());
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(40);
        getDescription().ValidateEntity(new IValidation[] { tempVar3 });

        getVLanTag().setIsValid(true);
        if ((Boolean) getHasVLanTag().getEntity())
        {
            IntegerValidation tempVar4 = new IntegerValidation();
            tempVar4.setMinimum(0);
            tempVar4.setMaximum(4094);
            getVLanTag().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
        }

        getMtu().setIsValid(true);
        if ((Boolean) getHasMtu().getEntity())
        {
            IntegerValidation tempVar5 = new IntegerValidation();
            tempVar5.setMinimum(68);
            tempVar5.setMaximum(9000);
            getMtu().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar5 });
        }

        return getName().getIsValid() && getVLanTag().getIsValid() && getDescription().getIsValid()
                && getMtu().getIsValid();
    }

    protected boolean firstInit = true;

    public void syncWithBackend() {
        final storage_pool dc = getSelectedDc();
        if (dc == null) {
            return;
        }

        // Get IsSupportBridgesReportByVDSM
        boolean isSupportBridgesReportByVDSM =
                (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.SupportBridgesReportByVDSM,
                        dc.getcompatibility_version().toString());
        setSupportBridgesReportByVDSM(isSupportBridgesReportByVDSM);

        // Get IsMTUOverrideSupported
        boolean isMTUOverrideSupported =
                (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.MTUOverrideSupported,
                        dc.getcompatibility_version().toString());

        setMTUOverrideSupported(isMTUOverrideSupported);

    }

    protected void addCommands(){
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        getCommands().add(tempVar3);
    }

    public storage_pool getSelectedDc() {
        return (storage_pool) getDataCenters().getSelectedItem();
    }

    public void flush() {
        network.setDataCenterId(getSelectedDc().getId());
        network.setName((String) getName().getEntity());
        network.setStp((Boolean) getIsStpEnabled().getEntity());
        network.setDescription((String) getDescription().getEntity());
        network.setVmNetwork((Boolean) getIsVmNetwork().getEntity());

        network.setMtu(0);
        if (getMtu().getEntity() != null)
        {
            network.setMtu(Integer.parseInt(getMtu().getEntity().toString()));
        }

        network.setVlanId(null);
        if ((Boolean) getHasVLanTag().getEntity())
        {
            network.setVlanId(Integer.parseInt(getVLanTag().getEntity().toString()));
        }
    }

    protected abstract void executeSave();

    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        if (succeeded)
        {
            cancel();
        }
        StopProgress();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    public void onSave()
    {
        if (!Validate())
        {
            return;
        }

        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

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
}
