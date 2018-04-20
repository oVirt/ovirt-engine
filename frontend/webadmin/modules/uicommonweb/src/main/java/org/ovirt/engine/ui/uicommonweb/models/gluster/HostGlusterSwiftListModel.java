package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class HostGlusterSwiftListModel extends SearchableListModel<VDS, GlusterServerService> {

    private UICommand startSwiftCommand;

    public UICommand getStartSwiftCommand() {
        return startSwiftCommand;
    }

    public void setStartSwiftCommand(UICommand startSwiftCommand) {
        this.startSwiftCommand = startSwiftCommand;
    }

    private UICommand stopSwiftCommand;

    public UICommand getStopSwiftCommand() {
        return stopSwiftCommand;
    }

    public void setStopSwiftCommand(UICommand stopSwiftCommand) {
        this.stopSwiftCommand = stopSwiftCommand;
    }

    private UICommand restartSwiftCommand;

    public UICommand getRestartSwiftCommand() {
        return restartSwiftCommand;
    }

    public void setRestartSwiftCommand(UICommand restartSwiftCommand) {
        this.restartSwiftCommand = restartSwiftCommand;
    }

    public HostGlusterSwiftListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().glusterSwiftTitle());
        setHelpTag(HelpTag.gluster_swift);
        setHashName("gluster_swift"); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);

        setStartSwiftCommand(new UICommand("StartSwift", this)); //$NON-NLS-1$
        setStopSwiftCommand(new UICommand("StopSwift", this)); //$NON-NLS-1$
        setRestartSwiftCommand(new UICommand("RestartSwift", this)); //$NON-NLS-1$

        getStartSwiftCommand().setIsExecutionAllowed(false);
        getStopSwiftCommand().setIsExecutionAllowed(false);
        getRestartSwiftCommand().setIsExecutionAllowed(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
        updateActionAvailability();
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

        AsyncDataProvider.getInstance().getGlusterSwiftServices(new AsyncQuery<>(returnValue -> {
            setItems(returnValue);
            updateActionAvailability();
        }), getEntity().getId());
    }

    private void updateActionAvailability() {
        boolean enableStart = false;
        boolean enableStop = false;
        boolean enableRestart = false;

        List<GlusterServerService> serviceList = (List<GlusterServerService>) getItems();
        if (serviceList == null || serviceList.isEmpty()) {
            enableStart = false;
            enableStop = false;
            enableRestart = false;
        } else {
            for (GlusterServerService service : serviceList) {
                if (service.getStatus() != GlusterServiceStatus.NOT_AVAILABLE) {
                    if (service.getStatus() != GlusterServiceStatus.RUNNING) {
                        enableStart = true;
                    }
                    if (service.getStatus() != GlusterServiceStatus.STOPPED) {
                        enableStop = true;
                    }
                    enableRestart = true;
                }

                if (enableStart && enableStop && enableStart) {
                    break;
                }
            }
        }

        getStartSwiftCommand().setIsExecutionAllowed(enableStart);
        getStopSwiftCommand().setIsExecutionAllowed(enableStop);
        getRestartSwiftCommand().setIsExecutionAllowed(enableRestart);
    }

    private void startSwift() {
        manageSwift("start"); //$NON-NLS-1$
    }

    private void stopSwift() {
        manageSwift("stop"); //$NON-NLS-1$
    }

    private void restartSwift() {
        manageSwift("restart"); //$NON-NLS-1$
    }

    private void manageSwift(String action) {
        GlusterServiceParameters parameters =
                new GlusterServiceParameters(getEntity().getClusterId(),
                        getEntity().getId(),
                        ServiceType.GLUSTER_SWIFT, action);

        Frontend.getInstance().runAction(ActionType.ManageGlusterService, parameters);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command.equals(getStartSwiftCommand())) {
            startSwift();
        } else if (command.equals(getStopSwiftCommand())) {
            stopSwift();
        } else if (command.equals(getRestartSwiftCommand())) {
            restartSwift();
        }
    }

    @Override
    protected String getListName() {
        return "HostGlusterSwiftListModel"; //$NON-NLS-1$
    }

}
