package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class TemplateGeneralModel extends EntityModel
{

    @Override
    public VmTemplate getEntity()
    {
        if (super.getEntity() == null)
        {
            return null;
        }
        if (super.getEntity() instanceof VmTemplate)
        {
            return (VmTemplate) super.getEntity();
        }
        else
        {
            Map.Entry<VmTemplate, ArrayList<DiskImage>> pair =
                    (Map.Entry<VmTemplate, ArrayList<DiskImage>>) super.getEntity();
            return pair.getKey();
        }
    }

    public void setEntity(VmTemplate value)
    {
        super.setEntity(value);
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
        }
    }

    private String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(description, value))
        {
            description = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
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

    public String getHostCluster()
    {
        return hostCluster;
    }

    public void setHostCluster(String value)
    {
        if (!StringHelper.stringsEqual(hostCluster, value))
        {
            hostCluster = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HostCluster")); //$NON-NLS-1$
        }
    }

    private String definedMemory;

    public String getDefinedMemory()
    {
        return definedMemory;
    }

    public void setDefinedMemory(String value)
    {
        if (!StringHelper.stringsEqual(definedMemory, value))
        {
            definedMemory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefinedMemory")); //$NON-NLS-1$
        }
    }

    private String cpuInfo;

    public String getCpuInfo()
    {
        return cpuInfo;
    }

    public void setCpuInfo(String value)
    {
        if (!StringHelper.stringsEqual(cpuInfo, value))
        {
            cpuInfo = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CpuInfo")); //$NON-NLS-1$
        }
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

    private String defaultDisplayType;

    public String getDefaultDisplayType()
    {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(String value)
    {
        if (!StringHelper.stringsEqual(defaultDisplayType, value))
        {
            defaultDisplayType = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType")); //$NON-NLS-1$
        }
    }

    private String origin;

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String value)
    {
        if (!StringHelper.stringsEqual(origin, value))
        {
            origin = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Origin")); //$NON-NLS-1$
        }
    }

    private boolean hasHighlyAvailable;

    public boolean getHasHighlyAvailable()
    {
        return hasHighlyAvailable;
    }

    public void setHasHighlyAvailable(boolean value)
    {
        if (hasHighlyAvailable != value)
        {
            hasHighlyAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasHighlyAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isHighlyAvailable;

    public boolean getIsHighlyAvailable()
    {
        return isHighlyAvailable;
    }

    public void setIsHighlyAvailable(boolean value)
    {
        if (isHighlyAvailable != value)
        {
            isHighlyAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsHighlyAvailable")); //$NON-NLS-1$
        }
    }

    private boolean hasPriority;

    public boolean getHasPriority()
    {
        return hasPriority;
    }

    public void setHasPriority(boolean value)
    {
        if (hasPriority != value)
        {
            hasPriority = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasPriority")); //$NON-NLS-1$
        }
    }

    private String priority;

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String value)
    {
        if (!StringHelper.stringsEqual(priority, value))
        {
            priority = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Priority")); //$NON-NLS-1$
        }
    }

    private boolean hasMonitorCount;

    public boolean getHasMonitorCount()
    {
        return hasMonitorCount;
    }

    public void setHasMonitorCount(boolean value)
    {
        if (hasMonitorCount != value)
        {
            hasMonitorCount = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasMonitorCount")); //$NON-NLS-1$
        }
    }

    private int monitorCount;

    public int getMonitorCount()
    {
        return monitorCount;
    }

    public void setMonitorCount(int value)
    {
        if (monitorCount != value)
        {
            monitorCount = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MonitorCount")); //$NON-NLS-1$
        }
    }

    private boolean allowConsoleReconnect;

    public boolean getAllowConsoleReconnect()
    {
        return allowConsoleReconnect;
    }

    public void setAllowConsoleReconnect(boolean value)
    {
        if (allowConsoleReconnect != value)
        {
            allowConsoleReconnect = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AllowConsoleReconnect")); //$NON-NLS-1$
        }
    }

    private boolean hasDomain;

    public boolean getHasDomain()
    {
        return hasDomain;
    }

    public void setHasDomain(boolean value)
    {
        if (hasDomain != value)
        {
            hasDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasDomain")); //$NON-NLS-1$
        }
    }

    private boolean hasTimeZone;

    public boolean getHasTimeZone()
    {
        return hasTimeZone;
    }

    public void setHasTimeZone(boolean value)
    {
        if (hasTimeZone != value)
        {
            hasTimeZone = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasTimeZone")); //$NON-NLS-1$
        }
    }

    private String domain;

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String value)
    {
        if (!StringHelper.stringsEqual(domain, value))
        {
            domain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Domain")); //$NON-NLS-1$
        }
    }

    private String timeZone;

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String value)
    {
        if (!StringHelper.stringsEqual(timeZone, value))
        {
            timeZone = value;
            OnPropertyChanged(new PropertyChangedEventArgs("TimeZone")); //$NON-NLS-1$
        }
    }

    private boolean hasUsbPolicy;

    public boolean getHasUsbPolicy()
    {
        return hasUsbPolicy;
    }

    public void setHasUsbPolicy(boolean value)
    {
        if (hasUsbPolicy != value)
        {
            hasUsbPolicy = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasUsbPolicy")); //$NON-NLS-1$
        }
    }

    private String usbPolicy;

    public String getUsbPolicy()
    {
        return usbPolicy;
    }

    public void setUsbPolicy(String value)
    {
        if (!StringHelper.stringsEqual(usbPolicy, value))
        {
            usbPolicy = value;
            OnPropertyChanged(new PropertyChangedEventArgs("UsbPolicy")); //$NON-NLS-1$
        }
    }

    private boolean isStateless;

    public boolean getIsStateless()
    {
        return isStateless;
    }

    public void setIsStateless(boolean value)
    {
        if (isStateless != value)
        {
            isStateless = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsStateless")); //$NON-NLS-1$
        }
    }

    public TemplateGeneralModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (super.getEntity() != null)
        {
            UpdateProperties();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        UpdateProperties();
    }

    private void UpdateProperties()
    {
        VmTemplate template = getEntity();

        setName(template.getname());
        setDescription(template.getDescription());
        setQuotaName(template.getQuotaName() != null ? template.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(template.getQuotaEnforcementType() != null
                && !template.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));
        setHostCluster(template.getvds_group_name());
        setDefinedMemory(template.getMemSizeMb() + " MB"); //$NON-NLS-1$
        setHasHighlyAvailable(template.getVmType() == VmType.Server);
        setIsHighlyAvailable(template.isAutoStartup());
        setPriority(PriorityToString(template.getPriority()));
        setMonitorCount(template.getNumOfMonitors());
        setAllowConsoleReconnect(template.isAllowConsoleReconnect());
        setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                template.getNumOfCpus(),
                template.getNumOfSockets(),
                template.getCpuPerSocket()));

        Translator translator = EnumTranslator.Create(VmOsType.class);
        setOS(translator.get(template.getOs()));

        translator = EnumTranslator.Create(DisplayType.class);
        setDefaultDisplayType(translator.get(template.getDefaultDisplayType()));

        translator = EnumTranslator.Create(OriginType.class);
        setOrigin(translator.get(template.getOrigin()));

        setHasDomain(AsyncDataProvider.IsWindowsOsType(template.getOs()));
        setDomain(template.getDomain());

        setHasTimeZone(AsyncDataProvider.IsWindowsOsType(template.getOs()));
        setTimeZone(template.getTimeZone());

        setHasUsbPolicy(true);
        translator = EnumTranslator.Create(UsbPolicy.class);
        setUsbPolicy(translator.get(template.getUsbPolicy()));

        setIsStateless(template.isStateless());
    }

    public String PriorityToString(int value)
    {
        String priorityStr;
        int highPriority = AsyncDataProvider.GetMaxVmPriority();
        int roundedPriority = AsyncDataProvider.RoundPriority(value);

        if (roundedPriority == 1)
        {
            priorityStr = "Low"; //$NON-NLS-1$
        }
        else if (roundedPriority == highPriority / 2)
        {
            priorityStr = "Medium"; //$NON-NLS-1$
        }
        else if (roundedPriority == highPriority)
        {
            priorityStr = "High"; //$NON-NLS-1$
        }
        else
        {
            priorityStr = "Unknown"; //$NON-NLS-1$
        }

        return priorityStr;
    }
}
