package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface NextRunFieldMessages extends ConstantsWithLookup {

    String clusterId();

    String memSizeMb();

    String maxMemorySizeMb();

    String numOfSockets();

    String cpuPerSocket();

    String threadsPerCpu();

    String numOfMonitors();

    String timeZone();

    String vmType();

    String usbPolicy();

    String smartcardEnabled();

    String defaultBootSequence();

    String cpuShares();

    String stateless();

    String ssoMethod();

    String vncKeyboardLayout();

    String minAllocatedMem();

    String runAndPause();

    String defaultDisplayType();

    String serialNumberPolicy();

    String customSerialNumber();

    String bootMenuEnabled();

    String spiceFileTransferEnabled();

    String spiceCopyPasteEnabled();

    String userDefinedProperties();

    String predefinedProperties();

    String customProperties();

    String customEmulatedMachine();

    String biosType();

    String customCpuName();

    String clusterCpuChange();

    String useHostCpuFlags();

    String numOfIoThreads();

    String customCompatibilityVersion();

    String leaseStorageDomainId();

    String resumeBehavior();

    String multiQueuesEnabled();

    String virtioScsiMultiQueuesEnabled();

    String kernelUrl();

    String kernelParams();

    String initrdUrl();

    // Devices

    String memballoon();

    String watchdog();

    String rng();

    String sound();

    String console();

    String virtioscsi();

    String graphicsProtocol();

    String interfaces();
}
