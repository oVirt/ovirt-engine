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
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterGlusterHookListModel extends SearchableListModel {

    private UICommand enableHookCommand;

    public UICommand getEnableHookCommand() {
        return enableHookCommand;
    }

    public void setEnableHookCommand(UICommand enableHookCommand) {
        this.enableHookCommand = enableHookCommand;
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

    @Override
    protected void SelectedItemsChanged() {
        super.SelectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        if (getSelectedItems() == null || getSelectedItems().size() == 0) {
            getEnableHookCommand().setIsExecutionAllowed(false);
            return;
        }

        boolean allowEnable = true;
        for (Object item : getSelectedItems()) {
            GlusterHookEntity hook = (GlusterHookEntity) item;
            if (hook.getStatus() == GlusterHookStatus.ENABLED) {
                allowEnable = false;
                break;
            }
        }
        getEnableHookCommand().setIsExecutionAllowed(allowEnable);
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search() {
        if (getEntity() != null) {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getGlusterHooks(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                List<GlusterHookEntity> glusterHooks = (List<GlusterHookEntity>) returnValue;
                setItems(glusterHooks);
            }
        }), getEntity().getId());
    }

    @Override
    protected void AsyncSearch() {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    protected String getListName() {
        return "ClusterGlusterHookListModel"; //$NON-NLS-1$
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getEnableHookCommand())) {
            enableHook();
        }
    }
}
