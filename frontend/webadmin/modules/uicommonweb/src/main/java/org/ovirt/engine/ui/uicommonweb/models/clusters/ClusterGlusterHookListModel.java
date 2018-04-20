package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterClusterParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookContentType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ClusterGlusterHookListModel extends SearchableListModel<Cluster, GlusterHookEntity> {

    private UICommand enableHookCommand;

    private UICommand disableHookCommand;

    private UICommand viewHookCommand;

    private UICommand resolveConflictsCommand;

    private UICommand syncWithServersCommand;

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

    public UICommand getViewHookCommand() {
        return viewHookCommand;
    }

    public void setViewHookCommand(UICommand viewHookCommand) {
        this.viewHookCommand = viewHookCommand;
    }

    public UICommand getResolveConflictsCommand() {
        return resolveConflictsCommand;
    }

    public void setResolveConflictsCommand(UICommand resolveConflictsCommand) {
        this.resolveConflictsCommand = resolveConflictsCommand;
    }

    public UICommand getSyncWithServersCommand() {
        return syncWithServersCommand;
    }

    public void setSyncWithServersCommand(UICommand syncWithServersCommand) {
        this.syncWithServersCommand = syncWithServersCommand;
    }

    public ClusterGlusterHookListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().glusterHooksTitle());
        setHelpTag(HelpTag.gluster_hooks);
        setHashName("gluster_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);

        setEnableHookCommand(new UICommand("EnableHook", this)); //$NON-NLS-1$
        getEnableHookCommand().setIsExecutionAllowed(false);

        setDisableHookCommand(new UICommand("DisableHook", this)); //$NON-NLS-1$
        getDisableHookCommand().setIsExecutionAllowed(false);

        setViewHookCommand(new UICommand("ViewContent", this)); //$NON-NLS-1$
        getViewHookCommand().setIsExecutionAllowed(false);

        setResolveConflictsCommand(new UICommand("ResolveConflicts", this)); //$NON-NLS-1$
        getResolveConflictsCommand().setIsExecutionAllowed(false);

        setSyncWithServersCommand(new UICommand("SyncWithServers", this)); //$NON-NLS-1$
    }

    private void enableHook() {
        if (getEntity() == null || getSelectedItems() == null || getSelectedItems().size() == 0) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (GlusterHookEntity hook : getSelectedItems()) {
            list.add(new GlusterHookParameters(hook.getId()));
        }
        Frontend.getInstance().runMultipleAction(ActionType.EnableGlusterHook, list, null, null);
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
        model.setHelpTag(HelpTag.disable_hooks);
        model.setHashName("disable_hooks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().disableGlusterHooksMessage());

        ArrayList<String> list = new ArrayList<>();
        for (GlusterHookEntity hook : getSelectedItems()) {
            list.add(hook.getName());
        }
        model.setItems(list);

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnDisableHook", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("OnCancelConfirmation", this); //$NON-NLS-1$
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

        ArrayList<ActionParametersBase> list = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            GlusterHookEntity hook = (GlusterHookEntity) item;
            list.add(new GlusterHookParameters(hook.getId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.DisableGlusterHook, list,
                result -> {

                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancelConfirmation();
                }, model);
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    private void cancel() {
        setWindow(null);
    }

    private void viewHook() {
        if (getWindow() != null) {
            return;
        }

        GlusterHookEntity hookEntity = getSelectedItem();

        if (hookEntity == null) {
            return;
        }

        final GlusterHookContentModel contentModel = new GlusterHookContentModel();
        contentModel.setTitle(ConstantsManager.getInstance().getConstants().viewContentGlusterHookTitle());
        contentModel.setHelpTag(HelpTag.view_gluster_hook);
        contentModel.setHashName("view_gluster_hook"); //$NON-NLS-1$
        setWindow(contentModel);

        contentModel.startProgress();

        AsyncDataProvider.getInstance().getGlusterHookContent(new AsyncQuery<>(content -> {
            contentModel.getContent().setEntity(content);
            if (content == null) {
                contentModel.getContent().setIsAvailable(false);
                contentModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .viewContentErrorGlusterHook());
            } else if (content.length() == 0) {
                contentModel.getContent().setIsAvailable(false);
                contentModel.setMessage(ConstantsManager.getInstance()
                        .getConstants()
                        .viewContentEmptyGlusterHook());
            }
            contentModel.stopProgress();
        }), hookEntity.getId(), null);

        UICommand command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().close());
        command.setIsCancel(true);
        contentModel.getCommands().add(command);
    }

    private void resolveConflicts() {
        if (getWindow() != null) {
            return;
        }

        final GlusterHookEntity hookEntity = getSelectedItem();

        if (hookEntity == null) {
            return;
        }

        final GlusterHookResolveConflictsModel conflictsModel = new GlusterHookResolveConflictsModel();
        conflictsModel.setTitle(ConstantsManager.getInstance().getConstants().resolveConflictsGlusterHookTitle());
        conflictsModel.setHelpTag(HelpTag.gluster_hook_resolve_conflicts);
        conflictsModel.setHashName("gluster_hook_resolve_conflicts"); //$NON-NLS-1$
        hookEntity.setServerHooks(new ArrayList<GlusterServerHook>());
        conflictsModel.setGlusterHookEntity(hookEntity);
        setWindow(conflictsModel);
        conflictsModel.startProgress();

        AsyncDataProvider.getInstance().getGlusterHook(new AsyncQuery<>(returnValue -> {

            List<GlusterServerHook> serverHooks = returnValue.getServerHooks();
            hookEntity.setServerHooks(serverHooks);

            ArrayList<EntityModel<GlusterServerHook>> serverHookModels = new ArrayList<>();
            GlusterServerHook engineCopy = new GlusterServerHook();
            engineCopy.setHookId(hookEntity.getId());
            engineCopy.setServerName("Engine (Master)"); //$NON-NLS-1$
            engineCopy.setStatus(hookEntity.getStatus());
            engineCopy.setContentType(hookEntity.getContentType());
            engineCopy.setChecksum(hookEntity.getChecksum());
            EntityModel<GlusterServerHook> engineCopyModel = new EntityModel<>(engineCopy);
            serverHookModels.add(engineCopyModel);

            for (GlusterServerHook serverHook : serverHooks) {
                serverHookModels.add(new EntityModel<>(serverHook));
            }

            conflictsModel.getHookSources().setItems(serverHookModels);
            conflictsModel.getHookSources().setSelectedItem(engineCopyModel);

            ArrayList<GlusterServerHook> serverHooksWithEngine = new ArrayList<>(serverHooks);
            serverHooksWithEngine.add(0, engineCopy);
            conflictsModel.getServerHooksList().setItems(serverHooksWithEngine);
            conflictsModel.getServerHooksList().setSelectedItem(engineCopy);

            conflictsModel.stopProgress();

            UICommand command = UICommand.createDefaultOkUiCommand("OnResolveConflicts", ClusterGlusterHookListModel.this); //$NON-NLS-1$
            conflictsModel.getCommands().add(command);

            command = new UICommand("Cancel", ClusterGlusterHookListModel.this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().close());
            command.setIsCancel(true);
            conflictsModel.getCommands().add(command);
        }), hookEntity.getId(), true);
    }

    /*
     * If there are multiple types of conflicts found for a hook and the user choose to resolve them. The conflicts will
     * be resolved in the following order 1)Content Conflict, 2)Status Conflict, 3)Missing Conflict
     *
     * If any conflict resolution is failed the next one will not be executed
     */

    private void onResolveConflicts() {

        final GlusterHookResolveConflictsModel resolveConflictsModel = (GlusterHookResolveConflictsModel) getWindow();

        if (resolveConflictsModel == null) {
            return;
        }

        if (!resolveConflictsModel.isAnyResolveActionSelected()) {
            resolveConflictsModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .noResolveActionSelectedGlusterHook());
            return;
        }

        resolveConflictsModel.startProgress();

        GlusterHookEntity hookEntity = resolveConflictsModel.getGlusterHookEntity();

        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();

        if (resolveConflictsModel.getResolveContentConflict().getEntity()) {
            actionTypes.add(ActionType.UpdateGlusterHook);
            GlusterServerHook serverHook =
                    resolveConflictsModel.getServerHooksList().getSelectedItem();
            Guid serverId = (serverHook == null) ? null : serverHook.getServerId();
            parameters.add(new GlusterHookManageParameters(hookEntity.getId(), serverId));
            IFrontendActionAsyncCallback callback = result -> {
                if (result.getReturnValue().getSucceeded()) {
                    resolveConflictsModel.getResolveContentConflict().setEntity(Boolean.FALSE);
                    resolveConflictsModel.getResolveContentConflict().setIsChangeable(Boolean.FALSE);
                }
            };
            callbacks.add(callback);
        }

        if (resolveConflictsModel.getResolveStatusConflict().getEntity()) {
            boolean isEnable = resolveConflictsModel.getResolveStatusConflictEnable().getEntity();
            actionTypes.add(isEnable ? ActionType.EnableGlusterHook : ActionType.DisableGlusterHook);
            parameters.add(new GlusterHookParameters(hookEntity.getId()));
            IFrontendActionAsyncCallback callback = result -> {
                if (result.getReturnValue().getSucceeded()) {
                    resolveConflictsModel.getResolveStatusConflict().setEntity(Boolean.FALSE);
                    resolveConflictsModel.getResolveStatusConflict().setIsChangeable(Boolean.FALSE);
                }
            };
            callbacks.add(callback);
        }

        if (resolveConflictsModel.getResolveMissingConflict().getEntity()) {
            boolean isAdd = resolveConflictsModel.getResolveMissingConflictCopy().getEntity();
            actionTypes.add(isAdd ? ActionType.AddGlusterHook : ActionType.RemoveGlusterHook);
            parameters.add(new GlusterHookManageParameters(hookEntity.getId()));
            IFrontendActionAsyncCallback callback = result -> {
                if (result.getReturnValue().getSucceeded()) {
                    resolveConflictsModel.getResolveMissingConflict().setEntity(Boolean.FALSE);
                    resolveConflictsModel.getResolveMissingConflict().setIsChangeable(Boolean.FALSE);
                }
            };
            callbacks.add(callback);
        }

        IFrontendActionAsyncCallback onFinishCallback = result -> {
            if (result.getReturnValue().getSucceeded()) {
                resolveConflictsModel.stopProgress();
                cancel();
                syncSearch();
            }
        };

        IFrontendActionAsyncCallback failureCallback = result -> resolveConflictsModel.stopProgress();

        // Replacing the last callback with onFinishCallback, as we just want to close the dialog and execute the search
        if (callbacks.size() > 0) {
            callbacks.remove(callbacks.size() - 1);
            callbacks.add(onFinishCallback);
            Frontend.getInstance().runMultipleActions(actionTypes, parameters, callbacks, failureCallback, null);
        }
    }

    private void syncWithServers() {
        Frontend.getInstance().runAction(ActionType.RefreshGlusterHooks, new GlusterClusterParameters(getEntity().getId()));
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        boolean allowEnable = true;
        boolean allowDisable = true;
        boolean allowViewContent = true;
        boolean allowResolveConflict = true;
        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            allowEnable = false;
            allowDisable = false;
            allowViewContent = false;
            allowResolveConflict = false;
        } else {
            for (Object item : getSelectedItems()) {
                GlusterHookEntity hook = (GlusterHookEntity) item;
                if (hook.getStatus() == GlusterHookStatus.ENABLED) {
                    allowEnable = false;
                } else if (hook.getStatus() == GlusterHookStatus.DISABLED) {
                    allowDisable = false;
                }
                if (!allowEnable && !allowDisable) {
                    break;
                }
            }
            allowViewContent = getSelectedItems().size() == 1
                    && getSelectedItems().get(0).getContentType() == GlusterHookContentType.TEXT;
            allowResolveConflict = getSelectedItems().size() == 1
                    && getSelectedItems().get(0).hasConflicts();
        }
        getEnableHookCommand().setIsExecutionAllowed(allowEnable);
        getDisableHookCommand().setIsExecutionAllowed(allowDisable);
        getViewHookCommand().setIsExecutionAllowed(allowViewContent);
        getResolveConflictsCommand().setIsExecutionAllowed(allowResolveConflict);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
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

        AsyncDataProvider.getInstance().getGlusterHooks(new AsyncQuery<>(glusterHooks -> setItems(glusterHooks)), getEntity().getId());
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
        } else if (command.equals(getDisableHookCommand())) {
            disableHook();
        } else if (command.getName().equals("OnDisableHook")) { //$NON-NLS-1$
            onDisableHook();
        } else if (command.getName().equals("OnCancelConfirmation")) { //$NON-NLS-1$
            cancelConfirmation();
        } else if (command.equals(getViewHookCommand())) {
            viewHook();
        } else if (command.equals(getResolveConflictsCommand())) {
            resolveConflicts();
        } else if (command.getName().equals("OnResolveConflicts")) { //$NON-NLS-1$
            onResolveConflicts();
        } else if (command.equals(getSyncWithServersCommand())) {
            syncWithServers();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        }
    }
}
