package org.ovirt.engine.ui.uicommon.models.clusters;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ClusterPolicyModel extends EntityModel
{
	private Integer lowLimitPowerSaving;
	private Integer highLimitPowerSaving;
	private Integer highLimitEvenlyDistributed;


	private UICommand privateEditCommand;
	public UICommand getEditCommand()
	{
		return privateEditCommand;
	}
	private void setEditCommand(UICommand value)
	{
		privateEditCommand = value;
	}



	public VDSGroup getEntity()
	{
		return (VDSGroup)((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
	}
	public void setEntity(VDSGroup value)
	{
		super.setEntity(value);
	}

	private Model window;
	public Model getWindow()
	{
		return window;
	}
	public void setWindow(Model value)
	{
		if (window != value)
		{
			window = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Window"));
		}
	}

	private EntityModel privateOverCommitTime;
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
			OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitLowLevel"));
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
			OnPropertyChanged(new PropertyChangedEventArgs("HasOverCommitHighLevel"));
		}
	}
	//Editing features
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
			OnPropertyChanged(new PropertyChangedEventArgs("SelectionAlgorithm"));
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
			OnPropertyChanged(new PropertyChangedEventArgs("OverCommitLowLevel"));
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
			OnPropertyChanged(new PropertyChangedEventArgs("OverCommitHighLevel"));
		}
	}

	public void SaveDefaultValues()
	{
		if (getSelectionAlgorithm() == VdsSelectionAlgorithm.EvenlyDistribute)
		{
			highLimitEvenlyDistributed = getOverCommitHighLevel();
		}
		else if (getSelectionAlgorithm() == VdsSelectionAlgorithm.PowerSave)
		{
			lowLimitPowerSaving = getOverCommitLowLevel();
			highLimitPowerSaving = getOverCommitHighLevel();
		}
	}



	public ClusterPolicyModel()
	{
		setTitle("General");

		setEditCommand(new UICommand("Edit", this));
		setOverCommitTime(new EntityModel());

		// Set all properties according to default selected algorithm:
		SelectionAlgorithmChanged();
	}


	public void Edit()
	{
		if (getWindow() != null)
		{
			return;
		}

		ClusterPolicyModel model = new ClusterPolicyModel();
		setWindow(model);
		model.setTitle("Edit Policy");
		model.setHashName("edit_policy");

		model.setSelectionAlgorithm(getEntity().getselection_algorithm());
		model.getOverCommitTime().setEntity(getEntity().getcpu_over_commit_duration_minutes());
		model.setOverCommitLowLevel(getEntity().getlow_utilization());
		model.setOverCommitHighLevel(getEntity().gethigh_utilization());

		model.SaveDefaultValues();

		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnSave()
	{
		ClusterPolicyModel model = (ClusterPolicyModel)getWindow();

		if (getEntity() == null)
		{
			Cancel();
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		VDSGroup cluster = (VDSGroup)Cloner.clone(getEntity());
		cluster.setselection_algorithm(model.getSelectionAlgorithm());
		if (model.getOverCommitTime().getIsAvailable())
		{
			cluster.setcpu_over_commit_duration_minutes(Integer.parseInt(model.getOverCommitTime().getEntity().toString()));
		}
		cluster.setlow_utilization(model.getOverCommitLowLevel());
		cluster.sethigh_utilization(model.getOverCommitHighLevel());

		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.UpdateVdsGroup, new VdsGroupOperationParameters(cluster));

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
		}
	}

	public boolean Validate()
	{
		IntegerValidation tempVar = new IntegerValidation();
		tempVar.setMinimum(1);
		tempVar.setMaximum(100);
		getOverCommitTime().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

		return getOverCommitTime().getIsValid();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			UpdateProperties();
		}

		UpdateActionAvailability();
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("selection_algorithm"))
		{
			UpdateProperties();
		}
	}

	private void UpdateActionAvailability()
	{
		getEditCommand().setIsExecutionAllowed(getEntity() != null);
	}

	private void UpdateProperties()
	{
		getOverCommitTime().setIsAvailable(getEntity().getselection_algorithm() != VdsSelectionAlgorithm.None);
		getOverCommitTime().setIsChangable(getOverCommitTime().getIsAvailable());
		setHasOverCommitLowLevel(getEntity().getselection_algorithm() == VdsSelectionAlgorithm.PowerSave);
		setHasOverCommitHighLevel(getEntity().getselection_algorithm() != VdsSelectionAlgorithm.None);
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
				if (highLimitEvenlyDistributed == null)
				{
					highLimitEvenlyDistributed = DataProvider.GetHighUtilizationForEvenDistribution();
				}
				setOverCommitHighLevel((highLimitEvenlyDistributed == null ? 0 : highLimitEvenlyDistributed));
				break;

			case PowerSave:
				getOverCommitTime().setIsAvailable(true);
				getOverCommitTime().setIsChangable(true);
				setHasOverCommitLowLevel(true);
				setHasOverCommitHighLevel(true);
				if (lowLimitPowerSaving == null)
				{
					lowLimitPowerSaving = DataProvider.GetLowUtilizationForPowerSave();
				}
				setOverCommitLowLevel((lowLimitPowerSaving == null ? 0 : lowLimitPowerSaving));

				if (highLimitPowerSaving == null)
				{
					highLimitPowerSaving = DataProvider.GetHighUtilizationForPowerSave();
				}
				setOverCommitHighLevel((highLimitPowerSaving == null ? 0 : highLimitPowerSaving));
				break;
		}
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getEditCommand())
		{
			Edit();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}