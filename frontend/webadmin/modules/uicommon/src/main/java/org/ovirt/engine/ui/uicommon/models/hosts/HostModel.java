package org.ovirt.engine.ui.uicommon.models.hosts;
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
public class HostModel extends Model implements ITaskTarget
{

	public static final int HostNameMaxLength = 255;
	public static final String PmSecureKey = "secure";
	public static final String PmPortKey = "port";
	public static final String PmSlotKey = "slot";
	public static final String BeginTestStage = "BeginTest";
	public static final String EndTestStage = "EndTest";



	private UICommand privateTestCommand;
	public UICommand getTestCommand()
	{
		return privateTestCommand;
	}
	private void setTestCommand(UICommand value)
	{
		privateTestCommand = value;
	}



	public boolean getIsNew()
	{
		return getHostId() == null;
	}

	private NGuid privateHostId;
	public NGuid getHostId()
	{
		return privateHostId;
	}
	public void setHostId(NGuid value)
	{
		privateHostId = value;
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

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	private void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateHost;
	public EntityModel getHost()
	{
		return privateHost;
	}
	private void setHost(EntityModel value)
	{
		privateHost = value;
	}
	private EntityModel privateManagementIp;
	public EntityModel getManagementIp()
	{
		return privateManagementIp;
	}
	private void setManagementIp(EntityModel value)
	{
		privateManagementIp = value;
	}
	private ListModel privateDataCenter;
	public ListModel getDataCenter()
	{
		return privateDataCenter;
	}
	private void setDataCenter(ListModel value)
	{
		privateDataCenter = value;
	}
	private ListModel privateCluster;
	public ListModel getCluster()
	{
		return privateCluster;
	}
	private void setCluster(ListModel value)
	{
		privateCluster = value;
	}
	private EntityModel privatePort;
	public EntityModel getPort()
	{
		return privatePort;
	}
	private void setPort(EntityModel value)
	{
		privatePort = value;
	}
	private EntityModel privateRootPassword;
	public EntityModel getRootPassword()
	{
		return privateRootPassword;
	}
	private void setRootPassword(EntityModel value)
	{
		privateRootPassword = value;
	}
	private EntityModel privateOverrideIpTables;
	public EntityModel getOverrideIpTables()
	{
		return privateOverrideIpTables;
	}
	private void setOverrideIpTables(EntityModel value)
	{
		privateOverrideIpTables = value;
	}
	private EntityModel privateIsPm;
	public EntityModel getIsPm()
	{
		return privateIsPm;
	}
	private void setIsPm(EntityModel value)
	{
		privateIsPm = value;
	}
	private EntityModel privatePmUserName;
	public EntityModel getPmUserName()
	{
		return privatePmUserName;
	}
	private void setPmUserName(EntityModel value)
	{
		privatePmUserName = value;
	}
	private EntityModel privatePmPassword;
	public EntityModel getPmPassword()
	{
		return privatePmPassword;
	}
	private void setPmPassword(EntityModel value)
	{
		privatePmPassword = value;
	}
	private ListModel privatePmType;
	public ListModel getPmType()
	{
		return privatePmType;
	}
	private void setPmType(ListModel value)
	{
		privatePmType = value;
	}
	private EntityModel privatePmSecure;
	public EntityModel getPmSecure()
	{
		return privatePmSecure;
	}
	private void setPmSecure(EntityModel value)
	{
		privatePmSecure = value;
	}
	private EntityModel privatePmPort;
	public EntityModel getPmPort()
	{
		return privatePmPort;
	}
	private void setPmPort(EntityModel value)
	{
		privatePmPort = value;
	}
	private EntityModel privatePmSlot;
	public EntityModel getPmSlot()
	{
		return privatePmSlot;
	}
	private void setPmSlot(EntityModel value)
	{
		privatePmSlot = value;
	}
	private EntityModel privatePmOptions;
	public EntityModel getPmOptions()
	{
		return privatePmOptions;
	}
	private void setPmOptions(EntityModel value)
	{
		privatePmOptions = value;
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

	private boolean isPowerManagementTabValid;
	public boolean getIsPowerManagementTabValid()
	{
		return isPowerManagementTabValid;
	}
	public void setIsPowerManagementTabValid(boolean value)
	{
		if (isPowerManagementTabValid != value)
		{
			isPowerManagementTabValid = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementTabValid"));
		}
	}

	private boolean isPowerManagementSelected;
	public boolean getIsPowerManagementSelected()
	{
		return isPowerManagementSelected;
	}
	public void setIsPowerManagementSelected(boolean value)
	{
		if (isPowerManagementSelected != value)
		{
			isPowerManagementSelected = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsPowerManagementSelected"));
		}
	}

	public java.util.HashMap<String, String> getPmOptionsMap()
	{
		java.util.HashMap<String, String> dict = new java.util.HashMap<String, String>();

			//Add well known pm options.
		if (getPmPort().getIsAvailable())
		{
			dict.put(PmPortKey, getPmPort().getEntity() == null ? "" : (String)getPmPort().getEntity());
		}
		if (getPmSlot().getIsAvailable())
		{
			dict.put(PmSlotKey, getPmSlot().getEntity() == null ? "" : (String)getPmSlot().getEntity());
		}
		if (getPmSecure().getIsAvailable())
		{
			dict.put(PmSecureKey, getPmSecure().getEntity().toString());
		}

			//Add unknown pm options.
			//Assume Validate method was called before this getter.
		String pmOptions = (String)getPmOptions().getEntity();
		if (!StringHelper.isNullOrEmpty(pmOptions))
		{
			for (String pair : pmOptions.split("[,]", -1))
			{
				String[] array = pair.split("[=]", -1);
				if (array.length == 2)
				{
					dict.put(array[0], array[1]);
				}
				else if (array.length == 1)
				{
					dict.put(array[0], "");
				}
			}
		}

		return dict;
	}
	public void setPmOptionsMap(java.util.HashMap<String, String> value)
	{
		String pmOptions = "";

		for (java.util.Map.Entry<String, String> pair : value.entrySet())
		{
			String k = pair.getKey();
			String v = pair.getValue();

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//			switch (k)
					//Handle well known pm options.
//ORIGINAL LINE: case PmPortKey:
			if (StringHelper.stringsEqual(k, PmPortKey))
			{
					getPmPort().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k));

			}
//ORIGINAL LINE: case PmSlotKey:
			else if (StringHelper.stringsEqual(k, PmSlotKey))
			{
					getPmSlot().setEntity(StringHelper.isNullOrEmpty(value.get(k)) ? "" : value.get(k));

			}
//ORIGINAL LINE: case PmSecureKey:
			else if (StringHelper.stringsEqual(k, PmSecureKey))
			{
					getPmSecure().setEntity(Boolean.parseBoolean(value.get(k)));

			}
			else
			{
						//Compose custom string from unknown pm options.
					if (StringHelper.isNullOrEmpty(v))
					{
						pmOptions += StringFormat.format("%1$s,", k);
					}
					else
					{
						pmOptions += StringFormat.format("%1$s=%2$s,", k, v);
					}
			}
		}

		if (!StringHelper.isNullOrEmpty(pmOptions))
		{
			getPmOptions().setEntity(pmOptions.substring(0, pmOptions.length() - 1));
		}
	}


