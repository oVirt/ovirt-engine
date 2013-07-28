package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ClusterHostListModel extends HostListModel
{

    public ClusterHostListModel() {
        setUpdateMomPolicyCommand(new UICommand("updateMomPolicyCommand", this)); //$NON-NLS-1$
    }

    private UICommand updateMomPolicyCommand;

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            setSearchString("hosts: cluster=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VDS);
        tempVar.setRefresh(getIsQueryFirstTime());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().execute();
        }
    }

    public UICommand getUpdateMomPolicyCommand() {
        return updateMomPolicyCommand;
    }

    public void setUpdateMomPolicyCommand(UICommand updateMomPolicyCommand) {
        this.updateMomPolicyCommand = updateMomPolicyCommand;
    }

    private void updateActionAvailability() {
        if (getEntity().getcompatibility_version().compareTo(Version.v3_3) >= 0) {
            getUpdateMomPolicyCommand().setIsAvailable(true);
            ArrayList<VDS> items =
                    getSelectedItems() != null ? Linq.<VDS> cast(getSelectedItems()) : new ArrayList<VDS>();
            boolean allHostRunning = !items.isEmpty();

            for (VDS vds : items) {
                if (vds.getStatus() != VDSStatus.Up) {
                    allHostRunning = false;
                    break;
                }
            }
            getUpdateMomPolicyCommand().setIsExecutionAllowed(allHostRunning);
        } else {
            getUpdateMomPolicyCommand().setIsAvailable(false);
        }
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

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command.equals(getUpdateMomPolicyCommand())) {
            updateMomPolicy();
        }
    }

    private void updateMomPolicy() {
        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            VDS vds = (VDS) item;
            list.add(new VdsActionParameters(vds.getId()));
        }

        Frontend.RunMultipleAction(VdcActionType.UpdateMomPolicy, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                }, null);
    }
}
