package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.NotifyCollectionChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class DataCenterListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateForceRemoveCommand;

    public UICommand getForceRemoveCommand()
    {
        return privateForceRemoveCommand;
    }

    private void setForceRemoveCommand(UICommand value)
    {
        privateForceRemoveCommand = value;
    }

    private UICommand privateActivateCommand;

    public UICommand getActivateCommand()
    {
        return privateActivateCommand;
    }

    private void setActivateCommand(UICommand value)
    {
        privateActivateCommand = value;
    }

    private UICommand privateGuideCommand;

    public UICommand getGuideCommand()
    {
        return privateGuideCommand;
    }

    private void setGuideCommand(UICommand value)
    {
        privateGuideCommand = value;
    }

    private UICommand privateRecoveryStorageCommand;

    public UICommand getRecoveryStorageCommand()
    {
        return privateRecoveryStorageCommand;
    }

    private void setRecoveryStorageCommand(UICommand value)
    {
        privateRecoveryStorageCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<storage_pool>().Select(a =>
    // a.id).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            ArrayList<Object> objL = new ArrayList<Object>();
            for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems()))
            {
                objL.add(a.getId());
            }
            return objL.toArray(new Object[] {});
        }
    }

    private Object privateGuideContext;

    public Object getGuideContext()
    {
        return privateGuideContext;
    }

    public void setGuideContext(Object value)
    {
        privateGuideContext = value;
    }

    DataCenterQuotaListModel quotaListModel;

    public DataCenterListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().dataCentersTitle());

        setDefaultSearchString("DataCenter:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_STORAGE_POOL_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        UICommand tempVar = new UICommand("ForceRemove", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(true);
        setForceRemoveCommand(tempVar);
        setRecoveryStorageCommand(new UICommand("RecoveryStorage", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this)); //$NON-NLS-1$
        setGuideCommand(new UICommand("Guide", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void guide()
    {
        DataCenterGuideModel model = new DataCenterGuideModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDataCenterGuideMeTitle());
        model.setHashName("new_data_center_-_guide_me"); //$NON-NLS-1$
        if (getGuideContext() == null) {
            StoragePool dataCenter = (StoragePool) getSelectedItem();
            setGuideContext(dataCenter.getId());
        }

        AsyncDataProvider.getDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        DataCenterListModel dataCenterListModel = (DataCenterListModel) target;
                        DataCenterGuideModel model = (DataCenterGuideModel) dataCenterListModel.getWindow();
                        model.setEntity((StoragePool) returnValue);

                        UICommand tempVar = new UICommand("Cancel", dataCenterListModel); //$NON-NLS-1$
                        tempVar.setTitle(ConstantsManager.getInstance().getConstants().configureLaterTitle());
                        tempVar.setIsDefault(true);
                        tempVar.setIsCancel(true);
                        model.getCommands().add(tempVar);
                    }
                }), (Guid) getGuideContext());
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new DataCenterStorageListModel());
        list.add(new DataCenterNetworkListModel());
        list.add(new DataCenterClusterListModel());
        quotaListModel = new DataCenterQuotaListModel();
        quotaListModel.setIsAvailable(false);
        list.add(quotaListModel);
        list.add(new PermissionListModel());
        list.add(new DataCenterEventListModel());
        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("datacenter"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.StoragePool);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);

    }

    @Override
    protected void asyncSearch()
    {
        super.asyncSearch();

        setAsyncResult(null);
        setItems(getAsyncResult().getData());
    }

    public void newEntity()
    {
        if (getWindow() != null)
        {
            return;
        }

        DataCenterModel model = new DataCenterModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newDataCenterTitle());
        model.setHashName("new_data_center"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getStorageTypeList().setSelectedItem(StorageType.NFS);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void edit()
    {
        StoragePool dataCenter = (StoragePool) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final DataCenterModel model = new DataCenterModel();
        setWindow(model);
        model.setEntity(dataCenter);
        model.setDataCenterId(dataCenter.getId());
        model.setTitle(ConstantsManager.getInstance().getConstants().editDataCenterTitle());
        model.setHashName("edit_data_center"); //$NON-NLS-1$
        model.getName().setEntity(dataCenter.getname());

        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter)
        {
            model.getName().setIsChangable(false);
            model.getName().setInfo("Cannot edit Data Center's Name in tree context"); //$NON-NLS-1$
        }

        model.getDescription().setEntity(dataCenter.getdescription());
        model.setOriginalName(dataCenter.getname());

        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;

                        if (storageDomainList.size() != 0) {
                            model.getStorageTypeList()
                                    .setChangeProhibitionReason("Cannot change Repository type with Storage Domains attached to it"); //$NON-NLS-1$
                            model.getStorageTypeList().setIsChangable(false);
                        }

                    }
                }), dataCenter.getId());

        model.getStorageTypeList().setSelectedItem(dataCenter.getstorage_pool_type());

        model.getQuotaEnforceTypeListModel().setSelectedItem(dataCenter.getQuotaEnforcementType());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDataCenterTitle());
        model.setHashName("remove_data_center"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().dataCentersMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems()))
        {
            list.add(a.getname());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void forceRemove()
    {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().forceRemoveDataCenterTitle());
        model.setHashName("force_remove_data_center"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().dataCentersMsg());
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangable(true);

        ArrayList<String> list = new ArrayList<String>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems()))
        {
            list.add(a.getname());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnForceRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void recoveryStorage()
    {
        final ConfirmationModel windowModel = new ConfirmationModel();
        setWindow(windowModel);
        windowModel.setTitle(ConstantsManager.getInstance().getConstants().dataCenterReInitializeTitle());
        windowModel.setHashName("data_center_re-initialize"); //$NON-NLS-1$
        windowModel.getLatch().setIsAvailable(true);
        windowModel.getLatch().setIsChangable(true);

        windowModel.startProgress(null);

        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                windowModel.stopProgress();
                List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;
                List<EntityModel> models = new ArrayList<EntityModel>();
                for (StorageDomain a : storageDomainList) {
                    if (a.getStorageDomainType() == StorageDomainType.Data
                            && a.getStorageType() == ((StoragePool) getSelectedItem()).getstorage_pool_type()
                            && (a.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached)) {
                        EntityModel tempVar = new EntityModel();
                        tempVar.setEntity(a);
                        models.add(tempVar);
                    }
                }
                windowModel.setItems(models);

                if (models.size() > 0) {
                    EntityModel entityModel = models.size() != 0 ? models.get(0) : null;
                    if (entityModel != null) {
                        entityModel.setIsSelected(true);
                    }
                }

                if (models.isEmpty()) {
                    windowModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .thereAreNoCompatibleStorageDomainsAttachThisDcMsg());

                    UICommand tempVar2 = new UICommand("Cancel", DataCenterListModel.this); //$NON-NLS-1$
                    tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
                    tempVar2.setIsDefault(true);
                    tempVar2.setIsCancel(true);
                    windowModel.getCommands().add(tempVar2);
                } else {
                    UICommand tempVar3 = new UICommand("OnRecover", DataCenterListModel.this); //$NON-NLS-1$
                    tempVar3.setTitle(ConstantsManager.getInstance().getConstants().ok());
                    tempVar3.setIsDefault(true);
                    windowModel.getCommands().add(tempVar3);
                    UICommand tempVar4 = new UICommand("Cancel", DataCenterListModel.this); //$NON-NLS-1$
                    tempVar4.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                    tempVar4.setIsCancel(true);
                    windowModel.getCommands().add(tempVar4);
                }

            }
        }));
    }

    public void onRecover()
    {

        final ConfirmationModel windowModel = (ConfirmationModel) getWindow();
        if (!windowModel.validate())
        {
            return;
        }

        AsyncDataProvider.getStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                StorageDomain master = null;
                List<StorageDomain> storageDomainList = (List<StorageDomain>) returnValue;
                for (StorageDomain a : storageDomainList)
                {
                    if (a.getStorageDomainType() == StorageDomainType.Master)
                    {
                        master = a;
                        break;
                    }
                }
                List<StorageDomain> items = new ArrayList<StorageDomain>();
                for (EntityModel a : Linq.<EntityModel> cast(windowModel.getItems()))
                {
                    if (a.getIsSelected())
                    {
                        items.add((StorageDomain) a.getEntity());
                    }
                }
                if (items.size() > 0)
                {
                    if (windowModel.getProgress() != null)
                    {
                        return;
                    }
                    ArrayList<VdcActionParametersBase> parameters =
                            new ArrayList<VdcActionParametersBase>();
                    for (StorageDomain a : items)
                    {
                        parameters.add(new RecoveryStoragePoolParameters(((StoragePool) getSelectedItem()).getId(),
                                a.getId()));
                    }
                    windowModel.startProgress(null);
                    Frontend.RunMultipleAction(VdcActionType.RecoveryStoragePool, parameters,
                            new IFrontendMultipleActionAsyncCallback() {
                                @Override
                                public void executed(FrontendMultipleActionAsyncResult result) {

                                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                                    localModel.stopProgress();
                                    cancel();

                                }
                            }, windowModel);
                }
                else
                {
                    cancel();
                }
            }
        }),
                ((StoragePool) getSelectedItem()).getId());
    }

    public void activate()
    {
        // Frontend.RunMultipleActions(VdcActionType.ActivateStoragePool,
        // SelectedItems.Cast<storage_pool>()
        // .Select(a => (VdcActionParametersBase)new StoragePoolParametersBase(a.id))
        // .ToList()
        // );
    }

    public void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (StoragePool a : Linq.<StoragePool> cast(getSelectedItems()))
        {
            parameters.add(new StoragePoolParametersBase(a.getId()));
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveStoragePool, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void onForceRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        if (!model.validate())
        {
            return;
        }
        VdcActionParametersBase parametersBase = new VdcActionParametersBase();
        StoragePoolParametersBase tempVar = new StoragePoolParametersBase(((StoragePool) getSelectedItem()).getId());
        tempVar.setForceDelete(true);
        parametersBase = tempVar;
        Frontend.RunAction(VdcActionType.RemoveStoragePool, parametersBase);
        cancel();
    }

    public void cancel()
    {
        cancelConfirmation();

        setGuideContext(null);
        setWindow(null);

        updateActionAvailability();
    }

    public void cancelConfirmation()
    {
        setConfirmWindow(null);
    }

    public void onSave()
    {
        DataCenterModel model = (DataCenterModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        if ((model.getIsNew() || model.getEntity() == null)
                && model.getQuotaEnforceTypeListModel().getSelectedItem() != QuotaEnforcementTypeEnum.DISABLED) {
            promptNoQuotaInDCMessage();
        }
        else if (!model.getIsNew()
                && getSelectedItem() != null
                && !((Version) model.getVersion().getSelectedItem()).equals(((StoragePool) getSelectedItem()).getcompatibility_version())) {
            ConfirmationModel confirmModel = new ConfirmationModel();
            setConfirmWindow(confirmModel);
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .changeDataCenterCompatibilityVersionTitle());
            confirmModel.setHashName("change_data_center_compatibility_version"); //$NON-NLS-1$
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .youAreAboutChangeDcCompatibilityVersionMsg());

            UICommand tempVar = new UICommand("OnSaveInternal", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            confirmModel.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            confirmModel.getCommands().add(tempVar2);
        }
        else if (getSelectedItem() != null
                && ((StoragePool) getSelectedItem()).getQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED
                && model.getQuotaEnforceTypeListModel().getSelectedItem() != QuotaEnforcementTypeEnum.DISABLED)
        {
            checkForQuotaInDC(model.getEntity(), this);
        }
        else
        {
            onSaveInternal();
        }
    }

    private void checkForQuotaInDC(StoragePool storage_pool, final ICommandTarget commandTarget) {
        GetQuotaByStoragePoolIdQueryParameters parameters = new GetQuotaByStoragePoolIdQueryParameters(storage_pool.getId());
        Frontend.RunQuery(VdcQueryType.GetQuotaByStoragePoolId,
                parameters,
                new AsyncQuery(
                        quotaListModel,
                        new INewAsyncCallback() {

                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                if (((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue()).size() == 0) {
                                    promptNoQuotaInDCMessage();
                                } else {
                                    onSaveInternal();
                                }
                            }
                        }));
    }

    private void promptNoQuotaInDCMessage() {
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle(ConstantsManager.getInstance()
                .getConstants()
                .changeDCQuotaEnforcementModeTitle());
        confirmModel.setHashName("change_data_center_quota_enforcement_mode"); //$NON-NLS-1$
        confirmModel.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .youAreAboutChangeDCQuotaEnforcementMsg());

        UICommand tempVar = new UICommand("OnSaveInternal", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getConfirmWindow().getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getConfirmWindow().getCommands().add(tempVar2);
    }

    public void onSaveInternal()
    {
        DataCenterModel model = (DataCenterModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        StoragePool dataCenter =
                model.getIsNew() ? new StoragePool() : (StoragePool) Cloner.clone(getSelectedItem());

        // cancel confirm window if there is
        cancelConfirmation();

        // Save changes.
        dataCenter.setname((String) model.getName().getEntity());
        dataCenter.setdescription((String) model.getDescription().getEntity());
        dataCenter.setstorage_pool_type((StorageType) model.getStorageTypeList().getSelectedItem());
        dataCenter.setcompatibility_version((Version) model.getVersion().getSelectedItem());
        dataCenter.setQuotaEnforcementType((QuotaEnforcementTypeEnum) model.getQuotaEnforceTypeListModel()
                .getSelectedItem());

        model.startProgress(null);


        if (model.getIsNew()) {
            // When adding a data center use sync action to be able present a Guide Me dialog afterwards.
            Frontend.RunAction(VdcActionType.AddEmptyStoragePool,
                new StoragePoolManagementParameter(dataCenter),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        DataCenterListModel localModel = (DataCenterListModel) result.getState();
                        localModel.postOnSaveInternal(result.getReturnValue());
                    }
                },
                this);
        } else {
            // Otherwise use async action in order to close dialog immediately.
            Frontend.RunMultipleAction(VdcActionType.UpdateStoragePool,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(
                    new StoragePoolManagementParameter(dataCenter))
                ),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        DataCenterListModel localModel = (DataCenterListModel) result.getState();
                        localModel.postOnSaveInternal(result.getReturnValue().get(0));
                    }
                },
                this);
        }
    }

    public void postOnSaveInternal(VdcReturnValueBase returnValue)
    {
        DataCenterModel model = (DataCenterModel) getWindow();

        model.stopProgress();

        cancel();

        if (model.getIsNew() && returnValue != null && returnValue.getSucceeded()) {

            setGuideContext(returnValue.getActionReturnValue());
            updateActionAvailability();
            getGuideCommand().execute();
        }
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void itemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
    {
        super.itemsCollectionChanged(sender, e);

        // Try to select an item corresponding to the system tree selection.
        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter)
        {
            StoragePool dataCenter = (StoragePool) getSystemTreeSelectedItem().getEntity();

            setSelectedItem(Linq.firstOrDefault(Linq.<StoragePool> cast(getItems()),
                    new Linq.DataCenterPredicate(dataCenter.getId())));
        }
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.selectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
        if (getSelectedItem() != null
                && ((StoragePool) getSelectedItem()).getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            quotaListModel.setIsAvailable(true);
        } else {
            quotaListModel.setIsAvailable(false);
        }
    }

    private void updateActionAvailability()
    {
        ArrayList<StoragePool> items =
                getSelectedItems() != null ? new ArrayList<StoragePool>(Linq.<StoragePool> cast(getSelectedItems()))
                        : new ArrayList<StoragePool>();

        boolean isAllDown = true;
        for (StoragePool item : items)
        {
            if (item.getstatus() == StoragePoolStatus.Up || item.getstatus() == StoragePoolStatus.Contend)
            {
                isAllDown = false;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && items.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && isAllDown);

        StoragePool storagePoolItem = (StoragePool) getSelectedItem();

        getForceRemoveCommand().setIsExecutionAllowed(storagePoolItem != null
                && items.size() == 1
                && storagePoolItem.getstatus() != StoragePoolStatus.Up);

        getGuideCommand().setIsExecutionAllowed(getGuideContext() != null
                || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));

        getActivateCommand().setIsExecutionAllowed(items.size() > 0);
        if (getActivateCommand().getIsExecutionAllowed())
        {
            for (StoragePool a : items)
            {
                if (a.getstatus() == StoragePoolStatus.Up || a.getstatus() == StoragePoolStatus.Uninitialized)
                {
                    getActivateCommand().setIsExecutionAllowed(false);
                    break;
                }
            }
        }

        getRecoveryStorageCommand().setIsExecutionAllowed(items != null && items.size() == 1
                && !items.iterator().next().getstorage_pool_type().equals(StorageType.LOCALFS));

        // System tree dependent actions.
        boolean isAvailable =
                !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter);

        getNewCommand().setIsAvailable(isAvailable);
        getRemoveCommand().setIsAvailable(isAvailable);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getForceRemoveCommand())
        {
            forceRemove();
        }
        else if (command == getActivateCommand())
        {
            activate();
        }
        else if (command == getGuideCommand())
        {
            guide();
        }
        else if (command == getRecoveryStorageCommand())
        {
            recoveryStorage();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnForceRemove")) //$NON-NLS-1$
        {
            onForceRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSaveInternal")) //$NON-NLS-1$
        {
            onSaveInternal();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation")) //$NON-NLS-1$
        {
            cancelConfirmation();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRecover")) //$NON-NLS-1$
        {
            onRecover();
        }
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        if (systemTreeSelectedItem != value)
        {
            systemTreeSelectedItem = value;
            onSystemTreeSelectedItemChanged();
        }
    }

    private void onSystemTreeSelectedItemChanged()
    {
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "DataCenterListModel"; //$NON-NLS-1$
    }
}
