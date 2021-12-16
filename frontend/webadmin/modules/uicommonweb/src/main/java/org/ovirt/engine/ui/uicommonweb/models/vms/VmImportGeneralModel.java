package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class VmImportGeneralModel extends AbstractGeneralModel<ImportVmData> {
    private static final VmTemplateNameRenderer vmTemplateNameRenderer = new VmTemplateNameRenderer();
    private static EnumTranslator translator = EnumTranslator.getInstance();
    final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private EntityModel<String> name;
    private ListModel<Integer> operatingSystems;
    private String description;
    private String template;
    private String definedMemory;
    private String minAllocatedMemory;
    private String os;
    private String defaultDisplayType;
    private String quotaName;
    private boolean quotaAvailable;
    private int monitorCount;
    private boolean hasDomain;
    private boolean hasTimeZone;
    private String usbPolicy;
    private String cpuInfo;
    private int guestCpuCount;
    private boolean isHighlyAvailable;
    private String priority;
    private boolean hasAlert;
    private String alert;
    private String domain;
    private String storageDomain;
    private String timeZone;
    private boolean hasDefaultHost;
    private String defaultHost;
    private boolean hasCustomProperties;
    private String customProperties;
    private String vmId;
    private String fqdn;
    private String compatibilityVersion;
    private String optimizedForSystemProfile;
    private Map<Guid, String> vmNamesMap;
    private Guid editedVmId;

    private ImportSource source;

    public VmImportGeneralModel() {
        name = new EntityModel<>();
        operatingSystems = new ListModel<>();
        vmNamesMap = new HashMap<>();
        registerNameAndOsListeners();
    }

    public void setSource(ImportSource source) {
        this.source = source;
    }

    public ImportSource getSource() {
        return source;
    }

    @Override public void cleanup() {
        vmNamesMap.clear();
        editedVmId = null;
        super.cleanup();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        if (getEntity() != null) {
            updateProperties();
        }
    }

    public String getOrigin() {
        return translator.translate(getEntity().getVm().getOrigin());
    }

    public ListModel<Integer> getOperatingSystems() {
        return operatingSystems;
    }

    private void updateVmNamesMap(String editedName) {
        if (editedVmId == null) {
            VM vm = getEntity().getVm();
            vmNamesMap.put(vm.getId(), vm.getName());
            return;
        }

        vmNamesMap.put(editedVmId, editedName);
    }

    private void addVmOriginalNameToMapIfMissing() {
        // Add the VM Id with it's original name to the map in case it is missing
        if (!vmNamesMap.containsKey(editedVmId)) {
            vmNamesMap.put(editedVmId, getEntity().getVm().getName());
        }
    }

    private String getVmEditedName() {
        return vmNamesMap.get(editedVmId);
    }

    private void updateProperties() {
        VM vm = getEntity().getVm();
        updateVmNamesMap(getName().getEntity());
        editedVmId = vm.getId();

        super.updateProperties(vm.getId());
        addVmOriginalNameToMapIfMissing();
        getName().setEntity(getVmEditedName());
        getOperatingSystems().setItems(
                AsyncDataProvider.getInstance()
                        .getSupportedOsIds(vm.getClusterArch(), vm.getCompatibilityVersion()));
        setDescription(vm.getVmDescription());
        setQuotaName(vm.getQuotaName() != null ? vm.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(vm.getQuotaEnforcementType() != null
                && !vm.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));

        setTemplate(vmTemplateNameRenderer.render(vm));

        setDefinedMemory(vm.getVmMemSizeMb() + " MB"); //$NON-NLS-1$
        setMinAllocatedMemory(vm.getMinAllocatedMem() + " MB"); //$NON-NLS-1$

        setOS(AsyncDataProvider.getInstance().getOsName(vm.getVmOsId()));

        if (vm.getOrigin() == null || vm.getOrigin() == OriginType.VMWARE) {
            setDefaultDisplayType(translator.translate(null));
        } else {
            setDefaultDisplayType(translator.translate(vm.getDefaultDisplayType()));
        }

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

        setHasTimeZone(AsyncDataProvider.getInstance().isWindowsOsType(vm.getVmOsId()));
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

        setHasDefaultHost(!vm.getDedicatedVmForVdsList().isEmpty());
        if (getHasDefaultHost()) {
            Frontend.getInstance().runQuery(QueryType.Search, new SearchParameters("Host: cluster = " + vm.getClusterName() //$NON-NLS-1$
                    + " sortby name", SearchType.VDS), new AsyncQuery<QueryReturnValue>(returnValue -> { //$NON-NLS-1$
                        VM localVm = getEntity() != null ? getEntity().getVm() : null;
                        if (localVm == null) {
                            return;
                        }
                        ArrayList<VDS> hosts = returnValue.getReturnValue();
                        for (VDS host : hosts) {
                            if (localVm.getDedicatedVmForVdsList().contains(host.getId())) {
                                setDefaultHost(host.getName());
                                break;
                            }
                        }

                    }));
        } else {
            setDefaultHost(ConstantsManager.getInstance().getConstants().anyHostInCluster());
        }

        setOptimizedForSystemProfile(translator.translate(vm.getVmType()));
    }

    public EntityModel<String> getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String value) {
        if (!Objects.equals(template, value)) {
            template = value;
            onPropertyChanged(new PropertyChangedEventArgs("Template")); //$NON-NLS-1$
        }
    }

    public String getDefinedMemory() {
        return definedMemory;
    }

    public void setDefinedMemory(String value) {
        if (!Objects.equals(definedMemory, value)) {
            definedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefinedMemory")); //$NON-NLS-1$
        }
    }

    public String getMinAllocatedMemory() {
        return minAllocatedMemory;
    }

    public void setMinAllocatedMemory(String value) {
        if (!Objects.equals(minAllocatedMemory, value)) {
            minAllocatedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("MinAllocatedMemory")); //$NON-NLS-1$
        }
    }

    public String getOS() {
        return os;
    }

    public void setOS(String value) {
        if (!Objects.equals(os, value)) {
            os = value;
            onPropertyChanged(new PropertyChangedEventArgs("OS")); //$NON-NLS-1$
        }
    }

    private void registerNameAndOsListeners() {
        getName().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getName().getEntity() != null) {
                getEntity().getVm().setName(getName().getEntity());
            }
        });

        getOperatingSystems().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (getOperatingSystems().getSelectedItem() != null) {
                getEntity().getVm().setVmOs(getOperatingSystems().getSelectedItem());
            }
        });
        getOperatingSystems().getItemsChangedEvent().addListener((ev, sender, args) -> getOperatingSystems().setSelectedItem(getEntity().getVm().getOs()));
    }

    public void clearAndRegisterNameAndOsListeners() {
        getName().getEntityChangedEvent().clearListeners();
        getOperatingSystems().getSelectedItemChangedEvent().clearListeners();
        getOperatingSystems().getItemsChangedEvent().clearListeners();

        registerNameAndOsListeners();
    }

    public String getDefaultDisplayType() {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(String value) {
        if (!Objects.equals(defaultDisplayType, value)) {
            defaultDisplayType = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType")); //$NON-NLS-1$
        }
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    public boolean isQuotaAvailable() {
        return quotaAvailable;
    }

    public void setQuotaAvailable(boolean quotaAvailable) {
        this.quotaAvailable = quotaAvailable;
    }

    public int getMonitorCount() {
        return monitorCount;
    }

    public void setMonitorCount(int value) {
        if (monitorCount != value) {
            monitorCount = value;
            onPropertyChanged(new PropertyChangedEventArgs("MonitorCount")); //$NON-NLS-1$
        }
    }

    public boolean getHasDomain() {
        return hasDomain;
    }

    public void setHasDomain(boolean value) {
        if (hasDomain != value) {
            hasDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasDomain")); //$NON-NLS-1$
        }
    }

    public boolean getHasTimeZone() {
        return hasTimeZone;
    }

    public void setHasTimeZone(boolean value) {
        if (hasTimeZone != value) {
            hasTimeZone = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasTimeZone")); //$NON-NLS-1$
        }
    }

    public String getUsbPolicy() {
        return usbPolicy;
    }

    public void setUsbPolicy(String value) {
        if (!Objects.equals(usbPolicy, value)) {
            usbPolicy = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsbPolicy")); //$NON-NLS-1$
        }
    }

    public String getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(String value) {
        if (!Objects.equals(cpuInfo, value)) {
            cpuInfo = value;
            onPropertyChanged(new PropertyChangedEventArgs("CpuInfo")); //$NON-NLS-1$
        }
    }

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

    public boolean getIsHighlyAvailable() {
        return isHighlyAvailable;
    }

    public void setIsHighlyAvailable(boolean value) {
        if (isHighlyAvailable != value) {
            isHighlyAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHighlyAvailable")); //$NON-NLS-1$
        }
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String value) {
        if (!Objects.equals(priority, value)) {
            priority = value;
            onPropertyChanged(new PropertyChangedEventArgs("Priority")); //$NON-NLS-1$
        }
    }

    public boolean getHasAlert() {
        return hasAlert;
    }

    public void setHasAlert(boolean value) {
        if (hasAlert != value) {
            hasAlert = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasAlert")); //$NON-NLS-1$
        }
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String value) {
        if (!Objects.equals(alert, value)) {
            alert = value;
            onPropertyChanged(new PropertyChangedEventArgs("Alert")); //$NON-NLS-1$
        }
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String value) {
        if (!Objects.equals(domain, value)) {
            domain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    public String getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(String value) {
        if (!Objects.equals(storageDomain, value)) {
            storageDomain = value;
            onPropertyChanged(new PropertyChangedEventArgs("StorageDomain")); //$NON-NLS-1$
        }
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String value) {
        if (!Objects.equals(timeZone, value)) {
            timeZone = value;
            onPropertyChanged(new PropertyChangedEventArgs("TimeZone")); //$NON-NLS-1$
        }
    }

    public boolean getHasDefaultHost() {
        return hasDefaultHost;
    }

    public void setHasDefaultHost(boolean value) {
        if (hasDefaultHost != value) {
            hasDefaultHost = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasDefaultHost")); //$NON-NLS-1$
        }
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String value) {
        if (!Objects.equals(defaultHost, value)) {
            defaultHost = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultHost")); //$NON-NLS-1$
        }
    }

    public boolean getHasCustomProperties() {
        return hasCustomProperties;
    }

    public void setHasCustomProperties(boolean value) {
        if (hasCustomProperties != value) {
            hasCustomProperties = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasCustomProperties")); //$NON-NLS-1$
        }
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String value) {
        if (!Objects.equals(customProperties, value)) {
            customProperties = value;
            onPropertyChanged(new PropertyChangedEventArgs("CustomProperties")); //$NON-NLS-1$
        }
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String value) {
        if (!Objects.equals(vmId, value)) {
            vmId = value;
            onPropertyChanged(new PropertyChangedEventArgs("VmId")); //$NON-NLS-1$
        }
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String value) {
        if (!Objects.equals(fqdn, value)) {
            fqdn = value;
            onPropertyChanged(new PropertyChangedEventArgs("FQDN")); //$NON-NLS-1$
        }
    }

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String value) {
        if (!Objects.equals(compatibilityVersion, value)) {
            compatibilityVersion = value;
            onPropertyChanged(new PropertyChangedEventArgs("CompatibilityVersion")); //$NON-NLS-1$
        }
    }

    public String getOptimizedForSystemProfile() {
        return optimizedForSystemProfile;
    }

    public void setOptimizedForSystemProfile(String value) {
        if (!Objects.equals(optimizedForSystemProfile, value)) {
            optimizedForSystemProfile = value;
            onPropertyChanged(new PropertyChangedEventArgs("OptimizedForSystemProfile")); //$NON-NLS-1$
        }
    }
}
