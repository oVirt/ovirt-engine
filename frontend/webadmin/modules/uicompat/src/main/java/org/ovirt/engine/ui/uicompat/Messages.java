package org.ovirt.engine.ui.uicompat;

import java.util.Date;
import java.util.List;

public interface Messages extends com.google.gwt.i18n.client.Messages {

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

    @DefaultMessage("{0} greater than {1}.")
    String integerValidationNumberGreaterInvalidReason(String prefixMsg, int min);

    @DefaultMessage("{0} less than {1}.")
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

    @DefaultMessage("Create operation failed. Domain {0} already exists in the system.")
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

    @DefaultMessage("New {0} Virtual Machine")
    String newVmTitle(String vmType);

    @DefaultMessage("Edit {0} Virtual Machine")
    String editVmTitle(String vmType);

    @DefaultMessage("Import process has begun for Template(s): {0}.\nYou can check import status in the main ''Events'' tab")
    String importProcessHasBegunForTemplates(String importedTemplates);

    @DefaultMessage("Template(s): {0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String templatesAlreadyExistonTargetExportDomain(String existingTemplates);

    @DefaultMessage("VM(s): {0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String vmsAlreadyExistOnTargetExportDomain(String existingVMs);

    @DefaultMessage("Shared disk(s) will not be a part of the VM export: {0}.")
    String sharedDisksWillNotBePartOfTheExport(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM export: {0}.")
    String directLUNDisksWillNotBePartOfTheExport(String diskList);

    @DefaultMessage("There are no disks allowing an export, only the configuration will be a part of the VM export.")
    String noExportableDisksFoundForTheExport();

    @DefaultMessage("Shared disk(s) will not be a part of the VM snapshot: {0}.")
    String sharedDisksWillNotBePartOfTheSnapshot(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM snapshot: {0}.")
    String directLUNDisksWillNotBePartOfTheSnapshot(String diskList);

    @DefaultMessage("There are no disks allowing a snapshot, only the configuration will be a part of the VM snapshot.")
    String noExportableDisksFoundForTheSnapshot();

    @DefaultMessage("Shared disk(s) will not be a part of the VM template: {0}.")
    String sharedDisksWillNotBePartOfTheTemplate(String diskList);

    @DefaultMessage("Direct LUN disk(s) will not be a part of the VM template: {0}.")
    String directLUNDisksWillNotBePartOfTheTemplate(String diskList);

    @DefaultMessage("There are no disks allowing an export, only the configuration will be a part of the VM template.")
    String noExportableDisksFoundForTheTemplate();

    @DefaultMessage("Error connecting to Virtual Machine using SPICE:\n{0}")
    String errConnectingVmUsingSpiceMsg(Object errCode);

    @DefaultMessage("Error connecting to Virtual Machine using RPD:\n{0}")
    String errConnectingVmUsingRdpMsg(Object errCode);

    @DefaultMessage("Are you sure you want to delete snapshot from {0} with description ''{1}''?")
    String areYouSureYouWantToDeleteSanpshot(Date from, Object description);

    @DefaultMessage("Edit Bond Interface {0}")
    String editBondInterfaceTitle(String name);

    @DefaultMessage("Edit Interface {0}")
    String editInterfaceTitle(String name);

    @DefaultMessage("Edit Network {0}")
    String editNetworkTitle(String name);

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

    @DefaultMessage("Remove {0} from Bond")
    String removeFromBond(String name);

    @DefaultMessage("No valid Operation for {0} and ")
    String noValidOperation(String name);

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

    // Vnic
    @DefaultMessage("Hot Plug is not supported on cluster version {0}")
    String hotPlugNotSupported(String clusterVersion);

    @DefaultMessage("Updating 'network' on a running virtual machine while the NIC is plugged is not supported on cluster version {0}")
    String hotNetworkUpdateNotSupported(String clusterVersion);

    @DefaultMessage("Custom({0})")
    String customSpmPriority(int priority);

    @DefaultMessage("Brick Details not supported for this Cluster''s compatibility version({0}).")
    String brickDetailsNotSupportedInClusterCompatibilityVersion(String version);

    @DefaultMessage("{0} ({1} Running VM(s))")
    String hostNumberOfRunningVms(String hostName, int runningVms);

    @DefaultMessage("{0} ({1})")
    String commonMessageWithBrackets(String subject, String inBrackets);

}
