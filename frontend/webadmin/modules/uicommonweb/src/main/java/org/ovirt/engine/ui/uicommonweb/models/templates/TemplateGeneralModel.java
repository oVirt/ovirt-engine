package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class TemplateGeneralModel extends EntityModel {

    @Override
    public VmTemplate getEntity() {
        if (super.getEntity() == null) {
            return null;
        }
        if (super.getEntity() instanceof VmTemplate) {
            return (VmTemplate) super.getEntity();
        } else if (super.getEntity() instanceof ImportTemplateData) {
            return ((ImportTemplateData) super.getEntity()).getTemplate();
        } else {
            Map.Entry<VmTemplate, ArrayList<DiskImage>> pair =
                    (Map.Entry<VmTemplate, ArrayList<DiskImage>>) super.getEntity();
            return pair.getKey();
        }
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!ObjectUtils.objectsEqual(name, value)) {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!ObjectUtils.objectsEqual(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
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

    private String hostCluster;

    public String getHostCluster() {
        return hostCluster;
    }

    public void setHostCluster(String value) {
        if (!ObjectUtils.objectsEqual(hostCluster, value)) {
            hostCluster = value;
            onPropertyChanged(new PropertyChangedEventArgs("HostCluster")); //$NON-NLS-1$
        }
    }

    private String definedMemory;

    public String getDefinedMemory() {
        return definedMemory;
    }

    public void setDefinedMemory(String value) {
        if (!ObjectUtils.objectsEqual(definedMemory, value)) {
            definedMemory = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefinedMemory")); //$NON-NLS-1$
        }
    }

    private String cpuInfo;

    public String getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(String value) {
        if (!ObjectUtils.objectsEqual(cpuInfo, value)) {
            cpuInfo = value;
            onPropertyChanged(new PropertyChangedEventArgs("CpuInfo")); //$NON-NLS-1$
        }
    }

    private String os;

    public String getOS() {
        return os;
    }

    public void setOS(String value) {
        if (!ObjectUtils.objectsEqual(os, value)) {
            os = value;
            onPropertyChanged(new PropertyChangedEventArgs("OS")); //$NON-NLS-1$
        }
    }

    private String defaultDisplayType;

    public String getDefaultDisplayType() {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(String value) {
        if (!ObjectUtils.objectsEqual(defaultDisplayType, value)) {
            defaultDisplayType = value;
            onPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType")); //$NON-NLS-1$
        }
    }

    private String origin;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String value) {
        if (!ObjectUtils.objectsEqual(origin, value)) {
            origin = value;
            onPropertyChanged(new PropertyChangedEventArgs("Origin")); //$NON-NLS-1$
        }
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
        if (!ObjectUtils.objectsEqual(priority, value)) {
            priority = value;
            onPropertyChanged(new PropertyChangedEventArgs("Priority")); //$NON-NLS-1$
        }
    }

    private boolean hasMonitorCount;

    public boolean getHasMonitorCount() {
        return hasMonitorCount;
    }

    public void setHasMonitorCount(boolean value) {
        if (hasMonitorCount != value) {
            hasMonitorCount = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasMonitorCount")); //$NON-NLS-1$
        }
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

    private boolean allowConsoleReconnect;

    public boolean getAllowConsoleReconnect() {
        return allowConsoleReconnect;
    }

    public void setAllowConsoleReconnect(boolean value) {
        if (allowConsoleReconnect != value) {
            allowConsoleReconnect = value;
            onPropertyChanged(new PropertyChangedEventArgs("AllowConsoleReconnect")); //$NON-NLS-1$
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

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        if (!ObjectUtils.objectsEqual(domain, value)) {
            domain = value;
            onPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    private String timeZone;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String value) {
        if (!ObjectUtils.objectsEqual(timeZone, value)) {
            timeZone = value;
            onPropertyChanged(new PropertyChangedEventArgs("TimeZone")); //$NON-NLS-1$
        }
    }

    private boolean hasUsbPolicy;

    public boolean getHasUsbPolicy() {
        return hasUsbPolicy;
    }

    public void setHasUsbPolicy(boolean value) {
        if (hasUsbPolicy != value) {
            hasUsbPolicy = value;
            onPropertyChanged(new PropertyChangedEventArgs("HasUsbPolicy")); //$NON-NLS-1$
        }
    }

    private String usbPolicy;

    public String getUsbPolicy() {
        return usbPolicy;
    }

    public void setUsbPolicy(String value) {
        if (!ObjectUtils.objectsEqual(usbPolicy, value)) {
            usbPolicy = value;
            onPropertyChanged(new PropertyChangedEventArgs("UsbPolicy")); //$NON-NLS-1$
        }
    }

    private boolean isStateless;

    public boolean getIsStateless() {
        return isStateless;
    }

    public void setIsStateless(boolean value) {
        if (isStateless != value) {
            isStateless = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsStateless")); //$NON-NLS-1$
        }
    }

    public TemplateGeneralModel() {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHelpTag(HelpTag.general);
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (super.getEntity() != null) {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        VmTemplate template = getEntity();

        setName(template.getName());
        setDescription(template.getDescription());
        setQuotaName(template.getQuotaName() != null ? template.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(template.getQuotaEnforcementType() != null
                && !template.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));
        setHostCluster(template.getVdsGroupName());
        setDefinedMemory(template.getMemSizeMb() + " MB"); //$NON-NLS-1$
        setIsHighlyAvailable(template.isAutoStartup());
        setPriority(AsyncDataProvider.getInstance().priorityToString(template.getPriority()));
        setMonitorCount(template.getNumOfMonitors());
        setAllowConsoleReconnect(template.isAllowConsoleReconnect());
        setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                template.getNumOfCpus(),
                template.getNumOfSockets(),
                template.getCpuPerSocket()));

        setOS(AsyncDataProvider.getInstance().getOsName(template.getOsId()));

        Translator translator = EnumTranslator.getInstance();
        setDefaultDisplayType(translator.get(template.getDefaultDisplayType()));

        setOrigin(translator.get(template.getOrigin()));

        setHasDomain(AsyncDataProvider.getInstance().isWindowsOsType(template.getOsId()));
        if (template.getVmInit() != null) {
            setDomain(template.getVmInit().getDomain());
        }

        setHasTimeZone(AsyncDataProvider.getInstance().isWindowsOsType(template.getOsId()));
        setTimeZone(template.getTimeZone());

        setHasUsbPolicy(true);
        setUsbPolicy(translator.get(template.getUsbPolicy()));

        setIsStateless(template.isStateless());
    }

}
