package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterGeneralModel extends EntityModel {

    public static Integer lowLimitPowerSaving = null;
    public static Integer highLimitPowerSaving = null;
    public static Integer highLimitEvenlyDistributed = null;

    private Integer noOfVolumesTotal;
    private Integer noOfVolumesUp;
    private Integer noOfVolumesDown;

    public String getNoOfVolumesTotal() {
        return Integer.toString(noOfVolumesTotal);
    }

    public void setNoOfVolumesTotal(Integer noOfVolumesTotal) {
        this.noOfVolumesTotal = noOfVolumesTotal;
    }

    public String getNoOfVolumesUp() {
        return Integer.toString(noOfVolumesUp);
    }

    public void setNoOfVolumesUp(Integer noOfVolumesUp) {
        this.noOfVolumesUp = noOfVolumesUp;
    }

    public String getNoOfVolumesDown() {
        return Integer.toString(noOfVolumesDown);
    }

    public void setNoOfVolumesDown(Integer noOfVolumesDown) {
        this.noOfVolumesDown = noOfVolumesDown;
    }

    private UICommand privateEditPolicyCommand;

    public UICommand getEditPolicyCommand()
    {
        return privateEditPolicyCommand;
    }

    private void setEditPolicyCommand(UICommand value)
    {
        privateEditPolicyCommand = value;
    }

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    public ClusterGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$

        setNoOfVolumesTotal(0);
        setNoOfVolumesUp(0);
        setNoOfVolumesDown(0);

        setEditPolicyCommand(new UICommand("EditPolicy", this)); //$NON-NLS-1$

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterGeneralModel.highLimitEvenlyDistributed = (Integer) result;
            }
        };
        if (ClusterGeneralModel.highLimitEvenlyDistributed == null)
        {
            AsyncDataProvider.GetHighUtilizationForEvenDistribution(_asyncQuery);
        }
        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterGeneralModel.lowLimitPowerSaving = (Integer) result;
            }
        };
        if (ClusterGeneralModel.lowLimitPowerSaving == null)
        {
            AsyncDataProvider.GetLowUtilizationForPowerSave(_asyncQuery);
        }

        _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterGeneralModel.highLimitPowerSaving = (Integer) result;
            }
        };
        if (ClusterGeneralModel.highLimitPowerSaving == null)
        {
            AsyncDataProvider.GetHighUtilizationForPowerSave(_asyncQuery);
        }
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            UpdateVolumeDetails();
        }

        UpdateActionAvailability();
    }

    public void EditPolicy()
    {
        if (getWindow() != null)
        {
            return;
        }

        ClusterPolicyModel model = new ClusterPolicyModel();

        model.setTitle(ConstantsManager.getInstance().getConstants().editPolicyTitle());
        model.setHashName("edit_policy"); //$NON-NLS-1$

        model.setSelectionAlgorithm(getEntity().getselection_algorithm());
        model.getOverCommitTime().setEntity(getEntity().getcpu_over_commit_duration_minutes());
        model.setOverCommitLowLevel(getEntity().getlow_utilization());
        model.setOverCommitHighLevel(getEntity().gethigh_utilization());

        model.SaveDefaultValues();

        setWindow(model);

        UICommand tempVar = new UICommand("OnSavePolicy", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSavePolicy()
    {
        ClusterPolicyModel model = (ClusterPolicyModel) getWindow();

        if (getEntity() == null)
        {
            Cancel();
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        VDSGroup cluster = (VDSGroup) Cloner.clone(getEntity());
        cluster.setselection_algorithm(model.getSelectionAlgorithm());
        if (model.getOverCommitTime().getIsAvailable())
        {
            cluster.setcpu_over_commit_duration_minutes(Integer.parseInt(model.getOverCommitTime()
                    .getEntity()
                    .toString()));
        }
        cluster.setlow_utilization(model.getOverCommitLowLevel());
        cluster.sethigh_utilization(model.getOverCommitHighLevel());

        Frontend.RunAction(VdcActionType.UpdateVdsGroup, new VdsGroupOperationParameters(cluster));

        Cancel();
    }

    public void Cancel()
    {
        setWindow(null);
    }

    private void UpdateActionAvailability()
    {
        getEditPolicyCommand().setIsExecutionAllowed(getEntity() != null);
    }

    private void UpdateVolumeDetails()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterGeneralModel innerGeneralModel = (ClusterGeneralModel) model;
                ArrayList<GlusterVolumeEntity> volumeList = (ArrayList<GlusterVolumeEntity>) result;
                int volumesUp = 0;
                int volumesDown = 0;
                for (GlusterVolumeEntity volumeEntity : volumeList)
                {
                    if (volumeEntity.getStatus() == GlusterStatus.UP)
                    {
                        volumesUp++;
                    }
                    else
                    {
                        volumesDown++;
                    }
                }
                setNoOfVolumesTotal(volumeList.size());
                setNoOfVolumesUp(volumesUp);
                setNoOfVolumesDown(volumesDown);
            }
        };
        AsyncDataProvider.GetVolumeList(_asyncQuery, getEntity().getname());
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getEditPolicyCommand())
        {
            EditPolicy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSavePolicy")) //$NON-NLS-1$
        {
            OnSavePolicy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }
}
