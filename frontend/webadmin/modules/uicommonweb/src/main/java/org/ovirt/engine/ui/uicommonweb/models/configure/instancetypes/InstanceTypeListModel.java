package org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes;

import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.HwOnlyCoreUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MigrationOptionsUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UsbPolicyUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExistingInstanceTypeModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewInstanceTypeModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;

import java.util.ArrayList;
import java.util.List;

public class InstanceTypeListModel extends ListWithDetailsModel {

    private UICommand newInstanceTypeCommand;

    private UICommand editInstanceTypeCommand;

    private UICommand deleteInstanceTypeCommand;

    public InstanceTypeListModel() {
        setDefaultSearchString("Instancetypes:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        this.newInstanceTypeCommand = new UICommand("NewInstanceType", this); //$NON-NLS-1$
        this.editInstanceTypeCommand = new UICommand("EditInstanceType", this); //$NON-NLS-1$
        this.deleteInstanceTypeCommand = new UICommand("DeleteInstanceType", this); //$NON-NLS-1$

        setSearchPageSize(1000);

        updateActionAvailability();
    }

    @Override
    protected void syncSearch() {
        SearchParameters params = new SearchParameters(getSearchString(), SearchType.InstanceType, isCaseSensitiveSearch());
        params.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, params);
    }

    private void newInstanceType() {
        createWindow(
                new NewInstanceTypeModelBehavior(),
                "new_instance_type", //$NON-NLS-1$
                "OnNewInstanceType", //$NON-NLS-1$
                true,
                ConstantsManager.getInstance().getConstants().newInstanceTypeTitle(),
                HelpTag.new_instance_type
        );
    }

    private void editInstanceType() {
        createWindow(
                new ExistingInstanceTypeModelBehavior((InstanceType) getSelectedItem()),
                "edit_instance_type", //$NON-NLS-1$
                "OnEditInstanceType", //$NON-NLS-1$
                false,
                ConstantsManager.getInstance().getConstants().editInstanceTypeTitle(),
                HelpTag.edit_instance_type
        );
    }

    private void deleteInstanceType() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel window = new ConfirmationModel();
        setWindow(window);
        window.setTitle(ConstantsManager.getInstance().getConstants().removeInstanceTypeTitle());
        window.setHelpTag(HelpTag.remove_instance_type);
        window.setHashName("remove_instance_type"); //$NON-NLS-1$
        List<String> instanceTypeNames = new ArrayList<String>();
        for (InstanceType instanceType : (List<InstanceType>) getSelectedItems()) {
            instanceTypeNames.add(instanceType.getName());
        }

        window.setItems(instanceTypeNames);

        UICommand tempVar = new UICommand("OnDeleteInstanceType", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        window.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        window.getCommands().add(tempVar2);
    }

