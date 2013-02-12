package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ClusterPolicyModel extends EntityModel
{


    private EntityModel privateOverCommitTime;
    private boolean editClusterPolicyFirst;

    public EntityModel getOverCommitTime()
    {
        return privateOverCommitTime;
    }

    public void setOverCommitTime(EntityModel value)
    {
        privateOverCommitTime = value;
    }

    private boolean hasOverCommitLowLevel;

    public boolean getHasOverCommitLowLevel()
    {
        return hasOverCommitLowLevel;
    }

    public void setHasOverCommitLowLevel(boolean value)
    {
        if (hasOverCommitLowLevel != value)
        {
            hasOverCommitLowLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitLowLevel")); //$NON-NLS-1$
        }
    }

    public boolean hasOverCommitHighLevel;

    public boolean getHasOverCommitHighLevel()
    {
        return hasOverCommitHighLevel;
    }

    public void setHasOverCommitHighLevel(boolean value)
    {
        if (hasOverCommitHighLevel != value)
        {
            hasOverCommitHighLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitHighLevel")); //$NON-NLS-1$
        }
    }

    // Editing features
    private VdsSelectionAlgorithm selectionAlgorithm = VdsSelectionAlgorithm.values()[0];

    public VdsSelectionAlgorithm getSelectionAlgorithm()
    {
        return selectionAlgorithm;
    }

    public void setSelectionAlgorithm(VdsSelectionAlgorithm value)
    {
        if (selectionAlgorithm != value)
        {
            selectionAlgorithm = value;
            SelectionAlgorithmChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("SelectionAlgorithm")); //$NON-NLS-1$
        }
    }

    private int overCommitLowLevel;

    public int getOverCommitLowLevel()
    {
        return overCommitLowLevel;
    }

    public void setOverCommitLowLevel(int value)
    {
        if (overCommitLowLevel != value)
        {
            overCommitLowLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OverCommitLowLevel")); //$NON-NLS-1$
        }
    }

    private int overCommitHighLevel;

    public int getOverCommitHighLevel()
    {
        return overCommitHighLevel;
    }

    public void setOverCommitHighLevel(int value)
    {
        if (overCommitHighLevel != value)
        {
            overCommitHighLevel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OverCommitHighLevel")); //$NON-NLS-1$
        }
    }

    public void SaveDefaultValues()
    {
        if (getSelectionAlgorithm() == VdsSelectionAlgorithm.EvenlyDistribute)
        {
            ClusterGeneralModel.highLimitEvenlyDistributed = getOverCommitHighLevel();
        }
        else if (getSelectionAlgorithm() == VdsSelectionAlgorithm.PowerSave)
        {
            ClusterGeneralModel.lowLimitPowerSaving = getOverCommitLowLevel();
            ClusterGeneralModel.highLimitPowerSaving = getOverCommitHighLevel();
        }
    }

    public ClusterPolicyModel()
    {
        setOverCommitTime(new EntityModel());

        // Set all properties according to default selected algorithm:
        SelectionAlgorithmChanged();
    }

    public boolean Validate()
    {
        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(100);
        getOverCommitTime().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        return getOverCommitTime().getIsValid();
    }

    private void SelectionAlgorithmChanged()
    {
        setHasOverCommitLowLevel(getSelectionAlgorithm() != VdsSelectionAlgorithm.EvenlyDistribute);

        switch (getSelectionAlgorithm())
        {
        case None:
            getOverCommitTime().setIsAvailable(false);
            setHasOverCommitLowLevel(false);
            setHasOverCommitHighLevel(false);
            setOverCommitLowLevel(0);
            setOverCommitHighLevel(0);
            break;

        case EvenlyDistribute:
            getOverCommitTime().setIsAvailable(true);
            getOverCommitTime().setIsChangable(true);
            setHasOverCommitLowLevel(false);
            setHasOverCommitHighLevel(true);
            setOverCommitLowLevel(0);
            setOverCommitHighLevel((ClusterGeneralModel.highLimitEvenlyDistributed == null ? 0
                    : ClusterGeneralModel.highLimitEvenlyDistributed));
            break;

        case PowerSave:
            getOverCommitTime().setIsAvailable(true);
            getOverCommitTime().setIsChangable(true);
            setHasOverCommitLowLevel(true);
            setHasOverCommitHighLevel(true);
            setOverCommitLowLevel((ClusterGeneralModel.lowLimitPowerSaving == null ? 0
                    : ClusterGeneralModel.lowLimitPowerSaving));

            setOverCommitHighLevel((ClusterGeneralModel.highLimitPowerSaving == null ? 0
                    : ClusterGeneralModel.highLimitPowerSaving));
            break;
        }
    }

    public boolean isEditClusterPolicyFirst() {
        return editClusterPolicyFirst;
    }

    public void setEditClusterPolicyFirst(boolean editClusterPolicyFirst) {
        this.editClusterPolicyFirst = editClusterPolicyFirst;
    }
}
