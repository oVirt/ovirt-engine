package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterServiceParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

@SuppressWarnings("unused")
public class HostGeneralModel extends EntityModel<VDS> {
    public static final String SUPPORTED_CPUS_PROPERTY_CHANGE = "supportedCpus"; //$NON-NLS-1$
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public static final EventDefinition requestEditEventDefinition;
    private Event<EventArgs> privateRequestEditEvent;
    private UICommand upgradeHostCommand;

    public UICommand getUpgradeHostCommand() {
        return upgradeHostCommand;
    }

    private void setUpgradeHostCommand(UICommand upgradeHostCommand) {
        this.upgradeHostCommand = upgradeHostCommand;
    }

    public Event<EventArgs> getRequestEditEvent() {
        return privateRequestEditEvent;
    }

    private void setRequestEditEvent(Event<EventArgs> value) {
        privateRequestEditEvent = value;
    }

    public static final EventDefinition requestGOToEventsTabEventDefinition;
    private Event<EventArgs> privateRequestGOToEventsTabEvent;

    public Event<EventArgs> getRequestGOToEventsTabEvent() {
        return privateRequestGOToEventsTabEvent;
    }

    private void setRequestGOToEventsTabEvent(Event<EventArgs> value) {
        privateRequestGOToEventsTabEvent = value;
    }

    private UICommand privateSaveNICsConfigCommand;

    public UICommand getSaveNICsConfigCommand() {
        return privateSaveNICsConfigCommand;
    }

    private void setSaveNICsConfigCommand(UICommand value) {
        privateSaveNICsConfigCommand = value;
    }

    private UICommand privateEditHostCommand;

    public UICommand getEditHostCommand() {
        return privateEditHostCommand;
    }

    private void setEditHostCommand(UICommand value) {
        privateEditHostCommand = value;
    }

    private UICommand privateGoToEventsCommand;

    public UICommand getGoToEventsCommand() {
        return privateGoToEventsCommand;
    }

    private void setGoToEventsCommand(UICommand value) {
        privateGoToEventsCommand = value;
    }

    private UICommand restartGlusterCommand;

    public UICommand getRestartGlusterCommand() {
        return restartGlusterCommand;
    }

    private void setRestartGlusterCommand(UICommand value) {
        restartGlusterCommand = value;
    }

    private boolean updateUpgradeAlert;

    @Override
    public void setEntity(VDS vds) {
        updateUpgradeAlert = vds == null || getEntity() == null
            || !vds.getId().equals(getEntity().getId())
            || !vds.getStatus().equals(getEntity().getStatus());

        super.setEntity(vds);
    }

    // 1st column in General tab

    private String os;

    public String getOS() {
        return os;
    }

    public void setOS(String value) {
        if (!Objects.equals(os, value)) {
            os = value;
            onPropertyChanged(new PropertyChangedEventArgs("OS")); //$NON-NLS-1$
        }
    }

    private String osPrettyName;

    public String getOsPrettyName() {
        return osPrettyName;
    }

