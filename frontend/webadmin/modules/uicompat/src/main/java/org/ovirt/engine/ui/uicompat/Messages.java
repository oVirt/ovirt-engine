package org.ovirt.engine.ui.uicompat;

import java.util.Date;

public interface Messages extends com.google.gwt.i18n.client.Messages {

    @DefaultMessage("{0} Alerts")
    String alertsTitle(String num);

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

    @DefaultMessage("When {0} {1} {2} is used, kernel path must be non-empty")
    String invalidPath(String kernel, String or, String inetd);

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

    @DefaultMessage("Name must contain only alphanumeric characters. Maximum length: {0}.")
    String nameMustConataionOnlyAlphanumericChars(int maxLen);

    @DefaultMessage("Name cannot contain blanks or special characters. Maximum length: {0}.")
    String nameCannotContainBlankOrSpecialChars(int maxLen);

    @DefaultMessage("New name (with suffix) cannot contain blanks or special characters. Maximum length: {0}.")
    String newNameWithSuffixCannotContainBlankOrSpecialChars(int maxLen);

    @DefaultMessage("Import process has begun for VM(s): {0}.\nYou can check import status in the ''Events'' tab of the specific destination storage domain, or in the main ''Events'' tab")
    String importProcessHasBegunForVms(String importedVms);

    @DefaultMessage("''{0}'' Storage Domain is not active. Please activate it.")
    String storageDomainIsNotActive(String storageName);

    @DefaultMessage("New {0} Virtual Machine")
    String newVmTitle(String vmType);

    @DefaultMessage("Edit {0} Virtual Machine")
    String editVmTitle(String vmType);

    @DefaultMessage("Import process has begun for Template(s): {0}.\nYou can check import status in the ''Events'' tab of the specific destination storage domain, or in the main ''Events'' tab")
    String importProcessHasBegunForTemplates(String importedTemplates);

    @DefaultMessage("Template(s):\n{0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String templatesAlreadyExistonTargetExportDomain(String existingTemplates);

    @DefaultMessage("VM(s):\n{0} already exist on the target Export Domain. If you want to override them, please check the ''Force Override'' check-box.")
    String vmsAlreadyExistOnTargetExportDomain(String existingVMs);

    @DefaultMessage("Error connecting to Virtual Machine using Spice:\n{0}")
    String errConnectingVmUsingSpiceMsg(Object errCode);

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

    @DefaultMessage("Please use your VNC client to connect to this VM.</BR></BR>Use the following parameters:</BR>IP:Port: {0}:{1}</BR>Password: {2}</BR>(note: this password is valid for {3} seconds)")
    String vncInfoMessage(String hostIp, int port, String password, int seconds);

    @DefaultMessage("Press {0} to Release Cursor")
    String pressKeyToReleaseCursor(String key);

    @DefaultMessage("LUN is already part of storage domain: {0}")
    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    @DefaultMessage("LUN is already used by disk: {0}")
    String lunUsedByDiskWarning(String diskAlias);

    @DefaultMessage("LUN is already in use: VG ID - {0}")
    String lunUsedByVGWarning(String vgID);

    @DefaultMessage("Replica count will be reduced from {0} to {1} on removing the following bricks. Are you sure want to continue?")
    String removeBricksReplicateVolumeMessage(int oldReplicaCount, int newReplicaCount);
}
