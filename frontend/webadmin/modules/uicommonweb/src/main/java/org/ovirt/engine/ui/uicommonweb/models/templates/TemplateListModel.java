package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.Collections;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
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
import org.ovirt.engine.core.compat.StringFormat;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
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
            java.util.ArrayList<Guid> items = new java.util.ArrayList<Guid>();
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
        setTitle("Templates");

        setDefaultSearchString("Template:");
        setSearchString(getDefaultSearchString());

        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));
        setExportCommand(new UICommand("Export", this));
        setCopyCommand(new UICommand("Copy", this));

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

        ListModel model = new ListModel();
        setWindow(model);
        model.setTitle("Copy Template");
        model.setHashName("copy_template");

        // Select all active data storage domains where the template is not
        // copied to.
        AsyncDataProvider.GetStorageDomainListByTemplate(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        TemplateListModel templateListModel = (TemplateListModel) target;
                        java.util.ArrayList<storage_domains> domainsWithTemplate =
                                (java.util.ArrayList<storage_domains>) returnValue;
                        templateListModel
                                .PostCopyGetStorageDomains(domainsWithTemplate);
                    }
                }), template.getId());
    }

    private void PostCopyGetStorageDomains(java.util.ArrayList<storage_domains> domainsWithTemplate)
    {
        VmTemplate template = (VmTemplate) getSelectedItem();
        Guid storagePoolId = template.getstorage_pool_id() != null ? template
                .getstorage_pool_id().getValue() : NGuid.Empty;

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(new Object[] {
                this, domainsWithTemplate }, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                Object[] array = (Object[]) target;
                TemplateListModel templateListModel = (TemplateListModel) array[0];
                java.util.ArrayList<storage_domains> domainsWithTemplate =
                        (java.util.ArrayList<storage_domains>) array[1];
                java.util.ArrayList<storage_domains> storageDomains =
                        (java.util.ArrayList<storage_domains>) returnValue;

                Collections.sort(storageDomains,
                        new Linq.StorageDomainByNameComparer());
                templateListModel.PostCopyGetStorageDomainList(storageDomains,
                        domainsWithTemplate);
            }
        }), storagePoolId);
    }

    private void PostCopyGetStorageDomainList(
            java.util.ArrayList<storage_domains> storageDomains,
            java.util.ArrayList<storage_domains> domainsWithTemplate)
    {
        java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
        boolean isTemplateExistInOneActiveDomain = false;
        for (storage_domains a : storageDomains) {
            boolean templateNotExistInAnyDomain_iter = true;
            for (storage_domains b : domainsWithTemplate) {
                if (b.getId().equals(a.getId())
                        && (b.getstatus() == null ? StorageDomainStatus.InActive
                                : b.getstatus()) == StorageDomainStatus.Active) {
                    templateNotExistInAnyDomain_iter = false;
                    isTemplateExistInOneActiveDomain = true;
                    break;
                }
            }

            if ((a.getstorage_domain_type() == StorageDomainType.Data || a
                    .getstorage_domain_type() == StorageDomainType.Master)
                    && templateNotExistInAnyDomain_iter
                    && (a.getstatus() == null ? null : a.getstatus()) == StorageDomainStatus.Active) {
                EntityModel entityModel = new EntityModel();
                entityModel.setEntity(a);

                items.add(entityModel);
            }
        }

        ListModel model = (ListModel) getWindow();
        model.setItems(items);

        if (items.size() == 1) {
            items.get(0).setIsSelected(true);
        }

        if (items.isEmpty()) {
            if (isTemplateExistInOneActiveDomain) {
                model.setMessage("Template already exists on all available Storage Domains.");
            } else {
                model.setMessage("No Storage Domain is available - check Storage Domains and Hosts status.");
            }

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        } else {
            UICommand tempVar2 = new UICommand("OnCopy", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    private void OnCopy()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();
        ListModel model = (ListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> items = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object item : model.getItems())
        {
            EntityModel a = (EntityModel) item;
            if (a.getIsSelected())
            {
                items.add(new MoveOrCopyParameters(template.getId(), ((storage_domains) a.getEntity()).getId()));
            }
        }

        // should be only 1
        if (items.isEmpty())
        {
            return;
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.MoveOrCopyTemplate, items,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ListModel localModel = (ListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void Export()
    {
        VmTemplate template = (VmTemplate) getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        ExportVmModel model = new ExportVmModel();
        setWindow(model);
        model.setTitle("Backup Template");
        model.setHashName("backup_template");

        model.getCollapseSnapshots().setIsAvailable(false);

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        TemplateListModel templateListModel = (TemplateListModel) target;
                        java.util.ArrayList<storage_domains> storageDomains =
                                (java.util.ArrayList<storage_domains>) returnValue;
                        java.util.ArrayList<storage_domains> filteredStorageDomains =
                                new java.util.ArrayList<storage_domains>();

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

    private void PostExportGetStorageDomainList(java.util.ArrayList<storage_domains> storageDomains)
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

            model.setMessage("Templates reside on several Data Centers. Make sure the exported Templates reside on the same Data Center.");

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }

        else if (storageDomains.isEmpty()) {
            model.getForceOverride().setIsChangable(false);

            model.setMessage("There is no Export Domain to export the Template into. Please attach an Export Domain to the Template's Data Center.");

            UICommand tempVar2 = new UICommand("Cancel", this);
            tempVar2.setTitle("Close");
            tempVar2.setIsDefault(true);
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
        } else if (!isAllStoragesActive) {
            model.getForceOverride().setIsChangable(false);

            model.setMessage("The relevant Export Domain in not active. Please activate it.");

            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Close");
            tempVar3.setIsDefault(true);
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        } else {
            showWarningOnExistingTemplates(model);

            UICommand tempVar4 = new UICommand("OnExport", this);
            tempVar4.setTitle("OK");
            tempVar4.setIsDefault(true);
            model.getCommands().add(tempVar4);
            UICommand tempVar5 = new UICommand("Cancel", this);
            tempVar5.setTitle("Cancel");
            tempVar5.setIsCancel(true);
            model.getCommands().add(tempVar5);
        }
    }

    private boolean SelectedTemplatesOnDifferentDataCenters()
    {
        java.util.ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
        // return templates.GroupBy(a => a.storage_pool_id).Count() > 1 ? true : false;

        java.util.Map<NGuid, java.util.ArrayList<VmTemplate>> t =
                new java.util.HashMap<NGuid, java.util.ArrayList<VmTemplate>>();
        for (VmTemplate a : templates)
        {
            if (!a.getId().equals(NGuid.Empty))
            {
                if (!t.containsKey(a.getstorage_pool_id()))
                {
                    t.put(a.getstorage_pool_id(), new java.util.ArrayList<VmTemplate>());
                }

                java.util.ArrayList<VmTemplate> list = t.get(a.getstorage_pool_id());
                list.add(a);
            }
        }

        return t.size() > 1 ? true : false;
    }

    private void showWarningOnExistingTemplates(ExportVmModel model)
    {
        Guid storageDomainId = ((storage_domains) model.getStorage().getSelectedItem()).getId();
        storage_pool storagePool = DataProvider.GetFirstStoragePoolByStorageDomain(storageDomainId);
        String existingTemplates = "";
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
                    java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>> items =
                            (java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>) returnValue.getReturnValue();

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
                        existingTemplates += "\u2022    " + template.getname() + "\n";
                    }
                }
            }
            if (!StringHelper.isNullOrEmpty(existingTemplates))
            {
                model.setMessage(StringFormat.format("Template(s):\n%1$s already exist on the target Export Domain. If you want to override them, please check the 'Force Override' check-box.",
                        existingTemplates));
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

        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
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
        list.add(new TemplateEventListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("template");
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

        if (getWindow() != null)
        {
            return;
        }

        UnitVmModel model = new UnitVmModel(new TemplateVmModelBehavior(template));
        setWindow(model);
        model.setTitle("Edit Template");
        model.setHashName("edit_template");
        model.setVmType(template.getvm_type());

        model.Initialize(this.getSystemTreeSelectedItem());

        UICommand tempVar = new UICommand("OnSave", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Template(s)");
        model.setHashName("remove_template");
        model.setMessage("Template(s)");

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        java.util.ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
        for (VmTemplate template : templates)
        {
            if (!template.getId().equals(NGuid.Empty))
            {
                items.add(template.getname());
            }
        }

        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
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

        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
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
            model.getName().getInvalidityReasons().add("Name must be unique.");
            model.getName().setIsValid(false);
            model.setIsGeneralTabValid(false);
            return;
        }

        // Save changes.
        template.setvm_type(model.getVmType());
        template.setname(name);
        template.setos((VmOsType) model.getOSType().getSelectedItem());
        template.setnum_of_monitors((Integer) model.getNumOfMonitors().getSelectedItem());
        template.setdescription((String) model.getDescription().getEntity());
        template.setdomain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem() : "");
        template.setmem_size_mb((Integer) model.getMemSize().getEntity());
        template.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getId());
        template.settime_zone((model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ? ((java.util.Map.Entry<String, String>) model.getTimeZone()
                .getSelectedItem()).getKey()
                : "");
        template.setnum_of_sockets((Integer) model.getNumOfSockets().getEntity());
        template.setcpu_per_socket((Integer) model.getTotalCPUCores().getEntity()
                / (Integer) model.getNumOfSockets().getEntity());
        template.setusb_policy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        template.setis_auto_suspend(false);
        template.setis_stateless((Boolean) model.getIsStateless().getEntity());
        template.setdefault_boot_sequence(model.getBootSequence());
        template.setiso_path(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem() : "");
        template.setauto_startup((Boolean) model.getIsHighlyAvailable().getEntity());
        template.setkernel_url((String) model.getKernel_path().getEntity());
        template.setkernel_params((String) model.getKernel_parameters().getEntity());
        template.setinitrd_url((String) model.getInitrd_path().getEntity());

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

        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private boolean SelectedItemsContainBlankTemplate()
    {
        if (getSelectedItems() != null)
        {
            java.util.ArrayList<VmTemplate> templates = Linq.<VmTemplate> Cast(getSelectedItems());
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
        java.util.ArrayList items =
                (((java.util.ArrayList) getSelectedItems()) != null) ? (java.util.ArrayList) getSelectedItems()
                        : new java.util.ArrayList();

        boolean blankSelected = getSelectedItem() != null && Guid.OpEquality(item.getId(), NGuid.Empty);

        getEditCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && item.getstatus() != VmTemplateStatus.Locked);
        if (getEditCommand().getIsExecutionAllowed() && blankSelected)
        {
            getEditCommand().setIsExecutionAllowed(false);
            getEditCommand().getExecuteProhibitionReasons().add("Blank Template cannot be edited");
        }

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.RemoveVmTemplate));
        if (getRemoveCommand().getIsExecutionAllowed() && blankSelected)
        {
            getRemoveCommand().setIsExecutionAllowed(false);
            getRemoveCommand().getExecuteProhibitionReasons().add("Blank Template cannot be removed");
        }

        getExportCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.ExportVmTemplate));

        if (getExportCommand().getIsExecutionAllowed() && blankSelected)
        {
            getExportCommand().setIsExecutionAllowed(false);
            getExportCommand().getExecuteProhibitionReasons().add("Blank Template cannot be exported");
        }

        getCopyCommand().setIsExecutionAllowed(items.size() == 1 && item != null
                && VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.MoveOrCopyTemplate));

        if (getCopyCommand().getIsExecutionAllowed() && blankSelected)
        {
            getCopyCommand().setIsExecutionAllowed(false);
            getCopyCommand().getExecuteProhibitionReasons().add("Blank Template cannot be copied");
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
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnCopy"))
        {
            OnCopy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnExport"))
        {
            OnExport();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateListModel";
    }
}
