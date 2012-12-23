package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
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
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

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
    private EntityModel privateIsEnabled;
    private EntityModel publicUse;
    private ListModel privateNetworkClusterList;
    private ArrayList<VDSGroup> privateOriginalClusters;
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

        setNetworkClusterList(new ListModel());
        setOriginalClusters(new ArrayList<VDSGroup>());
        setIsEnabled(new EntityModel() {
            @Override
            public void setEntity(Object value) {
                super.setEntity(value);
                getName().setIsChangable((Boolean) value);
                getDescription().setIsChangable((Boolean) value);
                getIsVmNetwork().setIsChangable(isSupportBridgesReportByVDSM() && (Boolean) value);
                getHasVLanTag().setIsChangable((Boolean) value);
                getVLanTag().setIsChangable((Boolean) getHasVLanTag().getEntity() && (Boolean) value);
                getHasMtu().setIsChangable((Boolean) value && isMTUOverrideSupported());
                getMtu().setIsChangable((Boolean) getHasMtu().getEntity() && (Boolean) value
                        && isMTUOverrideSupported());
                onIsEnableChange();
            }
        });
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

    public EntityModel getIsEnabled()
    {
        return privateIsEnabled;
    }

    public void setIsEnabled(EntityModel value)
    {
        privateIsEnabled = value;
    }

    public EntityModel getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(EntityModel publicUse) {
        this.publicUse = publicUse;
    }

    public ListModel getNetworkClusterList()
    {
        return privateNetworkClusterList;
    }

    public void setNetworkClusterList(ListModel value)
    {
        privateNetworkClusterList = value;
    }

    public ArrayList<VDSGroup> getOriginalClusters()
    {
        return privateOriginalClusters;
    }

    public void setOriginalClusters(ArrayList<VDSGroup> value)
    {
        privateOriginalClusters = value;
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
            tempVar4.setMaximum(4095);
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

        // Get dc- cluster list
        AsyncDataProvider.GetClusterList(new AsyncQuery(NetworkModel.this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object ReturnValue)
                    {
                        onGetClusterList((ArrayList<VDSGroup>) ReturnValue);
                    }
                }), dc.getId());
    }

    protected abstract void onGetClusterList(ArrayList<VDSGroup> clusterList);

    protected abstract void addCommands();

    public storage_pool getSelectedDc() {
        return (storage_pool) getDataCenters().getSelectedItem();
    }

    public void flush() {
        network.setstorage_pool_id(getSelectedDc().getId());
        network.setname((String) getName().getEntity());
        network.setstp((Boolean) getIsStpEnabled().getEntity());
        network.setdescription((String) getDescription().getEntity());
        network.setVmNetwork((Boolean) getIsVmNetwork().getEntity());

        network.setMtu(0);
        if (getMtu().getEntity() != null)
        {
            network.setMtu(Integer.parseInt(getMtu().getEntity().toString()));
        }

        network.setvlan_id(null);
        if ((Boolean) getHasVLanTag().getEntity())
        {
            network.setvlan_id(Integer.parseInt(getVLanTag().getEntity().toString()));
        }
    }

    public ArrayList<VDSGroup> getnewClusters()
    {
        ArrayList<VDSGroup> newClusters = new ArrayList<VDSGroup>();

        for (Object item : getNetworkClusterList().getItems())
        {
            NetworkClusterModel networkClusterModel = (NetworkClusterModel) item;
            if (networkClusterModel.isAttached())
            {
                newClusters.add(networkClusterModel.getEntity());
            }
        }
        return newClusters;
    }

    protected void executeSave() {
        ArrayList<VDSGroup> detachNetworkFromClusters =
                Linq.Except(getOriginalClusters(), getnewClusters());
        ArrayList<VdcActionParametersBase> actionParameters =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup detachNetworkFromCluster : detachNetworkFromClusters)
        {
            actionParameters.add(new AttachNetworkToVdsGroupParameter(detachNetworkFromCluster,
                    network));
        }

        StartProgress(null);

        if (!actionParameters.isEmpty()) {
            Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {
                            postExecuteSave();
                        }
                    },
                    null);
        } else {
            postExecuteSave();
        }
    }

    protected abstract void postExecuteSave();

    protected void postSaveAction(Guid networkGuid, boolean succeeded)
    {
        if (succeeded)
        {
            cancel();
        }
        else
        {
            StopProgress();
            return;
        }
        StopProgress();
        Guid networkId = network.getId() == null ? networkGuid : network.getId();
        ArrayList<VDSGroup> attachNetworkToClusters =
                Linq.Except(getnewClusters(), getOriginalClusters());
        ArrayList<VdcActionParametersBase> actionParameters1 =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : attachNetworkToClusters)
        {
            Network tempVar = new Network();
            tempVar.setId(networkId);
            tempVar.setname(network.getname());
            // Init default network_cluster values (required, display, status)
            tempVar.setCluster(new network_cluster());
            actionParameters1.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar));
        }

        Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
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

    public NetworkClusterModel findNetworkClusterModel(VDSGroup cluster) {
        for (Object item : getNetworkClusterList().getItems())
        {
            NetworkClusterModel ncm = (NetworkClusterModel) item;
            if (cluster.getname().equals(ncm.getName())) {
                return ncm;
            }
        }
        return null;
    }

    protected void refreshClustersTable() {
        getNetworkClusterList()
                .getItemsChangedEvent()
                .raise(getNetworkClusterList(), EventArgs.Empty);
    }

    public boolean isManagemet() {
        return StringHelper.stringsEqual(getNetwork().getname(), ENGINE_NETWORK);
    }

    protected void onIsEnableChange() {
        // Do nothing
    }

    protected abstract void initMtu();

    protected abstract void initIsVm();
}
