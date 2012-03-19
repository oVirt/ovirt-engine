package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.i18n.client.Constants.DefaultStringValue;

public interface ApplicationConstants extends CommonApplicationConstants {

    @DefaultStringValue("About")
    String aboutPopupCaption();

    @DefaultStringValue("")
    String copyRightNotice();

    // Login section

    @DefaultStringValue("User Name")
    String loginFormUserNameLabel();

    @DefaultStringValue("Password")
    String loginFormPasswordLabel();

    @DefaultStringValue("Domain")
    String loginFormDomainLabel();

    @DefaultStringValue("Connect Automatically")
    String loginFormConnectAutomaticallyLabel();

    @DefaultStringValue("Login")
    String loginButtonLabel();

    // Main section

    @DefaultStringValue("Sign Out")
    String logoutLinkLabel();

    @DefaultStringValue("About")
    String aboutLinkLabel();

    @DefaultStringValue("Guide")
    String guideLinkLabel();

    @DefaultStringValue("Basic")
    String basicMainTabLabel();

    @DefaultStringValue("Extended")
    String extendedMainTabLabel();

    @DefaultStringValue("Virtual Machines")
    String extendedVirtualMachineSideTabLabel();

    @DefaultStringValue("Templates")
    String extendedTemplateSideTabLabel();

    @DefaultStringValue("Resources")
    String extendedResourceSideTabLabel();

    @DefaultStringValue("General")
    String extendedVirtualMachineGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String extendedVirtualMachineNetworkInterfaceSubTabLabel();

    @DefaultStringValue("Virtual Disks")
    String extendedVirtualMachineVirtualDiskSubTabLabel();

    @DefaultStringValue("Snapshots")
    String extendedVirtualMachineSnapshotSubTabLabel();

    @DefaultStringValue("Permissions")
    String extendedVirtualMachinePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String extendedVirtualMachineEventSubTabLabel();

    @DefaultStringValue("Applications")
    String extendedVirtualMachineApplicationSubTabLabel();

    @DefaultStringValue("Monitor")
    String extendedVirtualMachineMonitorSubTabLabel();

    @DefaultStringValue("General")
    String extendedTemplateGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String extendedTemplateNetworkInterfacesSubTabLabel();

    @DefaultStringValue("Virtual Disks")
    String extendedTemplateVirtualDisksSubTabLabel();

    @DefaultStringValue("Events")
    String extendedTemplateEventsSubTabLabel();

    @DefaultStringValue("Permissions")
    String extendedTemplatePermissionsSubTabLabel();

    // Console popup view

    @DefaultStringValue("Spice")
    String spice();

    @DefaultStringValue("Pass Ctrl-Alt-Del to virtual machine")
    String ctrlAltDel();

    @DefaultStringValue("Enable USB Auto-Share")
    String usbAutoshare();

    @DefaultStringValue("Open in Full Screen")
    String openInFullScreen();

    @DefaultStringValue("RDP Options")
    String rdpOptions();

    @DefaultStringValue("Remote Desktop")
    String remoteDesctop();

    @DefaultStringValue("SPICE Options")
    String spiceOptions();

    @DefaultStringValue("Console Options")
    String consoleOptions();

    // VM Monitor sub tab

    @DefaultStringValue("CPU Usage")
    String vmMonitorCpuUsageLabel();

    @DefaultStringValue("Memory Usage")
    String vmMonitorMemoryUsageLabel();

    @DefaultStringValue("Network Usage")
    String vmMonitorNetworkUsageLabel();

    // Buttons on extended tab main grid
    @DefaultStringValue("Take VM")
    String takeVmLabel();

    @DefaultStringValue("Run")
    String runVmLabel();

    @DefaultStringValue("Return VM")
    String returnVmLabel();

    @DefaultStringValue("Shutdown")
    String shutdownVmLabel();

    @DefaultStringValue("Suspend")
    String suspendVmLabel();

    @DefaultStringValue("Stop")
    String stopVmLabel();

    @DefaultStringValue("Open Console")
    String openConsoleLabel();

    @DefaultStringValue("Edit Console Options")
    String editConsoleLabel();

}
