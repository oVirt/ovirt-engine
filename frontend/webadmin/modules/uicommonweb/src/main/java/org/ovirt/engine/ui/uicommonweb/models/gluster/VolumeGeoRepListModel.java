package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
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
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        getSearchCommand().execute();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability(getEntity());
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

        AsyncDataProvider.getInstance().getGlusterVolumeGeoRepStatusForMasterVolume(
                new SetSortedRawItemsAsyncQuery(Comparator.comparing(GlusterGeoRepSession::getSlaveVolumeName)),
                getEntity().getId());
    }

    private void updateActionAvailability(GlusterVolumeEntity volumeEntity) {
        boolean allowNewGeoRepSessionCommand = true;
        boolean allowStartSessionCommand = false;
        boolean allowStopSessionCommand = false;
        boolean allowResumeSessionCommand = false;
        boolean allowPauseSessionCommand = false;
        boolean allowSessionOptionsCommand = false;
        boolean allowRemoveSessionCommand = false;
        boolean allowSessionDetailsCommand = false;
        if(volumeEntity == null) {
            return;
        }
        if (getSelectedItems() != null && getSelectedItems().size() == 1) {
            GlusterGeoRepSession selectedSession =
                    getSelectedItem() == null ? getSelectedItems().get(0) : getSelectedItem();
            GeoRepSessionStatus sessionStatus = selectedSession.getStatus();
            allowStartSessionCommand =
                    sessionStatus == GeoRepSessionStatus.CREATED || sessionStatus == GeoRepSessionStatus.STOPPED;
            allowStopSessionCommand = !allowStartSessionCommand;
            allowResumeSessionCommand = sessionStatus == GeoRepSessionStatus.PAUSED;
            allowPauseSessionCommand =
                    sessionStatus == GeoRepSessionStatus.ACTIVE || sessionStatus == GeoRepSessionStatus.INITIALIZING;
            allowSessionOptionsCommand = true;
            allowNewGeoRepSessionCommand = volumeEntity.getStatus() == GlusterStatus.UP;
            allowRemoveSessionCommand =
                    sessionStatus == GeoRepSessionStatus.STOPPED || sessionStatus == GeoRepSessionStatus.CREATED;
            allowSessionDetailsCommand = true;
        }
        getNewSessionCommand().setIsExecutionAllowed(allowNewGeoRepSessionCommand);
        getRemoveSessionCommand().setIsExecutionAllowed(allowRemoveSessionCommand);
        getStartSessionCommand().setIsExecutionAllowed(allowStartSessionCommand);
        getStopSessionCommand().setIsExecutionAllowed(allowStopSessionCommand);
        getPauseSessionCommand().setIsExecutionAllowed(allowPauseSessionCommand);
        getResumeSessionCommand().setIsExecutionAllowed(allowResumeSessionCommand);
        getSessionOptionsCommand().setIsExecutionAllowed(allowSessionOptionsCommand);
        getViewSessionDetailsCommand().setIsExecutionAllowed(allowSessionDetailsCommand);
        getRefreshSessionsCommand().setIsAvailable(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getNewSessionCommand())) {
            createGeoRepSession();
        } else if (command.equals(getRemoveSessionCommand())) {
            confirmGeoRepAction(constants.geoReplicationRemoveTitle(), HelpTag.volume_geo_rep_remove_confirmation, "volume_geo_rep_remove_confirmation", "removeGeoRepSession", constants.removeGeoRep());//$NON-NLS-1$//$NON-NLS-2$
        } else if(command.equals(getStartSessionCommand())) {
            startGeoRepSession();
        } else if(command.equals(getStopSessionCommand())) {
            confirmGeoRepAction(constants.geoReplicationStopTitle(), HelpTag.volume_geo_rep_stop_confirmation, "volume_geo_rep_stop_confirmation", "stopGeoRepSesssion", constants.stopGeoRep());//$NON-NLS-1$//$NON-NLS-2$
        }  else if(command.equals(getPauseSessionCommand())) {
            confirmGeoRepAction(constants.geoReplicationPauseTitle(), HelpTag.volume_geo_rep_pause_confirmation, "volume_geo_rep_pause_confirmation", "pauseGeoRepSession", constants.pauseGeoRep());//$NON-NLS-1$//$NON-NLS-2$
        } else if(command.equals(getResumeSessionCommand())) {
            resumeGeoRepSession();
        } else if(command.equals(getSessionOptionsCommand())) {
            showSessionOptions();
        } else if(command.equals(getViewSessionDetailsCommand())) {
            showGeoRepSessionDetails(getSelectedItem());
        } else if (command.equals(getRefreshSessionsCommand())) {
            refreshSessions();
        } else if (command.getName().equalsIgnoreCase("onCreateSession")) {//$NON-NLS-1$
            onCreateSession();
        } else if (command.getName().equalsIgnoreCase("onStartGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(ActionType.StartGlusterVolumeGeoRep);
        } else if (command.getName().equalsIgnoreCase("onStopGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(ActionType.StopGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("onPauseGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(ActionType.PauseGlusterVolumeGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("onResumeGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(ActionType.ResumeGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("onRemoveGeoRepSession")) {//$NON-NLS-1$
            onGeoRepSessionAction(ActionType.DeleteGeoRepSession);
        } else if (command.getName().equalsIgnoreCase("ok")) {//$NON-NLS-1$
            updateConfig();
        } else if (command.getName().equalsIgnoreCase("closeWindow")) {//$NON-NLS-1$
            closeWindow();
        } else if(command.getName().equalsIgnoreCase("stopGeoRepSesssion")) {//$NON-NLS-1$
            stopGeoRepSession();
        } else if(command.getName().equalsIgnoreCase("removeGeoRepSession")) {//$NON-NLS-1$
            removeGeoRepSession();
        } else if(command.getName().equalsIgnoreCase("pauseGeoRepSession")) {//$NON-NLS-1$
            pauseGeoRepSession();
        } else if (command.getName().equalsIgnoreCase("closeConfirmWindow")) {//$NON-NLS-1$
            closeConfirmWindow();
        }
    }

    private void closeConfirmWindow() {
        setConfirmWindow(null);
    }

    private void populateStatus(final List<GlusterGeoRepSessionDetails> details) {
        final VolumeGeoRepSessionDetailsModel windowModel = new VolumeGeoRepSessionDetailsModel();
        windowModel.setHelpTag(HelpTag.geo_replication_status_detail);
        windowModel.setHashName("geo_replication_status_detail");//$NON-NLS-1$

        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        windowModel.setTitle(constants.geoReplicationSessionDetailsTitle());

        UICommand okCommand = new UICommand("closeWindow", this);//$NON-NLS-1$
        okCommand.setIsCancel(true);
        okCommand.setTitle(constants.ok());
        windowModel.getCommands().add(okCommand);

        setWindow(windowModel);

        final List<EntityModel<GlusterGeoRepSessionDetails>> detailRows = new ArrayList<>();
        for (GlusterGeoRepSessionDetails detail : details) {
            detailRows.add(new EntityModel<>(detail));
        }
        windowModel.getGeoRepSessionSummary().setItems(detailRows, detailRows.get(0));
    }

    public void showGeoRepSessionDetails(GlusterGeoRepSession session) {
        ArrayList<GlusterGeoRepSessionDetails> details = session.getSessionDetails();
        if(getWindow() != null) {
            return;
        }
        if(details == null || details.size() == 0) {
            final UIConstants constants = ConstantsManager.getInstance().getConstants();
            final ConfirmationModel cModel = new ConfirmationModel();
            cModel.setTitle(constants.geoReplicationSessionDetailsTitle());
            UICommand okCommand = new UICommand("closeConfirmWindow", this);//$NON-NLS-1$
            okCommand.setTitle(constants.ok());
            okCommand.setIsCancel(true);
            cModel.getCommands().add(okCommand);
            setConfirmWindow(cModel);
            cModel.setMessage(constants.geoRepSessionStatusDetailFetchFailed());
        } else {
            populateStatus(details);
        }
    }

    private void showSessionOptions() {
        if (getWindow() != null) {
            return;
        }
        GlusterGeoRepSession selectedGeoRepSession = getSelectedItem();
        GlusterVolumeGeoReplicationSessionConfigModel configModel = new GlusterVolumeGeoReplicationSessionConfigModel(selectedGeoRepSession);
        configModel.setTitle(constants.geoReplicationOptions());
        configModel.setHashName("volume_geo_rep_configuration_display");//$NON-NLS-1$
        configModel.setHelpTag(HelpTag.volume_geo_rep_configuration_display);
        configModel.startProgress();

        fetchConfigForSession(selectedGeoRepSession);
        setWindow(configModel);

        addUICommandsToConfigWindow(configModel);
    }

    private void fetchConfigForSession(GlusterGeoRepSession selectedSession) {
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeGeoRepConfigList, new IdQueryParameters(selectedSession.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    GlusterVolumeGeoReplicationSessionConfigModel geoRepConfigModel =
                            (GlusterVolumeGeoReplicationSessionConfigModel) getWindow();
                    geoRepConfigModel.stopProgress();
                    boolean queryExecutionStatus = returnValue.getSucceeded();
                    geoRepConfigModel.updateCommandExecutabilities(queryExecutionStatus);
                    if (!queryExecutionStatus) {
                        geoRepConfigModel.setMessage(ConstantsManager.getInstance().getConstants().errorInFetchingVolumeOptionList());
                    } else {
                        List<GlusterGeoRepSessionConfiguration> sessionConfigs = returnValue.getReturnValue();
                        List<EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>>> sessionConfigEntities =
                                new ArrayList<>();
                                for (GlusterGeoRepSessionConfiguration currentSession : sessionConfigs) {
                                    sessionConfigEntities.add(new EntityModel<>(new Pair<>(false,
                                            currentSession)));
                                }
                                geoRepConfigModel.getConfigsModel().setItems(sessionConfigEntities);
                                geoRepConfigModel.copyConfigsToMap(sessionConfigs);
                    }
                }));
    }

    private void updateConfig() {
        ArrayList<ActionType> actionTypes = new ArrayList<>();
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        List<IFrontendActionAsyncCallback> callbacks;

        final GlusterVolumeGeoReplicationSessionConfigModel geoRepConfigModel =
                (GlusterVolumeGeoReplicationSessionConfigModel) getWindow();

        Map<String, String> oldConfigs = geoRepConfigModel.getConfigs();

        geoRepConfigModel.startProgress();

        for (EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>> newConfigEntity : geoRepConfigModel.getConfigsModel()
                .getItems()) {
            Pair<Boolean, GlusterGeoRepSessionConfiguration> newConfigPair = newConfigEntity.getEntity();
            GlusterGeoRepSessionConfiguration newConfig = newConfigPair.getSecond();
            boolean isOldConfigNull = oldConfigs.get(newConfig.getKey()) == null;
            boolean isNewConfigNull = newConfig.getValue() == null;
            if (!isNewConfigNull && !newConfig.getValue().isEmpty()
                    && (isOldConfigNull || !newConfig.getValue().equals(oldConfigs.get(newConfig.getKey())))) {
                actionTypes.add(ActionType.SetGeoRepConfig);
                parameters.add(geoRepConfigModel.formGeoRepConfigParameters(newConfig));
            }
            if (newConfigPair.getFirst()) {
                actionTypes.add(ActionType.ResetDefaultGeoRepConfig);
                parameters.add(geoRepConfigModel.formGeoRepConfigParameters(newConfig));
            }
        }
        int numberOfConfigUpdates = parameters.size();
        if (numberOfConfigUpdates == 0) {
            geoRepConfigModel.stopProgress();
            closeWindow();
            return;
        }
        callbacks = new ArrayList<>(Collections.nCopies(numberOfConfigUpdates, (IFrontendActionAsyncCallback) null));
        callbacks.set(numberOfConfigUpdates - 1, result -> {
            geoRepConfigModel.stopProgress();
            closeWindow();
        });
        // Failure call back. Update the config list just to reflect any new changes and default error msg
        // dialog is thrown.
        Frontend.getInstance().runMultipleActions(actionTypes,
                parameters,
                callbacks,
                result -> fetchConfigForSession(geoRepConfigModel.getGeoRepSession()),
        this);
    }

    private void addUICommandsToConfigWindow(GlusterVolumeGeoReplicationSessionConfigModel geoRepConfigModel) {
        UICommand okCommand = UICommand.createDefaultOkUiCommand("ok", this);//$NON-NLS-1$
        geoRepConfigModel.addUpdateConfigsCommand(okCommand);

        UICommand cancelCommand = UICommand.createCancelUiCommand("closeWindow", this);//$NON-NLS-1$
        geoRepConfigModel.addCancelCommand(cancelCommand);
    }

    private void onCreateSession() {
        final GlusterVolumeGeoRepCreateModel createModel = (GlusterVolumeGeoRepCreateModel) getWindow();
        if (!createModel.validate()) {
            return;
        }
        createModel.startProgress();
        final Guid masterVolumeId = getEntity().getId();
        final String remoteVolumeName = createModel.getSlaveVolumes().getSelectedItem().getName();
        final String remoteHostName = createModel.getSlaveHosts().getSelectedItem().getFirst();
        String remoteUserName = createModel.getSlaveUserName().getEntity();
        String remoteUserGroup = createModel.getSlaveUserGroupName().getEntity();
        final Guid remoteHostId = createModel.getSlaveHosts().getSelectedItem().getSecond();
        Frontend.getInstance().runAction(ActionType.CreateGlusterVolumeGeoRepSession,
                new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                        remoteVolumeName,
                        remoteHostId ,
                        remoteUserName,
                        remoteUserGroup,
                        !createModel.getShowEligibleVolumes().getEntity()),
                result -> {
                    createModel.stopProgress();
                    if (result.getReturnValue().getSucceeded()) {
                        closeWindow();
                        if (createModel.getStartSession().getEntity()) {
                            initializeGeoRepActionConfirmation(constants.geoReplicationStartTitle(), HelpTag.volume_geo_rep_start_confirmation, "volume_geo_rep_start_confirmation", constants.geoRepForceHelp(), messages.geoRepForceTitle(constants.startGeoRep()), "onStartGeoRepSession", getEntity().getName(), remoteVolumeName, remoteHostName, null);//$NON-NLS-1$//$NON-NLS-2$
                            final GlusterVolumeGeoRepActionConfirmationModel cModel = (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
                            cModel.startProgress();
                            Frontend.getInstance().runAction(ActionType.StartGlusterVolumeGeoRep,
                                    new GlusterVolumeGeoRepSessionParameters(masterVolumeId,
                                            remoteVolumeName,
                                            remoteHostId),
                                    result1 -> {
                                        cModel.stopProgress();
                                        if (!result1.getReturnValue().getSucceeded()) {
                                            cModel.setMessage(result1.getReturnValue().getFault().getMessage());
                                        } else {
                                            closeWindow();
                                        }
                                    },
                                    VolumeGeoRepListModel.this,
                                    false);
                        }
                    }
                },
                this,
                true);
    }

    private void createGeoRepSession() {
        if (getWindow() != null || getEntity() == null) {
            return;
        }

        final UIConstants constants = ConstantsManager.getInstance().getConstants();

        GlusterVolumeEntity selectedMasterVolume = getEntity();

        final GlusterVolumeGeoRepCreateModel geoRepCreateModel =
                new GlusterVolumeGeoRepCreateModel(selectedMasterVolume);
        setWindow(geoRepCreateModel);

        geoRepCreateModel.getSlaveUserName().setEntity(constants.rootUser());
        geoRepCreateModel.getShowEligibleVolumes().setEntity(true);

        UICommand ok = new UICommand("onCreateSession", this);//$NON-NLS-1$
        ok.setTitle(constants.ok());
        ok.setIsDefault(true);
        geoRepCreateModel.getCommands().add(ok);

        UICommand close = new UICommand("closeWindow", this);//$NON-NLS-1$
        close.setTitle(constants.cancel());
        close.setIsCancel(true);
        geoRepCreateModel.getCommands().add(close);
    }

    private void closeWindow() {
        setWindow(null);
    }

    private void startGeoRepSession() {
        performGeoRepAction("onStartGeoRepSession", constants.geoReplicationStartTitle(), HelpTag.volume_geo_rep_start_confirmation, "volume_geo_rep_start_confirmation", constants.startGeoRep(), ActionType.StartGlusterVolumeGeoRep);//$NON-NLS-1$//$NON-NLS-2$
    }

    private void stopGeoRepSession() {
        performGeoRepAction("onStopGeoRepSession", constants.geoReplicationStopTitle(), HelpTag.volume_geo_rep_stop_confirmation, "volume_geo_rep_stop_confirmation", constants.stopGeoRep(), ActionType.StopGeoRepSession);//$NON-NLS-1$//$NON-NLS-2$
    }

    private void confirmGeoRepAction(String title, HelpTag helpTag, String hashName, String commandName, String action) {
        GlusterGeoRepSession selectedSession = getSelectedItem();
        if (selectedSession == null) {
            return;
        }

        initializeGeoRepActionConfirmation(title, helpTag, hashName, null, null, commandName, selectedSession.getMasterVolumeName(), selectedSession.getSlaveVolumeName(), selectedSession.getSlaveHostName(), messages.geoRepActionConfirmationMessage(action));
    }

    private void pauseGeoRepSession() {
        performGeoRepAction("onPauseGeoRepSession", constants.geoReplicationPauseTitle(), HelpTag.volume_geo_rep_pause_confirmation, "volume_geo_rep_pause_confirmation", constants.pauseGeoRep(), ActionType.PauseGlusterVolumeGeoRepSession);//$NON-NLS-1$//$NON-NLS-2$
    }

    private void resumeGeoRepSession() {
        performGeoRepAction("onResumeGeoRepSession", constants.geoReplicationResumeTitle(), HelpTag.volume_geo_rep_resume_confirmation, "volume_geo_rep_resume_confirmation", constants.resumeGeoRep(), ActionType.ResumeGeoRepSession);//$NON-NLS-1$//$NON-NLS-2$
    }

    private void removeGeoRepSession() {
        performGeoRepAction("onRemoveGeoRepSession", constants.geoReplicationRemoveTitle(), HelpTag.volume_geo_rep_remove_confirmation, "volume_geo_rep_remove_confirmation", constants.removeGeoRep(), ActionType.DeleteGeoRepSession);//$NON-NLS-1$//$NON-NLS-2$
    }

    private void performGeoRepAction(String commandName,
            String confirmTitle,
            HelpTag helpTag,
            String hashName,
            String action,
            ActionType actionType) {
        GlusterGeoRepSession selectedSession = getSelectedItem();
        if (selectedSession == null) {
            return;
        }

        initializeGeoRepActionConfirmation(confirmTitle, helpTag, hashName, constants.geoRepForceHelp(), messages.geoRepForceTitle(action), commandName, selectedSession.getMasterVolumeName(), selectedSession.getSlaveVolumeName(), selectedSession.getSlaveHostName(), null);
        onGeoRepSessionAction(actionType);
    }

    private void initializeGeoRepActionConfirmation(String title, HelpTag helpTag, String hashName, String forceHelp, String forceLabelText, String commandName, String masterVolumeName, String slaveVolumeName, String slaveHostName, String message) {
        GlusterVolumeGeoRepActionConfirmationModel cModel;
        if(getWindow() != null) {
            if(getWindow() instanceof GlusterVolumeGeoRepActionConfirmationModel) {
                cModel = (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
            } else {
                return;
            }
        } else {
            cModel = new GlusterVolumeGeoRepActionConfirmationModel();
            cModel.setTitle(title);
        }
        cModel.setHelpTag(helpTag);
        cModel.setHashName(hashName);

        setWindow(cModel);

        cModel.initWindow(masterVolumeName, slaveVolumeName, slaveHostName);

        cModel.setActionConfirmationMessage(message);
        cModel.setForceHelp(forceHelp);
        cModel.setForceLabel(forceLabelText);

        List<UICommand> geoRepActionCommands = Arrays.asList(UICommand.createDefaultOkUiCommand(commandName, this), UICommand.createCancelUiCommand("closeWindow", this));//$NON-NLS-1$

        if(cModel.getCommands().size() > 0) {
            cModel.setCommands(geoRepActionCommands);
        } else {
            cModel.getCommands().addAll(geoRepActionCommands);
        }
    }

    private void refreshSessions() {
        Frontend.getInstance().runAction(ActionType.RefreshGeoRepSessions,
                new GlusterVolumeParameters(getEntity().getId()));
    }

    @Override
    public void setEntity(GlusterVolumeEntity value) {
        super.setEntity(value);
        updateActionAvailability(value);
    }

    private void onGeoRepSessionAction(ActionType actionType) {
        final GlusterVolumeGeoRepActionConfirmationModel cModel = (GlusterVolumeGeoRepActionConfirmationModel) getWindow();
        cModel.startProgress();
        boolean force = cModel.getForce().getEntity();
        GlusterGeoRepSession selectedSession = getSelectedItem();
        GlusterVolumeGeoRepSessionParameters sessionParamters = new GlusterVolumeGeoRepSessionParameters(selectedSession.getMasterVolumeId(), selectedSession.getId());
        sessionParamters.setForce(force);
        Frontend.getInstance().runAction(actionType, sessionParamters, result -> {
            if (cModel == null) {
                return;
            } else {
                cModel.stopProgress();
                if (!result.getReturnValue().getSucceeded()) {
                    //cModel.setActionConfirmationMessage(result.getReturnValue().getFault().getMessage());
                    setErrorMessage(result.getReturnValue(), cModel);
                } else {
                    setWindow(null);
                }
            }
        },
        this,
        false);
    }

    private void setErrorMessage(ActionReturnValue result, GlusterVolumeGeoRepActionConfirmationModel cModel) {
        String errorMessage = ""; //$NON-NLS-1$
        if (result == null) {
            errorMessage = ConstantsManager.getInstance().getConstants().testFailedUnknownErrorMsg();
        } else if (!result.getSucceeded()) {
            errorMessage = result.isValid() ?
                    result.getFault().getMessage() :
                    result.getValidationMessages().get(0);
        }
        cModel.setActionConfirmationMessage(errorMessage);
    }
}