	public HostModel()
	{
		setTestCommand(new UICommand("Test", this));

		setName(new EntityModel());
		setHost(new EntityModel());
		setManagementIp(new EntityModel());
		setDataCenter(new ListModel());
		getDataCenter().getSelectedItemChangedEvent().addListener(this);
		setCluster(new ListModel());
		getCluster().getSelectedItemChangedEvent().addListener(this);
		setPort(new EntityModel());
		setRootPassword(new EntityModel());
		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setOverrideIpTables(tempVar);
		setPmUserName(new EntityModel());
		setPmPassword(new EntityModel());
		setPmType(new ListModel());
		getPmType().getSelectedItemChangedEvent().addListener(this);
		setPmSecure(new EntityModel());
		getPmSecure().setIsAvailable(false);
		getPmSecure().setEntity(false);
		setPmPort(new EntityModel());
		getPmPort().setIsAvailable(false);
		setPmSlot(new EntityModel());
		getPmSlot().setIsAvailable(false);
		setPmOptions(new EntityModel());

		setIsPm(new EntityModel());
		getIsPm().getEntityChangedEvent().addListener(this);
		getIsPm().setEntity(false);

		setIsPowerManagementTabValid(true);
		setIsGeneralTabValid(getIsPowerManagementTabValid());
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter())
		{
			DataCenter_SelectedItemChanged();
		}
		else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getCluster())
		{
			Cluster_SelectedItemChanged();
		}
		else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getPmType())
		{
			PmType_SelectedItemChanged();
		}
		else if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getIsPm())
		{
			IsPm_EntityChanged();
		}
	}

	private void IsPm_EntityChanged()
	{
		UpdatePmModels();
	}

	private void DataCenter_SelectedItemChanged()
	{
		storage_pool dataCenter = (storage_pool)getDataCenter().getSelectedItem();
		if (dataCenter != null)
		{
			VDSGroup oldCluster = (VDSGroup)getCluster().getSelectedItem();

			java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList(dataCenter.getId());
			getCluster().setItems(clusters);

			if (oldCluster != null)
			{
				getCluster().setSelectedItem(Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(oldCluster.getId())));
			}

			if (getCluster().getSelectedItem() == null)
			{
				getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));
			}
		}
	}

	private void Cluster_SelectedItemChanged()
	{
		VDSGroup cluster = (VDSGroup)getCluster().getSelectedItem();
		if (cluster != null)
		{
			String pmType = (String)getPmType().getSelectedItem();
			getPmType().setItems(DataProvider.GetPmTypeList(cluster.getcompatibility_version()));
			if (((java.util.ArrayList<String>)getPmType().getItems()).contains(pmType))
			{
				getPmType().setSelectedItem(pmType);
			}
			else
			{
				getPmType().setSelectedItem(null);
			}
		}
	}

	private void PmType_SelectedItemChanged()
	{
		UpdatePmModels();
	}

	private void UpdatePmModels()
	{
		String pmType = (String)getPmType().getSelectedItem();
		java.util.ArrayList<String> pmOptions = !StringHelper.isNullOrEmpty(pmType) ? DataProvider.GetPmOptions(pmType) : new java.util.ArrayList<String>();

		getPmPort().setIsAvailable(pmOptions.contains(PmPortKey));
		getPmSlot().setIsAvailable(pmOptions.contains(PmSlotKey));
		getPmSecure().setIsAvailable(pmOptions.contains(PmSecureKey));


		boolean isPm = (Boolean)getIsPm().getEntity();

		getTestCommand().setIsExecutionAllowed(isPm);

		getManagementIp().setIsChangable((Boolean)getIsPm().getEntity());
		getManagementIp().setIsValid(true);
		getPmUserName().setIsChangable((Boolean)getIsPm().getEntity());
		getPmUserName().setIsValid(true);
		getPmPassword().setIsChangable((Boolean)getIsPm().getEntity());
		getPmPassword().setIsValid(true);
		getPmType().setIsChangable((Boolean)getIsPm().getEntity());
		getPmType().setIsValid(true);
		getPmOptions().setIsChangable((Boolean)getIsPm().getEntity());
		getPmSecure().setIsChangable((Boolean)getIsPm().getEntity());
		getPmPort().setIsChangable((Boolean)getIsPm().getEntity());
		getPmPort().setIsValid(true);
		getPmSlot().setIsChangable((Boolean)getIsPm().getEntity());
	}

	public void Test()
	{
		//Validate user input.
		if ((Boolean)getIsPm().getEntity())
		{
			getCluster().setIsValid(true);
			getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
			ValidatePmModels();
		}

		if (!getManagementIp().getIsValid() || !getPmUserName().getIsValid() || !getPmPassword().getIsValid() || !getPmType().getIsValid() || !getPmPort().getIsValid() || !getPmOptions().getIsValid())
		{
			return;
		}


		setMessage("Testing in progress. It will take a few seconds. Please wait...");
		getTestCommand().setIsExecutionAllowed(false);

		VDSGroup cluster = (VDSGroup)getCluster().getSelectedItem();

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { BeginTestStage, getManagementIp().getEntity(), getPmType().getSelectedItem(), getPmUserName().getEntity(), getPmPassword().getEntity(), cluster.getstorage_pool_id() != null ? cluster.getstorage_pool_id().getValue() : Guid.Empty, getPmOptionsMap() }))).Run();
	}

	private void ValidatePmModels()
	{
		getManagementIp().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new HostAddressValidation() });
		getPmUserName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		getPmPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		getPmType().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		IntegerValidation tempVar = new IntegerValidation();
		tempVar.setMinimum(1);
		tempVar.setMaximum(65535);
		getPmPort().ValidateEntity(new IValidation[] { tempVar });
		getPmOptions().ValidateEntity(new IValidation[] { new KeyValuePairValidation(true) });
	}

	public boolean Validate()
	{
		String hostNameRegex = StringFormat.format("^[0-9a-zA-Z-_\\.]{1,%1$s}$", HostNameMaxLength);
		String hostNameMessage = StringFormat.format("This field can't contain blanks or special characters, must " + "be at least one character long, legal values are 0-9, a-z, '_', '.' " + "and a length of up to %1$s characters.", HostNameMaxLength);

		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression(hostNameRegex);
		tempVar.setMessage(hostNameMessage);
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });

		getHost().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new HostAddressValidation() });

		IntegerValidation tempVar2 = new IntegerValidation();
		tempVar2.setMinimum(1);
		tempVar2.setMaximum(65535);
		getPort().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar2 });


		getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		getCluster().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });


		String name = (String)getName().getEntity();

		//Check name unicitate.
		if (name.compareToIgnoreCase(getOriginalName()) != 0 && !DataProvider.IsHostNameUnique(name))
		{
			getName().setIsValid(false);
			getName().getInvalidityReasons().add("Name must be unique.");
		}

		if ((Boolean)getIsPm().getEntity())
		{
			ValidatePmModels();
		}

		setIsGeneralTabValid(getName().getIsValid() && getHost().getIsValid() && getPort().getIsValid() && getCluster().getIsValid());

		setIsPowerManagementTabValid(getManagementIp().getIsValid() && getPmUserName().getIsValid() && getPmPassword().getIsValid() && getPmType().getIsValid() && getPmPort().getIsValid() && getPmOptions().getIsValid());

		return getName().getIsValid() && getHost().getIsValid() && getPort().getIsValid() && getCluster().getIsValid() && getManagementIp().getIsValid() && getPmUserName().getIsValid() && getPmPassword().getIsValid() && getPmType().getIsValid() && getPmPort().getIsValid() && getPmOptions().getIsValid();
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getTestCommand())
		{
			Test();
		}
	}

	public void run(TaskContext context)
	{
		java.util.ArrayList<Object> state = (java.util.ArrayList<Object>)context.getState();
		String stage = (String)state.get(0);

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//		switch (stage)
//ORIGINAL LINE: case BeginTestStage:
		if (StringHelper.stringsEqual(stage, BeginTestStage))
		{
					String message = null;

					GetNewVdsFenceStatusParameters param = new GetNewVdsFenceStatusParameters();
					if (getHostId() != null)
					{
						param.setVdsId(getHostId().getValue());
					}
					param.setManagementIp((String)state.get(1));
					param.setPmType((String)state.get(2));
					param.setUser((String)state.get(3));
					param.setPassword((String)state.get(4));
					param.setStoragePoolId((Guid)state.get(5));
					param.setFencingOptions(new ValueObjectMap((java.util.HashMap<String, String>)state.get(6), false));

					VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetNewVdsFenceStatus, param);
					if (returnValue != null && returnValue.getReturnValue() != null)
					{
						FenceStatusReturnValue fenceStatusReturnValue = (FenceStatusReturnValue) returnValue.getReturnValue();
						message = fenceStatusReturnValue.toString();
					}
					else
					{
						message = "Test Failed (unknown error).";
					}

					context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { EndTestStage, message })));

		}
//ORIGINAL LINE: case EndTestStage:
		else if (StringHelper.stringsEqual(stage, EndTestStage))
		{
					setMessage((String)state.get(1));
					getTestCommand().setIsExecutionAllowed(true);
		}
	}
}