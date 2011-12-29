package org.ovirt.engine.ui.uicommonweb.models.vms;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class SpiceConsoleModel extends ConsoleModel implements IFrontendMultipleQueryAsyncCallback
{

	public static EventDefinition SpiceDisconnectedEventDefinition;
	public static EventDefinition SpiceConnectedEventDefinition;
	public static EventDefinition SpiceMenuItemSelectedEventDefinition;




	private SpiceMenu menu;
	private ISpice privatespice;
	public ISpice getspice()
	{
		return privatespice;
	}
	public void setspice(ISpice value)
	{
		privatespice = value;
	}

	static
	{
		SpiceDisconnectedEventDefinition = new EventDefinition("SpiceDisconnected", SpiceConsoleModel.class);
		SpiceConnectedEventDefinition = new EventDefinition("SpiceConnected", SpiceConsoleModel.class);
		SpiceMenuItemSelectedEventDefinition = new EventDefinition("SpiceMenuItemSelected", SpiceConsoleModel.class);
	}

	public SpiceConsoleModel()
	{
		setTitle("Spice");

		setspice((ISpice)TypeResolver.getInstance().Resolve(ISpice.class));
		getConfigurator().Configure(getspice());

		getspice().getConnectedEvent().addListener(this);
	}

	@Override
	protected void Connect()
	{
		if (getEntity() != null)
		{
			getLogger().Debug("Connecting to Spice console...");
			if (!getspice().getIsInstalled())
			{
				getLogger().Info("Spice client is not installed.");
				getspice().Install();
				return;
			}

			//Check a spice version.
			if (getConfigurator().getIsAdmin() && getspice().getCurrentVersion().compareTo(getspice().getDesiredVersion()) < 0)
			{
				getLogger().Info("Spice client version is not as desired (" + getspice().getDesiredVersion() + ")");
				getspice().Install();
				return;
			}

			//Don't connect if there VM is not running on any host.
			if (getEntity().getrun_on_vds() == null)
			{
				return;
			}


			SendVmTicket();
		}
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(getspice().getDisconnectedEvent()))
		{
			Spice_Disconnected(sender, (ErrorCodeEventArgs)args);
		}
		else if (ev.equals(getspice().getConnectedEvent()))
		{
			Spice_Connected(sender);
		}
		else if (ev.equals(getspice().getMenuItemSelectedEvent()))
		{
			Spice_MenuItemSelected(sender, (SpiceMenuItemEventArgs)args);
		}
	}

	private void Spice_MenuItemSelected(Object sender, SpiceMenuItemEventArgs e)
	{
		if (getEntity() != null)
		{
			//SpiceMenuCommandItem item = menu.Descendants()
			//	.OfType<SpiceMenuCommandItem>()
			//	.FirstOrDefault(a => a.Id == e.MenuItemId);
			SpiceMenuCommandItem item = null;
			for (SpiceMenuItem a : menu.Descendants())
			{
				if (a.getClass() == SpiceMenuCommandItem.class && a.getId() == e.getMenuItemId())
				{
					item = (SpiceMenuCommandItem)a;
					break;
				}
			}
			if (item != null)
			{
//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//				switch (item.CommandName)
//ORIGINAL LINE: case CommandPlay:
				if (StringHelper.stringsEqual(item.getCommandName(), CommandPlay))
				{
						//use sysprep iff the vm is not initialized and vm has Win OS
						boolean reinitialize = !getEntity().getis_initialized() && DataProvider.IsWindowsOsType(getEntity().getvm_os());
						RunVmParams tempVar = new RunVmParams(getEntity().getvm_guid());
						tempVar.setRunAsStateless(getEntity().getis_stateless());
						tempVar.setReinitialize(reinitialize);
						Frontend.RunMultipleAction(VdcActionType.RunVm, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { tempVar })));

				}
//ORIGINAL LINE: case CommandSuspend:
				else if (StringHelper.stringsEqual(item.getCommandName(), CommandSuspend))
				{
						Frontend.RunMultipleAction(VdcActionType.HibernateVm, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new HibernateVmParameters(getEntity().getvm_guid()) })));

				}
