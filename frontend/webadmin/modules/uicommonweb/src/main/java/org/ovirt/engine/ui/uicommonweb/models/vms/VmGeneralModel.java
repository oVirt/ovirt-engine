package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmGeneralModel extends AbstractGeneralModel<VM> {

    public static final String HAS_HOST_PASSTHROUGH_PROPERTY_CHANGE = "HasHostPassthrough";//$NON-NLS-1$

    public static final String HAS_CUSTOM_CPU_PROPERTY_CHANGE = "HasCustomCpuType";//$NON-NLS-1$

    public static final String GUEST_CPU_TYPE_PROPERTY_CHANGE = "GuestCpuType";//$NON-NLS-1$

    public static final String CONFIGURED_CPU_TYPE_PROPERTY_CHANGE = "ConfiguredCpuType";//$NON-NLS-1$

    public static final String IS_HOSTED_ENGINE = "IsHostedEngine";//$NON-NLS-1$

    public static final String STATUS = "Status";//$NON-NLS-1$

    public static final String ARCHITECTURE = "VmArchitecture";//$NON-NLS-1$

    public static final String BIOS_TYPE = "VmBiosType";//$NON-NLS-1$

    private static final VmTemplateNameRenderer vmTemplateNameRenderer = new VmTemplateNameRenderer();

    final UIConstants constants = ConstantsManager.getInstance().getConstants();
    final UIMessages messages = ConstantsManager.getInstance().getMessages();

    public static final EventDefinition updateCompleteEventDefinition;
    private Event<EventArgs> privateUpdateCompleteEvent;

    public Event<EventArgs> getUpdateCompleteEvent() {
        return privateUpdateCompleteEvent;
    }

    private void setUpdateCompleteEvent(Event<EventArgs> value) {
        privateUpdateCompleteEvent = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!Objects.equals(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private VMStatus status;

    public VMStatus getStatus() {
        return status;
    }

    public void setStatus(VMStatus value) {
        if (!Objects.equals(status, value)) {
            status = value;
            onPropertyChanged(new PropertyChangedEventArgs(STATUS)); //$NON-NLS-1$
        }
    }

    private Double uptime;

    public Double getUptime() {
        return uptime;
    }

    public void setUptime(Double value) {
        if (!Objects.equals(uptime, value)) {
            uptime = value;
            onPropertyChanged(new PropertyChangedEventArgs("Uptime")); //$NON-NLS-1$
        }
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private String template;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String value) {
        if (!Objects.equals(template, value)) {
            template = value;
            onPropertyChanged(new PropertyChangedEventArgs("Template")); //$NON-NLS-1$
        }
    }

    private String definedMemory;

    public String getDefinedMemory() {
        return definedMemory;
    }

    public void setDefinedMemory(String value) {
        if (!Objects.equals(definedMemory, value)) {
            definedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefinedMemory")); //$NON-NLS-1$
        }
    }

    private String guestFreeCachedBufferedMemInfo;

    public String getGuestFreeCachedBufferedMemInfo() {
        return guestFreeCachedBufferedMemInfo;
    }

    public void setGuestFreeCachedBufferedMemInfo(String value) {
        if (!Objects.equals(guestFreeCachedBufferedMemInfo, value)) {
            guestFreeCachedBufferedMemInfo = value;
            onPropertyChanged(new PropertyChangedEventArgs("GuestFreeCachedBufferedMemInfo")); //$NON-NLS-1$
        }
    }

    private String guestFreeCachedBufferedCombinedMemInfo;

    public String getGuestFreeCachedBufferedCombinedMemInfo() {
        return guestFreeCachedBufferedCombinedMemInfo;
    }

    public void setGuestFreeCachedBufferedCombinedMemInfo(String value) {
        if (!Objects.equals(guestFreeCachedBufferedCombinedMemInfo, value)) {
            guestFreeCachedBufferedCombinedMemInfo = value;
            onPropertyChanged(new PropertyChangedEventArgs("GuestFreeCachedBufferedCombinedMemInfo")); //$NON-NLS-1$
        }
    }

    private boolean guestMemInfoUsingUnusedMem;

    public boolean isGuestMemInfoUsingUnusedMem() {
        return guestMemInfoUsingUnusedMem;
    }

    public void setGuestMemInfoUsingUnusedMem(boolean value) {
        guestMemInfoUsingUnusedMem = value;
    }

    private String minAllocatedMemory;

    public String getMinAllocatedMemory() {
        return minAllocatedMemory;
    }

    public void setMinAllocatedMemory(String value) {
        if (!Objects.equals(minAllocatedMemory, value)) {
            minAllocatedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("MinAllocatedMemory")); //$NON-NLS-1$
        }
    }

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

    private ArchitectureType architecture;

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    public void setArchitecture(ArchitectureType value) {
        if (!Objects.equals(architecture, value)) {
            architecture = value;
            onPropertyChanged(new PropertyChangedEventArgs(ARCHITECTURE));
        }
    }

    private BiosType biosType;

    public BiosType getBiosType() {
        return biosType;
    }

    public void setBiosType(BiosType value) {
        if (!Objects.equals(biosType, value)) {
            biosType = value;
            onPropertyChanged(new PropertyChangedEventArgs(BIOS_TYPE));
        }
    }

    private String defaultDisplayType;

    public String getDefaultDisplayType() {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(String value) {
        if (!Objects.equals(defaultDisplayType, value)) {
            defaultDisplayType = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType")); //$NON-NLS-1$
        }
    }

    private String origin;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String value) {
        if (!Objects.equals(origin, value)) {
            origin = value;
            onPropertyChanged(new PropertyChangedEventArgs("Origin")); //$NON-NLS-1$
        }
    }

    private String quotaName;

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    private boolean quotaAvailable;

    public boolean isQuotaAvailable() {
        return quotaAvailable;
    }

    public void setQuotaAvailable(boolean quotaAvailable) {
        this.quotaAvailable = quotaAvailable;
    }

    private int monitorCount;

    public int getMonitorCount() {
        return monitorCount;
    }

    public void setMonitorCount(int value) {
        if (monitorCount != value) {
            monitorCount = value;
            onPropertyChanged(new PropertyChangedEventArgs("MonitorCount")); //$NON-NLS-1$
        }
    }

    private boolean hasDomain;

    public boolean getHasDomain() {
        return hasDomain;
    }

    public void setHasDomain(boolean value) {
        if (hasDomain != value) {
            hasDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasDomain")); //$NON-NLS-1$
        }
    }

    private boolean hasTimeZone;

    public boolean getHasTimeZone() {
        return hasTimeZone;
    }

    public void setHasTimeZone(boolean value) {
        if (hasTimeZone != value) {
            hasTimeZone = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasTimeZone")); //$NON-NLS-1$
        }
    }

    private String usbPolicy;

    public String getUsbPolicy() {
        return usbPolicy;
    }

    public void setUsbPolicy(String value) {
        if (!Objects.equals(usbPolicy, value)) {
            usbPolicy = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsbPolicy")); //$NON-NLS-1$
        }
    }

    private String cpuInfo;

    public String getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(String value) {
        if (!Objects.equals(cpuInfo, value)) {
            cpuInfo = value;
            onPropertyChanged(new PropertyChangedEventArgs("CpuInfo")); //$NON-NLS-1$
        }
    }

    private int guestCpuCount;

    public String getGuestCpuCount() {
        if (guestCpuCount > 0) {
            return String.valueOf(guestCpuCount);
        } else {
            return ConstantsManager.getInstance().getConstants().notAvailableLabel();
        }
    }

    public void setGuestCpuCount(int value) {
        guestCpuCount = value;
    }

    private boolean isHighlyAvailable;

    public boolean getIsHighlyAvailable() {
        return isHighlyAvailable;
    }

    public void setIsHighlyAvailable(boolean value) {
        if (isHighlyAvailable != value) {
            isHighlyAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHighlyAvailable")); //$NON-NLS-1$
        }
    }

    private String priority;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String value) {
        if (!Objects.equals(priority, value)) {
            priority = value;
            onPropertyChanged(new PropertyChangedEventArgs("Priority")); //$NON-NLS-1$
        }
    }

    private String optimizedForSystemProfile;

    public String getOptimizedForSystemProfile() {
        return optimizedForSystemProfile;
    }

    public void setOptimizedForSystemProfile(String value) {
        if (!Objects.equals(optimizedForSystemProfile, value)) {
            optimizedForSystemProfile = value;
            onPropertyChanged(new PropertyChangedEventArgs("OptimizedForSystemProfile")); //$NON-NLS-1$
        }
    }

    private boolean hasAlert;

    public boolean getHasAlert() {
        return hasAlert;
    }

    public void setHasAlert(boolean value) {
        if (hasAlert != value) {
            hasAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAlert")); //$NON-NLS-1$
        }
    }

    private String alert;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String value) {
        if (!Objects.equals(alert, value)) {
            alert = value;
            onPropertyChanged(new PropertyChangedEventArgs("Alert")); //$NON-NLS-1$
        }
    }

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        if (!Objects.equals(domain, value)) {
            domain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    private String storageDomain;

    public String getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(String value) {
        if (!Objects.equals(storageDomain, value)) {
            storageDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("StorageDomain")); //$NON-NLS-1$
        }
    }

    private String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String value) {
        if (!Objects.equals(timeZone, value)) {
            timeZone = value;
            onPropertyChanged(new PropertyChangedEventArgs("TimeZone")); //$NON-NLS-1$
        }
    }

    private boolean hasDefaultHost;

    public boolean getHasDefaultHost() {
        return hasDefaultHost;
    }

    public void setHasDefaultHost(boolean value) {
        if (hasDefaultHost != value) {
            hasDefaultHost = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasDefaultHost")); //$NON-NLS-1$
        }
    }

    private String defaultHost;

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String value) {
        if (!Objects.equals(defaultHost, value)) {
            defaultHost = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultHost")); //$NON-NLS-1$
        }
    }

    private boolean hasCustomProperties;

    public boolean getHasCustomProperties() {
        return hasCustomProperties;
    }

    public void setHasCustomProperties(boolean value) {
        if (hasCustomProperties != value) {
            hasCustomProperties = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasCustomProperties")); //$NON-NLS-1$
        }
    }

    private String customProperties;

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String value) {
        if (!Objects.equals(customProperties, value)) {
            customProperties = value;
            onPropertyChanged(new PropertyChangedEventArgs("CustomProperties")); //$NON-NLS-1$
        }
    }

    private String vmId;

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String value) {
        if (!Objects.equals(vmId, value)) {
            vmId = value;
            onPropertyChanged(new PropertyChangedEventArgs("VmId")); //$NON-NLS-1$
        }
    }

    private String fqdn;

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String value) {
        if (!Objects.equals(fqdn, value)) {
            fqdn = value;
            onPropertyChanged(new PropertyChangedEventArgs("FQDN")); //$NON-NLS-1$
        }
    }

    private String compatibilityVersion;

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String value) {
        if (!Objects.equals(compatibilityVersion, value)) {
            compatibilityVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("CompatibilityVersion")); //$NON-NLS-1$
        }
    }

    private boolean hasCreatedByUser;

    public boolean getHasCreatedByUser() {
        return hasCreatedByUser;
    }

    public void setHasCreatedByUser(boolean value) {
        if (hasCreatedByUser != value) {
            hasCreatedByUser = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasCreatedByUser")); //$NON-NLS-1$
        }
    }

    private String createdByUser;

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String value) {
        if (!Objects.equals(createdByUser, value)) {
            createdByUser = value;
            onPropertyChanged(new PropertyChangedEventArgs("CreatedByUser")); //$NON-NLS-1$
        }
    }

    private boolean hasHostPassthrough;

    public boolean hasHostPassthrough() {
        return hasHostPassthrough;
    }

    public void setHasHostPassthrough(boolean value) {
        if (hasHostPassthrough != value) {
            hasHostPassthrough = value;
            onPropertyChanged(new PropertyChangedEventArgs(HAS_HOST_PASSTHROUGH_PROPERTY_CHANGE));
        }
    }

    private boolean hasCustomCpuType;

    public boolean hasCustomCpuType() {
        return hasCustomCpuType;
    }

    public void setHasCustomCpuType(boolean value) {
        if (hasCustomCpuType != value) {
            hasCustomCpuType = value;
            onPropertyChanged(new PropertyChangedEventArgs(HAS_CUSTOM_CPU_PROPERTY_CHANGE));
        }
    }

    private String guestCpuType;

    public String getGuestCpuType() {
        return guestCpuType != null
                ? guestCpuType
                : ConstantsManager.getInstance().getConstants().notAvailableLabel();
    }

    public void setGuestCpuType(String value) {
        if (!Objects.equals(guestCpuType, value)) {
            guestCpuType = value;
            onPropertyChanged(new PropertyChangedEventArgs(GUEST_CPU_TYPE_PROPERTY_CHANGE));
        }
    }

    private String configuredCpuType;

    public String getConfiguredCpuType() {
        return configuredCpuType;
    }

    public void setConfiguredCpuType(String value) {
        if (!Objects.equals(configuredCpuType, value)) {
            configuredCpuType = value;
            onPropertyChanged(new PropertyChangedEventArgs(CONFIGURED_CPU_TYPE_PROPERTY_CHANGE));
        }
    }

    private boolean hostedEngine;

    public boolean isHostedEngine() {
        return hostedEngine;
    }

    public void setHostedEngine(boolean value) {
        if (!Objects.equals(hostedEngine, value)) {
            hostedEngine = value;
            onPropertyChanged(new PropertyChangedEventArgs(IS_HOSTED_ENGINE));
        }
    }

    static {
        updateCompleteEventDefinition = new EventDefinition("UpdateComplete", VmGeneralModel.class); //$NON-NLS-1$
    }

    public VmGeneralModel() {
        setUpdateCompleteEvent(new Event<>(updateCompleteEventDefinition));

        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        VM vm = getEntity();

        super.updateProperties(vm.getId());

        setName(vm.getName());
        setStatus(vm.getStatus());
        setUptime(vm.getElapsedTime());
        setDescription(vm.getVmDescription());
        setQuotaName(vm.getQuotaName() != null ? vm.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(vm.getQuotaEnforcementType() != null
                && !vm.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));

        setTemplate(vmTemplateNameRenderer.render(vm));

        setDefinedMemory(vm.getVmMemSizeMb() + " MB"); //$NON-NLS-1$
        setMinAllocatedMemory(vm.getMinAllocatedMem() + " MB"); //$NON-NLS-1$

        if(vm.isRunningOrPaused() && vm.getGuestMemoryFree() != null) {
            // If old OGA guest memory reported. Zero checks is for windows OGA.
            if (vm.getGuestMemoryBuffered() != null && vm.getGuestMemoryCached() != null && vm.getGuestMemoryBuffered() != 0 && vm.getGuestMemoryCached() != 0) {
                setGuestMemInfoUsingUnusedMem(false);
                setGuestFreeCachedBufferedMemInfo((vm.getGuestMemoryFree() / 1024L) + " / " // $NON-NLS-1$
                                                + (vm.getGuestMemoryBuffered() / 1024L)  + " / " // $NON-NLS-1$
                                                + (vm.getGuestMemoryCached() / 1024L) + " MB"); //$NON-NLS-1$
            } else if (vm.getGuestMemoryUnused() != null && vm.getGuestMemoryUnused() != 0) {
                setGuestMemInfoUsingUnusedMem(true);
                setGuestFreeCachedBufferedCombinedMemInfo((vm.getGuestMemoryFree() / 1024L) + " / " // $NON-NLS-1$
                                                        + ((vm.getGuestMemoryFree() - vm.getGuestMemoryUnused()) / 1024L )
                                                        + " MB"); // $NON-NLS-1$
            } else {
                setGuestFreeCachedBufferedMemInfo(null); // Handled in form
                setGuestFreeCachedBufferedCombinedMemInfo(null); // Handled in form
            }
        }

        setOS(AsyncDataProvider.getInstance().getOsName(vm.getVmOsId()));

        EnumTranslator translator = EnumTranslator.getInstance();

        setArchitecture(vm.getClusterArch());

        setBiosType(vm.getBiosType());

        setDefaultDisplayType(translator.translate(vm.getDefaultDisplayType()));

        setOrigin(translator.translate(vm.getOrigin()));

        setIsHighlyAvailable(vm.isAutoStartup());

        setPriority(AsyncDataProvider.getInstance().priorityToString(vm.getPriority()));

        setOptimizedForSystemProfile(translator.translate(vm.getVmType()));

        setMonitorCount(vm.getNumOfMonitors());

        setUsbPolicy(translator.translate(vm.getUsbPolicy()));

        if (VmCpuCountHelper.isAutoPinning(vm)) {
            if (vm.isRunning() && vm.getCurrentNumOfCpus() > 0) {
                setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                        vm.getCurrentNumOfCpus(),
                        vm.getCurrentSockets(),
                        vm.getCurrentCoresPerSocket(),
                        vm.getCurrentThreadsPerCore()));
            } else {
                setCpuInfo(ConstantsManager.getInstance().getConstants().adjustToHost());
            }
        } else {
            setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                    vm.getNumOfCpus(),
                    vm.getNumOfSockets(),
                    vm.getCpuPerSocket(),
                    vm.getThreadsPerCpu()));
        }

        setGuestCpuCount(vm.getGuestCpuCount());

        setHasDomain(AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId()));
        if (vm.getVmInit() != null) {
            setDomain(vm.getVmInit().getDomain());
        }

        setHasTimeZone(!StringHelper.isNullOrEmpty(vm.getTimeZone()));
        setTimeZone(vm.getTimeZone());

        setHasCustomProperties(!StringHelper.isNullOrEmpty(vm.getCustomProperties()));
        setCustomProperties(getHasCustomProperties() ? constants.configured() : constants.notConfigured());

        setCompatibilityVersion(vm.getCompatibilityVersion() != null
                ? vm.getCompatibilityVersion().toString()
                : ""); //$NON-NLS-1$

        setVmId(vm.getId().toString());
        setFqdn(vm.getFqdn());

        setHasAlert(vm.getVmPauseStatus() != VmPauseStatus.NONE && vm.getVmPauseStatus() != VmPauseStatus.NOERR);
        if (getHasAlert()) {
            setAlert(translator.translate(vm.getVmPauseStatus()));
        } else {
            setAlert(null);
        }

        setHasCreatedByUser(vm.getCreatedByUserId() != null);
        if (getHasCreatedByUser()) {
            Frontend.getInstance().runQuery(QueryType.GetDbUserByUserId, new IdQueryParameters(vm.getCreatedByUserId()),
                    new AsyncQuery<>(new AsyncCallback<QueryReturnValue>() {
                        @Override
                        public void onSuccess(QueryReturnValue result) {
                            DbUser dbUser = result.getReturnValue();
                            if (dbUser != null) {
                                setCreatedByUser(getUserName(dbUser));
                            }
                        }

                        private String getUserName(DbUser dbUser) {
                            if (StringHelper.isNotNullOrEmpty(dbUser.getFirstName()) || StringHelper.isNotNullOrEmpty(dbUser.getLastName())) {
                                return messages.userName(
                                        nullToEmpty(dbUser.getFirstName()),
                                        nullToEmpty(dbUser.getLastName()));
                            }
                            return dbUser.getLoginName();
                        }

                        private String nullToEmpty(String val) {
                            return val == null ? "" : val;
                        }
                    }));
        }

        setHasDefaultHost(vm.getDedicatedVmForVdsList().size() > 0);
        if (getHasDefaultHost()) {
            Frontend.getInstance().runQuery(QueryType.Search, new SearchParameters("Host: cluster = " + vm.getClusterName() //$NON-NLS-1$
                    + " sortby name", SearchType.VDS).withoutRefresh(), new AsyncQuery<QueryReturnValue>(returnValue -> { //$NON-NLS-1$

                        VM localVm = getEntity();
                        if (localVm == null) {
                            return;
                        }
                        ArrayList<VDS> hosts = returnValue.getReturnValue();
                        if (localVm.getDedicatedVmForVdsList().size() > 0) {
                            String defaultHost = "";
                            for (VDS host : hosts) {
                                if (localVm.getDedicatedVmForVdsList().contains(host.getId())) {
                                    if (defaultHost.isEmpty()) {
                                        defaultHost = host.getName();
                                    } else {
                                        defaultHost += ", " + host.getName(); //$NON-NLS-1$
                                    }
                                }
                            }
                            setDefaultHost(defaultHost);
                        }

                    }));
        } else {
            setDefaultHost(ConstantsManager.getInstance().getConstants().anyHostInCluster());
        }

        setHostedEngine(vm.isHostedEngine());
        setHasHostPassthrough(vm.isUseHostCpuFlags());
        setHasCustomCpuType(vm.getCustomCpuName() != null);
        setGuestCpuType(calculateGuestCpuTypeText(vm));
        setConfiguredCpuType(vm.getConfiguredCpuVerb());
    }

    private String calculateGuestCpuTypeText(VM vm) {
        if (vm.isHostedEngine()) {
            return constants.notAvailableLabel();
        }

        if (vm.isUseHostCpuFlags()) {
            if (vm.getCpuName() != null && !vm.getCpuName().isEmpty()) {
                String guestCpuType = vm.getCpuName();

                // if cpu-pass through is enabled then guestCpuType includes a list of cpu flags and supported cpu
                // models reported by the host.
                // Need to filter out all supported CPU models from of the list and leave only all cpu flags that the vm
                // is running with
                guestCpuType = Stream.of(guestCpuType.split(",")) //$NON-NLS-1$
                        .filter(flag -> !flag.contains("model_")) //$NON-NLS-1$
                        .collect(Collectors.joining(", ")); //$NON-NLS-1$
                return guestCpuType;
            } else {
                return constants.cpuPassthrough();
            }
        }

        return vm.getCpuName() != null
                ? vm.getCpuName()
                : (vm.getCustomCpuName() != null
                        ? vm.getCustomCpuName()
                        : vm.getClusterCpuVerb());
    }

    @Override
    public void cleanup() {
        cleanupEvents(getUpdateCompleteEvent());
        super.cleanup();
    }
}
