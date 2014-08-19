package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

import com.google.gwt.i18n.client.Constants.DefaultStringValue;

public interface ApplicationConstants extends CommonApplicationConstants {

    @DefaultStringValue("oVirt Engine User Portal")
    String applicationTitle();

    @DefaultStringValue("About")
    String aboutPopupCaption();

    @DefaultStringValue("oVirt Engine Version:")
    String ovirtVersionAbout();

    @DefaultStringValue("")
    String copyRightNotice();

    // Login section

    @DefaultStringValue("User Name")
    String loginFormUserNameLabel();

    @DefaultStringValue("Password")
    String loginFormPasswordLabel();

    @DefaultStringValue("Profile")
    String loginFormProfileLabel();

    @DefaultStringValue("Login")
    String loginButtonLabel();

    @DefaultStringValue("Connect Automatically")
    String loginFormConnectAutomaticallyLabel();

    @DefaultStringValue("Message of the Day")
    String motdHeaderLabel();

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

    @DefaultStringValue("Disks")
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

    @DefaultStringValue("Sessions")
    String extendedVirtualMachineSessionsSubTabLabel();

    @DefaultStringValue("General")
    String extendedTemplateGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String extendedTemplateNetworkInterfacesSubTabLabel();

    @DefaultStringValue("Disks")
    String extendedTemplateVirtualDisksSubTabLabel();

    @DefaultStringValue("Events")
    String extendedTemplateEventsSubTabLabel();

    @DefaultStringValue("Permissions")
    String extendedTemplatePermissionsSubTabLabel();

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

    @DefaultStringValue("Suspend")
    String suspendVmLabel();

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

    // machine status messages
    @DefaultStringValue("Powering Up")
    String WaitForLaunch();

    @DefaultStringValue("Powering Up")
    String PoweringUp();

    @DefaultStringValue("Powering Up")
    String RebootInProgress();

    @DefaultStringValue("Powering Up")
    String RestoringState();

    @DefaultStringValue("Machine is Ready")
    String MigratingFrom();

    @DefaultStringValue("Machine is Ready")
    String MigratingTo();

    @DefaultStringValue("Machine is Ready")
    String Up();

    @DefaultStringValue("Paused")
    String Paused();

    @DefaultStringValue("Suspended")
    String Suspended();

    @DefaultStringValue("Powering Down")
    String PoweringDown();

    @DefaultStringValue("Not Available")
    String Unknown();

    @DefaultStringValue("Not Available")
    String Unassigned();

    @DefaultStringValue("Not Available")
    String NotResponding();

    @DefaultStringValue("Please Wait..")
    String SavingState();

    @DefaultStringValue("Please Wait..")
    String ImageLocked();

    @DefaultStringValue("Machine is Down")
    String Down();

    // machine messages
    @DefaultStringValue("Shutdown VM")
    String shutdownVm();

    @DefaultStringValue("Suspend VM")
    String suspendVm();

    @DefaultStringValue("Take VM")
    String takeVm();

    @DefaultStringValue("Run VM")
    String runVm();

    @DefaultStringValue("Reboot VM")
    String rebootVm();

    @DefaultStringValue("Double Click for Console")
    String doubleClickForConsole();


    @DefaultStringValue("Used by Others")
    String othersUseQuota();

    @DefaultStringValue("Used by You")
    String youUseQuota();

    @DefaultStringValue("Free")
    String freeQuota();

    @DefaultStringValue("Free Memory: ")
    String freeMemory();

    @DefaultStringValue("Free Storage: ")
    String freeStorage();

    @DefaultStringValue("Quota Summary ")
    String quotaSummary();

    @DefaultStringValue("Virtual Machines' Disks & Snapshots ")
    String vmDisksAndSnapshots();

    @DefaultStringValue("Quota")
    String tooltipQuotaLabel();

    @DefaultStringValue("Total usage")
    String tooltipTotalUsageLabel();
}
