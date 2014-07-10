package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

@SuppressWarnings("unused")
public class HostGeneralModel extends EntityModel
{
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public static final EventDefinition requestEditEventDefinition;
    private Event privateRequestEditEvent;

    public Event getRequestEditEvent()
    {
        return privateRequestEditEvent;
    }

    private void setRequestEditEvent(Event value)
    {
        privateRequestEditEvent = value;
    }

    public static final EventDefinition requestGOToEventsTabEventDefinition;
    private Event privateRequestGOToEventsTabEvent;

    public Event getRequestGOToEventsTabEvent()
    {
        return privateRequestGOToEventsTabEvent;
    }

    private void setRequestGOToEventsTabEvent(Event value)
    {
        privateRequestGOToEventsTabEvent = value;
    }

    private UICommand privateSaveNICsConfigCommand;

    public UICommand getSaveNICsConfigCommand()
    {
        return privateSaveNICsConfigCommand;
    }

    private void setSaveNICsConfigCommand(UICommand value)
    {
        privateSaveNICsConfigCommand = value;
    }

    private UICommand privateEditHostCommand;

    public UICommand getEditHostCommand()
    {
        return privateEditHostCommand;
    }

    private void setEditHostCommand(UICommand value)
    {
        privateEditHostCommand = value;
    }

    private UICommand privateGoToEventsCommand;

    public UICommand getGoToEventsCommand()
    {
        return privateGoToEventsCommand;
    }

    private void setGoToEventsCommand(UICommand value)
    {
        privateGoToEventsCommand = value;
    }