    private void onNewInstanceType() {
        if (!((UnitVmModel) getWindow()).validateHwPart()) {
            return;
        }

        AsyncDataProvider.isTemplateNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        boolean isNameUnique = (Boolean) returnValue;
                        if (isNameUnique) {
                            postInstanceTypeNameUniqueCheck();
                        } else {
                            UnitVmModel VmModel = (UnitVmModel) getWindow();
                            VmModel.getInvalidityReasons().clear();
                            VmModel.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance()
                                            .getConstants()
                                            .nameMustBeUniqueInvalidReason());
                            VmModel.getName().setIsValid(false);
                            VmModel.setIsValid(false);
                        }
                    }
                }), ((UnitVmModel) getWindow()).getName().getEntity());
    }

    private void buildVmStatic(VmBase vmBase) {
        UnitVmModel model = (UnitVmModel) getWindow();
        BuilderExecutor.build(model, vmBase,
                new HwOnlyCoreUnitToVmBaseBuilder(),
                new NameUnitToVmBaseBuilder(),
                new UsbPolicyUnitToVmBaseBuilder(),
                new MigrationOptionsUnitToVmBaseBuilder()
        );

        // CommonUnitToVmBaseBuilder
        vmBase.setAutoStartup(model.getIsHighlyAvailable().getEntity());
        vmBase.setPriority(model.getPriority().getSelectedItem().getEntity());
    }

    private void postInstanceTypeNameUniqueCheck() {
        UnitVmModel model = (UnitVmModel) getWindow();
        VM vm = new VM();
        buildVmStatic(vm.getStaticData());
        vm.setVmDescription(model.getDescription().getEntity());

        AddVmTemplateParameters addInstanceTypeParameters =
                new AddVmTemplateParameters(vm, model.getName().getEntity(), model.getDescription().getEntity());
        addInstanceTypeParameters.setTemplateType(VmEntityType.INSTANCE_TYPE);
        addInstanceTypeParameters.setVmTemplateId(null);
        addInstanceTypeParameters.setPublicUse(true);

        addInstanceTypeParameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        addInstanceTypeParameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        addInstanceTypeParameters.setBalloonEnabled(model.getMemoryBalloonDeviceEnabled().getEntity());
        addInstanceTypeParameters.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());

        setVmWatchdogToParams(model, addInstanceTypeParameters);

        getWindow().startProgress(null);

        Frontend.getInstance().runAction(VdcActionType.AddVmTemplate, addInstanceTypeParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        getWindow().stopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded()) {
                            cancel();
                        }
                    }
                }, this);
    }

    private void onEditInstanceType() {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.validateHwPart()) {
            return;
        }

        VmTemplate instanceType = (VmTemplate) Cloner.clone(selectedItem);
        instanceType.setTemplateType(VmEntityType.INSTANCE_TYPE);
        buildVmStatic(instanceType);
        instanceType.setDescription(model.getDescription().getEntity());

        UpdateVmTemplateParameters updateInstanceTypeParameters =
                new UpdateVmTemplateParameters(instanceType);

        updateInstanceTypeParameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        updateInstanceTypeParameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        updateInstanceTypeParameters.setBalloonEnabled(model.getMemoryBalloonDeviceEnabled().getEntity());
        updateInstanceTypeParameters.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());

        setVmWatchdogToParams(model, updateInstanceTypeParameters);

        getWindow().startProgress(null);

        Frontend.getInstance().runAction(VdcActionType.UpdateVmTemplate, updateInstanceTypeParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        getWindow().stopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded()) {
                            cancel();
                        }
                    }
                }, this);
    }


    private void onDeleteInstanceType() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (InstanceType instanceType : (List<InstanceType>) getSelectedItems()) {
            list.add(new VmTemplateParametersBase(instanceType.getId()));
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.RemoveVmTemplate, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        model.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void createWindow(VmModelBehaviorBase<UnitVmModel> behavior, String hashName, String onOkAction, boolean isNew, String title, HelpTag helpTag) {
        if (getWindow() != null) {
            return;
        }

        UnitVmModel model = new UnitVmModel(behavior);
        setWindow(model);

        model.setTitle(title);
        model.setHelpTag(helpTag);
        model.setHashName(hashName); //$NON-NLS-1$
        model.setIsNew(isNew);

        model.initialize(null);

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        UICommand newTemplate = new UICommand(onOkAction, this); //$NON-NLS-1$
        newTemplate.setTitle(ConstantsManager.getInstance().getConstants().ok());
        newTemplate.setIsDefault(true);
        model.getCommands().add(newTemplate);

        UICommand cancel = new UICommand("Cancel", this); //$NON-NLS-1$
        cancel.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancel.setIsCancel(true);
        model.getCommands().add(cancel);
    }

    private void cancel() {
        setWindow(null);
    }

    protected void updateActionAvailability() {
        int numOfSelectedItems = getSelectedItems() != null ? getSelectedItems().size() : 0;

        getEditInstanceTypeCommand().setIsExecutionAllowed(numOfSelectedItems == 1);
        getDeleteInstanceTypeCommand().setIsExecutionAllowed(numOfSelectedItems > 0);
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();

        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "InstanceTypeListModel"; //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewInstanceTypeCommand()) {
            newInstanceType();
        } else if (ObjectUtils.objectsEqual(command.getName(), "OnNewInstanceType")) { //$NON-NLS-1$
            onNewInstanceType();
        } else if (command == getEditInstanceTypeCommand()) {
            editInstanceType();
        } else if (ObjectUtils.objectsEqual(command.getName(), "OnEditInstanceType")) { //$NON-NLS-1$
            onEditInstanceType();
        } else if (command == getDeleteInstanceTypeCommand()) {
            deleteInstanceType();
        } else if (ObjectUtils.objectsEqual(command.getName(), "OnDeleteInstanceType")) { //$NON-NLS-1$
            onDeleteInstanceType();
        } else if (ObjectUtils.objectsEqual(command.getName(), "Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected void initDetailModels() {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new InstanceTypeGeneralModel());
        setDetailModels(list);
    }

    private void setVmWatchdogToParams(final UnitVmModel model, VmTemplateParametersBase updateVmParams) {
        VmWatchdogType wdModel = VmWatchdogType.getByName(model.getWatchdogModel()
                .getSelectedItem());
        updateVmParams.setUpdateWatchdog(true);
        if (wdModel != null) {
            VmWatchdog vmWatchdog = new VmWatchdog();
            vmWatchdog.setAction(VmWatchdogAction.getByName(model.getWatchdogAction()
                    .getSelectedItem()));
            vmWatchdog.setModel(wdModel);
            updateVmParams.setWatchdog(vmWatchdog);
        }
    }

    public UICommand getNewInstanceTypeCommand() {
        return newInstanceTypeCommand;
    }

    public UICommand getEditInstanceTypeCommand() {
        return editInstanceTypeCommand;
    }

    public UICommand getDeleteInstanceTypeCommand() {
        return deleteInstanceTypeCommand;
    }

}