    public void setOsPrettyName(String value) {
        if (!Objects.equals(osPrettyName, value)) {
            osPrettyName = value;
            onPropertyChanged(new PropertyChangedEventArgs("OsPrettyName")); //$NON-NLS-1$
        }
    }
    private String kernelVersion;

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String value) {
        if (!Objects.equals(kernelVersion, value)) {
            kernelVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("KernelVersion")); //$NON-NLS-1$
        }
    }

    private String kvmVersion;

    public String getKvmVersion() {
        return kvmVersion;
    }

    public void setKvmVersion(String value) {
        if (!Objects.equals(kvmVersion, value)) {
            kvmVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("KvmVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion libvirtVersion;

    public RpmVersion getLibvirtVersion() {
        return libvirtVersion;
    }

    public void setLibvirtVersion(RpmVersion value) {
        if (!Objects.equals(libvirtVersion, value)) {
            libvirtVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("LibvirtVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion vdsmVersion;

    public RpmVersion getVdsmVersion() {
        return vdsmVersion;
    }

    public void setVdsmVersion(RpmVersion value) {
        if (!Objects.equals(vdsmVersion, value)) {
            vdsmVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("VdsmVersion")); //$NON-NLS-1$
        }
    }

    private String spiceVersion;

    public String getSpiceVersion() {
        return spiceVersion;
    }

    public void setSpiceVersion(String value) {
        if (!Objects.equals(spiceVersion, value)) {
            spiceVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("SpiceVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion glusterVersion;

    public RpmVersion getGlusterVersion() {
        return glusterVersion;
    }

    public void setGlusterVersion(RpmVersion value) {
        if (!Objects.equals(glusterVersion, value)) {
            glusterVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("glusterVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion librbdVersion;

    public RpmVersion getLibrbdVersion() {
        return librbdVersion;
    }

    public void setLibrbdVersion(RpmVersion value) {
        if (!Objects.equals(librbdVersion, value)) {
            librbdVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("LibrbdVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion ovsVersion;

    public RpmVersion getOvsVersion() {
        return ovsVersion;
    }

    public void setOvsVersion(RpmVersion ovsVersion) {
        if (!Objects.equals(this.ovsVersion, ovsVersion)) {
            this.ovsVersion = ovsVersion;
            onPropertyChanged(new PropertyChangedEventArgs("OpenvSwitchVersion")); //$NON-NLS-1$
        }

    }

    private RpmVersion nmstateVersion;

    public RpmVersion getNmstateVersion() {
        return nmstateVersion;
    }

    public void setNmstateVersion(RpmVersion nmstateVersion) {
        if (!Objects.equals(this.nmstateVersion, nmstateVersion)) {
            this.nmstateVersion = nmstateVersion;
            onPropertyChanged(new PropertyChangedEventArgs("NmstateVersion")); //$NON-NLS-1$
        }

    }

    private String iScsiInitiatorName;

    public String getIScsiInitiatorName() {
        return iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value) {
        if (!Objects.equals(iScsiInitiatorName, value)) {
            iScsiInitiatorName = value;
            onPropertyChanged(new PropertyChangedEventArgs("IScsiInitiatorName")); //$NON-NLS-1$
        }
    }

    // 2nd column in General tab

    private Integer spmPriorityValue;
    private Integer spmMaxPriorityValue;
    private Integer spmDefaultPriorityValue;
    private int spmLowPriorityValue;
    private int spmHighPriorityValue;
    private int spmNeverPriorityValue = -1;

    public Integer getSpmPriorityValue() {
        return spmPriorityValue;
    }

    public void setSpmPriorityValue(Integer spmPriorityValue) {
        if (this.spmPriorityValue == null || !this.spmPriorityValue.equals(spmPriorityValue)) {
            this.spmPriorityValue = spmPriorityValue;

            if (spmMaxPriorityValue == null || spmDefaultPriorityValue == null) {
                retrieveMaxSpmPriority();
            } else {
                updateSpmPriority();
            }
        }
    }

    private void retrieveMaxSpmPriority() {
        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery<>(returnValue -> {
            spmMaxPriorityValue = returnValue;
            retrieveDefaultSpmPriority();
        }));
    }

    private void retrieveDefaultSpmPriority() {
        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery<>(returnValue -> {
            spmDefaultPriorityValue = returnValue;
            updateSpmPriorityValues();
            updateSpmPriority();
        }));
    }

    private void updateSpmPriorityValues() {
        spmLowPriorityValue = spmDefaultPriorityValue / 2;
        spmHighPriorityValue = spmDefaultPriorityValue + (spmMaxPriorityValue - spmDefaultPriorityValue) / 2;
    }

    private void updateSpmPriority() {
        if (spmPriorityValue == null) {
            setSpmPriority(null);
        } else if (spmPriorityValue == spmLowPriorityValue) {
            setSpmPriority(constants.lowTitle());
        } else if (spmPriorityValue.equals(spmDefaultPriorityValue)) {
            setSpmPriority(constants.mediumTitle());
        } else if (spmPriorityValue == spmHighPriorityValue) {
            setSpmPriority(constants.highTitle());
        } else if (spmPriorityValue == spmNeverPriorityValue) {
            setSpmPriority(constants.neverTitle());
        } else {
            setSpmPriority(messages.customSpmPriority(spmPriorityValue));
        }
    }

    private String spmPriority;

    public String getSpmPriority() {
        return spmPriority;
    }

    public void setSpmPriority(String spmPriority) {
        if (this.spmPriority == null || !this.spmPriority.equals(spmPriority)) {
            this.spmPriority = spmPriority;
            onPropertyChanged(new PropertyChangedEventArgs("SpmPriority")); //$NON-NLS-1$
        }
    }


    private Integer activeVms;

    public Integer getActiveVms() {
        return activeVms;
    }

    public void setActiveVms(Integer value) {
        if (activeVms == null && value == null) {
            return;
        }
        if (activeVms == null || !activeVms.equals(value)) {
            activeVms = value;
            onPropertyChanged(new PropertyChangedEventArgs("ActiveVms")); //$NON-NLS-1$
        }
    }

    // 3rd column in General tab

    private Integer sharedMemory;

    public Integer getSharedMemory() {
        return sharedMemory;
    }

    public void setSharedMemory(Integer value) {
        if (sharedMemory == null && value == null) {
            return;
        }
        if (sharedMemory == null || !sharedMemory.equals(value)) {
            sharedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("SharedMemory")); //$NON-NLS-1$
        }
    }

    private Integer physicalMemory;

    public Integer getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(Integer value) {
        if (physicalMemory == null && value == null) {
            return;
        }
        if (physicalMemory == null || !physicalMemory.equals(value)) {
            physicalMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("PhysicalMemory")); //$NON-NLS-1$
        }
    }

    private Long swapTotal;

    public Long getSwapTotal() {
        return swapTotal;
    }

    public void setSwapTotal(Long value) {
        if (swapTotal == null && value == null) {
            return;
        }
        if (swapTotal == null || !swapTotal.equals(value)) {
            swapTotal = value;
            onPropertyChanged(new PropertyChangedEventArgs("SwapTotal")); //$NON-NLS-1$
        }
    }

    private Long swapFree;

    public Long getSwapFree() {
        return swapFree;
    }

    public void setSwapFree(Long value) {
        if (swapFree == null && value == null) {
            return;
        }
        if (swapFree == null || !swapFree.equals(value)) {
            swapFree = value;
            onPropertyChanged(new PropertyChangedEventArgs("SwapFree")); //$NON-NLS-1$
        }
    }

    private Integer freeMemory;

    public Integer getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(Integer value) {
        if (freeMemory == null && value == null) {
            return;
        }
        if (freeMemory == null || !freeMemory.equals(value)) {
            freeMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("FreeMemory")); //$NON-NLS-1$
        }
    }

    private Integer usedMemory;

    public Integer getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Integer value) {
        if (usedMemory == null && value == null) {
            return;
        }
        if (usedMemory == null || !usedMemory.equals(value)) {
            usedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsedMemory")); //$NON-NLS-1$
        }
    }

    private Long usedSwap;

    public Long getUsedSwap() {
        return usedSwap;
    }

    public void setUsedSwap(Long value) {
        if (usedSwap == null && value == null) {
            return;
        }
        if (usedSwap == null || !usedSwap.equals(value)) {
            usedSwap = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsedSwap")); //$NON-NLS-1$
        }
    }

    private Float maxSchedulingMemory;

    public Float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

    private Boolean memoryPageSharing;

    public Boolean getMemoryPageSharing() {
        return memoryPageSharing;
    }

    public void setMemoryPageSharing(Boolean value) {
        if (memoryPageSharing == null && value == null) {
            return;
        }
        if (memoryPageSharing == null || !memoryPageSharing.equals(value)) {
            memoryPageSharing = value;
            onPropertyChanged(new PropertyChangedEventArgs("MemoryPageSharing")); //$NON-NLS-1$
        }
    }

    private Object automaticLargePage;

    public Object getAutomaticLargePage() {
        return automaticLargePage;
    }

    public void setAutomaticLargePage(Object value) {
        if (automaticLargePage != value) {
            automaticLargePage = value;
            onPropertyChanged(new PropertyChangedEventArgs("AutomaticLargePage")); //$NON-NLS-1$
        }
    }

    private String hugePages;

    public String getHugePages() {
        return hugePages;
    }

    public void setHugePages(String value) {
        if (!Objects.equals(hugePages, value)) {
            hugePages = value;
            onPropertyChanged(new PropertyChangedEventArgs("HugePages")); //$NON-NLS-1$
        }
    }

    private String kdumpStatus;

    public String getKdumpStatus() {
        return kdumpStatus;
    }

    public void setKdumpStatus(String value) {
        if (!Objects.equals(kdumpStatus, value)) {
            kdumpStatus = value;
            onPropertyChanged(new PropertyChangedEventArgs("KdumpStatus")); //$NON-NLS-1$
        }
    }

    private String hostedEngineHa;

    public String getHostedEngineHa() {
        return hostedEngineHa;
    }

    public void setHostedEngineHa(String value) {
        if (hostedEngineHa == null && value == null) {
            return;
        }
        if (hostedEngineHa == null || !hostedEngineHa.equals(value)) {
            hostedEngineHa = value;
            onPropertyChanged(new PropertyChangedEventArgs("HostedEngineHa")); //$NON-NLS-1$
        }
    }

    private Boolean hostedEngineHaIsConfigured;

    public Boolean getHostedEngineHaIsConfigured() {
        return hostedEngineHaIsConfigured;
    }

    public void setHostedEngineHaIsConfigured(Boolean value) {
        if (hostedEngineHaIsConfigured == null && value == null) {
            return;
        }
        if (hostedEngineHaIsConfigured == null || !hostedEngineHaIsConfigured.equals(value)) {
            hostedEngineHaIsConfigured = value;
            onPropertyChanged(new PropertyChangedEventArgs("HostedEngineHaIsConfigured")); //$NON-NLS-1$
        }
    }

    // Alert section in general tab

    private boolean hasAnyAlert;

    public boolean getHasAnyAlert() {
        return hasAnyAlert;
    }

    public void setHasAnyAlert(boolean value) {
        if (hasAnyAlert != value) {
            hasAnyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasUpgradeAlert;

    public boolean getHasUpgradeAlert() {
        return hasUpgradeAlert;
    }

    public void setHasUpgradeAlert(boolean value) {
        if (hasUpgradeAlert != value) {
            hasUpgradeAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasUpgradeAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasManualFenceAlert;

    public boolean getHasManualFenceAlert() {
        return hasManualFenceAlert;
    }

    public void setHasManualFenceAlert(boolean value) {
        if (hasManualFenceAlert != value) {
            hasManualFenceAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasManualFenceAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallRequiredAlert;

    public boolean getHasReinstallRequiredAlert() {
        return hasReinstallRequiredAlert;
    }

    public void setHasReinstallRequiredAlert(boolean value) {
        if (hasReinstallRequiredAlert != value) {
            hasReinstallRequiredAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallRequiredAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasNoPowerManagementAlert;

    public boolean getHasNoPowerManagementAlert() {
        return hasNoPowerManagementAlert;
    }

    public void setHasNoPowerManagementAlert(boolean value) {
        if (hasNoPowerManagementAlert != value) {
            hasNoPowerManagementAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNoPowerManagementAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasGlusterDisconnectedAlert;

    public boolean getHasGlusterDisconnectedAlert() {
        return hasGlusterDisconnectedAlert;
    }

    public void setHasGlusterDisconnectedAlert(boolean value) {
        if (hasGlusterDisconnectedAlert != value) {
            hasGlusterDisconnectedAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasGlusterDisconnectedAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertNonResponsive;

    public boolean getHasReinstallAlertNonResponsive() {
        return hasReinstallAlertNonResponsive;
    }

    public void setHasReinstallAlertNonResponsive(boolean value) {
        if (hasReinstallAlertNonResponsive != value) {
            hasReinstallAlertNonResponsive = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertNonResponsive")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertInstallFailed;

    public boolean getHasReinstallAlertInstallFailed() {
        return hasReinstallAlertInstallFailed;
    }

    public void setHasReinstallAlertInstallFailed(boolean value) {
        if (hasReinstallAlertInstallFailed != value) {
            hasReinstallAlertInstallFailed = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertInstallFailed")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertMaintenance;

    public boolean getHasReinstallAlertMaintenance() {
        return hasReinstallAlertMaintenance;
    }

    public void setHasReinstallAlertMaintenance(boolean value) {
        if (hasReinstallAlertMaintenance != value) {
            hasReinstallAlertMaintenance = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertMaintenance")); //$NON-NLS-1$
        }
    }

    private boolean hasNICsAlert;

    public boolean getHasNICsAlert() {
        return hasNICsAlert;
    }

    public void setHasNICsAlert(boolean value) {
        if (hasNICsAlert != value) {
            hasNICsAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNICsAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasDefaultRouteAlert;

    public boolean getHasDefaultRouteAlert() {
        return hasDefaultRouteAlert;
    }

    public void setHasDefaultRouteAlert(boolean value) {
        if (hasDefaultRouteAlert != value) {
            hasDefaultRouteAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasDefaultRouteAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasSmtDiscrepancyAlert;

    public boolean getHasSmtDiscrepancyAlert() {
        return hasSmtDiscrepancyAlert;
    }

    public void setHasSmtDiscrepancyAlert(boolean value) {
        if (value != hasSmtDiscrepancyAlert) {
            hasSmtDiscrepancyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSmtDiscrepancyAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasSmtClusterDiscrepancyAlert;

    public boolean getHasSmtClusterDiscrepancyAlert() {
        return hasSmtClusterDiscrepancyAlert;
    }

    public void setHasSmtClusterDiscrepancyAlert(boolean value) {
        if (hasSmtClusterDiscrepancyAlert != value) {
            hasSmtClusterDiscrepancyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasSmtClusterDiscrepancyAlert")); //$NON-NLS-1$
        }
    }

    private Set<String> missingCpuFlags;

    public Set<String> getMissingCpuFlags() {
        if (missingCpuFlags == null) {
            return new HashSet<>();
        }
        return missingCpuFlags;
    }

    public void setMissingCpuFlags(Set<String> missingCpuFlags) {
        this.missingCpuFlags = missingCpuFlags;
    }

    private List<String> supportedCpus;

    public List<String> getSupportedCpus() {
        return supportedCpus;
    }

    public void setSupportedCpus(List<String> supportedCpus) {
        if (!Objects.equals(this.supportedCpus, supportedCpus)) {
            this.supportedCpus = supportedCpus;
            onPropertyChanged(new PropertyChangedEventArgs(SUPPORTED_CPUS_PROPERTY_CHANGE)); //$NON-NLS-1$
        }
    }

    private NonOperationalReason nonOperationalReasonEntity;

    public NonOperationalReason getNonOperationalReasonEntity() {
        return nonOperationalReasonEntity;
    }

    public void setNonOperationalReasonEntity(NonOperationalReason value) {
        if (nonOperationalReasonEntity != value) {
            nonOperationalReasonEntity = value;
            onPropertyChanged(new PropertyChangedEventArgs("NonOperationalReasonEntity")); //$NON-NLS-1$
        }
    }

    private Date bootTime;

    public Date getBootTime() {
        return bootTime;
    }

    public void setBootTime(Long value) {
        /* Factor by 1000 since Date works with millis since epoch and we store seconds (as provided by machines) */
        if (value == null) {
            if (bootTime == null) {
                return;
            }
            bootTime = null;
        } else {
            if (bootTime == null) {
                bootTime = new Date(value * 1000);
            } else if ((bootTime.getTime() / 1000) != value) {
                bootTime.setTime(value * 1000);
            } else {
                return;
            }
        }

        onPropertyChanged(new PropertyChangedEventArgs("bootTime")); //$NON-NLS-1$
    }

    private Integer logicalCores;

    public Integer getLogicalCores() {
        return logicalCores;
    }

    public void setLogicalCores(Integer value) {
        if (logicalCores == null && value == null) {
            return;
        }
        if (logicalCores == null || !logicalCores.equals(value)) {
            logicalCores = value;
            onPropertyChanged(new PropertyChangedEventArgs("logicalCores")); //$NON-NLS-1$
        }
    }

    private String onlineCores;

    public String getOnlineCores() {
        return onlineCores;
    }

    public void setOnlineCores(String value) {
        if (onlineCores == null && value == null) {
            return;
        }
        if (onlineCores == null || !onlineCores.equals(value)) {
            onlineCores = value;
            onPropertyChanged(new PropertyChangedEventArgs("onlineCores")); //$NON-NLS-1$
        }
    }

    private String selinuxEnforceMode;

    public String getSelinuxEnforceMode() {
        return selinuxEnforceMode;
    }

    public void setSelinuxEnforceMode(String newMode) {
        if (!Objects.equals(selinuxEnforceMode, newMode)) {
            selinuxEnforceMode = newMode;
            onPropertyChanged(new PropertyChangedEventArgs("selinuxEnforceMode")); //$NON-NLS-1$
        }
    }

    private String clusterCompatibilityVersion;

    public String getClusterCompatibilityVersion() {
        return clusterCompatibilityVersion;
    }

    public void setClusterCompatibilityVersion(String clusterCompatibilityVersion) {
        if (!Objects.equals(this.clusterCompatibilityVersion, clusterCompatibilityVersion)) {
            this.clusterCompatibilityVersion = clusterCompatibilityVersion;
            onPropertyChanged(new PropertyChangedEventArgs("clusterCompatibilityVersion")); //$NON-NLS-1$
        }
    }

    private boolean hostDevicePassthroughSupport;

    public boolean isHostDevicePassthroughSupport() {
        return hostDevicePassthroughSupport;
    }

    public void setHostDevicePassthroughSupport(boolean value) {
        if (hostDevicePassthroughSupport != value) {
            hostDevicePassthroughSupport = value;
            onPropertyChanged(new PropertyChangedEventArgs("hostDevicePassthroughSupport")); //$NON-NLS-1$
        }
    }

    /**
     * e.g. "PTI: 0, IBPB: 0, IBRS: 0"
     */
    private String kernelFeatures;

    public String getKernelFeatures() {
        return kernelFeatures != null
                ? kernelFeatures
                : constants.notAvailableLabel();
    }

    public void setKernelFeatures(String kernelFeatures) {
        this.kernelFeatures = kernelFeatures;
    }

    private boolean ovnConfigured;

    public boolean isOvnConfigured() {
        return ovnConfigured;
    }

    public void setOvnConfigured(boolean value) {
        if (ovnConfigured != value) {
            ovnConfigured = value;
            onPropertyChanged(new PropertyChangedEventArgs("ovnConfigured")); //$NON-NLS-1$
        }
    }

    static {
        requestEditEventDefinition = new EventDefinition("RequestEditEvent", HostGeneralModel.class); //$NON-NLS-1$
        requestGOToEventsTabEventDefinition = new EventDefinition("RequestGOToEventsTabEvent", HostGeneralModel.class); //$NON-NLS-1$
    }

    public HostGeneralModel() {
        setRequestEditEvent(new Event<>(requestEditEventDefinition));
        setRequestGOToEventsTabEvent(new Event<>(requestGOToEventsTabEventDefinition));
        setTitle(constants.generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$

        setSaveNICsConfigCommand(new UICommand("SaveNICsConfig", this)); //$NON-NLS-1$
        setEditHostCommand(new UICommand("EditHost", this)); //$NON-NLS-1$
        setUpgradeHostCommand(new UICommand("Upgrade", this)); //$NON-NLS-1$
        setGoToEventsCommand(new UICommand("GoToEvents", this)); //$NON-NLS-1$
        setRestartGlusterCommand(new UICommand("RestartGluster", this)); //$NON-NLS-1$
    }

    public void saveNICsConfig() {
        Frontend.getInstance().runMultipleAction(ActionType.CommitNetworkChanges,
                new ArrayList<>(Arrays.asList(new ActionParametersBase[]{new VdsActionParameters(getEntity().getId())})),
                result -> {

                },
                null);
    }

    public void editHost() {
        // Let's the parent model know about request.
        getRequestEditEvent().raise(this, EventArgs.EMPTY);
    }

    public void restartGluster() {
        // call restart gluster
        GlusterServiceParameters parameters =
                new GlusterServiceParameters(getEntity().getClusterId(),
                        getEntity().getId(),
                        ServiceType.GLUSTER,
                        "restart"); //$NON-NLS-1$
        Frontend.getInstance().runAction(ActionType.ManageGlusterService, parameters);
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            updateAlerts();
            updateMemory();
            updateSwapUsed();
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("net_config_dirty") || e.propertyName.equals("status") //$NON-NLS-1$ //$NON-NLS-2$
                || e.propertyName.equals("spm_status") || e.propertyName.equals("vm_active")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateUpgradeAlert = true;
            updateAlerts();
        }

        if (e.propertyName.equals("usage_mem_percent") || e.propertyName.equals("physical_mem_mb")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateMemory();
        }

        if (e.propertyName.equals("swap_total") || e.propertyName.equals("swap_free")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateSwapUsed();
        }
    }

    private void updateProperties() {
        VDS vds = getEntity();

        setOS(vds.getHostOs());
        setOsPrettyName(vds.getPrettyName());
        setKernelVersion(vds.getKernelVersion());
        setKvmVersion(vds.getKvmVersion());
        setLibvirtVersion(vds.getLibvirtVersion());
        setVdsmVersion(vds.getVersion());
        setSpiceVersion(vds.getSpiceVersion());
        setGlusterVersion(vds.getGlusterVersion());
        setLibrbdVersion(vds.getLibrbdVersion());
        setOvsVersion(vds.getOvsVersion());
        setNmstateVersion(vds.getNmstateVersion());
        setIScsiInitiatorName(vds.getIScsiInitiatorName());

        setHostName(vds.getHostName());
        setSpmPriorityValue(vds.getVdsSpmPriority());
        setActiveVms(vds.getVmActive());

        setPhysicalMemory(vds.getPhysicalMemMb());
        setSwapTotal(vds.getSwapTotal());
        setSwapFree(vds.getSwapFree());
        setSharedMemory(vds.getMemSharedPercent());
        setMemoryPageSharing(vds.getKsmState());
        setAutomaticLargePage(vds.getTransparentHugePagesState());
        if (vds.getHugePages() != null) {
            setHugePages(
                    vds.getHugePages().stream().map(
                                    page -> messages.hugePages(
                                            String.valueOf(page.getSizeKB()),
                                            String.valueOf(page.getFree() != null ? page.getFree() : constants.notAvailableLabel()),
                                            String.valueOf(page.getTotal() != null ? page.getTotal() : constants.notAvailableLabel()))
                            ).collect(Collectors.joining(", "))); //$NON-NLS-1$
        } else {
            setHugePages(constants.notAvailableLabel());
        }
        setBootTime(vds.getBootTime());

        setKdumpStatus(EnumTranslator.getInstance().translate(vds.getKdumpStatus()));
        setSelinuxEnforceMode(EnumTranslator.getInstance().translate(vds.getSELinuxEnforceMode()));
        setClusterCompatibilityVersion(vds.getSupportedClusterLevels());

        setHostDevicePassthroughSupport(vds.isHostDevicePassthroughEnabled());

        if (!vds.getHighlyAvailableIsConfigured()) {
            setHostedEngineHaIsConfigured(false);
            setHostedEngineHa(constants.bracketedNotAvailableLabel());
        } else {
            setHostedEngineHaIsConfigured(true);
            if (!vds.getHighlyAvailableIsActive()) {
                setHostedEngineHa(constants.haNotActive());
            } else if (vds.getHighlyAvailableGlobalMaintenance()) {
                setHostedEngineHa(constants.haGlobalMaintenance());
            } else if (vds.getHighlyAvailableLocalMaintenance()) {
                setHostedEngineHa(constants.haLocalMaintenance());
            } else {
                setHostedEngineHa(messages.haActive(vds.getHighlyAvailableScore()));
            }
        }

        setLogicalCores(vds.getCpuThreads());

        String onlineCores = vds.getOnlineCpus();
        if (onlineCores != null) {
            onlineCores = Arrays.stream(onlineCores.split(",")) //$NON-NLS-1$
                .sorted(
                    (s1, s2) -> {
                        Integer i1;
                        Integer i2;
                        try {
                            i1 = Integer.parseInt(s1);
                            i2 = Integer.parseInt(s2);
                        } catch (NumberFormatException ex) {
                            return s1.compareTo(s2);
                        }
                        return i1.compareTo(i2);
                    })
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
        }
        setOnlineCores(onlineCores);

        setKernelFeatures(formatKernelFeatures(vds.getKernelFeatures()));
        setvncEncryptionEnabled(vds.isVncEncryptionEnabled());
        setFipsEnabled(vds.isFipsEnabled());
        setOvnConfigured(vds.isOvnConfigured());
    }

    private String formatKernelFeatures(Map<String, Object> kernelFeatures) {
        if (kernelFeatures == null) {
            return null;
        }

        final int vdsmNotAvailable = -1;
        final String concatenatedPairs = kernelFeatures.entrySet().stream()
                // only string and int values are shown, -1 int values are hidden - considered "N/A"
                .filter(pair ->
                        pair.getValue() instanceof String
                                || (pair.getValue() instanceof Integer && !Objects.equals(vdsmNotAvailable,
                                pair.getValue())))
                .map(pair -> pair.getKey() + ": " + pair.getValue()) //$NON-NLS-1$
                .collect(Collectors.joining(", ")); //$NON-NLS-1$
        if (concatenatedPairs.isEmpty()) {
            return constants.notAvailableLabel();
        }
        return concatenatedPairs;
    }

    private void updateAlerts() {
        setHasAnyAlert(false);
        setHasUpgradeAlert(false);
        setHasManualFenceAlert(false);
        setHasNoPowerManagementAlert(false);
        setHasReinstallRequiredAlert(false);
        setHasReinstallAlertNonResponsive(false);
        setHasReinstallAlertInstallFailed(false);
        setHasReinstallAlertMaintenance(false);
        setHasNICsAlert(false);
        setHasGlusterDisconnectedAlert(false);
        setHasDefaultRouteAlert(false);
        setMissingCpuFlags(null);


        // Check the network alert presense.
        setHasNICsAlert(getEntity().getNetConfigDirty() == null ? false : getEntity().getNetConfigDirty());

        setHasDefaultRouteAlert(!getEntity().isDefaultRouteRoleNetworkAttached());

        // Check manual fence alert presense.
        if (getEntity().getStatus() == VDSStatus.NonResponsive
                && !getEntity().isPmEnabled()
                && ((getEntity().getVmActive() == null ? 0 : getEntity().getVmActive()) > 0 || getEntity().getSpmStatus() == VdsSpmStatus.SPM)) {
            setHasManualFenceAlert(true);
        } else if (!getEntity().isPmEnabled()) {
            setHasNoPowerManagementAlert(true);
        }

        if (getEntity().getStaticData().isReinstallRequired()) {
            setHasReinstallRequiredAlert(true);
        }

        // Check the reinstall alert presence.
        if (getEntity().getStatus() == VDSStatus.NonResponsive) {
            setHasReinstallAlertNonResponsive(true);
        } else if (getEntity().getStatus() == VDSStatus.InstallFailed) {
            setHasReinstallAlertInstallFailed(true);
        } else if (getEntity().getStatus() == VDSStatus.Maintenance) {
            setHasReinstallAlertMaintenance(true);
        }
        if (getEntity().getClusterSupportsGlusterService()
                && !(getEntity().getStatus() == VDSStatus.Installing
                        || getEntity().getStatus() == VDSStatus.Initializing)
                && getEntity().getGlusterPeerStatus() != PeerStatus.CONNECTED) {
            setHasGlusterDisconnectedAlert(true);
        }
        // Update SMT status
        setHasSmtDiscrepancyAlert(getEntity() != null && getEntity().hasSmtDiscrepancyAlert());
        setHasSmtClusterDiscrepancyAlert(getEntity() != null && getEntity().hasSmtClusterDiscrepancyAlert());

        // Set cpu information
        setMissingCpuFlags(getEntity() == null ? null : getEntity().getCpuFlagsMissing());
        setSupportedCpus(getEntity().getSupportedCpus());

        setNonOperationalReasonEntity(getEntity().getNonOperationalReason() == NonOperationalReason.NONE ?
                null : getEntity().getNonOperationalReason());

        updateActionAvailability();
        setHasUpgradeAlert(getEntity().isUpdateAvailable() && getEntity().getStatus() != VDSStatus.Installing);
        setHasAnyAlert();
    }

    private void updateActionAvailability() {
        getEditHostCommand().setIsExecutionAllowed(canExecuteCommand(ActionType.UpdateVds));
        getUpgradeHostCommand().setIsExecutionAllowed(getEntity().isUpdateAvailable()
                && canExecuteCommand(ActionType.UpgradeHost));
    }

    private boolean canExecuteCommand(ActionType actionType) {
        return ActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VDS[]{getEntity()})),
                VDS.class,
                actionType);
    }

    public void setHasAnyAlert() {
        setHasAnyAlert(getHasNICsAlert() || getHasUpgradeAlert() || getHasManualFenceAlert()
                || getHasNoPowerManagementAlert() || getHasReinstallAlertNonResponsive()
                || getHasReinstallAlertInstallFailed() || getHasReinstallAlertMaintenance()
                || getHasGlusterDisconnectedAlert() || getHasDefaultRouteAlert()
                || getHasSmtDiscrepancyAlert() || getHasSmtClusterDiscrepancyAlert());
    }

    private void goToEvents() {
        this.getRequestGOToEventsTabEvent().raise(this, null);
    }

    private void updateMemory() {
        setFreeMemory(null);
        setUsedMemory(null);
        if (getEntity().getPhysicalMemMb() != null && getEntity().getUsageMemPercent() != null) {
            setUsedMemory((int) Math.round(getEntity().getPhysicalMemMb() * (getEntity().getUsageMemPercent() / 100.0)));
            setFreeMemory(getEntity().getPhysicalMemMb() - getUsedMemory());
        }
    }

    private void updateSwapUsed() {
        setUsedSwap(null);
        if (getEntity().getSwapTotal() != null && getEntity().getSwapFree() != null) {
            setUsedSwap(getEntity().getSwapTotal() - getEntity().getSwapFree());
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getSaveNICsConfigCommand()) {
            saveNICsConfig();
        } else if (command == getEditHostCommand()) {
            editHost();
        } else if (command == getGoToEventsCommand()) {
            goToEvents();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getUpgradeHostCommand()) {
            upgrade();
        } else if (command == getRestartGlusterCommand()) {
            restartGluster();
        }
    }

    private void upgrade() {
        if (getWindow() != null) {
            return;
        }

        final VDS host = getEntity();
        Model model = createUpgradeModel(host);
        model.initialize();
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); // $NON-NLS-1$
        setWindow(model);
    }

    public static Model createUpgradeModel(final VDS host) {
        return new UpgradeConfirmationModel(host);
    }

    private boolean vncEncryptionEnabled;

    public boolean isVncEncryptionEnabled() {
        return vncEncryptionEnabled;
    }

    public void setvncEncryptionEnabled(boolean value) {
        if (vncEncryptionEnabled != value) {
            vncEncryptionEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("vncEncryptionEnabled")); //$NON-NLS-1$
        }
    }

    private boolean fipsEnabled;

    public boolean isFipsEnabled() {
        return fipsEnabled;
    }

    public void setFipsEnabled(boolean value) {
        if (fipsEnabled != value) {
            fipsEnabled = value;
            onPropertyChanged(new PropertyChangedEventArgs("fipsEnabled")); //$NON-NLS-1$
        }
    }

    private String hostName;

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }
}
