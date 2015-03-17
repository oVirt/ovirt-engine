package org.ovirt.engine.ui.uicompat;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;

public interface UIMessages extends com.google.gwt.i18n.client.Messages {

    @DefaultMessage("One of the parameters isn''t supported (available parameter(s): {0})")
    String customPropertyOneOfTheParamsIsntSupported(String parameters);

    @DefaultMessage("the value for parameter <{0}> should be in the format of: <{1}>")
    String customPropertyValueShouldBeInFormatReason(String parameter, String format);

    @DefaultMessage("Create operation failed. Domain {0} already exists in the system.")
    String createOperationFailedDcGuideMsg(String storageName);

    @DefaultMessage("Name can contain only ''A-Z'', ''a-z'', ''0-9'', ''_'' or ''-'' characters, max length: {0}")
    String nameCanContainOnlyMsg(int maxNameLength);

    @DefaultMessage("Note: {0} will be removed!")
    String detachNote(String localStoragesFormattedString);

    @DefaultMessage("You are about to disconnect the Management Interface ({0}).\nAs a result, the Host might become unreachable.\n\n"
            + "Are you sure you want to disconnect the Management Interface?")
    String youAreAboutToDisconnectHostInterfaceMsg(String nicName);

    @DefaultMessage("Could not connect to the agent on the guest, it may be unresponsive or not installed.\nAs a result, some features may not work.")
    String connectingToGuestWithNotResponsiveAgentMsg();

    @DefaultMessage("This field can''t contain blanks or special characters, must be at least one character long, legal values are 0-9, a-z, ''_'', ''.'' and a length of up to {0} characters.")
    String hostNameMsg(int hostNameMaxLength);

    @DefaultMessage("{0} between {1} and {2}.")
    String integerValidationNumberBetweenInvalidReason(String prefixMsg, int min, int max);

    @DefaultMessage("{0} greater than or equal to {1}.")
    String integerValidationNumberGreaterInvalidReason(String prefixMsg, int min);

    @DefaultMessage("{0} less than or equal to {1}.")
    String integerValidationNumberLessInvalidReason(String prefixMsg, int max);

    @DefaultMessage("Field content must not exceed {0} characters.")
    String lenValidationFieldMusnotExceed(int maxLength);

    @DefaultMessage("Disks'' Storage Domains are not accessible.")
    String vmStorageDomainIsNotAccessible();

    @DefaultMessage("No Storage Domain is active.")
    String noActiveStorageDomain();

    @DefaultMessage("This name was already assigned to another cloned Virtual Machine.")
    String alreadyAssignedClonedVmName();

    @DefaultMessage("This suffix will cause a name collision with another cloned Virtual Machine: {0}.")
    String suffixCauseToClonedVmNameCollision(String vmName);

    @DefaultMessage("This name was already assigned to another cloned Template.")
    String alreadyAssignedClonedTemplateName();

    @DefaultMessage("This suffix will cause a name collision with another cloned Template: {0}.")
    String suffixCauseToClonedTemplateNameCollision(String templateName);

    @DefaultMessage("Create operation failed. Storage connection is already used by the following storage domain: {0}.")
    String createFailedDomainAlreadyExistStorageMsg(String storageName);

    @DefaultMessage("Import operation failed. Domain {0} already exists in the system.")
    String importFailedDomainAlreadyExistStorageMsg(String storageName);

    @DefaultMessage("Memory size is between {0} MB and {1} MB")
    String memSizeBetween(int minMemSize, int maxMemSize);

    @DefaultMessage("Maximum memory size is {0} MB.")
    String maxMemSizeIs(int maxMemSize);

    @DefaultMessage("Minimum memory size is {0} MB.")
    String minMemSizeIs(int minMemSize);

    @DefaultMessage("Name must contain only alphanumeric characters, \"-\", \"_\" or \".\". Maximum length: {0}.")
    String nameMustConataionOnlyAlphanumericChars(int maxLen);

    @DefaultMessage("Name (with suffix) can contain only alphanumeric, '.', '_' or '-' characters. Maximum length: {0}.")
    String newNameWithSuffixCannotContainBlankOrSpecialChars(int maxLen);

    @DefaultMessage("Import process has begun for VM(s): {0}.\nYou can check import status in the main ''Events'' tab")
    String importProcessHasBegunForVms(String importedVms);

    @DefaultMessage("''{0}'' Storage Domain is not active. Please activate it.")
    String storageDomainIsNotActive(String storageName);

