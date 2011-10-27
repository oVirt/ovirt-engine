package org.ovirt.engine.ui.uicommonweb;
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

import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.ui.uicommonweb.models.vms.*;

/**
 Provides configuration values for client side.
*/
@SuppressWarnings("unused")
public class Configurator
{
	/**
	 Gets the value indincating whether the model state should be changed
	 asynchronous in response on property change or command execution.
	*/
	private boolean privateIsAsync;
	public boolean getIsAsync()
	{
		return privateIsAsync;
	}
	protected void setIsAsync(boolean value)
	{
		privateIsAsync = value;
	}

	/**
	 Gets or sets the value specifying what is the desired Spice version.
	*/
	private Version privateSpiceVersion;
	public Version getSpiceVersion()
	{
		return privateSpiceVersion;
	}
	protected void setSpiceVersion(Version value)
	{
		privateSpiceVersion = value;
	}

	private boolean privateIsAdmin;
	public boolean getIsAdmin()
	{
		return privateIsAdmin;
	}
	protected void setIsAdmin(boolean value)
	{
		privateIsAdmin = value;
	}

	private int privateSpiceDefaultUsbPort;
	public int getSpiceDefaultUsbPort()
	{
		return privateSpiceDefaultUsbPort;
	}
	protected void setSpiceDefaultUsbPort(int value)
	{
		privateSpiceDefaultUsbPort = value;
	}
	private int privateSpiceDisableUsbListenPort;
	public int getSpiceDisableUsbListenPort()
	{
		return privateSpiceDisableUsbListenPort;
	}
	protected void setSpiceDisableUsbListenPort(int value)
	{
		privateSpiceDisableUsbListenPort = value;
	}
	private boolean privateSpiceAdminConsole;
	public boolean getSpiceAdminConsole()
	{
		return privateSpiceAdminConsole;
	}
	protected void setSpiceAdminConsole(boolean value)
	{
		privateSpiceAdminConsole = value;
	}
	private boolean privateSpiceFullScreen;
	public boolean getSpiceFullScreen()
	{
		return privateSpiceFullScreen;
	}
	protected void setSpiceFullScreen(boolean value)
	{
		privateSpiceFullScreen = value;
	}
	private ValidateServerCertificateEnum privateValidateServerCertificate = ValidateServerCertificateEnum.values()[0];
	public ValidateServerCertificateEnum getValidateServerCertificate()
	{
		return privateValidateServerCertificate;
	}
	protected void setValidateServerCertificate(ValidateServerCertificateEnum value)
	{
		privateValidateServerCertificate = value;
	}
	private String privateBackendPort;
	public String getBackendPort()
	{
		return privateBackendPort;
	}
	protected void setBackendPort(String value)
	{
		privateBackendPort = value;
	}
	private String privateLogLevel;
	public String getLogLevel()
	{
		return privateLogLevel;
	}
	protected void setLogLevel(String value)
	{
		privateLogLevel = value;
	}

	/**
	 Specifies the interval fronend calls backend to check for updated results
	 for registered queries and searches. Values is in milliseconds.
	*/
	private int privatePollingTimerInterval;
	public int getPollingTimerInterval()
	{
		return privatePollingTimerInterval;
	}
	protected void setPollingTimerInterval(int value)
	{
		privatePollingTimerInterval = value;
	}



	public Configurator()
	{
		setSpiceVersion(new Version(4, 4));
		setSpiceDefaultUsbPort(32023);
		setSpiceDisableUsbListenPort(0);
		setBackendPort("8080");
		setLogLevel("INFO");
		setPollingTimerInterval(5000);
	}

	public void Configure(SearchableListModel searchableListModel)
	{
		searchableListModel.setIsAsync(getIsAsync());
	}

	public void Configure(ISpice spice)
	{
		boolean isUsbEnabled = DataProvider.IsUSBEnabledByDefault();
		int usbListenPort = isUsbEnabled ? getSpiceDefaultUsbPort() : getSpiceDisableUsbListenPort();
		spice.setUsbListenPort(usbListenPort);

		spice.setDesiredVersion(getSpiceVersion());
		spice.setAdminConsole(getSpiceAdminConsole());
		spice.setFullScreen(getSpiceFullScreen());
	}
}