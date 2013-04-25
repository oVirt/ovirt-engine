package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class ClusterGlusterHookListModel extends SearchableListModel {

    private UICommand enableHookCommand;

    private UICommand disableHookCommand;

    public UICommand getEnableHookCommand() {
        return enableHookCommand;
    }

    public void setEnableHookCommand(UICommand enableHookCommand) {
        this.enableHookCommand = enableHookCommand;
    }

    public UICommand getDisableHookCommand() {
        return disableHookCommand;
    }

    public void setDisableHookCommand(UICommand disableHookCommand) {
        this.disableHookCommand = disableHookCommand;
    }

    @Override
    public VDSGroup getEntity() {
        return (VDSGroup) super.getEntity();
    }

    public ClusterGlusterHookListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().glusterHooksTitle());
        setHashName("gluster_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);

        setEnableHookCommand(new UICommand("EnableHook", this)); //$NON-NLS-1$
        getEnableHookCommand().setIsExecutionAllowed(false);
        setDisableHookCommand(new UICommand("DisableHook", this)); //$NON-NLS-1$
        getDisableHookCommand().setIsExecutionAllowed(false);
    }

    private void enableHook() {
        if (getEntity() == null || getSelectedItems() == null || getSelectedItems().size() == 0) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            GlusterHookEntity hook = (GlusterHookEntity) item;
            list.add(new GlusterHookParameters(getEntity().getId(), hook.getId()));
        }
        Frontend.RunMultipleAction(VdcActionType.EnableGlusterHook, list, null, null);
    }

    private void disableHook() {
        if (getWindow() != null) {
            return;
        }

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().confirmDisableGlusterHooks());
        model.setHashName("disable_hooks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().disableGlusterHooksMessage());

        ArrayList<String> list = new ArrayList<String>();
        for (Object item : getSelectedItems()) {
            GlusterHookEntity hook = (GlusterHookEntity) item;
            list.add(hook.getName());
        }
        model.setItems(list);

        UICommand okCommand = new UICommand("OnDisableHook", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("OnCancelConfirmation", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onDisableHook() {
        if (getConfirmWindow() == null) {
            return;
        }

        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems()) {
            GlusterHookEntity hook = (GlusterHookEntity) item;
            list.add(new GlusterHookParameters(getEntity().getId(), hook.getId()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.DisableGlusterHook, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        cancelConfirmation();
                    }
                }, model);
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        boolean allowEnable = true;
        boolean allowDisable = true;

        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowEnable = false;
            allowDisable = false;
        }
        else {
            for (Object item : getSelectedItems()) {
                GlusterHookEntity hook = (GlusterHookEntity) item;
                if (hook.getStatus() == GlusterHookStatus.ENABLED) {
                    allowEnable = false;
                }
                else if (hook.getStatus() == GlusterHookStatus.DISABLED) {
                    allowDisable = false;
                }
                if (!allowEnable && !allowDisable) {
                    break;
                }
            }
        }
        getEnableHookCommand().setIsExecutionAllowed(allowEnable);
        getDisableHookCommand().setIsExecutionAllowed(allowDisable);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getGlusterHooks(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<GlusterHookEntity> glusterHooks = (List<GlusterHookEntity>) returnValue;
                setItems(glusterHooks);
            }
        }), getEntity().getId());
    }

    @Override
    protected void asyncSearch() {
        super.asyncSearch();
        syncSearch();
    }

    @Override
    protected String getListName() {
        return "ClusterGlusterHookListModel"; //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getEnableHookCommand())) {
            enableHook();
        }
        else if (command.equals(getDisableHookCommand())) {
            disableHook();
        }
        else if (command.getName().equals("OnDisableHook")) { //$NON-NLS-1$
            onDisableHook();
        }
        else if (command.getName().equals("OnCancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
        }
    }
}
