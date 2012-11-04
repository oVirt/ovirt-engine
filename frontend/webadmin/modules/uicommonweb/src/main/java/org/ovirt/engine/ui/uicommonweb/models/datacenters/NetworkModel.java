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
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setIsStpEnabled(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(false);
        setHasVLanTag(tempVar2);
        setMtu(new EntityModel());
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(false);
        setHasMtu(tempVar3);
        EntityModel tempVar4 = new EntityModel();
        tempVar4.setEntity(true);
        setIsVmNetwork(tempVar4);

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
                getVLanTag().setIsChangable((Boolean)getHasVLanTag().getEntity() && (Boolean) value);
                getHasMtu().setIsChangable((Boolean) value && isMTUOverrideSupported());
                getMtu().setIsChangable((Boolean) getHasMtu().getEntity() && (Boolean) value
                        && isMTUOverrideSupported());
            }

        });
        init();
        syncWithBackend();
    }

    protected static String ENGINE_NETWORK = (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.ManagementNetwork);

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel privateVLanTag;

    public EntityModel getVLanTag()
    {
        return privateVLanTag;
    }

    private void setVLanTag(EntityModel value)
    {
        privateVLanTag = value;
    }

    private EntityModel privateIsStpEnabled;

    public EntityModel getIsStpEnabled()
    {
        return privateIsStpEnabled;
    }

    private void setIsStpEnabled(EntityModel value)
    {
        privateIsStpEnabled = value;
    }

    private EntityModel privateHasVLanTag;

    public EntityModel getHasVLanTag()
    {
        return privateHasVLanTag;
    }

    private void setHasVLanTag(EntityModel value)
    {
        privateHasVLanTag = value;
    }

    private EntityModel privateHasMtu;

    public EntityModel getHasMtu()
    {
        return privateHasMtu;
    }

    private void setHasMtu(EntityModel value)
    {
        privateHasMtu = value;
    }

    private EntityModel privateMtu;

    public EntityModel getMtu()
    {
        return privateMtu;
    }

    private void setMtu(EntityModel value)
    {
        privateMtu = value;
    }

    private EntityModel privateIsVmNetwork;

    public EntityModel getIsVmNetwork()
    {
        return privateIsVmNetwork;
    }

    public void setIsVmNetwork(EntityModel value)
    {
        privateIsVmNetwork = value;
    }

    private EntityModel privateIsEnabled;

    public EntityModel getIsEnabled()
    {
        return privateIsEnabled;
    }

    public void setIsEnabled(EntityModel value)
    {
        privateIsEnabled = value;
    }

    private ListModel privateNetworkClusterList;

    public ListModel getNetworkClusterList()
    {
        return privateNetworkClusterList;
    }

    public void setNetworkClusterList(ListModel value)
    {
        privateNetworkClusterList = value;
    }

    private ArrayList<VDSGroup> privateOriginalClusters;

    public ArrayList<VDSGroup> getOriginalClusters()
    {
        return privateOriginalClusters;
    }

    public void setOriginalClusters(ArrayList<VDSGroup> value)
    {
        privateOriginalClusters = value;
    }

    private boolean isSupportBridgesReportByVDSM = true;

    public boolean isSupportBridgesReportByVDSM() {
        return isSupportBridgesReportByVDSM;
    }

    public void setSupportBridgesReportByVDSM(boolean isSupportBridgesReportByVDSM) {
        this.isSupportBridgesReportByVDSM = isSupportBridgesReportByVDSM;
        if (!isSupportBridgesReportByVDSM) {
            getIsVmNetwork().setEntity(true);
            getIsVmNetwork().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .bridlessNetworkNotSupported(getSelectedDc().getcompatibility_version().toString()));
            getIsVmNetwork().setIsChangable(false);
        }else{
            getIsVmNetwork().setIsChangable(true);
        }
    }

    private boolean mtuOverrideSupported;

    public boolean isMTUOverrideSupported() {
        return mtuOverrideSupported;
    }

    public void setMTUOverrideSupported(boolean mtuOverrideSupported) {
        this.mtuOverrideSupported = mtuOverrideSupported;
        if (!mtuOverrideSupported) {
            getHasMtu().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .mtuOverrideNotSupported(getSelectedDc().getcompatibility_version().toString()));
            getHasMtu().setIsChangable(false);
            getMtu().setIsChangable(false);
            getHasMtu().setEntity(false);
            getMtu().setEntity(null);
        }else{
            getHasMtu().setIsChangable(true);
            getMtu().setIsChangable(true);
        }
    }

    private ListModel privateDataCenters;

    public ListModel getDataCenters()
    {
        return privateDataCenters;
    }

    private void setDataCenters(ListModel value)
    {
        privateDataCenters = value;
    }

    private final Network network;

    public Network getNetwork(){
        return network;
    }

    private final ListModel sourceListModel;

    public ListModel getSourceListModel(){
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

    public void syncWithBackend(){
        final storage_pool dc = getSelectedDc();
        if (dc == null){
            return;
        }

        // Get IsSupportBridgesReportByVDSM
        AsyncDataProvider.IsSupportBridgesReportByVDSM(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        boolean isSupportBridgesReportByVDSM = (Boolean) returnValue;
                        setSupportBridgesReportByVDSM(isSupportBridgesReportByVDSM);

                        // Get IsMTUOverrideSupported
                        AsyncDataProvider.IsMTUOverrideSupported(new AsyncQuery(NetworkModel.this,
                                new INewAsyncCallback() {
                                    @Override
                                    public void OnSuccess(Object model, Object returnValue) {
                                        boolean isMTUOverrideSupported = (Boolean) returnValue;

                                        setMTUOverrideSupported(isMTUOverrideSupported);

                                        // Get dc- cluster list
                                        AsyncDataProvider.GetClusterList(new AsyncQuery(NetworkModel.this, new INewAsyncCallback() {
                                            @Override
                                            public void OnSuccess(Object model, Object ReturnValue)
                                            {
                                                onGetClusterList((ArrayList<VDSGroup>) ReturnValue);
                                            }
                                        }), dc.getId());
                                    }
                                }),
                                dc.getcompatibility_version().toString());
                    }
                }),
                dc.getcompatibility_version().toString());
    }

    protected abstract void onGetClusterList(ArrayList<VDSGroup> clusterList);
    protected abstract void addCommands();

    public storage_pool getSelectedDc(){
        return (storage_pool) getDataCenters().getSelectedItem();
    }
    public void flush(){
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
        ArrayList<VDSGroup> newClusters =new ArrayList<VDSGroup>();

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

    protected void executeSave(){
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

        if (!actionParameters.isEmpty()){
            Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {
                           postExecuteSave();
                        }
                    },
                    null);
        }else{
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

    private void cancel(){
            sourceListModel.setWindow(null);
            sourceListModel.setConfirmWindow(null);
    }

    protected abstract void init();

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

    protected void refreshClustersTable(){
        getNetworkClusterList()
        .getItemsChangedEvent()
        .raise(getNetworkClusterList(), EventArgs.Empty);
    }
}
