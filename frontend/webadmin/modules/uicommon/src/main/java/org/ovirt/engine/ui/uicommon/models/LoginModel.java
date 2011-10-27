package org.ovirt.engine.ui.uicommon.models;
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

import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.core.common.users.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public class LoginModel extends Model implements ITaskTarget
{

	public static final String BeginLoginStage = "BeginTest";
	public static final String EndLoginStage = "EndTest";



	public static EventDefinition LoggedInEventDefinition;
	private Event privateLoggedInEvent;
	public Event getLoggedInEvent()
	{
		return privateLoggedInEvent;
	}
	private void setLoggedInEvent(Event value)
	{
		privateLoggedInEvent = value;
	}

	public static EventDefinition LoginFailedEventDefinition;
	private Event privateLoginFailedEvent;
	public Event getLoginFailedEvent()
	{
		return privateLoginFailedEvent;
	}
	private void setLoginFailedEvent(Event value)
	{
		privateLoginFailedEvent = value;
	}



	private UICommand privateLoginCommand;
	public UICommand getLoginCommand()
	{
		return privateLoginCommand;
	}
	public void setLoginCommand(UICommand value)
	{
		privateLoginCommand = value;
	}
	private UICommand privateAboutCommand;
	public UICommand getAboutCommand()
	{
		return privateAboutCommand;
	}
	private void setAboutCommand(UICommand value)
	{
		privateAboutCommand = value;
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

	private ListModel privateDomain;
	public ListModel getDomain()
	{
		return privateDomain;
	}
	private void setDomain(ListModel value)
	{
		privateDomain = value;
	}
	private EntityModel privateUserName;
	public EntityModel getUserName()
	{
		return privateUserName;
	}
	private void setUserName(EntityModel value)
	{
		privateUserName = value;
	}
	private EntityModel privatePassword;
	public EntityModel getPassword()
	{
		return privatePassword;
	}
	private void setPassword(EntityModel value)
	{
		privatePassword = value;
	}

	private boolean isConnecting;
	public boolean getIsConnecting()
	{
		return isConnecting;
	}
	public void setIsConnecting(boolean value)
	{
		if (isConnecting != value)
		{
			isConnecting = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsConnecting"));
		}
	}



	static
	{
		LoggedInEventDefinition = new EventDefinition("LoggedIn", LoginModel.class);
		LoginFailedEventDefinition = new EventDefinition("LoginFailed", LoginModel.class);
	}

	public LoginModel()
	{
		setLoggedInEvent(new Event(LoggedInEventDefinition));
		setLoginFailedEvent(new Event(LoginFailedEventDefinition));

		UICommand tempVar = new UICommand("Login", this);
		tempVar.setIsExecutionAllowed(false);
		setLoginCommand(tempVar);

		UICommand tempVar2 = new UICommand("About", this);
		tempVar2.setIsExecutionAllowed(false);
		setAboutCommand(tempVar2);

		setDomain(new ListModel());
		getDomain().setIsChangable(false);
		setUserName(new EntityModel());
		getUserName().setIsChangable(false);
		getUserName().getEntityChangedEvent().addListener(this);
		setPassword(new EntityModel());
		getPassword().setIsChangable(false);

		setIsConnecting(true);

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setHandleFailure(true);
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			setIsConnecting(false);

			LoginModel loginModel = (LoginModel)model;
			if(ReturnValue == null)
			{
				loginModel.setMessage("Could not connect to oVirt Engine Service, please try to refresh the page. If the problem persists contact your System Administrator.");
				return;
			}
			AsyncQuery _asyncQuery1 = new AsyncQuery();
			_asyncQuery1.setModel(loginModel);
			_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object ReturnValue1)
			{
				LoginModel loginModel1 = (LoginModel)model1;

				loginModel1.getLoginCommand().setIsExecutionAllowed(true);
				loginModel1.getAboutCommand().setIsExecutionAllowed(true);
				loginModel1.getUserName().setIsChangable(true);
				loginModel1.getPassword().setIsChangable(true);
				loginModel1.getDomain().setIsChangable(true);

				java.util.List<String> domains = (java.util.List<String>)ReturnValue1;
				loginModel1.getDomain().setItems(domains);
				loginModel1.getDomain().setSelectedItem(Linq.FirstOrDefault(domains));
			}};
			AsyncDataProvider.GetDomainListViaPublic(_asyncQuery1, false);
		}};
		AsyncDataProvider.IsBackendAvailable(_asyncQuery);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getUserName())
		{
			UserName_EntityChanged();
		}
	}

	private void UserName_EntityChanged()
	{
		getDomain().setIsChangable(GetDomainAvailability());
	}

	private boolean GetDomainAvailability()
	{
		//Check whether the user name contains domain part.
		boolean hasDomain = GetUserNameParts((String)getUserName().getEntity())[1] != null;

		return !hasDomain;
	}

	private String[] GetUserNameParts(String value)
	{
		if (!StringHelper.isNullOrEmpty(value))
		{
			int index = value.indexOf('@');

			//Always return array of two elements representing user name and domain.)
			return new String[] { index > -1 ? value.substring(0, index) : value, index > -1 ? value.substring(index + 1) : null };
		}

		return new String[] { "", null };
	}

	public void Login()
	{
		if (!Validate())
		{
			return;
		}

		getUserName().setIsChangable(false);
		getPassword().setIsChangable(false);
		getDomain().setIsChangable(false);
		getLoginCommand().setIsExecutionAllowed(false);

		//Clear config cache on login (to make sure we don't use old config in a new session)
		DataProvider.ClearConfigCache();

		String fullUserName = (String)getUserName().getEntity();
		String[] parts = GetUserNameParts(fullUserName);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { BeginLoginStage, fullUserName, getPassword().getEntity(), parts[1] }))).Run();
	}

	protected boolean Validate()
	{
		getUserName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		getPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
		getDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		return getUserName().getIsValid() && getPassword().getIsValid() && getDomain().getIsValid();
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getLoginCommand())
		{
			Login();
		}
		else if (command == getAboutCommand())
		{
			About();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}

	public void About()
	{
		AboutModel model = new AboutModel();
		setWindow(model);
		model.setTitle("About oVirt Engine");
		model.setHashName("about_rhev_manager");
		model.setShowOnlyVersion(true);

		UICommand tempVar = new UICommand("Cancel", this);
		tempVar.setTitle("Close");
		tempVar.setIsDefault(true);
		tempVar.setIsCancel(true);
		model.getCommands().add(tempVar);
	}

	public void Cancel()
	{
		setWindow(null);
	}

	public void run(TaskContext context)
	{
		java.util.ArrayList<Object> state = (java.util.ArrayList<Object>)context.getState();
		String stage = (String)state.get(0);

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//		switch (stage)
//ORIGINAL LINE: case BeginLoginStage:
		if (StringHelper.stringsEqual(stage, BeginLoginStage))
		{
					String fullUserName = (String)state.get(1);
					String password = (String)state.get(2);
					String domain = (String)state.get(3);

					VdcUser user = Frontend.Login(fullUserName, password, StringHelper.isNullOrEmpty(domain) ? (String)getDomain().getSelectedItem() : domain);

					context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { EndLoginStage, user })));

		}
//ORIGINAL LINE: case EndLoginStage:
		else if (StringHelper.stringsEqual(stage, EndLoginStage))
		{
					VdcUser user = (VdcUser)state.get(1);

					if (user == null)
					{
						getPassword().setEntity("");
						getLoginFailedEvent().raise(this, EventArgs.Empty);
					}
					else
					{
						getLoggedInEvent().raise(this, EventArgs.Empty);
					}
		}


		getUserName().setIsChangable(true);
		getPassword().setIsChangable(true);
		getDomain().setIsChangable(GetDomainAvailability());
		getLoginCommand().setIsExecutionAllowed(true);
	}
}