//ORIGINAL LINE: case CommandStop:
				else if (StringHelper.stringsEqual(item.getCommandName(), CommandStop))
				{
						Frontend.RunMultipleAction(VdcActionType.ShutdownVm, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new ShutdownVmParameters(getEntity().getvm_guid(),true) })));

				}
//ORIGINAL LINE: case CommandChangeCD:
				else if (StringHelper.stringsEqual(item.getCommandName(), CommandChangeCD))
				{
						Frontend.RunMultipleAction(VdcActionType.ChangeDisk, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(getEntity().getvm_guid(), StringHelper.stringsEqual(item.getText(), EjectLabel) ? "" : item.getText()) })));
				}
			}
		}
	}

	private void Spice_Disconnected(Object sender, ErrorCodeEventArgs e)
	{
		getspice().getDisconnectedEvent().removeListener(this);
		getspice().getMenuItemSelectedEvent().removeListener(this);

		setIsConnected(false);
		UpdateActionAvailability();

		if (e.getErrorCode() > 100)
		{
			getErrorEvent().raise(this, e);
		}
	}

	private void Spice_Connected(Object sender)
	{
		setIsConnected(true);
		UpdateActionAvailability();
	}

	private void Cancel()
	{
		setWindow(null);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}

	@Override
	protected void UpdateActionAvailability()
	{
		super.UpdateActionAvailability();

		getConnectCommand().setIsExecutionAllowed(getConfigurator().IsDisplayTypeSupported(DisplayType.qxl) && !getIsConnected() && getEntity() != null && getEntity().getdisplay_type() != DisplayType.vnc && IsVmConnectReady());
	}

	private void ExecuteQuery(VM vm)
	{
		AsyncQuery _asyncQuery0 = new AsyncQuery();
		_asyncQuery0.setModel(this);

		_asyncQuery0.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model0, Object result0)
			{
				SpiceConsoleModel thisSpiceConsoleModel = (SpiceConsoleModel)model0;
				VM thisVm = thisSpiceConsoleModel.getEntity();

				storage_domains isoDomain = null;
				if (result0 != null)
				{
					isoDomain = (storage_domains)result0;
				}

				java.util.ArrayList<VdcQueryType> queryTypeList = new java.util.ArrayList<VdcQueryType>();
				queryTypeList.add(VdcQueryType.GetVdsByVdsId);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);
				queryTypeList.add(VdcQueryType.GetVdsCertificateSubjectByVdsId);
				queryTypeList.add(VdcQueryType.GetCACertificate);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);
				queryTypeList.add(VdcQueryType.GetConfigurationValue);

				java.util.ArrayList<VdcQueryParametersBase> parametersList = new java.util.ArrayList<VdcQueryParametersBase>();
				parametersList.add(new GetVdsByVdsIdParameters(thisVm.getrun_on_vds().getValue()));
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SSLEnabled));
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.CipherSuite));
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceSecureChannels));
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.EnableSpiceRootCertificateValidation));
				parametersList.add(new GetVdsByVdsIdParameters(thisVm.getrun_on_vds().getValue()));
				parametersList.add(new VdcQueryParametersBase());
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceToggleFullScreenKeys));
				parametersList.add(new GetConfigurationValueParameters(ConfigurationValues.SpiceReleaseCursorKeys));

				if (isoDomain != null)
				{
					queryTypeList.add(VdcQueryType.GetAllIsoImagesList);

					GetAllIsoImagesListParameters getIsoPamams = new GetAllIsoImagesListParameters();
					getIsoPamams.setStorageDomainId(isoDomain.getid());
					getIsoPamams.setForceRefresh(false);
					parametersList.add(getIsoPamams);
				}

				Frontend.RunMultipleQueries(queryTypeList, parametersList, thisSpiceConsoleModel);
			}};

		AsyncDataProvider.GetIsoDomainByDataCenterId(_asyncQuery0, vm.getstorage_pool_id());
	}

	private String ticket;

	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		java.util.List<VdcQueryReturnValue> returnValues = result.getReturnValues();

		boolean success = true;
		for (VdcQueryReturnValue returnValue : returnValues)
		{
			if (!returnValue.getSucceeded())
			{
				success = false;
				break;
			}
		}


		if (!success)
		{
			boolean enableSpiceRootCertificateValidation = (Boolean) result.getReturnValues().get(4).getReturnValue();
			VdcQueryReturnValue caCertificateReturnValue = result.getReturnValues().get(6);

			// If only the caCertificate query failed - ignore failure (goto OnSuccess)
			if (!caCertificateReturnValue.getSucceeded() && !enableSpiceRootCertificateValidation)
			{
				// Verify that all queries (except caCertificate) succeeded
				// If succeeded goto 'OnSuccess'; Otherwise, 'OnFailure'.
				for (VdcQueryReturnValue returnValue : returnValues)
				{
					if (!returnValue.getSucceeded() && returnValue != caCertificateReturnValue)
					{
						return;
					}
				}
			}
		}


		String cipherSuite = null;
		String spiceSecureChannels = null;

		boolean isSSLEnabled = (Boolean)returnValues.get(1).getReturnValue();
		if (isSSLEnabled)
		{
			cipherSuite = (String)returnValues.get(2).getReturnValue();
			spiceSecureChannels = (String)returnValues.get(3).getReturnValue();
		}

		String certificateSubject = "";
		String caCertificate = "";

		if ((Boolean)returnValues.get(4).getReturnValue())
		{
			certificateSubject = (String)returnValues.get(5).getReturnValue();
			caCertificate = (String)returnValues.get(6).getReturnValue();
		}

		getspice().setHost(getEntity().getdisplay_ip());
		getspice().setPort((getEntity().getdisplay() == null ? 0 : getEntity().getdisplay()));
		getspice().setPassword(ticket);
		getspice().setNumberOfMonitors(getEntity().getnum_of_monitors());
		getspice().setGuestHostName(getEntity().getvm_host().split("[ ]", -1)[0]);
		if (getEntity().getdisplay_secure_port() != null)
		{
			getspice().setSecurePort(getEntity().getdisplay_secure_port());
		}
		if (!StringHelper.isNullOrEmpty(spiceSecureChannels))
		{
			getspice().setSslChanels(spiceSecureChannels);
		}
		if (!StringHelper.isNullOrEmpty(cipherSuite))
		{
			getspice().setCipherSuite(cipherSuite);
		}

		getspice().setHostSubject(certificateSubject);
		getspice().setTrustStore(caCertificate);

		String toggleFullScreenKeys = (String) returnValues.get(7).getReturnValue();
		String releaseCursorKeys = (String) returnValues.get(8).getReturnValue();
		String ctrlAltDel = "ctrl+alt+del";
		String ctrlAltEnd = "ctrl+alt+end";

		String toggleFullScreenKeysTranslated = DataProvider.GetComplexValueFromSpiceRedKeysResource((toggleFullScreenKeys != null) ? toggleFullScreenKeys : "shift+f11");
		String releaseCursorKeysTranslated = DataProvider.GetComplexValueFromSpiceRedKeysResource((releaseCursorKeys != null) ? releaseCursorKeys : "shift+f12");
		String ctrlAltDelTranslated = DataProvider.GetComplexValueFromSpiceRedKeysResource(ctrlAltDel);
		String ctrlAltEndTranslated = DataProvider.GetComplexValueFromSpiceRedKeysResource(ctrlAltEnd);

		getspice().setTitle(getEntity().getvm_name() + ":%d" + (StringHelper.isNullOrEmpty(releaseCursorKeysTranslated) ? "" : (" - Press " + releaseCursorKeysTranslated + " to Release Cursor")));

		// If 'AdminConsole' is true, send true; otherwise, false should be sent only for VMs with SPICE driver installed.
		getspice().setAdminConsole(getConfigurator().getSpiceAdminConsole() ? true : getEntity().getSpiceDriverVersion() != null ? false : true);

		// Update 'UsbListenPort' value
		getspice().setUsbListenPort(getConfigurator().getIsUsbEnabled() && getEntity().getusb_policy() == UsbPolicy.Enabled ? getConfigurator().getSpiceDefaultUsbPort() : getConfigurator().getSpiceDisableUsbListenPort());

		//At lease one of the hot-keys is not empty -> send it to SPICE:
		if (!StringHelper.isNullOrEmpty(releaseCursorKeys) || !StringHelper.isNullOrEmpty(toggleFullScreenKeys))
		{
			String comma = (!StringHelper.isNullOrEmpty(releaseCursorKeys) && !StringHelper.isNullOrEmpty(toggleFullScreenKeys)) ? "," : "";

			String releaseCursorKeysParameter = StringHelper.isNullOrEmpty(releaseCursorKeys) ? "" : "release-cursor=" + releaseCursorKeys;

			String toggleFullScreenKeysParameter = StringHelper.isNullOrEmpty(toggleFullScreenKeys) ? "" : "toggle-fullscreen=" + toggleFullScreenKeys;

			getspice().setHotKey(releaseCursorKeysParameter + comma + toggleFullScreenKeysParameter);
		}

		getspice().setLocalizedStrings(new String[] { "USB", "USB Devices,No USB devices,Client's SPICE USB Redirector is not installed" });


		//Create menu.
		int id = 1;
		menu = new SpiceMenu();

		menu.getItems().add(new SpiceMenuCommandItem(ID_SYS_MENU_SEND_CTRL_ALT_DEL, "S&end " + ctrlAltDelTranslated + "\t" + ctrlAltEndTranslated, ""));
		menu.getItems().add(new SpiceMenuCommandItem(ID_SYS_MENU_TOGGLE_FULL_SCREEN, "&Toggle Full Screen\t" + toggleFullScreenKeysTranslated, ""));

		SpiceMenuContainerItem specialKes = new SpiceMenuContainerItem(id, "&Special Keys");
		menu.getItems().add(specialKes);

		specialKes.getItems().add(new SpiceMenuCommandItem(ID_SYS_MENU_SEND_SHIFT_F11, "&" + toggleFullScreenKeysTranslated, ""));
		specialKes.getItems().add(new SpiceMenuCommandItem(ID_SYS_MENU_SEND_SHIFT_F12, "&" + releaseCursorKeysTranslated, ""));
		specialKes.getItems().add(new SpiceMenuCommandItem(ID_SYS_MENU_SEND_CTRL_ALT_END, "&" + ctrlAltEndTranslated, ""));
		menu.getItems().add(new SpiceMenuSeparatorItem(id));
		id++;

		SpiceMenuContainerItem changeCDItem = new SpiceMenuContainerItem(id, "Change CD");
		id++;

		java.util.ArrayList<String> isos = new java.util.ArrayList<String>();

		if (returnValues.size() > 9)
		{
			java.util.ArrayList<RepoFileMetaData> repoList = (java.util.ArrayList<RepoFileMetaData>)returnValues.get(9).getReturnValue();
			for (RepoFileMetaData RepoFileMetaData : repoList)
			{
				isos.add(RepoFileMetaData.getRepoFileName());
			}
		}

		isos = isos.size() > 0 ? isos : new java.util.ArrayList<String>(java.util.Arrays.asList(new String[] { "No CDs" }));

		Collections.sort(isos);

		for (String fileName : isos)
		{
			changeCDItem.getItems().add(new SpiceMenuCommandItem(id, fileName, CommandChangeCD));
			id++;
		}
		changeCDItem.getItems().add(new SpiceMenuCommandItem(id, EjectLabel, CommandChangeCD));
		id++;

		menu.getItems().add(changeCDItem);
		menu.getItems().add(new SpiceMenuSeparatorItem(id));
		id++;
		menu.getItems().add(new SpiceMenuCommandItem(id, "Play", CommandPlay));
		id++;
		menu.getItems().add(new SpiceMenuCommandItem(id, "Suspend", CommandSuspend));
		id++;
		menu.getItems().add(new SpiceMenuCommandItem(id, "Stop", CommandStop));

		getspice().setMenu(menu.toString());

		getspice().setGuestID(getEntity().getvm_guid().toString());

		//Subscribe to events.
		getspice().getDisconnectedEvent().addListener(this);
		getspice().getMenuItemSelectedEvent().addListener(this);

		if (StringHelper.isNullOrEmpty(getEntity().getdisplay_ip()) || StringHelper.stringsEqual(getEntity().getdisplay_ip(), "0"))
		{
			VDS host = (VDS)returnValues.get(0).getReturnValue();
			if (host == null)
			{
				return;
			}

			AsyncQuery _asyncQuery = new AsyncQuery();
			_asyncQuery.setModel(this);
			_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
			{
				SpiceConsoleModel spiceConsoleModel = (SpiceConsoleModel)model;
				Iterable networkInterfaces = (Iterable)((VdcQueryReturnValue)ReturnValue).getReturnValue();
				java.util.Iterator networkInterfacesIterator = networkInterfaces.iterator();
				while (networkInterfacesIterator.hasNext())
				{
					VdsNetworkInterface currentNetworkInterface = (VdsNetworkInterface)networkInterfacesIterator.next();
					if (currentNetworkInterface == null)
					{
						continue;
					}
					if (currentNetworkInterface.getIsManagement())
					{
						spiceConsoleModel.getspice().setHost(currentNetworkInterface.getAddress());
						spiceConsoleModel.SpiceConnect();
						return;
					}
				}
			}};

			Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId, new GetVdsByVdsIdParameters(host.getvds_id()), _asyncQuery);
		}
		else
		{
			//Try to connect.
			SpiceConnect();
		}
	}

	private void SendVmTicket()
	{
		//Create ticket for single sign on.
		Frontend.RunAction(VdcActionType.SetVmTicket, new SetVmTicketParameters(getEntity().getvm_guid(), null, 120),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			SpiceConsoleModel model = (SpiceConsoleModel)result.getState();
			model.PostSendVmTicket(result.getReturnValue());

			}
		}, this);
	}

	public void PostSendVmTicket(VdcReturnValueBase returnValue)
	{
		if (returnValue == null || !returnValue.getSucceeded())
		{
			return;
		}

		ticket = (String)returnValue.getActionReturnValue();

		// Only if the VM has agent and we connect through user-portal
		// we attempt to perform SSO (otherwise an error will be thrown)
		if (!getConfigurator().getIsAdmin() && getEntity().getGuestAgentVersion() != null && getEntity().getstatus() == VMStatus.Up)
		{
			getLogger().Info("SpiceConsoleManager::Connect: Attempting to perform SSO on Desktop " + getEntity().getvm_name());

			Frontend.RunAction(VdcActionType.VmLogon, new VmOperationParameterBase(getEntity().getvm_guid()),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				SpiceConsoleModel model = (SpiceConsoleModel)result.getState();
				VdcReturnValueBase returnValue1 = result.getReturnValue();
				boolean success1 = returnValue1 != null && returnValue1.getSucceeded();
				if (success1)
				{
					model.ExecuteQuery(getEntity());
				}
				else
				{
					String vmName = returnValue1 != null ? returnValue1.getDescription() : "";
					model.getLogger().Info("SpiceConsoleManager::Connect: Failed to perform SSO on Destkop " + vmName + " continuing without SSO.");
				}

			}
		}, this);
		}
		else
		{
			ExecuteQuery(getEntity());
		}
	}

	public void SpiceConnect()
	{
		try
		{
			getspice().Connect();
		}
		catch (RuntimeException ex)
		{
			getLogger().Error("Exception on Spice connect", ex);
		}
	}


	private static final String CommandStop = "Stop";
	private static final String CommandPlay = "Play";
	private static final String CommandSuspend = "Suspend";
	private static final String CommandChangeCD = "ChangeCD";

	private static final int ID_SYS_MENU_DISCONNECT = 0x1200;
	private static final int ID_SYS_MENU_SEND_CTRL_ALT_DEL = 0x1300;
	private static final int ID_SYS_MENU_TOGGLE_FULL_SCREEN = 0x1400;
	private static final int ID_SYS_MENU_SEND_SHIFT_F11 = 0x1500;
	private static final int ID_SYS_MENU_SEND_SHIFT_F12 = 0x1600;
	private static final int ID_SYS_MENU_SEND_CTRL_ALT_END = 0x1700;
	private static final int ID_SYS_MENU_USB_DEVICES = 0x1800;
}