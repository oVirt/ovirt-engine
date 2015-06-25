package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

@SuppressWarnings("unused")
public class VmGeneralModel extends AbstractGeneralModel<VM> {

    private static final VmTemplateNameRenderer vmTemplateNameRenderer = new VmTemplateNameRenderer();

    final UIConstants constants = ConstantsManager.getInstance().getConstants();

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
        setDescription(vm.getVmDescription());
        setQuotaName(vm.getQuotaName() != null ? vm.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(vm.getQuotaEnforcementType() != null
                && !vm.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));

        setTemplate(vmTemplateNameRenderer.render(vm));

        setDefinedMemory(vm.getVmMemSizeMb() + " MB"); //$NON-NLS-1$
        setMinAllocatedMemory(vm.getMinAllocatedMem() + " MB"); //$NON-NLS-1$

        if(vm.isRunningOrPaused() && vm.getGuestMemoryBuffered() != null && vm.getGuestMemoryCached() != null  && vm.getGuestMemoryFree() != null) {
            setGuestFreeCachedBufferedMemInfo((vm.getGuestMemoryFree() / 1024L) + " / " // $NON-NLS-1$
                                            + (vm.getGuestMemoryBuffered() / 1024L)  + " / " // $NON-NLS-1$
                                            + (vm.getGuestMemoryCached() / 1024L) + " MB"); //$NON-NLS-1$
        }
        else {
            setGuestFreeCachedBufferedMemInfo(null); // Handled in form
        }

        setOS(AsyncDataProvider.getInstance().getOsName(vm.getVmOsId()));

        EnumTranslator translator = EnumTranslator.getInstance();
        setDefaultDisplayType(translator.translate(vm.getDefaultDisplayType()));

        setOrigin(translator.translate(vm.getOrigin()));

        setIsHighlyAvailable(vm.isAutoStartup());

        setPriority(AsyncDataProvider.getInstance().priorityToString(vm.getPriority()));

        setMonitorCount(vm.getNumOfMonitors());

        setUsbPolicy(translator.translate(vm.getUsbPolicy()));

        setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                vm.getNumOfCpus(),
                vm.getNumOfSockets(),
                vm.getCpuPerSocket(),
                vm.getThreadsPerCpu()));

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
        setFqdn(vm.getVmFQDN());

        setHasAlert(vm.getVmPauseStatus() != VmPauseStatus.NONE && vm.getVmPauseStatus() != VmPauseStatus.NOERR);
        if (getHasAlert()) {
            setAlert(translator.translate(vm.getVmPauseStatus()));
        }
        else {
            setAlert(null);
        }

        setHasDefaultHost(vm.getDedicatedVmForVdsList().size() > 0);
        if (getHasDefaultHost()) {
            Frontend.getInstance().runQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + vm.getClusterName() //$NON-NLS-1$
                    + " sortby name", SearchType.VDS).withoutRefresh(), new AsyncQuery(this, //$NON-NLS-1$
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            VmGeneralModel model = (VmGeneralModel) target;
                            VM localVm = model.getEntity();
                            if (localVm == null) {
                                return;
                            }
                            ArrayList<VDS> hosts = ((VdcQueryReturnValue) returnValue).getReturnValue();
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
                                model.setDefaultHost(defaultHost);
                            }

                        }
                    }));
        }
        else {
            setDefaultHost(ConstantsManager.getInstance().getConstants().anyHostInCluster());
        }
    }
}
