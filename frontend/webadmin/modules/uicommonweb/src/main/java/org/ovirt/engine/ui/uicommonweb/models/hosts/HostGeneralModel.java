package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class HostGeneralModel extends EntityModel
{

    public static EventDefinition RequestEditEventDefinition;
    private Event privateRequestEditEvent;

    public Event getRequestEditEvent()
    {
        return privateRequestEditEvent;
    }

    private void setRequestEditEvent(Event value)
    {
        privateRequestEditEvent = value;
    }

    public static EventDefinition RequestGOToEventsTabEventDefinition;
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

    private UICommand privateInstallCommand;

    public UICommand getInstallCommand()
    {
        return privateInstallCommand;
    }

    private void setInstallCommand(UICommand value)
    {
        privateInstallCommand = value;
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
            || !vds.getstatus().equals(getEntity().getstatus());

        super.setEntity(value);
    }

    private String os;

    public String getOS()
    {
        return os;
    }

    public void setOS(String value)
    {
        if (!StringHelper.stringsEqual(os, value))
        {
            os = value;
            OnPropertyChanged(new PropertyChangedEventArgs("OS")); //$NON-NLS-1$
        }
    }

    private String kernelVersion;

    public String getKernelVersion()
    {
        return kernelVersion;
    }

    public void setKernelVersion(String value)
    {
        if (!StringHelper.stringsEqual(kernelVersion, value))
        {
            kernelVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("KernelVersion")); //$NON-NLS-1$
        }
    }

    private String kvmVersion;

    public String getKvmVersion()
    {
        return kvmVersion;
    }

    public void setKvmVersion(String value)
    {
        if (!StringHelper.stringsEqual(kvmVersion, value))
        {
            kvmVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("KvmVersion")); //$NON-NLS-1$
        }
    }

    private String libvirtVersion;

    public String getLibvirtVersion() {
        return libvirtVersion;
    }

    public void setLibvirtVersion(String value) {
        if (libvirtVersion == null && value == null) {
            return;
        }

        if(libvirtVersion == null || !libvirtVersion.equals(value)) {
            libvirtVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LibvirtVersion")); //$NON-NLS-1$
        }
    }

    private RpmVersion vdsmVersion;

    public RpmVersion getVdsmVersion()
    {
        return vdsmVersion;
    }

    public void setVdsmVersion(RpmVersion value)
    {
        if (Version.OpInequality(vdsmVersion, value))
        {
            vdsmVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("VdsmVersion")); //$NON-NLS-1$
        }
    }

    private String spiceVersion;

    public String getSpiceVersion()
    {
        return spiceVersion;
    }

    public void setSpiceVersion(String value)
    {
        if (!StringHelper.stringsEqual(spiceVersion, value))
        {
            spiceVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("SpiceVersion")); //$NON-NLS-1$
        }
    }

    private String iScsiInitiatorName;

    public String getIScsiInitiatorName()
    {
        return iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value)
    {
        if (!StringHelper.stringsEqual(iScsiInitiatorName, value))
        {
            iScsiInitiatorName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IScsiInitiatorName")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("ActiveVms")); //$NON-NLS-1$
        }
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
            OnPropertyChanged(new PropertyChangedEventArgs("MemoryPageSharing")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("AutomaticLargePage")); //$NON-NLS-1$
        }
    }

    private Integer numberOfCPUs;

    public Integer getNumberOfCPUs()
    {
        return numberOfCPUs;
    }

    public void setNumberOfCPUs(Integer value)
    {
        if (numberOfCPUs == null && value == null)
        {
            return;
        }
        if (numberOfCPUs == null || !numberOfCPUs.equals(value))
        {
            numberOfCPUs = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NumberOfCPUs")); //$NON-NLS-1$
        }
    }

    private String cpuName;

    public String getCpuName()
    {
        return cpuName;
    }

    public void setCpuName(String value)
    {
        if (!StringHelper.stringsEqual(cpuName, value))
        {
            cpuName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CpuName")); //$NON-NLS-1$
        }
    }

    private String cpuType;

    public String getCpuType()
    {
        return cpuType;
    }

    public void setCpuType(String value)
    {
        if (!StringHelper.stringsEqual(cpuType, value))
        {
            cpuType = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CpuType")); //$NON-NLS-1$
        }
    }

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
            OnPropertyChanged(new PropertyChangedEventArgs("SharedMemory")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("PhysicalMemory")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("SwapTotal")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("SwapFree")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("FreeMemory")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("UsedMemory")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("UsedSwap")); //$NON-NLS-1$
        }
    }

    private Float maxSchedulingMemory;

    public Float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

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
            OnPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasUpgradeAlert")); //$NON-NLS-1$
        }
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasManualFenceAlert")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasNoPowerManagementAlert")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertNonResponsive")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertInstallFailed")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertMaintenance")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasNICsAlert")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("NonOperationalReasonEntity")); //$NON-NLS-1$
        }
    }

    static
    {
        RequestEditEventDefinition = new EventDefinition("RequestEditEvent", HostGeneralModel.class); //$NON-NLS-1$
        RequestGOToEventsTabEventDefinition = new EventDefinition("RequestGOToEventsTabEvent", HostGeneralModel.class); //$NON-NLS-1$
    }

    public HostGeneralModel()
    {
        setRequestEditEvent(new Event(RequestEditEventDefinition));
        setRequestGOToEventsTabEvent(new Event(RequestGOToEventsTabEventDefinition));
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$

        setSaveNICsConfigCommand(new UICommand("SaveNICsConfig", this)); //$NON-NLS-1$
        setInstallCommand(new UICommand("Install", this)); //$NON-NLS-1$
        setEditHostCommand(new UICommand("EditHost", this)); //$NON-NLS-1$
        setGoToEventsCommand(new UICommand("GoToEvents", this)); //$NON-NLS-1$
    }

    public void SaveNICsConfig()
    {
        Frontend.RunMultipleAction(VdcActionType.CommitNetworkChanges,
                new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] {new VdsActionParameters(getEntity().getId())})),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                    }
                },
                null);
    }

    public void Install() {

        if (getWindow() != null) {
            return;
        }

        InstallModel model = new InstallModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().installHostTitle());
        model.setHashName("install_host"); //$NON-NLS-1$
        model.getOVirtISO().setIsAvailable(false);
        model.getRootPassword().setIsAvailable(false);
        model.getOverrideIpTables().setIsAvailable(false);

        model.getHostVersion().setEntity(getEntity().gethost_os());
        model.getHostVersion().setIsAvailable(false);

        getWindow().StartProgress(null);
        if (getEntity().getvds_type() == VDSType.oVirtNode) {
            AsyncDataProvider.GetoVirtISOsList(new AsyncQuery(model,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            InstallModel model = (InstallModel) target;

                            ArrayList<RpmVersion> isos = (ArrayList<RpmVersion>) returnValue;
                            isos = Linq.OrderByDescending(isos, new Linq.RpmVersionComparer());
                            model.getOVirtISO().setItems(isos);
                            model.getOVirtISO().setSelectedItem(Linq.FirstOrDefault(isos));
                            model.getOVirtISO().setIsAvailable(true);
                            model.getOVirtISO().setIsChangable(!isos.isEmpty());
                            model.getHostVersion().setIsAvailable(true);

                            if (isos.isEmpty()) {
                                model.setMessage(ConstantsManager.getInstance()
                                        .getConstants()
                                        .thereAreNoISOversionsVompatibleWithHostCurrentVerMsg());
                            }

                            AddInstallCommands(model, isos.isEmpty());
                            getWindow().StopProgress();
                        }
                    }),
                    getEntity().getId());
        } else {
            model.getRootPassword().setIsAvailable(true);
            model.getRootPassword().setIsChangable(true);

            Version v3 = new Version(3, 0);
            boolean isLessThan3 = getEntity().getvds_group_compatibility_version().compareTo(v3) < 0;

            if (!isLessThan3) {
                model.getOverrideIpTables().setIsAvailable(true);
                model.getOverrideIpTables().setEntity(true);
            }

            AddInstallCommands(model, false);
            getWindow().StopProgress();
        }
    }

    private void AddInstallCommands(InstallModel model, boolean isOnlyClose) {

        if (!isOnlyClose) {

            UICommand command = new UICommand("OnInstall", this); //$NON-NLS-1$
            command.setTitle(ConstantsManager.getInstance().getConstants().ok());
            command.setIsDefault(true);
            model.getCommands().add(command);
        }

        UICommand command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(isOnlyClose ? ConstantsManager.getInstance().getConstants().close()
                : ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    public void EditHost()
    {
        // Let's the parent model know about request.
        getRequestEditEvent().raise(this, EventArgs.Empty);
    }

    public void OnInstall()
    {
        InstallModel model = (InstallModel) getWindow();
        boolean isOVirt = getEntity().getvds_type() == VDSType.oVirtNode;

        if (!model.Validate(isOVirt))
        {
            return;
        }

        UpdateVdsActionParameters param = new UpdateVdsActionParameters();
        param.setvds(getEntity());
        param.setVdsId(getEntity().getId());
        param.setRootPassword((String) model.getRootPassword().getEntity());
        param.setIsReinstallOrUpgrade(true);
        param.setInstallVds(true);
        param.setoVirtIsoFile(isOVirt ? ((RpmVersion) model.getOVirtISO().getSelectedItem()).getRpmName() : null);
        param.setOverrideFirewall((Boolean) model.getOverrideIpTables().getEntity());

        Frontend.RunAction(
                VdcActionType.UpdateVds,
                param,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        if (returnValue != null && returnValue.getSucceeded()) {
                            Cancel();
                        }
                    }
                }
                );
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            UpdateAlerts();
            UpdateMemory();
            UpdateSwapUsed();
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("net_config_dirty") || e.PropertyName.equals("status") //$NON-NLS-1$ //$NON-NLS-2$
                || e.PropertyName.equals("spm_status") || e.PropertyName.equals("vm_active")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateUpgradeAlert = true;
            UpdateAlerts();
        }

        if (e.PropertyName.equals("usage_mem_percent") || e.PropertyName.equals("physical_mem_mb")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            UpdateMemory();
        }

        if (e.PropertyName.equals("swap_total") || e.PropertyName.equals("swap_free")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            UpdateSwapUsed();
        }
    }

    private void UpdateProperties()
    {
        VDS vds = getEntity();

        setOS(vds.gethost_os());
        setKernelVersion(vds.getkernel_version());
        setKvmVersion(vds.getkvm_version());
        setLibvirtVersion(vds.getlibvirt_version());
        setVdsmVersion(vds.getVersion());
        setSpiceVersion(vds.getspice_version());
        setIScsiInitiatorName(vds.getIScsiInitiatorName());
        setActiveVms(vds.getvm_active());
        setMemoryPageSharing(vds.getksm_state());
        setAutomaticLargePage(vds.getTransparentHugePagesState());
        setNumberOfCPUs(vds.getcpu_cores());
        setCpuName(vds.getCpuName() != null ? vds.getCpuName().getCpuName() : null);
        setCpuType(vds.getcpu_model());
        setSharedMemory(vds.getmem_shared_percent());
        setPhysicalMemory(vds.getphysical_mem_mb());
        setSwapTotal(vds.getswap_total());
        setSwapFree(vds.getswap_free());
    }

    private void UpdateAlerts()
    {
        setHasAnyAlert(false);

        if (updateUpgradeAlert) {
            setHasUpgradeAlert(false);
        }

        setHasManualFenceAlert(false);
        setHasNoPowerManagementAlert(false);
        setHasReinstallAlertNonResponsive(false);
        setHasReinstallAlertInstallFailed(false);
        setHasReinstallAlertMaintenance(false);
        setHasNICsAlert(false);
        getInstallCommand().setIsExecutionAllowed(true);
        getEditHostCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(new ArrayList<VDS>(Arrays.asList(new VDS[] { getEntity() })),
                VDS.class,
                VdcActionType.UpdateVds));
        // Check the network alert presense.
        setHasNICsAlert((getEntity().getnet_config_dirty() == null ? false : getEntity().getnet_config_dirty()));

        // Check manual fence alert presense.
        if (getEntity().getstatus() == VDSStatus.NonResponsive
                && !getEntity().getpm_enabled()
                && ((getEntity().getvm_active() == null ? 0 : getEntity().getvm_active()) > 0 || getEntity().getspm_status() == VdsSpmStatus.SPM))
        {
            setHasManualFenceAlert(true);
        }
        else if (!getEntity().getpm_enabled())
        {
            setHasNoPowerManagementAlert(true);
        }

        // Check the reinstall alert presense.
        if (getEntity().getstatus() == VDSStatus.NonResponsive)
        {
            setHasReinstallAlertNonResponsive(true);
        }
        else if (getEntity().getstatus() == VDSStatus.InstallFailed)
        {
            setHasReinstallAlertInstallFailed(true);
        }
        else if (getEntity().getstatus() == VDSStatus.Maintenance)
        {
            setHasReinstallAlertMaintenance(true);
        }

        // TODO: Need to come up with a logic to show the Upgrade action-item.
        // Currently, this action-item will be shown for all oVirts assuming there are
        // available oVirt ISOs that are returned by the backend's GetoVirtISOs query.
        else if (getEntity().getvds_type() == VDSType.oVirtNode && updateUpgradeAlert)
        {
            AsyncDataProvider.GetoVirtISOsList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            HostGeneralModel hostGeneralModel = (HostGeneralModel) target;
                            ArrayList<RpmVersion> isos = (ArrayList<RpmVersion>) returnValue;
                            if (isos.size() > 0)
                            {
                                VDS vds = hostGeneralModel.getEntity();
                                hostGeneralModel.setHasUpgradeAlert(true);

                                boolean executionAllowed = vds.getstatus() != VDSStatus.Up
                                    && vds.getstatus() != VDSStatus.Installing
                                    && vds.getstatus() != VDSStatus.PreparingForMaintenance
                                    && vds.getstatus() != VDSStatus.Reboot
                                    && vds.getstatus() != VDSStatus.PendingApproval;

                                if (!executionAllowed)
                                {
                                    hostGeneralModel.getInstallCommand()
                                            .getExecuteProhibitionReasons()
                                            .add(ConstantsManager.getInstance()
                                                    .getConstants()
                                                    .switchToMaintenanceModeToEnableUpgradeReason());
                                }
                                hostGeneralModel.getInstallCommand().setIsExecutionAllowed(executionAllowed);
                            }

                        }
                    }),
                    getEntity().getId());
        }

        setNonOperationalReasonEntity((getEntity().getNonOperationalReason() == NonOperationalReason.NONE ? null
                : (NonOperationalReason) getEntity().getNonOperationalReason()));

        setHasAnyAlert(getHasNICsAlert() || getHasUpgradeAlert() || getHasManualFenceAlert()
                || getHasNoPowerManagementAlert() || getHasReinstallAlertNonResponsive()
                || getHasReinstallAlertInstallFailed() || getHasReinstallAlertMaintenance());
    }

    private void GoToEvents()
    {
        this.getRequestGOToEventsTabEvent().raise(this, null);
    }

    private void UpdateMemory()
    {
        setFreeMemory(null);
        setUsedMemory(null);
        if (getEntity().getphysical_mem_mb() != null && getEntity().getusage_mem_percent() != null)
        {
            setUsedMemory((int) Math.round(getEntity().getphysical_mem_mb() * (getEntity().getusage_mem_percent() / 100.0)));
            setFreeMemory(getEntity().getphysical_mem_mb() - getUsedMemory());
        }
    }

    private void UpdateSwapUsed()
    {
        setUsedSwap(null);
        if (getEntity().getswap_total() != null && getEntity().getswap_free() != null)
        {
            setUsedSwap(getEntity().getswap_total() - getEntity().getswap_free());
        }
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getSaveNICsConfigCommand())
        {
            SaveNICsConfig();
        }
        else if (command == getInstallCommand())
        {
            Install();
        }
        else if (command == getEditHostCommand())
        {
            EditHost();
        }
        else if (command == getGoToEventsCommand())
        {
            GoToEvents();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnInstall")) //$NON-NLS-1$
        {
            OnInstall();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }
}
