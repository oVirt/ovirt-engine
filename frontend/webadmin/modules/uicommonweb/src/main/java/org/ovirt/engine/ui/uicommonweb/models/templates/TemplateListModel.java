package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class TemplateListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

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

    private UICommand privateExportCommand;

    public UICommand getExportCommand()
    {
        return privateExportCommand;
    }

    private void setExportCommand(UICommand value)
    {
        privateExportCommand = value;
    }

    private UICommand privateCopyCommand;

    public UICommand getCopyCommand()
    {
        return privateCopyCommand;
    }

    private void setCopyCommand(UICommand value)
    {
        privateCopyCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VmTemplate>().Select(a =>
    // a.vmt_guid).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            ArrayList<Guid> items = new ArrayList<Guid>();
            for (Object item : getSelectedItems())
            {
                VmTemplate a = (VmTemplate) item;
                items.add(a.getId());
            }
            return items.toArray(new Guid[] {});
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
        }
    }

    public TemplateListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());

        setDefaultSearchString("Template:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());

        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setExportCommand(new UICommand("Export", this)); //$NON-NLS-1$
        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void Copy()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        CopyDiskModel model = new CopyDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().copyTemplateTitle());
        model.setHashName("copy_template"); //$NON-NLS-1$
        model.setIsVolumeFormatAvailable(false);
        model.setIsSourceStorageDomainAvailable(true);
        model.setIsSourceStorageDomainChangable(true);
        model.setEntity(this);

        model.StartProgress(null);

        AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                TemplateListModel templateListModel = (TemplateListModel) target;
                CopyDiskModel copyDiskModel = (CopyDiskModel) templateListModel.getWindow();
                ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) returnValue;

                copyDiskModel.init(diskImages);
            }
        }), template.getId());
    }

    private void Export()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        ExportVmModel model = new ExportVmModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().backupTemplateTitle());
        model.setHashName("backup_template"); //$NON-NLS-1$

        model.getCollapseSnapshots().setIsAvailable(false);

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        TemplateListModel templateListModel = (TemplateListModel) target;
                        ArrayList<storage_domains> storageDomains =
                                (ArrayList<storage_domains>) returnValue;
                        ArrayList<storage_domains> filteredStorageDomains =
                                new ArrayList<storage_domains>();

                        for (storage_domains a : storageDomains) {
                            if (a.getstorage_domain_type() == StorageDomainType.ImportExport) {
                                filteredStorageDomains.add(a);
                            }
                        }

                        templateListModel
                                .PostExportGetStorageDomainList(filteredStorageDomains);
                    }
                }), template.getstorage_pool_id().getValue());
    }

    private void PostExportGetStorageDomainList(ArrayList<storage_domains> storageDomains)
    {
        ExportVmModel model = (ExportVmModel) getWindow();
        model.getStorage().setItems(storageDomains);
        model.getStorage().setSelectedItem(Linq.FirstOrDefault(storageDomains));

        boolean isAllStoragesActive = true;
        for (storage_domains a : storageDomains) {
            if (a.getstatus() != StorageDomainStatus.Active) {
                isAllStoragesActive = false;
                break;
            }
        }

        if (SelectedTemplatesOnDifferentDataCenters()) {
            model.getCollapseSnapshots().setIsChangable(false);
            model.getForceOverride().setIsChangable(false);

            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg());

            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }

        else if (storageDomains.isEmpty()) {
            model.getForceOverride().setIsChangable(false);

            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .thereIsNoExportDomainToExportTheTemplateIntoMsg());

            UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar2.setIsDefault(true);
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
        } else if (!isAllStoragesActive) {
            model.getForceOverride().setIsChangable(false);

            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .theRelevantExportDomainIsNotActivePleaseActivateItMsg());

            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar3.setIsDefault(true);
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        } else {
            showWarningOnExistingTemplates(model);

            UICommand tempVar4 = new UICommand("OnExport", this); //$NON-NLS-1$
            tempVar4.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar4.setIsDefault(true);
            model.getCommands().add(tempVar4);
            UICommand tempVar5 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar5.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar5.setIsCancel(true);
            model.getCommands().add(tempVar5);
        }
    }

    private boolean SelectedTemplatesOnDifferentDataCenters()
    {
        ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
        // return templates.GroupBy(a => a.storage_pool_id).Count() > 1 ? true : false;

        Map<NGuid, ArrayList<VmTemplate>> t =
                new HashMap<NGuid, ArrayList<VmTemplate>>();
        for (VmTemplate a : templates)
        {
            if (!a.getId().equals(NGuid.Empty))
            {
                if (!t.containsKey(a.getstorage_pool_id()))
                {
                    t.put(a.getstorage_pool_id(), new ArrayList<VmTemplate>());
                }

                ArrayList<VmTemplate> list = t.get(a.getstorage_pool_id());
                list.add(a);
            }
        }

        return t.size() > 1 ? true : false;
    }

    private void showWarningOnExistingTemplates(ExportVmModel model)
    {
        Guid storageDomainId = ((storage_domains) model.getStorage().getSelectedItem()).getId();
        storage_pool storagePool = DataProvider.GetFirstStoragePoolByStorageDomain(storageDomainId);
        String existingTemplates = ""; //$NON-NLS-1$
        if (storagePool != null)
        {
            GetAllFromExportDomainQueryParamenters tempVar =
                    new GetAllFromExportDomainQueryParamenters(storagePool.getId(), storageDomainId);
            tempVar.setGetAll(true);
            VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar);

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                // foreach (VmTemplate template in SelectedItems.Cast<VmTemplate>())
                for (VmTemplate template : Linq.<VmTemplate> Cast(getSelectedItems()))
                {
                    boolean found = false;
                    HashMap<VmTemplate, ArrayList<DiskImage>> items =
                            (HashMap<VmTemplate, ArrayList<DiskImage>>) returnValue.getReturnValue();

                    for (VmTemplate a : items.keySet())
                    {
                        if (a.getId().equals(template.getId()))
                        {
                            found = true;
                            break;
                        }
                    }

                    // if (((Dictionary<VmTemplate,
                    // List<DiskImage>>)returnValue.ReturnValue).getKey()s.SingleOrDefault(a => a.vmt_guid ==
                    // template.vmt_guid) != null)
                    if (found)
                    {
                        existingTemplates += "\u2022    " + template.getname() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            if (!StringHelper.isNullOrEmpty(existingTemplates))
            {
                model.setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .templatesAlreadyExistonTargetExportDomain(existingTemplates));
            }
        }
    }

    private void OnExport()
    {
        ExportVmModel model = (ExportVmModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmTemplate a = (VmTemplate) item;
            if (a.getId().equals(NGuid.Empty))
            {
                continue;
            }
            MoveOrCopyParameters tempVar =
                    new MoveOrCopyParameters(a.getId(),
                            ((storage_domains) model.getStorage().getSelectedItem()).getId());
            tempVar.setForceOverride((Boolean) model.getForceOverride().getEntity());
            list.add(tempVar);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ExportVmTemplate, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ExportVmModel localModel = (ExportVmModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new TemplateGeneralModel());
        list.add(new TemplateVmListModel());
        list.add(new TemplateInterfaceListModel());
        list.add(new TemplateDiskListModel());
        list.add(new TemplateStorageListModel());
        addCustomModelsDetailModelList(list);
        setDetailModels(list);
    }

    protected void addCustomModelsDetailModelList(ObservableCollection<EntityModel> list) {
        list.add(new TemplateEventListModel());
        list.add(new PermissionListModel());
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("template"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VmTemplate);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VmTemplate, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    private void Edit()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        UnitVmModel model = new UnitVmModel(new TemplateVmModelBehavior(template));
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTemplateTitle());
        model.setHashName("edit_template"); //$NON-NLS-1$
        model.setVmType(template.getvm_type());

        model.Initialize(this.getSystemTreeSelectedItem());

        UICommand command;

        command = new UICommand("OnSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTemplatesTitle());
        model.setHashName("remove_template"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().templatesMsg());

        ArrayList<String> items = new ArrayList<String>();
        ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
        for (VmTemplate template : templates)
        {
            if (!template.getId().equals(NGuid.Empty))
            {
                items.add(template.getname());
            }
        }

        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmTemplate a = (VmTemplate) item;
            list.add(new VmTemplateParametersBase(a.getId()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplate, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void OnSave()
    {
        UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.Validate())
        {
            return;
        }

        String name = (String) model.getName().getEntity();

        AsyncDataProvider.IsTemplateNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        TemplateListModel templateListModel = (TemplateListModel) target;
                        boolean isNameUnique = (Boolean) returnValue;
                        templateListModel.PostNameUniqueCheck(isNameUnique);

                    }
                }), name);
    }

    public void PostNameUniqueCheck(boolean isNameUnique)
    {
        UnitVmModel model = (UnitVmModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        VmTemplate selectedItem = (VmTemplate) getSelectedItem();
        VmTemplate template = (VmTemplate) Cloner.clone(selectedItem);

        String name = (String) model.getName().getEntity();

        // Check name unicitate.
        if (!isNameUnique && name.compareToIgnoreCase(template.getname()) != 0)
        {
            model.getName()
                    .getInvalidityReasons()
                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
            model.getName().setIsValid(false);
            model.setIsGeneralTabValid(false);
            return;
        }

        // Save changes.
        template.setvm_type(model.getVmType());
        template.setname(name);
        template.setos((VmOsType) model.getOSType().getSelectedItem());
        template.setnum_of_monitors((Integer) model.getNumOfMonitors().getSelectedItem());
        template.setAllowConsoleReconnect((Boolean) model.getAllowConsoleReconnect().getEntity());
        template.setdescription((String) model.getDescription().getEntity());
        template.setdomain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem() : ""); //$NON-NLS-1$
        template.setmem_size_mb((Integer) model.getMemSize().getEntity());
        template.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        template.settime_zone((model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ? ((Map.Entry<String, String>) model.getTimeZone()
                .getSelectedItem()).getKey()
                : ""); //$NON-NLS-1$
        template.setnum_of_sockets((Integer) model.getNumOfSockets().getEntity());
        template.setcpu_per_socket((Integer) model.getTotalCPUCores().getEntity()
                / (Integer) model.getNumOfSockets().getEntity());
        template.setusb_policy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        template.setis_auto_suspend(false);
        template.setis_stateless((Boolean) model.getIsStateless().getEntity());
        template.setdefault_boot_sequence(model.getBootSequence());
        template.setiso_path(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        template.setauto_startup((Boolean) model.getIsHighlyAvailable().getEntity());
        template.setkernel_url((String) model.getKernel_path().getEntity());
        template.setkernel_params((String) model.getKernel_parameters().getEntity());
        template.setinitrd_url((String) model.getInitrd_path().getEntity());

        if (model.getQuota().getIsAvailable()) {
            template.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        template.setdefault_display_type((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        template.setpriority((Integer) prioritySelectedItem.getEntity());

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.UpdateVmTemplate, new UpdateVmTemplateParameters(template),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        TemplateListModel localModel = (TemplateListModel) result.getState();
                        localModel.PostUpdateVmTemplate(result.getReturnValue());

                    }
                }, this);
    }

    public void PostUpdateVmTemplate(VdcReturnValueBase returnValue)
    {
        UnitVmModel model = (UnitVmModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
        }
    }

    private void Cancel()
    {
        Frontend.Unsubscribe();

        setWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private boolean SelectedItemsContainBlankTemplate()
    {
        if (getSelectedItems() != null)
        {
            ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
            for (VmTemplate template : templates)
            {
                if (template != null && template.getId().equals(NGuid.Empty))
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected void UpdateActionAvailability()
    {
        VmTemplate item = (VmTemplate) getSelectedItem();
        ArrayList items =
                (((ArrayList) getSelectedItems()) != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        boolean blankSelected = getSelectedItem() != null && Guid.OpEquality(item.getId(), NGuid.Empty);

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && item.getstatus() != VmTemplateStatus.Locked);
        if (getEditCommand().getIsExecutionAllowed() && blankSelected)
        {
            getEditCommand().setIsExecutionAllowed(false);
            getEditCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeEdited());
        }

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.RemoveVmTemplate));
        if (getRemoveCommand().getIsExecutionAllowed() && blankSelected)
        {
            getRemoveCommand().setIsExecutionAllowed(false);
            getRemoveCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeRemoved());
        }

        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.ExportVmTemplate));

        if (getExportCommand().getIsExecutionAllowed() && blankSelected)
        {
            getExportCommand().setIsExecutionAllowed(false);
            getExportCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeExported());
        }

        getCopyCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.MoveOrCopyTemplate));

        if (getCopyCommand().getIsExecutionAllowed() && blankSelected)
        {
            getCopyCommand().setIsExecutionAllowed(false);
            getCopyCommand().getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .blankTemplateCannotBeCopied());
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getCopyCommand())
        {
            Copy();
        }
        else if (command == getExportCommand())
        {
            Export();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnExport")) //$NON-NLS-1$
        {
            OnExport();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateListModel"; //$NON-NLS-1$
    }
}
