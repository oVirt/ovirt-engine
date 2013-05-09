package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ClusterPolicyModel extends EntityModel {

    protected static Integer lowLimitPowerSaving = null;
    protected static Integer highLimitPowerSaving = null;
    protected static Integer highLimitEvenlyDistributed = null;

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
            onPropertyChanged(new PropertyChangedEventArgs("HasOverCommitLowLevel")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("HasOverCommitHighLevel")); //$NON-NLS-1$
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
            selectionAlgorithmChanged();
            onPropertyChanged(new PropertyChangedEventArgs("SelectionAlgorithm")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("OverCommitLowLevel")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("OverCommitHighLevel")); //$NON-NLS-1$
        }
    }

    private EntityModel privateEnableTrustedService;

    public EntityModel getEnableTrustedService() {
        return privateEnableTrustedService;
    }

    public void setEnableTrustedService(EntityModel value) {
        this.privateEnableTrustedService = value;
    }

    public void saveDefaultValues()
    {
        if (getSelectionAlgorithm() == VdsSelectionAlgorithm.EvenlyDistribute)
        {
            ClusterPolicyModel.highLimitEvenlyDistributed = getOverCommitHighLevel();
        }
        else if (getSelectionAlgorithm() == VdsSelectionAlgorithm.PowerSave)
        {
            ClusterPolicyModel.lowLimitPowerSaving = getOverCommitLowLevel();
            ClusterPolicyModel.highLimitPowerSaving = getOverCommitHighLevel();
        }
    }

    private UICommand privateEditPolicyCommand;

    public UICommand getEditPolicyCommand()
    {
        return privateEditPolicyCommand;
    }

    public void setEditPolicyCommand(UICommand value)
    {
        privateEditPolicyCommand = value;
    }

    public ClusterPolicyModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().clusterPolicyTitle());
        setHashName("policy"); //$NON-NLS-1$

        if (ClusterPolicyModel.highLimitEvenlyDistributed == null) {
            ClusterPolicyModel.highLimitEvenlyDistributed =
                    (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.HighUtilizationForEvenlyDistribute);
        }

        if (ClusterPolicyModel.lowLimitPowerSaving == null) {
            ClusterPolicyModel.lowLimitPowerSaving =
                    (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.LowUtilizationForPowerSave);
        }

        if (ClusterPolicyModel.highLimitPowerSaving == null) {
            ClusterPolicyModel.highLimitPowerSaving =
                    (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.HighUtilizationForPowerSave);
        }

        setOverCommitTime(new EntityModel());
        setEnableTrustedService(new EntityModel(false));
        // Set all properties according to default selected algorithm:
        selectionAlgorithmChanged();
    }

    public boolean validate()
    {
        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(100);
        getOverCommitTime().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

        return getOverCommitTime().getIsValid();
    }

    private void selectionAlgorithmChanged()
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
            setOverCommitHighLevel((ClusterPolicyModel.highLimitEvenlyDistributed == null ? 0
                    : ClusterPolicyModel.highLimitEvenlyDistributed));
            break;

        case PowerSave:
            getOverCommitTime().setIsAvailable(true);
            getOverCommitTime().setIsChangable(true);
            setHasOverCommitLowLevel(true);
            setHasOverCommitHighLevel(true);
            setOverCommitLowLevel((ClusterPolicyModel.lowLimitPowerSaving == null ? 0
                    : ClusterPolicyModel.lowLimitPowerSaving));

            setOverCommitHighLevel((ClusterPolicyModel.highLimitPowerSaving == null ? 0
                    : ClusterPolicyModel.highLimitPowerSaving));
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