    @DefaultMessage("Import process has begun for Template(s): {0}.\nYou can check import status in the main ''Events'' tab")
    String importProcessHasBegunForTemplates(String importedTemplates);

    @DefaultMessage("Template(s): {0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String templatesAlreadyExistonTargetExportDomain(String existingTemplates);

    @DefaultMessage("VM(s): {0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String vmsAlreadyExistOnTargetExportDomain(String existingVMs);

    @DefaultMessage("Template {0} (dependent VM(s): {1})")
    String templatesWithDependentVMs(String template, String vms);

    @DefaultMessage("Shared disk(s) will not be a part of the VM export: {0}.")
    String sharedDisksWillNotBePartOfTheExport(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM export: {0}.")
    String directLUNDisksWillNotBePartOfTheExport(String diskList);

    @DefaultMessage("Disk snapshot(s) will not be a part of the VM export: {0}.")
    String snapshotDisksWillNotBePartOfTheExport(String diskList);

    @DefaultMessage("There are no disks allowing an export, only the configuration will be a part of the VM export.")
    String noExportableDisksFoundForTheExport();

    @DefaultMessage("Shared disk(s) will not be a part of the VM snapshot: {0}.")
    String sharedDisksWillNotBePartOfTheSnapshot(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM snapshot: {0}.")
    String directLUNDisksWillNotBePartOfTheSnapshot(String diskList);

    @DefaultMessage("Disk snapshot(s) will not be a part of the VM snapshot: {0}.")
    String snapshotDisksWillNotBePartOfTheSnapshot(String diskList);

    @DefaultMessage("There are no disks allowing a snapshot, only the configuration will be a part of the VM snapshot.")
    String noExportableDisksFoundForTheSnapshot();

    @DefaultMessage("Shared disk(s) will not be a part of the VM template: {0}.")
    String sharedDisksWillNotBePartOfTheTemplate(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM template: {0}.")
    String directLUNDisksWillNotBePartOfTheTemplate(String diskList);

    @DefaultMessage("Snapshot disk(s) will not be a part of the VM template: {0}.")
    String snapshotDisksWillNotBePartOfTheTemplate(String diskList);

    @DefaultMessage("There are no disks allowing an export, only the configuration will be a part of the VM template.")
    String noExportableDisksFoundForTheTemplate();

    @DefaultMessage("{0} (Last scan: {1})")
    String diskAlignment(String alignment, String lastScanDate);

    @DefaultMessage("Error connecting to Virtual Machine using SPICE:\n{0}")
    String errConnectingVmUsingSpiceMsg(Object errCode);

    @DefaultMessage("Error connecting to Virtual Machine using RDP:\n{0}")
    String errConnectingVmUsingRdpMsg(Object errCode);

    @DefaultMessage("Are you sure you want to delete snapshot from {0} with description ''{1}''?")
    String areYouSureYouWantToDeleteSanpshot(String from, Object description);

    @DefaultMessage("Edit Bond Interface {0}")
    String editBondInterfaceTitle(String name);

    @DefaultMessage("Edit Interface {0}")
    String editInterfaceTitle(String name);

    @DefaultMessage("Edit Network {0}")
    String editNetworkTitle(String name);

    @DefaultMessage("Setup Host {0} Networks")
    String setupHostNetworksTitle(String hostName);

    @DefaultMessage("({0} bricks selected)")
    String noOfBricksSelected(int brickCount);

    @DefaultMessage("Please use your VNC client to connect to this VM.<br/><br/>Use the following parameters:<br/>IP:Port --  {0}:{1}<br/><br/> Password: {2}<br/>(note: this password is valid for {3} seconds)")
    String vncInfoMessage(String hostIp, int port, String password, int seconds);

    @DefaultMessage("Press {0} to Release Cursor")
    String pressKeyToReleaseCursor(String key);

    @DefaultMessage("LUN is already part of Storage Domain: {0}")
    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    @DefaultMessage("LUN is already used by disk: {0}")
    String lunUsedByDiskWarning(String diskAlias);

    @DefaultMessage("Used by VG: {0}")
    String lunUsedByVG(String vgID);

    @DefaultMessage("Replica count will be reduced from {0} to {1}. Are you sure you want to remove the following Brick(s)?")
    String removeBricksReplicateVolumeMessage(int oldReplicaCount, int newReplicaCount);

    @DefaultMessage("Break Bond {0}")
    String breakBond(String bondName);

    @DefaultMessage("Detach Network {0}")
    String detachNetwork(String networkName);

    @DefaultMessage("Remove Network {0}")
    String removeNetwork(String networkName);

    @DefaultMessage("Attach {0} to")
    String attachTo(String name);

    @DefaultMessage("Bond {0} with")
    String bondWith(String name);

    @DefaultMessage("Add {0} to Bond")
    String addToBond(String name);

    @DefaultMessage("Extend {0} with")
    String extendBond(String name);

    @DefaultMessage("Remove {0} from Bond")
    String removeFromBond(String name);

    @DefaultMessage("This might work without network {0}.")
    String suggestDetachNetwork(String networkName);

    @DefaultMessage("Label {0} cannot be assigned to this interface, it is already assigned to interface {1}.")
    String labelInUse(String label, String ifaceName);

    @DefaultMessage("Incorrect number of Total Virtual CPUs. It is not possible to compose this number from the available Virtual Sockets and Cores per Virtual Sockets")
    String incorrectVCPUNumber();

    @DefaultMessage("The max allowed name length is {0} for {1} VMs in pool")
    String poolNameLengthInvalid(int maxLength, int vmsInPool);

    @DefaultMessage("The max allowed num of VMs is {0} when the length of the pool name is {1}")
    String numOfVmsInPoolInvalod(int maxLength, int vmsInPool);

    @DefaultMessage("Refresh Interval: {0} sec")
    String refreshInterval(int intervalSec);

    @DefaultMessage("Name field is empty for host with address {0}")
    String importClusterHostNameEmpty(String address);

    @DefaultMessage("Root Password field is empty for host with address {0}")
    String importClusterHostPasswordEmpty(String address);

    @DefaultMessage("Fingerprint field is empty for host with address {0}")
    String importClusterHostFingerprintEmpty(String address);

    @DefaultMessage("Unable to fetch the Fingerprint of the host(s) {0,list,text}")
    String unreachableGlusterHosts(List<String> hosts);

    @DefaultMessage("{0} in Data Center {1} ({2})")
    String networkDcDescription(String networkName, String dcName, String description);

    @DefaultMessage("{0} in Data Center {1}")
    String networkDc(String networkName, String dcName);

    @DefaultMessage("Vnic {0} from VM {1}")
    String vnicFromVm(String vnic, String vm);

    @DefaultMessage("VM Interface Profile {0} from Network {1}")
    String vnicProfileFromNetwork(String vnicProfile, String network);

    @DefaultMessage("Vnic {0} from Template {1}")
    String vnicFromTemplate(String vnic, String template);

    @DefaultMessage("Non-VM networks are not supported for Cluster version {0}")
    String bridlessNetworkNotSupported(String version);

    @DefaultMessage("Overriding MTU configuration is not supported for Cluster version {0}")
    String mtuOverrideNotSupported(String version);

    @DefaultMessage("{0} VMs")
    String numberOfVmsForHostsLoad(int numberOfVms);

    @DefaultMessage("{0} ({1} Socket(s), {2} Core(s) per Socket)")
    String cpuInfoLabel(int numberOfCpus, int numberOfSockets, int numberOfCpusPerSocket);

    @DefaultMessage("{0} (from Storage Domain {1})")
    String templateDiskDescription(String diskAlias, String storageDomainName);

    @DefaultMessage("Virtual Machine must have at least one network interface defined to boot from network.")
    String interfaceIsRequiredToBootFromNetwork();

    @DefaultMessage("Virtual Machine must have at least one bootable disk defined to boot from hard disk.")
    String bootableDiskIsRequiredToBootFromDisk();

    @DefaultMessage("Diskless Virtual Machine cannot run in stateless mode")
    String disklessVmCannotRunAsStateless();

    @DefaultMessage("Given URL must not contain a scheme (e.g. ''{0}'').")
    String urlSchemeMustBeEmpty(String passedScheme);

    @DefaultMessage("Given URL does not contain a scheme, it must contain one of the following:\n\n{0}")
    String urlSchemeMustNotBeEmpty(String allowedSchemes);

    @DefaultMessage("Given URL contains invalid scheme ''{0}'', only the following schemes are allowed:\n\n{1}")
    String urlSchemeInvalidScheme(String passedScheme, String allowedSchemes);

    @DefaultMessage("Changing the URL of this provider might hurt the proper functioning of the following entities provided by it.\n\n{0}")
    String providerUrlWarningText(String providedEntities);

    // Vnic
    @DefaultMessage("The virtual machine is running and NIC Hot Plug is not supported on cluster version {0} or by the operating system.")
    String nicHotPlugNotSupported(String clusterVersion);

    @DefaultMessage("Updating 'profile' on a running virtual machine while the NIC is plugged is not supported on cluster version {0}")
    String hotProfileUpdateNotSupported(String clusterVersion);

    @DefaultMessage("Custom({0})")
    String customSpmPriority(int priority);

    @DefaultMessage("Brick Details not supported for this Cluster''s compatibility version({0}).")
    String brickDetailsNotSupportedInClusterCompatibilityVersion(String version);

    @DefaultMessage("{0} ({1} Running VM(s))")
    String hostNumberOfRunningVms(String hostName, int runningVms);

    @DefaultMessage("{0} ({1})")
    String commonMessageWithBrackets(String subject, String inBrackets);

    @DefaultMessage("This Network QoS is used by {0} Vnic Profiles.\nAre you sure you want to remove this Network QoS?\n\n Profiles using this QoS:\n")
    String removeNetworkQoSMessage(int numOfProfiles);

    @DefaultMessage("This Storage QoS is used by {0} Disk Profiles.\nAre you sure you want to remove this Storage QoS?\n\n Profiles using this QoS:\n")
    String removeStorageQoSMessage(int numOfProfiles);

    @DefaultMessage("This CPU QoS is used by {0} CPU Profiles.\nAre you sure you want to remove this CPU QoS?\n\n Profiles using this QoS:\n")
    String removeCpuQoSMessage(int numOfProfiles);

    @DefaultMessage("This Host Network QoS is used by {0} Networks.\nAre you sure you want to remove it?\n\nNetworks using this QoS:\n")
    String removeHostNetworkQosMessage(int numOfNetworks);

    @DefaultMessage("{0} ({1} Socket(s), {2} Core(s) per Socket)")
    String cpuInfoMessage(int numOfCpus, int sockets, int coresPerSocket);

    @DefaultMessage("NUMA Topology - {0}")
    String numaTopologyTitle(String hostName);

    @DefaultMessage("Could not fetch rebalance status of volume :  {0}")
    String rebalanceStatusFailed(String name);

    @DefaultMessage("Could not fetch profile statistics of volume : {0}")
    String volumeProfileStatisticsFailed(String volName);

    @DefaultMessage("Could not fetch remove brick status of volume :  {0}")
    String removeBrickStatusFailed(String name);

    @DefaultMessage("Are you sure you want to stop the rebalance operation on the volume : {0}?")
    String confirmStopVolumeRebalance(String name);

    @DefaultMessage("The following disks cannot be moved: {0}")
    String cannotMoveDisks(String disks);

    @DefaultMessage("The following disks cannot be copied: {0}")
    String cannotCopyDisks(String disks);

    @DefaultMessage("The following disks will become preallocated, and may consume considerably more space on the target: {0}")
    String moveDisksPreallocatedWarning(String disks);

    @DefaultMessage("Error connecting to {0} using {1} protocol")
    String errorConnectingToConsole(String name, String s);

    @DefaultMessage("Cannot connect to the console for {0}")
    String cannotConnectToTheConsole(String vmName);

    @DefaultMessage("Optimize scheduling for host weighing (ordering):\n" +
            "Utilization: include weight modules in scheduling to allow best selection\n" +
            "Speed: skip host weighing in case there are more than {0} pending requests")
    String schedulerOptimizationInfo(int numOfRequests);

    @DefaultMessage("Overbooking: Allows running cluster''s scheduling requests in parallel, " +
            "without preserving resource allocation. This option allows handling a mass of " +
            "scheduling requests ({0} requests), while some requests may fail due to the re-use of the " +
            "same resource allocation (Use this option only if you are familiar with this behavior).")
    String schedulerAllowOverbookingInfo(int numOfRequests);

    @DefaultMessage("{0} (Clone/Independent)")
    String vmTemplateWithCloneProvisioning(String templateName);

    @DefaultMessage("{0} (Thin/Dependent)")
    String vmTemplateWithThinProvisioning(String templateName);

    @DefaultMessage("You are about to change the Data Center Compatibility Version. This will upgrade all the " +
            "Storage Domains belonging to the Data Center, and make them unusable with versions older than {0}. " +
            "Are you sure you want to continue?")
    String youAreAboutChangeDcCompatibilityVersionWithUpgradeMsg(String version);

    @DefaultMessage("Active (Score: {0})")
    String haActive(int score);

    @DefaultMessage("Volume Profile Statistics - {0}")
    String volumeProfilingStatsTitle(String volumeName);

    @DefaultMessage("Network couldn''t be assigned to ''{0}'' via label ''{1}''.")
    String networkLabelConflict(String nicName, String labelName);

    @DefaultMessage("Network should be assigned to ''{0}'' via label ''{1}''. However, for some reason it isn''t.")
    String labeledNetworkNotAttached(String nicName, String labelName);

    @DefaultMessage("VM Boot menu is not supported in Cluster version {0}")
    String bootMenuNotSupported(String clusterVersion);

    @DefaultMessage("Disk {0} from Snapshot {1}")
    String diskSnapshotLabel(String diskAlias, String snapshotDescription);

    @DefaultMessage("This option is not supported in Cluster version {0}")
    String optionNotSupportedClusterVersionTooOld(String clusterVersion);

    @DefaultMessage("This option requires SPICE display protocol to be used")
    String optionRequiresSpiceEnabled();

    @DefaultMessage("Random Number Generator source ''{0}'' is not supported by the cluster")
    String rngSourceNotSupportedByCluster(String source);

    @DefaultMessage("Current interval of profiling has been running for {0} {1} of the total {2} {3} profiling time")
    String glusterVolumeCurrentProfileRunTime(int currentRunTime, String currentRunTimeUnit, int totalRunTime, String totalRunTimeUnit);

    @DefaultMessage("{0} {1} have been read in the current profiling interval out of {2} {3} during profiling")
    String bytesReadInCurrentProfileInterval(String currentBytesRead, String currentBytesReadUnit, String totalBytes, String totalBytesUnit);

    @DefaultMessage("{0} {1} have been written in the current profiling interval out of {2} {3} during profiling")
    String bytesWrittenInCurrentProfileInterval(String currentBytesWritten, String currentBytesWrittenUnit, String totalBytes, String totalBytesUnit);

    @DefaultMessage("Default ({0})")
    String defaultMtu(int mtu);

    @DefaultMessage("{0} ({1})")
    String threadsAsCoresPerSocket(int cores, int threads);

    @DefaultMessage("Do you approve trusting certificate subject {0} issued by {1}, SHA-1 fingerprint {2}?")
    String approveCertificateTrust(String subject, String issuer, String sha1Fingerprint);

    @DefaultMessage("Do you approve trusting self signed certificate subject {0}, SHA-1 fingerprint {1}?")
    String approveRootCertificateTrust(String subject, String sha1Fingerprint);

    @DefaultMessage("Force {0} session")
    String geoRepForceTitle(String action);

    // Gluster Volume Snapshots
    @DefaultMessage("Value of cluster configuration parameter {0} is empty")
    String clusterSnapshotOptionValueEmpty(String option);

    @DefaultMessage("Value of volume configuration parameter {0} is empty")
    String volumeSnapshotOptionValueEmpty(String option);

    @DefaultMessage("{0}: ({1} GB) {2} {3}")
    String vmDialogDisk(String name, String sizeInGb, String type, String boot);

    @DefaultMessage("Restore Snapshot on volume - {0}")
    String confirmRestoreSnapshot(String volumeName);

    @DefaultMessage("Remove Snapshot on volume - {0}")
    String confirmRemoveSnapshot(String volumeName);

    @DefaultMessage("Remove All Snapshots on volume - {0}")
    String confirmRemoveAllSnapshots(String volumeName);

    @DefaultMessage("Activate Snapshot on volume - {0}")
    String confirmActivateSnapshot(String volumeName);

    @DefaultMessage("Deactivate Snapshot on volume - {0}")
    String confirmDeactivateSnapshot(String volumeName);

    @DefaultMessage("Below snapshots would be removed. Do you want to continue?\n\n {0}")
    String confirmVolumeSnapshotDeleteMessage(String snapshotNames);

    @DefaultMessage("Incorrect enum")
    @AlternateMessage({
        "UNKNOWN", "None",
        "INTERVAL", "Minutely",
        "HOURLY", "Hourly",
        "DAILY", "Daily",
        "WEEKLY", "Weekly",
        "MONTHLY", "Monthly"
    })
    String recurrenceType(@Select GlusterVolumeSnapshotScheduleRecurrence recurrence);
}
