package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

public interface ApplicationConstants extends CommonApplicationConstants {
    String applicationTitle();

    String aboutPopupCaption();

    String ovirtVersionAbout();

    String copyRightNotice();

    String loginFormUserNameLabel();

    String loginFormPasswordLabel();

    String loginFormProfileLabel();

    String loginButtonLabel();

    String motdHeaderLabel();

    String logoutLinkLabel();

    String optionsLinkLabel();

    String aboutLinkLabel();

    String guideLinkLabel();

    String basicMainTabLabel();

    String extendedMainTabLabel();

    String extendedVirtualMachineSideTabLabel();

    String extendedTemplateSideTabLabel();

    String extendedResourceSideTabLabel();

    String extendedVirtualMachineGeneralSubTabLabel();

    String extendedVirtualMachineNetworkInterfaceSubTabLabel();

    String extendedVirtualMachineVirtualDiskSubTabLabel();

    String extendedVirtualMachineSnapshotSubTabLabel();

    String extendedVirtualMachinePermissionSubTabLabel();

    String extendedVirtualMachineEventSubTabLabel();

    String extendedVirtualMachineApplicationSubTabLabel();

    String extendedVirtualMachineGuestContainersSubTabLabel();

    String extendedVirtualMachineMonitorSubTabLabel();

    String extendedVirtualMachineGuestInfoSubTabLabel();

    String extendedTemplateGeneralSubTabLabel();

    String extendedTemplateNetworkInterfacesSubTabLabel();

    String extendedTemplateVirtualDisksSubTabLabel();

    String extendedTemplateEventsSubTabLabel();

    String extendedTemplatePermissionsSubTabLabel();

    String vmMonitorCpuUsageLabel();

    String vmMonitorMemoryUsageLabel();

    String vmMonitorNetworkUsageLabel();

    String takeVmLabel();

    String runVmLabel();

    String returnVmLabel();

    String suspendVmLabel();

    String openConsoleLabel();

    String editConsoleLabel();

    String cancel();

    String ok();

    @Override
    String empty();

    String editTemplate();

    String removeTemplate();

    String editVm();

    String removeVm();

    String runOnceVm();

    String changeCdVm();

    String makeTemplateVm();

    String virualMachineVm();

    String disksVm();

    String virtualSizeVm();

    String actualSizeVm();

    String snapshotsVm();

    String setConsoleKey();

    String vmsExtResource();

    String definedVmsExtResource();

    String runningVmsExtResource();

    String vcpusExtResource();

    String definedvCpusExtResource();

    String udedvCpusExtResource();

    String memExtResource();

    String definedMenExtResource();

    String memUsageExtResource();

    String storageExtResource();

    String totalSizeExtResource();

    String numOfSnapshotsExtResource();

    String totalSizeSnapshotsExtResource();

    String osBasicDetails();

    String definedMemBasicDetails();

    String numOfCoresBasicDetails();

    String drivesBasicDetails();

    String consoleBasicDetails();

    String editBasicDetails();

    String loggedInUser();

    String userPortalDoc();

    String WaitForLaunch();

    String PoweringUp();

    String RebootInProgress();

    String RestoringState();

    String MigratingFrom();

    String MigratingTo();

    String Up();

    String Paused();

    String Suspended();

    String PoweringDown();

    String Unknown();

    String Unassigned();

    String NotResponding();

    String SavingState();

    String ImageLocked();

    String Down();

    String shutdownVm();

    String suspendVm();

    String stopVm();

    String takeVm();

    String runVm();

    String rebootVm();

    String doubleClickForConsole();

    String othersUseQuota();

    String youUseQuota();

    String freeQuota();

    String freeMemory();

    String freeStorage();

    String quotaSummary();

    String vmDisksAndSnapshots();

    String tooltipQuotaLabel();

    String tooltipTotalUsageLabel();

    String unlimitedQuota();

    String exceededQuota();

    String consoleInUse();
}

