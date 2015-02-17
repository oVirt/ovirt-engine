package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VolumeGeoRepListModel extends SearchableListModel<GlusterVolumeEntity, GlusterGeoRepSession> {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();
    private UICommand newSessionCommand;
    private UICommand removeSessionCommand;
    private UICommand startSessionCommand;
    private UICommand stopSessionCommand;
    private UICommand sessionOptionsCommand;
    private UICommand viewSessionDetailsCommand;
    private UICommand refreshSessionsCommand;
    private UICommand pauseSessionCommand;
    private UICommand resumeSessionCommand;

    @Override
    protected String getListName() {
        return "VolumeGeoReplicationModel";//$NON-NLS-1$
    }

    public VolumeGeoRepListModel() {
        setTitle(constants.geoReplicationTitle());
        setHelpTag(HelpTag.geo_replication);
        setHashName("geo_replication");//$NON-NLS-1$
        setNewSessionCommand(new UICommand("createNewSession", this));//$NON-NLS-1$
        setRemoveSessionCommand(new UICommand("removeSession", this));//$NON-NLS-1$
        setStartSessionCommand(new UICommand("startSession", this));//$NON-NLS-1$
        setStopSessionCommand(new UICommand("stopSession", this));//$NON-NLS-1$
        setSessionOptionsCommand(new UICommand("sessionOptions", this));//$NON-NLS-1$
        setViewSessionDetailsCommand(new UICommand("viewSessionDetails", this));//$NON-NLS-1$
        setRefreshSessionsCommand(new UICommand("refreshSessions", this));//$NON-NLS-1$
        setPauseSessionCommand(new UICommand("pauseSession", this));//$NON-NLS-1$
        setResumeSessionCommand(new UICommand("resumeSession", this));//$NON-NLS-1$
    }

    public UICommand getViewSessionDetailsCommand() {
        return viewSessionDetailsCommand;
    }

    public void setViewSessionDetailsCommand(UICommand viewDetailsCommand) {
        this.viewSessionDetailsCommand = viewDetailsCommand;
    }

    public UICommand getNewSessionCommand() {
        return newSessionCommand;
    }

    public void setNewSessionCommand(UICommand newSessionCommand) {
        this.newSessionCommand = newSessionCommand;
    }

    public UICommand getRemoveSessionCommand() {
        return removeSessionCommand;
    }

    public void setRemoveSessionCommand(UICommand removeSessionCommand) {
        this.removeSessionCommand = removeSessionCommand;
    }

    public UICommand getStartSessionCommand() {
        return startSessionCommand;
    }

    public void setStartSessionCommand(UICommand startCommand) {
        this.startSessionCommand = startCommand;
    }

    public UICommand getStopSessionCommand() {
        return stopSessionCommand;
    }

    public void setStopSessionCommand(UICommand stopCommand) {
        this.stopSessionCommand = stopCommand;
    }

    public UICommand getSessionOptionsCommand() {
        return sessionOptionsCommand;
    }

    public void setSessionOptionsCommand(UICommand optionsCommand) {
        this.sessionOptionsCommand = optionsCommand;
    }

    public UICommand getRefreshSessionsCommand() {
        return refreshSessionsCommand;
    }

    public void setRefreshSessionsCommand(UICommand optionsCommand) {
        this.refreshSessionsCommand = optionsCommand;
    }

    public UICommand getPauseSessionCommand() {
        return pauseSessionCommand;
    }

    public void setPauseSessionCommand(UICommand pauseSessionCommand) {
        this.pauseSessionCommand = pauseSessionCommand;
    }

    public UICommand getResumeSessionCommand() {
        return resumeSessionCommand;
    }

    public void setResumeSessionCommand(UICommand resumeSessionCommand) {
        this.resumeSessionCommand = resumeSessionCommand;
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);
        getSearchCommand().execute();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability(getEntity());
    }

    @Override
    public void search()
    {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterVolumeGeoRepStatusForMasterVolume(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<GlusterGeoRepSession> geoRepSessions = (ArrayList<GlusterGeoRepSession>) returnValue;
                Collections.sort(geoRepSessions, new Linq.GlusterVolumeGeoRepSessionComparer());
                setItems(geoRepSessions);
            }
        }), getEntity().getId());

    }

    private void updateActionAvailability(GlusterVolumeEntity volumeEntity) {
        boolean allowStartSessionCommand = false;
        boolean allowStopSessionCommand = false;
        boolean allowResumeSessionCommand = false;
        boolean allowPauseSessionCommand = false;
        if(volumeEntity == null) {
            return;
        }
        if (getSelectedItems() != null && getSelectedItems().size() == 1) {
            GlusterGeoRepSession selectedSession = getSelectedItem();
            GeoRepSessionStatus sessionStatus = selectedSession.getStatus();
            allowStartSessionCommand =
                    sessionStatus == GeoRepSessionStatus.NOTSTARTED || sessionStatus == GeoRepSessionStatus.STOPPED;
            allowStopSessionCommand = !allowStartSessionCommand;
            allowResumeSessionCommand = sessionStatus == GeoRepSessionStatus.PAUSED;
            allowPauseSessionCommand =
                    sessionStatus == GeoRepSessionStatus.ACTIVE || sessionStatus == GeoRepSessionStatus.INITIALIZING;
        }
        getNewSessionCommand().setIsAvailable(true);
        getRemoveSessionCommand().setIsAvailable(false);
        getStartSessionCommand().setIsExecutionAllowed(allowStartSessionCommand);
        getStopSessionCommand().setIsExecutionAllowed(allowStopSessionCommand);
        getPauseSessionCommand().setIsExecutionAllowed(allowPauseSessionCommand);
        getResumeSessionCommand().setIsExecutionAllowed(allowResumeSessionCommand);
        getSessionOptionsCommand().setIsExecutionAllowed(true);
        getViewSessionDetailsCommand().setIsAvailable(false);
        getRefreshSessionsCommand().setIsAvailable(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getRemoveSessionCommand())) {

        } else if(command.equals(getStartSessionCommand())) {
            startGeoRepSession();
        } else if(command.equals(getStopSessionCommand())) {
            stopGeoRepSession();
        }  else if(command.equals(getPauseSessionCommand())) {
            pauseGeoRepSession();
        } else if(command.equals(getResumeSessionCommand())) {
            resumeGeoRepSession();
        } else if(command.equals(getSessionOptionsCommand())) {

        } else if(command.equals(getViewSessionDetailsCommand())) {

        } else if (command.equals(getRefreshSessionsCommand())) {
            refreshSessions();
        } else if (command.getName().equalsIgnoreCase("onStartGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(VdcActionType.StartGlusterVolumeGeoRep);
        } else if (command.getName().equalsIgnoreCase("onStopGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(VdcActionType.StopGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("onPauseGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(VdcActionType.PauseGlusterVolumeGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("onResumeGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(VdcActionType.ResumeGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("closeWindow")) {//$NON-NLS-1$
            closeWindow();
        }
    }

    private void closeWindow() {
        setWindow(null);
    }

    private void startGeoRepSession() {
        performGeoRepAction("onStartGeoRepSession", constants.geoReplicationStartTitle(), HelpTag.volume_geo_rep_start_confirmation, "volume_geo_rep_start_confirmation", constants.startGeoRep(), VdcActionType.StartGlusterVolumeGeoRep, constants.startGeoRepProgressText());//$NON-NLS-1$//$NON-NLS-2$
    }

    private void stopGeoRepSession() {
        performGeoRepAction("onStopGeoRepSession", constants.geoReplicationStopTitle(), HelpTag.volume_geo_rep_stop_confirmation, "volume_geo_rep_stop_confirmation", constants.stopGeoRep(), VdcActionType.StopGeoRepSession, constants.stopGeoRepProgressText());//$NON-NLS-1$//$NON-NLS-2$
    }

    private void pauseGeoRepSession() {
        performGeoRepAction("onPauseGeoRepSession", constants.geoReplicationPauseTitle(), HelpTag.volume_geo_rep_pause_confirmation, "volume_geo_rep_pause_confirmation", constants.pauseGeoRep(), VdcActionType.PauseGlusterVolumeGeoRepSession, constants.pauseGeoRepProgressText());//$NON-NLS-1$//$NON-NLS-2$
    }

    private void resumeGeoRepSession() {
        performGeoRepAction("onResumeGeoRepSession", constants.geoReplicationResumeTitle(), HelpTag.volume_geo_rep_resume_confirmation, "volume_geo_rep_resume_confirmation", constants.resumeGeoRep(), VdcActionType.ResumeGeoRepSession, constants.resumeGeoRepProgressText());//$NON-NLS-1$//$NON-NLS-2$
    }

    private void performGeoRepAction(String commandName,
            String confirmTitle,
            HelpTag helpTag,
            String hashName,
            String action,
            VdcActionType actionType,
            String actionProgressText) {
        GlusterGeoRepSession selectedSession = getSelectedItem();
        if (selectedSession == null) {
            return;
        }

        initializeGeoRepActionConfirmation(confirmTitle, helpTag, hashName, constants.geoRepForceHelp(), messages.geoRepForceTitle(action), commandName);
        onGeoRepSessionAction(actionType);
    }

    private void initializeGeoRepActionConfirmation(String title, HelpTag helpTag, String hashName, String forceHelp, String forceLabelText, String commandName) {
        GlusterGeoRepSession selectedSession = getSelectedItem();
        GlusterVolumeGeoRepActionConfirmationModel cModel = new GlusterVolumeGeoRepActionConfirmationModel();
        cModel.setTitle(title);
        cModel.setHelpTag(helpTag);
        cModel.setHashName(hashName);

        setWindow(cModel);

        cModel.initWindow(selectedSession);

        cModel.setForceHelp(forceHelp);
        cModel.setForceLabel(forceLabelText);

        UICommand okCommand = new UICommand(commandName, this);
        okCommand.setTitle(constants.ok());
        okCommand.setIsDefault(true);
        cModel.getCommands().add(okCommand);

        UICommand cancelCommand = new UICommand("closeWindow", this);//$NON-NLS-1$
        cancelCommand.setTitle(constants.cancel());
        cancelCommand.setIsCancel(true);
        cModel.getCommands().add(cancelCommand);
    }

    private void refreshSessions() {
        Frontend.getInstance().runAction(VdcActionType.RefreshGeoRepSessions,
                new GlusterVolumeParameters(getEntity().getId()));
    }

    public void setEntity(GlusterVolumeEntity value)
    {
        super.setEntity(value);
        updateActionAvailability(value);
    }

    private void onGeoRepSessionAction(VdcActionType actionType) {
        final GlusterVolumeGeoRepActionConfirmationModel cModel = (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
        cModel.startProgress(null);
        boolean force = cModel.getForce().getEntity();
        GlusterGeoRepSession selectedSession = getSelectedItem();
        GlusterVolumeGeoRepSessionParameters sessionParamters = new GlusterVolumeGeoRepSessionParameters(selectedSession.getMasterVolumeId(), selectedSession.getId());
        sessionParamters.setForce(force);
        Frontend.getInstance().runAction(actionType, sessionParamters, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                if (cModel == null) {
                    return;
                }
                else {
                    cModel.stopProgress();
                    if (!result.getReturnValue().getSucceeded()) {
                        cModel.setMessage(result.getReturnValue().getFault().getMessage());
                    } else {
                        setWindow(null);
                    }
                }
            }
        },
        this,
        false);
    }
}
