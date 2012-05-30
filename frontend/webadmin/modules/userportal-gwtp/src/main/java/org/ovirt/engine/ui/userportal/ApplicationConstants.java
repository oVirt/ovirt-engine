package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.i18n.client.Constants.DefaultStringValue;

public interface ApplicationConstants extends CommonApplicationConstants {

    @DefaultStringValue("oVirt Enterprise Virtualization Engine User Portal")
    String applicationTitle();

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

    @DefaultStringValue("Login")
    String loginButtonLabel();

    @DefaultStringValue("Connect Automatically")
    String loginFormConnectAutomaticallyLabel();

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

    @DefaultStringValue("Enable WAN Options")
    String enableWanOptions();

    @DefaultStringValue("RDP Options")
    String rdpOptions();

    @DefaultStringValue("Use Local Drives")
    String useLocalDrives();

    @DefaultStringValue("Remote Desktop")
    String remoteDesktop();

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

    @DefaultStringValue("Cancel")
    String cancel();

    @DefaultStringValue("OK")
    String ok();

    @Override
    @DefaultStringValue("")
    String empty();

    // Template
    @DefaultStringValue("Edit")
    String editTemplate();

    @DefaultStringValue("Remove")
    String removeTemplate();

    // Vm
    @DefaultStringValue("New Server")
    String newServerVm();

    @DefaultStringValue("New Desktop")
    String newDesktopVm();

    @DefaultStringValue("Edit")
    String editVm();

    @DefaultStringValue("Remove")
    String removeVm();

    @DefaultStringValue("Run Once")
    String runOnceVm();

    @DefaultStringValue("Change CD")
    String changeCdVm();

    @DefaultStringValue("Make Template")
    String makeTemplateVm();

    @DefaultStringValue("Virtual Machine")
    String virualMachineVm();

    @DefaultStringValue("Disks")
    String disksVm();

    @DefaultStringValue("Virtual Size")
    String virtualSizeVm();

    @DefaultStringValue("Actual Size")
    String actualSizeVm();

    @DefaultStringValue("Snapshots")
    String snapshotsVm();

    @DefaultStringValue("VNC console access is not supported from the user portal.<br/>" +
            "Please ask the administrator to configure this " +
            "virtual machine to use SPICE for console access.")
    String vncNotSupportedMsg();

    @DefaultStringValue("Your browser/platform does not support console opening")
    String browserNotSupportedMsg();

    // Extended resource
    @DefaultStringValue("Virtual Machines")
    String vmsExtResource();

    @DefaultStringValue("Defined VMs")
    String definedVmsExtResource();

    @DefaultStringValue("Running VMs")
    String runningVmsExtResource();

    @DefaultStringValue("Virtual CPUs")
    String vcpusExtResource();

    @DefaultStringValue("Defined vCPUs")
    String definedvCpusExtResource();

    @DefaultStringValue("Used vCPUs")
    String udedvCpusExtResource();

    @DefaultStringValue("Memory")
    String memExtResource();

    @DefaultStringValue("Defined Memory")
    String definedMenExtResource();

    @DefaultStringValue("Memory Usage")
    String memUsageExtResource();

    @DefaultStringValue("Storage")
    String storageExtResource();

    @DefaultStringValue("Total Size")
    String totalSizeExtResource();

    @DefaultStringValue("Number of Snapshots")
    String numOfSnapshotsExtResource();

    @DefaultStringValue("Total Size of Snapshots")
    String totalSizeSnapshotsExtResource();

    // Basic details
    @DefaultStringValue("Operating System")
    String osBasicDetails();

    @DefaultStringValue("Defined Memory")
    String definedMemBasicDetails();

    @DefaultStringValue("Number of Cores")
    String numOfCoresBasicDetails();

    @DefaultStringValue("Drives")
    String drivesBasicDetails();

    @DefaultStringValue("Console")
    String consoleBasicDetails();

    @DefaultStringValue("Edit")
    String editBasicDetails();

    // Header
    @DefaultStringValue("Logged in user")
    String loggedInUser();

    @DefaultStringValue("UserPortal Documentation")
    String userPortalDoc();
}
