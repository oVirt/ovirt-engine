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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class ClusterModel extends Model
{

	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}
	private String privateOriginalName;
	public String getOriginalName()
	{
		return privateOriginalName;
	}
	public void setOriginalName(String value)
	{
		privateOriginalName = value;
	}
	private NGuid privateClusterId;
	public NGuid getClusterId()
	{
		return privateClusterId;
	}
	public void setClusterId(NGuid value)
	{
		privateClusterId = value;
	}
	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	public void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateDescription;
	public EntityModel getDescription()
	{
		return privateDescription;
	}
	public void setDescription(EntityModel value)
	{
		privateDescription = value;
	}
	private ListModel privateDataCenter;
	public ListModel getDataCenter()
	{
		return privateDataCenter;
	}
	public void setDataCenter(ListModel value)
	{
		privateDataCenter = value;
	}
	private ListModel privateCPU;
	public ListModel getCPU()
	{
		return privateCPU;
	}
	public void setCPU(ListModel value)
	{
		privateCPU = value;
	}
	private ListModel privateVersion;
	public ListModel getVersion()
	{
		return privateVersion;
	}
	public void setVersion(ListModel value)
	{
		privateVersion = value;
	}

	private EntityModel privateOptimizationNone;
	public EntityModel getOptimizationNone()
	{
		return privateOptimizationNone;
	}
	public void setOptimizationNone(EntityModel value)
	{
		privateOptimizationNone = value;
	}
	private EntityModel privateOptimizationForServer;
	public EntityModel getOptimizationForServer()
	{
		return privateOptimizationForServer;
	}
	public void setOptimizationForServer(EntityModel value)
	{
		privateOptimizationForServer = value;
	}
	private EntityModel privateOptimizationForDesktop;
	public EntityModel getOptimizationForDesktop()
	{
		return privateOptimizationForDesktop;
	}
	public void setOptimizationForDesktop(EntityModel value)
	{
		privateOptimizationForDesktop = value;
	}
	private EntityModel privateOptimizationCustom;
	public EntityModel getOptimizationCustom()
	{
		return privateOptimizationCustom;
	}
	public void setOptimizationCustom(EntityModel value)
	{
		privateOptimizationCustom = value;
	}

	private boolean isGeneralTabValid;
	public boolean getIsGeneralTabValid()
	{
		return isGeneralTabValid;
	}
	public void setIsGeneralTabValid(boolean value)
	{
		if (isGeneralTabValid != value)
		{
			isGeneralTabValid = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
		}
	}

	private MigrateOnErrorOptions migrateOnErrorOption = MigrateOnErrorOptions.values()[0];
	public MigrateOnErrorOptions getMigrateOnErrorOption()
	{
		return migrateOnErrorOption;
	}

	public void setMigrateOnErrorOption(MigrateOnErrorOptions value)
	{
		if (migrateOnErrorOption != value)
		{
			migrateOnErrorOption = value;
			OnPropertyChanged(new PropertyChangedEventArgs("MigrateOnErrorOption"));
		}
	}

	private boolean privateisResiliencePolicyTabAvailable;
	public boolean getisResiliencePolicyTabAvailable()
	{
		return privateisResiliencePolicyTabAvailable;
	}
	public void setisResiliencePolicyTabAvailable(boolean value)
	{
		privateisResiliencePolicyTabAvailable = value;
	}
	public boolean getIsResiliencePolicyTabAvailable()
	{
		return getisResiliencePolicyTabAvailable();
	}

	public void setIsResiliencePolicyTabAvailable(boolean value)
	{
		if (getisResiliencePolicyTabAvailable() != value)
		{
			setisResiliencePolicyTabAvailable(value);
			OnPropertyChanged(new PropertyChangedEventArgs("IsResiliencePolicyTabAvailable"));
		}
	}

	public int getMemoryOverCommit()
	{
		if (getOptimizationNone().getIsSelected())
		{
			return (Integer)getOptimizationNone().getEntity();
		}

		if (getOptimizationForServer().getIsSelected())
		{
			return (Integer)getOptimizationForServer().getEntity();
		}

		if (getOptimizationForDesktop().getIsSelected())
		{
			return (Integer)getOptimizationForDesktop().getEntity();
		}

		if (getOptimizationCustom().getIsSelected())
		{
			return (Integer)getOptimizationCustom().getEntity();
		}

		return DataProvider.GetClusterDefaultMemoryOverCommit();
	}
	public void setMemoryOverCommit(int value)
	{
		getOptimizationNone().setIsSelected(value == (Integer)getOptimizationNone().getEntity());
		getOptimizationForServer().setIsSelected(value == (Integer)getOptimizationForServer().getEntity());
		getOptimizationForDesktop().setIsSelected(value == (Integer)getOptimizationForDesktop().getEntity());

		if (!getOptimizationNone().getIsSelected() && !getOptimizationForServer().getIsSelected() && !getOptimizationForDesktop().getIsSelected())
		{
			getOptimizationCustom().setIsAvailable(true);
			getOptimizationCustom().setEntity(value);
			getOptimizationCustom().setIsSelected(true);
		}
	}


	public ClusterModel()
	{
		setName(new EntityModel());
		setDescription(new EntityModel());


		//Optimization methods.
		int defaultOverCommit = DataProvider.GetClusterDefaultMemoryOverCommit();
		int serverOverCommit = DataProvider.GetClusterServerMemoryOverCommit();
		int desktopOverCommit = DataProvider.GetClusterDesktopMemoryOverCommit();

		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(defaultOverCommit);
		tempVar.setIsSelected(desktopOverCommit != defaultOverCommit && serverOverCommit != defaultOverCommit);
		setOptimizationNone(tempVar);

		EntityModel tempVar2 = new EntityModel();
		tempVar2.setEntity(serverOverCommit);
		tempVar2.setIsSelected(serverOverCommit == defaultOverCommit);
		setOptimizationForServer(tempVar2);

		EntityModel tempVar3 = new EntityModel();
		tempVar3.setEntity(desktopOverCommit);
		tempVar3.setIsSelected(desktopOverCommit == defaultOverCommit);
		setOptimizationForDesktop(tempVar3);

		EntityModel tempVar4 = new EntityModel();
		tempVar4.setIsAvailable(false);
		setOptimizationCustom(tempVar4);


		setDataCenter(new ListModel());
		getDataCenter().getSelectedItemChangedEvent().addListener(this);
		setCPU(new ListModel());
		setVersion(new ListModel());
		getVersion().getSelectedItemChangedEvent().addListener(this);
		setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

		setIsGeneralTabValid(true);
		setIsResiliencePolicyTabAvailable(true);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition))
		{
			if (sender == getDataCenter())
			{
				StoragePool_SelectedItemChanged(args);
			}
			else if (sender == getVersion())
			{
				Version_SelectedItemChanged(args);
			}
		}
	}

	private void Version_SelectedItemChanged(EventArgs e)
	{
		Version version;
		if (getVersion().getSelectedItem() != null)
		{
			version = (Version)getVersion().getSelectedItem();
		}
		else
		{
			version = ((storage_pool)getDataCenter().getSelectedItem()).getcompatibility_version();
		}

		java.util.ArrayList<ServerCpu> cpus = DataProvider.GetCPUList(version);
		ServerCpu oldSelectedCpu = (ServerCpu)getCPU().getSelectedItem();
		getCPU().setItems(cpus);

		if (oldSelectedCpu != null)
		{
			getCPU().setSelectedItem(Linq.FirstOrDefault(cpus, new Linq.ServerCpuPredicate(oldSelectedCpu.getCpuName())));
		}

		if (getCPU().getSelectedItem() == null)
		{
			getCPU().setSelectedItem(Linq.FirstOrDefault(cpus));
		}
	}

	private void StoragePool_SelectedItemChanged(EventArgs e)
	{
		// possible versions for new cluster (when editing cluster, this event won't occur)
		// are actually the possible versions for the data-center that the cluster is going
		// to be attached to.
		storage_pool selectedDataCenter = (storage_pool)getDataCenter().getSelectedItem();
		if (selectedDataCenter.getstorage_pool_type() == StorageType.LOCALFS)
		{
			setIsResiliencePolicyTabAvailable(false);
		}
		else
		{
			setIsResiliencePolicyTabAvailable(true);
		}
		java.util.ArrayList<Version> versions = DataProvider.GetDataCenterClusterVersions(selectedDataCenter == null ? null : (NGuid)(selectedDataCenter.getId()));
		getVersion().setItems(versions);
		if (!versions.contains((Version)getVersion().getSelectedItem()))
		{
			if (versions.contains(selectedDataCenter.getcompatibility_version()))
			{
				getVersion().setSelectedItem((Version)selectedDataCenter.getcompatibility_version());
			}
			else
			{
				getVersion().setSelectedItem(Linq.SelectHighestVersion(versions));
			}

		}
	}

	public boolean Validate()
	{
		return Validate(true);
	}

	public boolean Validate(boolean validateStoragePool)
	{
		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression("^[A-Za-z0-9_-]+$");
		tempVar.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new NoSpacesValidation(), tempVar });
		if (validateStoragePool)
		{
			getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}
		getCPU().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		getVersion().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });


		String name = (String)getName().getEntity();

		//Check name unicitate.
		if (name.compareToIgnoreCase(getOriginalName()) != 0 && !DataProvider.IsClusterNameUnique(name))
		{
			getName().setIsValid(false);
			getName().getInvalidityReasons().add("Name must be unique.");
		}

		setIsGeneralTabValid(getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid() && getVersion().getIsValid());

		return getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid() && getVersion().getIsValid();
	}
}