    private boolean updateUpgradeAlert;

    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
    }

    @Override
    public void setEntity(Object value)
    {
        VDS vds = (VDS) value;
        updateUpgradeAlert = vds == null || getEntity() == null
            || !vds.getId().equals(getEntity().getId())
            || !vds.getStatus().equals(getEntity().getStatus());

        super.setEntity(value);
    }

    // 1st column in General tab

    private String os;

    public String getOS()
    {
        return os;
    }

    public void setOS(String value)
    {
        if (!ObjectUtils.objectsEqual(os, value))
        {
            os = value;
            onPropertyChanged(new PropertyChangedEventArgs("OS")); //$NON-NLS-1$
        }
    }

    private String kernelVersion;

    public String getKernelVersion()
    {
        return kernelVersion;
    }

    public void setKernelVersion(String value)
    {
        if (!ObjectUtils.objectsEqual(kernelVersion, value))
        {
            kernelVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("KernelVersion")); //$NON-NLS-1$
        }
    }

    private String kvmVersion;

    public String getKvmVersion()
    {
        return kvmVersion;
    }

    public void setKvmVersion(String value)
    {
        if (!ObjectUtils.objectsEqual(kvmVersion, value))
        {
            kvmVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("KvmVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion libvirtVersion;

    public RpmVersion getLibvirtVersion() {
        return libvirtVersion;
    }

    public void setLibvirtVersion(RpmVersion value) {
        if (!ObjectUtils.objectsEqual(libvirtVersion, value)) {
            libvirtVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("LibvirtVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion vdsmVersion;

    public RpmVersion getVdsmVersion()
    {
        return vdsmVersion;
    }

    public void setVdsmVersion(RpmVersion value)
    {
        if (!ObjectUtils.objectsEqual(vdsmVersion, value))
        {
            vdsmVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("VdsmVersion")); //$NON-NLS-1$
        }
    }

    private String spiceVersion;

    public String getSpiceVersion()
    {
        return spiceVersion;
    }

    public void setSpiceVersion(String value)
    {
        if (!ObjectUtils.objectsEqual(spiceVersion, value))
        {
            spiceVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("SpiceVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion glusterVersion;

    public RpmVersion getGlusterVersion() {
        return glusterVersion;
    }

    public void setGlusterVersion(RpmVersion value) {
        if (!ObjectUtils.objectsEqual(glusterVersion, value)) {
            glusterVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("glusterVersion")); //$NON-NLS-1$
        }
    }

    private String iScsiInitiatorName;

    public String getIScsiInitiatorName()
    {
        return iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value)
    {
        if (!ObjectUtils.objectsEqual(iScsiInitiatorName, value))
        {
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
            }
            else {
                updateSpmPriority();
            }
        }
    }

    private void retrieveMaxSpmPriority() {
        AsyncDataProvider.getInstance().getMaxSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                spmMaxPriorityValue = (Integer) returnValue;
                retrieveDefaultSpmPriority();
            }
        }));
    }

    private void retrieveDefaultSpmPriority() {
        AsyncDataProvider.getInstance().getDefaultSpmPriority(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                spmDefaultPriorityValue = (Integer) returnValue;
                updateSpmPriorityValues();
                updateSpmPriority();
            }
        }));
    }

    private void updateSpmPriorityValues() {
        spmLowPriorityValue = spmDefaultPriorityValue / 2;
        spmHighPriorityValue = spmDefaultPriorityValue + (spmMaxPriorityValue - spmDefaultPriorityValue) / 2;
    }

    private void updateSpmPriority() {
        if (spmPriorityValue == null) {
            setSpmPriority(null);
        }
        else if (spmPriorityValue == spmLowPriorityValue) {
            setSpmPriority(constants.lowTitle());
        }
        else if (spmPriorityValue.equals(spmDefaultPriorityValue)) {
            setSpmPriority(constants.mediumTitle());
        }
        else if (spmPriorityValue == spmHighPriorityValue) {
            setSpmPriority(constants.highTitle());
        }
        else if (spmPriorityValue == spmNeverPriorityValue) {
            setSpmPriority(constants.neverTitle());
        }
        else {
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

    public Integer getActiveVms()
    {
        return activeVms;
    }

    public void setActiveVms(Integer value)
    {
        if (activeVms == null && value == null)
        {
            return;
        }
        if (activeVms == null || !activeVms.equals(value))
        {
            activeVms = value;
            onPropertyChanged(new PropertyChangedEventArgs("ActiveVms")); //$NON-NLS-1$
        }
    }

    // 3rd column in General tab

    private Integer sharedMemory;

    public Integer getSharedMemory()
    {
        return sharedMemory;
    }

    public void setSharedMemory(Integer value)
    {
        if (sharedMemory == null && value == null)
        {
            return;
        }
        if (sharedMemory == null || !sharedMemory.equals(value))
        {
            sharedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("SharedMemory")); //$NON-NLS-1$
        }
    }

    private Integer physicalMemory;

    public Integer getPhysicalMemory()
    {
        return physicalMemory;
    }

    public void setPhysicalMemory(Integer value)
    {
        if (physicalMemory == null && value == null)
        {
            return;
        }
        if (physicalMemory == null || !physicalMemory.equals(value))
        {
            physicalMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("PhysicalMemory")); //$NON-NLS-1$
        }
    }

    private Long swapTotal;

    public Long getSwapTotal()
    {
        return swapTotal;
    }

    public void setSwapTotal(Long value)
    {
        if (swapTotal == null && value == null)
        {
            return;
        }
        if (swapTotal == null || !swapTotal.equals(value))
        {
            swapTotal = value;
            onPropertyChanged(new PropertyChangedEventArgs("SwapTotal")); //$NON-NLS-1$
        }
    }

    private Long swapFree;

    public Long getSwapFree()
    {
        return swapFree;
    }

    public void setSwapFree(Long value)
    {
        if (swapFree == null && value == null)
        {
            return;
        }
        if (swapFree == null || !swapFree.equals(value))
        {
            swapFree = value;
            onPropertyChanged(new PropertyChangedEventArgs("SwapFree")); //$NON-NLS-1$
        }
    }

    private Integer freeMemory;

    public Integer getFreeMemory()
    {
        return freeMemory;
    }

    public void setFreeMemory(Integer value)
    {
        if (freeMemory == null && value == null)
        {
            return;
        }
        if (freeMemory == null || !freeMemory.equals(value))
        {
            freeMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("FreeMemory")); //$NON-NLS-1$
        }
    }

    private Integer usedMemory;

    public Integer getUsedMemory()
    {
        return usedMemory;
    }

    public void setUsedMemory(Integer value)
    {
        if (usedMemory == null && value == null)
        {
            return;
        }
        if (usedMemory == null || !usedMemory.equals(value))
        {
            usedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsedMemory")); //$NON-NLS-1$
        }
    }

    private Long usedSwap;

    public Long getUsedSwap()
    {
        return usedSwap;
    }

    public void setUsedSwap(Long value)
    {
        if (usedSwap == null && value == null)
        {
            return;
        }
        if (usedSwap == null || !usedSwap.equals(value))
        {
            usedSwap = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsedSwap")); //$NON-NLS-1$
        }
    }

    private Float maxSchedulingMemory;

    public Float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

    private Boolean memoryPageSharing;

    public Boolean getMemoryPageSharing()
    {
        return memoryPageSharing;
    }

    public void setMemoryPageSharing(Boolean value)
    {
        if (memoryPageSharing == null && value == null)
        {
            return;
        }
        if (memoryPageSharing == null || !memoryPageSharing.equals(value))
        {
            memoryPageSharing = value;
            onPropertyChanged(new PropertyChangedEventArgs("MemoryPageSharing")); //$NON-NLS-1$
        }
    }

    private Boolean liveSnapshotSupport;

    public Boolean getLiveSnapshotSupport()
    {
        return liveSnapshotSupport;
    }

    public void setLiveSnapshotSupport(Boolean value)
    {
        if (liveSnapshotSupport == null && value == null)
        {
            return;
        }
        if (liveSnapshotSupport == null || !liveSnapshotSupport.equals(value))
        {
            liveSnapshotSupport = value;
            onPropertyChanged(new PropertyChangedEventArgs("LiveSnapshotSupport")); //$NON-NLS-1$
        }
    }

    private Object automaticLargePage;

    public Object getAutomaticLargePage()
    {
        return automaticLargePage;
    }

    public void setAutomaticLargePage(Object value)
    {
        if (automaticLargePage != value)
        {
            automaticLargePage = value;
            onPropertyChanged(new PropertyChangedEventArgs("AutomaticLargePage")); //$NON-NLS-1$
        }
    }

    private String kdumpStatus;

    public String getKdumpStatus()
    {
        return kdumpStatus;
    }

    public void setKdumpStatus(String value)
    {
        if (!ObjectUtils.objectsEqual(kdumpStatus, value))
        {
            kdumpStatus = value;
            onPropertyChanged(new PropertyChangedEventArgs("KdumpStatus")); //$NON-NLS-1$
        }
    }

    private String hostedEngineHa;

    public String getHostedEngineHa()
    {
        return hostedEngineHa;
    }

    public void setHostedEngineHa(String value)
    {
        if (hostedEngineHa == null && value == null)
        {
            return;
        }
        if (hostedEngineHa == null || !hostedEngineHa.equals(value))
        {
            hostedEngineHa = value;
            onPropertyChanged(new PropertyChangedEventArgs("HostedEngineHa")); //$NON-NLS-1$
        }
    }

    private Boolean hostedEngineHaIsConfigured;

    public Boolean getHostedEngineHaIsConfigured()
    {
        return hostedEngineHaIsConfigured;
    }

    public void setHostedEngineHaIsConfigured(Boolean value)
    {
        if (hostedEngineHaIsConfigured == null && value == null)
        {
            return;
        }
        if (hostedEngineHaIsConfigured == null || !hostedEngineHaIsConfigured.equals(value))
        {
            hostedEngineHaIsConfigured = value;
            onPropertyChanged(new PropertyChangedEventArgs("HostedEngineHaIsConfigured")); //$NON-NLS-1$
        }
    }

    // Alert section in general tab

    private boolean hasAnyAlert;

    public boolean getHasAnyAlert()
    {
        return hasAnyAlert;
    }

    public void setHasAnyAlert(boolean value)
    {
        if (hasAnyAlert != value)
        {
            hasAnyAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasUpgradeAlert;

    public boolean getHasUpgradeAlert()
    {
        return hasUpgradeAlert;
    }

    public void setHasUpgradeAlert(boolean value)
    {
        if (hasUpgradeAlert != value)
        {
            hasUpgradeAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasUpgradeAlert")); //$NON-NLS-1$
        }
    }

    protected static boolean shouldAlertUpgrade(ArrayList<RpmVersion> isos, String[] hostOs)
    {
        // HhostOs holds the following components:
        // hostOs[0] holds prefix
        // hostOs[1] holds version
        // hostOs[2] holds release
        final int VERSION_FIELDS_NUMBER = 4;
        boolean alert = false;
        // Fix hostOs[1] to be format of major.minor.build.revision
        // Add ".0" for missing parts
        String[] hostOsVersionParts = hostOs[1].split("\\."); //$NON-NLS-1$
        for (int counter = 0; counter < VERSION_FIELDS_NUMBER - hostOsVersionParts.length; counter++) {
            hostOs[1] = hostOs[1].trim() + ".0"; //$NON-NLS-1$
        }
        Version hostVersion = new Version(hostOs[1].trim());
        String releaseHost = hostOs[2].trim();

        for (RpmVersion iso : isos) {
            // Major check
            if (hostVersion.getMajor() == iso.getMajor()) {
                // Minor and Buildiso.getRpmName()
                if (iso.getMinor() > hostVersion.getMinor() ||
                        iso.getBuild() > hostVersion.getBuild()) {
                    alert = true;
                    break;
                }

                String rpmFromIso = iso.getRpmName();
                // Removes the ".iso" file extension , and get the release part from it
                int isoIndex = rpmFromIso.indexOf(".iso"); //$NON-NLS-1$
                if (isoIndex != -1) {
                    rpmFromIso = iso.getRpmName().substring(0, isoIndex);
                }
                if (RpmVersionUtils.compareRpmParts(RpmVersionUtils.splitRpmToParts(rpmFromIso)[2], releaseHost) > 0) {
                    alert = true;
                    break;
                }
            }
        }
        return alert;
    }

    private boolean hasManualFenceAlert;

    public boolean getHasManualFenceAlert()
    {
        return hasManualFenceAlert;
    }

    public void setHasManualFenceAlert(boolean value)
    {
        if (hasManualFenceAlert != value)
        {
            hasManualFenceAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasManualFenceAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasNoPowerManagementAlert;

    public boolean getHasNoPowerManagementAlert()
    {
        return hasNoPowerManagementAlert;
    }

    public void setHasNoPowerManagementAlert(boolean value)
    {
        if (hasNoPowerManagementAlert != value)
        {
            hasNoPowerManagementAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNoPowerManagementAlert")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertNonResponsive;

    public boolean getHasReinstallAlertNonResponsive()
    {
        return hasReinstallAlertNonResponsive;
    }

    public void setHasReinstallAlertNonResponsive(boolean value)
    {
        if (hasReinstallAlertNonResponsive != value)
        {
            hasReinstallAlertNonResponsive = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertNonResponsive")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertInstallFailed;

    public boolean getHasReinstallAlertInstallFailed()
    {
        return hasReinstallAlertInstallFailed;
    }

    public void setHasReinstallAlertInstallFailed(boolean value)
    {
        if (hasReinstallAlertInstallFailed != value)
        {
            hasReinstallAlertInstallFailed = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertInstallFailed")); //$NON-NLS-1$
        }
    }

    private boolean hasReinstallAlertMaintenance;

    public boolean getHasReinstallAlertMaintenance()
    {
        return hasReinstallAlertMaintenance;
    }

    public void setHasReinstallAlertMaintenance(boolean value)
    {
        if (hasReinstallAlertMaintenance != value)
        {
            hasReinstallAlertMaintenance = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertMaintenance")); //$NON-NLS-1$
        }
    }

    private boolean hasNICsAlert;

    public boolean getHasNICsAlert()
    {
        return hasNICsAlert;
    }

    public void setHasNICsAlert(boolean value)
    {
        if (hasNICsAlert != value)
        {
            hasNICsAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasNICsAlert")); //$NON-NLS-1$
        }
    }

    private NonOperationalReason nonOperationalReasonEntity;

    public NonOperationalReason getNonOperationalReasonEntity()
    {
        return nonOperationalReasonEntity;
    }

    public void setNonOperationalReasonEntity(NonOperationalReason value)
    {
        if (nonOperationalReasonEntity != value)
        {
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
        } if (logicalCores == null || !logicalCores.equals(value)) {
            logicalCores = value;
            onPropertyChanged(new PropertyChangedEventArgs("logicalCores")); //$NON-NLS-1$
        }
    }

    private String selinuxEnforceMode;

    public String getSelinuxEnforceMode() {
        return selinuxEnforceMode;
    }

    public void setSelinuxEnforceMode(String newMode) {
        if (!ObjectUtils.objectsEqual(selinuxEnforceMode, newMode)) {
            selinuxEnforceMode = newMode;
            onPropertyChanged(new PropertyChangedEventArgs("selinuxEnforceMode")); //$NON-NLS-1$
        }
    }

    static
    {
        requestEditEventDefinition = new EventDefinition("RequestEditEvent", HostGeneralModel.class); //$NON-NLS-1$
        requestGOToEventsTabEventDefinition = new EventDefinition("RequestGOToEventsTabEvent", HostGeneralModel.class); //$NON-NLS-1$
    }

    public HostGeneralModel()
    {
        setRequestEditEvent(new Event(requestEditEventDefinition));
        setRequestGOToEventsTabEvent(new Event(requestGOToEventsTabEventDefinition));
        setTitle(constants.generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$

        setSaveNICsConfigCommand(new UICommand("SaveNICsConfig", this)); //$NON-NLS-1$
        setEditHostCommand(new UICommand("EditHost", this)); //$NON-NLS-1$
        setGoToEventsCommand(new UICommand("GoToEvents", this)); //$NON-NLS-1$
    }

    public void saveNICsConfig()
    {
        Frontend.getInstance().runMultipleAction(VdcActionType.CommitNetworkChanges,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] {new VdsActionParameters(getEntity().getId())})),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                    }
                },
                null);
    }

    public void editHost()
    {
        // Let's the parent model know about request.
        getRequestEditEvent().raise(this, EventArgs.EMPTY);
    }

    public void cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateAlerts();
            updateMemory();
            updateSwapUsed();
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("net_config_dirty") || e.propertyName.equals("status") //$NON-NLS-1$ //$NON-NLS-2$
                || e.propertyName.equals("spm_status") || e.propertyName.equals("vm_active")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateUpgradeAlert = true;
            updateAlerts();
        }

        if (e.propertyName.equals("usage_mem_percent") || e.propertyName.equals("physical_mem_mb")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateMemory();
        }

        if (e.propertyName.equals("swap_total") || e.propertyName.equals("swap_free")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateSwapUsed();
        }
    }

    private void updateProperties()
    {
        VDS vds = getEntity();

        setOS(vds.getHostOs());
        setKernelVersion(vds.getKernelVersion());
        setKvmVersion(vds.getKvmVersion());
        setLibvirtVersion(vds.getLibvirtVersion());
        setVdsmVersion(vds.getVersion());
        setSpiceVersion(vds.getSpiceVersion());
        setGlusterVersion(vds.getGlusterVersion());
        setIScsiInitiatorName(vds.getIScsiInitiatorName());

        setSpmPriorityValue(vds.getVdsSpmPriority());
        setActiveVms(vds.getVmActive());

        setPhysicalMemory(vds.getPhysicalMemMb());
        setSwapTotal(vds.getSwapTotal());
        setSwapFree(vds.getSwapFree());
        setSharedMemory(vds.getMemSharedPercent());
        setMemoryPageSharing(vds.getKsmState());
        setAutomaticLargePage(vds.getTransparentHugePagesState());
        setBootTime(vds.getBootTime());

        setKdumpStatus(EnumTranslator.getInstance().get(vds.getKdumpStatus()));
        setSelinuxEnforceMode(EnumTranslator.getInstance().get(vds.getSELinuxEnforceMode()));

        setLiveSnapshotSupport(vds.getLiveSnapshotSupport());

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

        if (vds.getVdsGroupCompatibilityVersion() != null
                && Version.v3_2.compareTo(vds.getVdsGroupCompatibilityVersion()) > 0) {
            setLogicalCores(vds.getCpuCores());
        } else {
            setLogicalCores(vds.getCpuThreads());
        }
    }

    private void updateAlerts()
    {
        setHasAnyAlert(false);
        setHasUpgradeAlert(false);
        setHasManualFenceAlert(false);
        setHasNoPowerManagementAlert(false);
        setHasReinstallAlertNonResponsive(false);
        setHasReinstallAlertInstallFailed(false);
        setHasReinstallAlertMaintenance(false);
        setHasNICsAlert(false);
        getEditHostCommand().setIsExecutionAllowed(VdcActionUtils.canExecute(new ArrayList<VDS>(Arrays.asList(new VDS[]{getEntity()})),
                VDS.class,
                VdcActionType.UpdateVds));
        // Check the network alert presense.
        setHasNICsAlert((getEntity().getNetConfigDirty() == null ? false : getEntity().getNetConfigDirty()));

        // Check manual fence alert presense.
        if (getEntity().getStatus() == VDSStatus.NonResponsive
                && !getEntity().getpm_enabled()
                && ((getEntity().getVmActive() == null ? 0 : getEntity().getVmActive()) > 0 || getEntity().getSpmStatus() == VdsSpmStatus.SPM))
        {
            setHasManualFenceAlert(true);
        }
        else if (!getEntity().getpm_enabled())
        {
            setHasNoPowerManagementAlert(true);
        }

        // Check the reinstall alert presense.
        if (getEntity().getStatus() == VDSStatus.NonResponsive)
        {
            setHasReinstallAlertNonResponsive(true);
        }
        else if (getEntity().getStatus() == VDSStatus.InstallFailed)
        {
            setHasReinstallAlertInstallFailed(true);
        }
        else if (getEntity().getStatus() == VDSStatus.Maintenance)
        {
            setHasReinstallAlertMaintenance(true);
        }

        setNonOperationalReasonEntity((getEntity().getNonOperationalReason() == NonOperationalReason.NONE ? null
                : (NonOperationalReason) getEntity().getNonOperationalReason()));

        setHasAnyAlert();
    }

    public void setHasAnyAlert() {
        setHasAnyAlert(getHasNICsAlert() || getHasUpgradeAlert() || getHasManualFenceAlert()
                || getHasNoPowerManagementAlert() || getHasReinstallAlertNonResponsive()
                || getHasReinstallAlertInstallFailed() || getHasReinstallAlertMaintenance());
    }

    private void goToEvents()
    {
        this.getRequestGOToEventsTabEvent().raise(this, null);
    }

    private void updateMemory()
    {
        setFreeMemory(null);
        setUsedMemory(null);
        if (getEntity().getPhysicalMemMb() != null && getEntity().getUsageMemPercent() != null)
        {
            setUsedMemory((int) Math.round(getEntity().getPhysicalMemMb() * (getEntity().getUsageMemPercent() / 100.0)));
            setFreeMemory(getEntity().getPhysicalMemMb() - getUsedMemory());
        }
    }

    private void updateSwapUsed()
    {
        setUsedSwap(null);
        if (getEntity().getSwapTotal() != null && getEntity().getSwapFree() != null)
        {
            setUsedSwap(getEntity().getSwapTotal() - getEntity().getSwapFree());
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getSaveNICsConfigCommand())
        {
            saveNICsConfig();
        }
        else if (command == getEditHostCommand())
        {
            editHost();
        }
        else if (command == getGoToEventsCommand())
        {
            goToEvents();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }
}
