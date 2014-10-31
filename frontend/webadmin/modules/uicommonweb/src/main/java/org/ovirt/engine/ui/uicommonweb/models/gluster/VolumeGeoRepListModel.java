package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
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
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VolumeGeoRepListModel extends SearchableListModel{

    private UICommand newSessionCommand;
    private UICommand removeSessionCommand;
    private UICommand startSessionCommand;
    private UICommand stopSessionCommand;
    private UICommand sessionOptionsCommand;
    private UICommand viewSessionDetailsCommand;
    private UICommand refreshSessionsCommand;


    @Override
    protected String getListName() {
        return "VolumeGeoReplicationModel";//$NON-NLS-1$
    }

    public VolumeGeoRepListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().geoReplicationTitle());
        setHelpTag(HelpTag.geo_replication);
        setHashName("geo_replication");//$NON-NLS-1$
        setNewSessionCommand(new UICommand("createNewSession", this));//$NON-NLS-1$
        setRemoveSessionCommand(new UICommand("removeSession", this));//$NON-NLS-1$
        setStartSessionCommand(new UICommand("startSession", this));//$NON-NLS-1$
        setStopSessionCommand(new UICommand("stopSession", this));//$NON-NLS-1$
        setSessionOptionsCommand(new UICommand("sessionOptions", this));//$NON-NLS-1$
        setViewSessionDetailsCommand(new UICommand("viewSessionDetails", this));//$NON-NLS-1$
        setRefreshSessionsCommand(new UICommand("refreshSessions", this));//$NON-NLS-1$
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
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null)
        {
            return;
        }

        AsyncDataProvider.getGlusterVolumeGeoRepStatusForMasterVolume(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<GlusterGeoRepSession> geoRepSessions = (ArrayList<GlusterGeoRepSession>) returnValue;
                Collections.sort(geoRepSessions, new Linq.GlusterVolumeGeoRepSessionComparer());
                setItems(geoRepSessions);
            }
        }), getEntity().getId());

    }

    private void updateActionAvailability(GlusterVolumeEntity volumeEntity) {
        if(volumeEntity == null) {
            return;
        }
        getNewSessionCommand().setIsAvailable(true);
        getRemoveSessionCommand().setIsAvailable(false);
        getStartSessionCommand().setIsAvailable(false);
        getStopSessionCommand().setIsAvailable(false);
        getSessionOptionsCommand().setIsAvailable(false);
        getViewSessionDetailsCommand().setIsAvailable(false);
        getRefreshSessionsCommand().setIsAvailable(true);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if(command.equals(getNewSessionCommand())) {
            createNewGeoRepSession();
        } else if(command.equals(getRemoveSessionCommand())) {

        } else if(command.equals(getStartSessionCommand())) {

        } else if(command.equals(getStopSessionCommand())) {

        } else if(command.equals(getSessionOptionsCommand())) {

        } else if(command.equals(getViewSessionDetailsCommand())) {

        } else if (command.equals(getRefreshSessionsCommand())) {
            refreshSessions();
        }
    }

    private void createNewGeoRepSession() {

    }

    private void refreshSessions() {
        Frontend.getInstance().runAction(VdcActionType.RefreshGeoRepSessions,
                new GlusterVolumeParameters(getEntity().getId()));
    }

    @Override
    public GlusterVolumeEntity getEntity()
    {
        return (GlusterVolumeEntity) super.getEntity();
    }

    public void setEntity(GlusterVolumeEntity value)
    {
        super.setEntity(value);
    }

}
