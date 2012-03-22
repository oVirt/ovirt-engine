package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("Oops!")
    String errorPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

    // Widgets

    @DefaultStringValue("Next >>")
    String actionTableNextPageButtonLabel();

    @DefaultStringValue("<< Prev")
    String actionTablePrevPageButtonLabel();

    // Table columns

    @DefaultStringValue("Disk Activate/Deactivate while VM is running, is supported only for Clusters of version 3.1 and above")
    String diskHotPlugNotSupported();

    @DefaultStringValue("Disks Allocation:")
    String disksAllocation();

    @DefaultStringValue("Disk ")
    String diskNamePrefix();

    @DefaultStringValue("Single Destination Storage")
    String singleDestinationStorage();

    @DefaultStringValue(" and Quota")
    String singleQuota();

    // Model-bound widgets

    @DefaultStringValue("Boot Options:")
    String runOncePopupBootOptionsLabel();

    @DefaultStringValue("Display Protocol:")
    String runOncePopupDisplayProtocolLabel();

    @DefaultStringValue("Custom Properties")
    String runOncePopupCustomPropertiesLabel();

    @DefaultStringValue("Vnc")
    String runOncePopupDisplayConsoleVncLabel();

    @DefaultStringValue("Spice")
    String runOncePopupDisplayConsoleSpiceLabel();

    @DefaultStringValue("Run Stateless")
    String runOncePopupRunAsStatelessLabel();

    @DefaultStringValue("Start in Pause Mode")
    String runOncePopupRunAndPauseLabel();

    @DefaultStringValue("Linux Boot Options:")
    String runOncePopupLinuxBootOptionsLabel();

    @DefaultStringValue("kernel path")
    String runOncePopupKernelPathLabel();

    @DefaultStringValue("initrd path")
    String runOncePopupInitrdPathLabel();

    @DefaultStringValue("kernel params")
    String runOncePopupKernelParamsLabel();

    @DefaultStringValue("Attach Floppy")
    String runOncePopupAttachFloppyLabel();

    @DefaultStringValue("Attach CD")
    String runOncePopupAttachIsoLabel();

    @DefaultStringValue("Windows Sysprep:")
    String runOncePopupWindowsSysprepLabel();

    @DefaultStringValue("Domain")
    String runOncePopupSysPrepDomainNameLabel();

    @DefaultStringValue("Alternate Credentials")
    String runOnceUseAlternateCredentialsLabel();

    @DefaultStringValue("User Name")
    String runOncePopupSysPrepUserNameLabel();

    @DefaultStringValue("Password")
    String runOncePopupSysPrepPasswordLabel();

    @DefaultStringValue("Boot Sequence:")
    String runOncePopupBootSequenceLabel();

    @DefaultStringValue("Name")
    String makeTemplatePopupNameLabel();

    @DefaultStringValue("Description")
    String makeTemplatePopupDescriptionLabel();

    @DefaultStringValue("Host Cluster")
    String makeTemplateClusterLabel();

    @DefaultStringValue("Quota")
    String makeTemplateQuotaLabel();

    @DefaultStringValue("Storage Domain")
    String makeTemplateStorageDomainLabel();

    @DefaultStringValue("Make Private")
    String makeTemplateIsTemplatePrivateEditorLabel();

    @DefaultStringValue("Description")
    String virtualMachineSnapshotCreatePopupDescriptionLabel();

    @DefaultStringValue("Loading...")
    String loadingLabel();

    @DefaultStringValue("General")
    String generalLabel();

    @DefaultStringValue("Virtual Disks")
    String disksLabel();

    @DefaultStringValue("Network Interfaces")
    String nicsLabel();

    @DefaultStringValue("Installed Applications")
    String applicationsLabel();

    @DefaultStringValue("Preview Mode")
    String previewModelLabel();

    @DefaultStringValue("%1$s (%2$s Socket(s), %3$s Core(s) per Socket)")
    String cpuInfoLabel();

    @DefaultStringValue("Read-Only")
    String readonlyLabel();

    @DefaultStringValue("Current")
    String currentSnapshotLabel();

    @DefaultStringValue("Current (Previous)")
    String previousCurrentSnapshotLabel();

    @DefaultStringValue("Clone VM from Snapshot is supported only for Clusters of version 3.1 and above")
    String cloneVmNotSupported();

}
