package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Iterator;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class VmGeneralModel extends EntityModel
{

    public static EventDefinition UpdateCompleteEventDefinition;
    private Event privateUpdateCompleteEvent;

    public Event getUpdateCompleteEvent()
    {
        return privateUpdateCompleteEvent;
    }

    private void setUpdateCompleteEvent(Event value)
    {
        privateUpdateCompleteEvent = value;
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

    private String template;

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String value)
    {
        if (!StringHelper.stringsEqual(template, value))
        {
            template = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Template")); //$NON-NLS-1$
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

    private String minAllocatedMemory;

    public String getMinAllocatedMemory()
    {
        return minAllocatedMemory;
    }

    public void setMinAllocatedMemory(String value)
    {
        if (!StringHelper.stringsEqual(minAllocatedMemory, value))
        {
            minAllocatedMemory = value;
            OnPropertyChanged(new PropertyChangedEventArgs("MinAllocatedMemory")); //$NON-NLS-1$
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

    private boolean hasStorageDomain;

    public boolean getHasStorageDomain()
    {
        return hasStorageDomain;
    }

    public void setHasStorageDomain(boolean value)
    {
        if (hasStorageDomain != value)
        {
            hasStorageDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasStorageDomain")); //$NON-NLS-1$
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

    private boolean hasAlert;

    public boolean getHasAlert()
    {
        return hasAlert;
    }

    public void setHasAlert(boolean value)
    {
        if (hasAlert != value)
        {
            hasAlert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasAlert")); //$NON-NLS-1$
        }
    }

    private String alert;

    public String getAlert()
    {
        return alert;
    }

    public void setAlert(String value)
    {
        if (!StringHelper.stringsEqual(alert, value))
        {
            alert = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Alert")); //$NON-NLS-1$
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

    private String storageDomain;

    public String getStorageDomain()
    {
        return storageDomain;
    }

    public void setStorageDomain(String value)
    {
        if (!StringHelper.stringsEqual(storageDomain, value))
        {
            storageDomain = value;
            OnPropertyChanged(new PropertyChangedEventArgs("StorageDomain")); //$NON-NLS-1$
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

    private boolean hasDefaultHost;

    public boolean getHasDefaultHost()
    {
        return hasDefaultHost;
    }

    public void setHasDefaultHost(boolean value)
    {
        if (hasDefaultHost != value)
        {
            hasDefaultHost = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasDefaultHost")); //$NON-NLS-1$
        }
    }

    private String defaultHost;

    public String getDefaultHost()
    {
        return defaultHost;
    }

    public void setDefaultHost(String value)
    {
        if (!StringHelper.stringsEqual(defaultHost, value))
        {
            defaultHost = value;
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultHost")); //$NON-NLS-1$
        }
    }

    private boolean hasCustomProperties;

    public boolean getHasCustomProperties()
    {
        return hasCustomProperties;
    }

    public void setHasCustomProperties(boolean value)
    {
        if (hasCustomProperties != value)
        {
            hasCustomProperties = value;
            OnPropertyChanged(new PropertyChangedEventArgs("HasCustomProperties")); //$NON-NLS-1$
        }
    }

    private String customProperties;

    public String getCustomProperties()
    {
        return customProperties;
    }

    public void setCustomProperties(String value)
    {
        if (!StringHelper.stringsEqual(customProperties, value))
        {
            customProperties = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CustomProperties")); //$NON-NLS-1$
        }
    }

    private String compatibilityVersion;

    public String getCompatibilityVersion()
    {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String value)
    {
        if (!StringHelper.stringsEqual(compatibilityVersion, value))
        {
            compatibilityVersion = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CompatibilityVersion")); //$NON-NLS-1$
        }
    }

    static
    {
        UpdateCompleteEventDefinition = new EventDefinition("UpdateComplete", VmGeneralModel.class); //$NON-NLS-1$
    }

    public VmGeneralModel()
    {
        setUpdateCompleteEvent(new Event(UpdateCompleteEventDefinition));

        setTitle(ConstantsManager.getInstance().getConstants().generalTitle());
        setHashName("general"); //$NON-NLS-1$
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
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
        VM vm = (VM) getEntity();

        setName(vm.getName());
        setDescription(vm.getVmDescription());
        setQuotaName(vm.getQuotaName() != null ? vm.getQuotaName() : ""); //$NON-NLS-1$
        setQuotaAvailable(vm.getQuotaEnforcementType() != null
                && !vm.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED));
        setTemplate(vm.getVmtName());
        setDefinedMemory(vm.getVmMemSizeMb() + " MB"); //$NON-NLS-1$
        setMinAllocatedMemory(vm.getMinAllocatedMem() + " MB"); //$NON-NLS-1$

        Translator translator = EnumTranslator.Create(VmOsType.class);
        setOS(translator.get(vm.getVmOs()));

        translator = EnumTranslator.Create(DisplayType.class);
        setDefaultDisplayType(translator.get(vm.getDefaultDisplayType()));

        translator = EnumTranslator.Create(OriginType.class);
        setOrigin(translator.get(vm.getOrigin()));

        setHasHighlyAvailable(vm.getVmType() == VmType.Server);
        setIsHighlyAvailable(vm.isAutoStartup());

        setHasPriority(vm.getVmType() == VmType.Server);
        setPriority(PriorityToString(vm.getPriority()));

        setHasMonitorCount(vm.getVmType() == VmType.Desktop);
        setMonitorCount(vm.getNumOfMonitors());

        setHasUsbPolicy(true);
        translator = EnumTranslator.Create(UsbPolicy.class);
        setUsbPolicy(translator.get(vm.getUsbPolicy()));

        setCpuInfo(vm.getNumOfCpus() + " " + "(" + vm.getNumOfSockets() + " Socket(s), " + vm.getCpuPerSocket() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " Core(s) per Socket)"); //$NON-NLS-1$

        setHasDomain(AsyncDataProvider.IsWindowsOsType(vm.getVmOs()));
        setDomain(vm.getVmDomain());

        setHasTimeZone(AsyncDataProvider.IsWindowsOsType(vm.getVmOs()));
        setTimeZone(vm.getTimeZone());

        setHasCustomProperties(!StringHelper.stringsEqual(vm.getCustomProperties(), "")); //$NON-NLS-1$
        setCustomProperties(getHasCustomProperties() ? "Configured" : "Not-Configured"); //$NON-NLS-1$ //$NON-NLS-2$

        setCompatibilityVersion(vm.getVdsGroupCompatibilityVersion() != null ?
                vm.getVdsGroupCompatibilityVersion().toString() : ""); //$NON-NLS-1$

        setHasAlert(vm.getVmPauseStatus() != VmPauseStatus.NONE && vm.getVmPauseStatus() != VmPauseStatus.NOERR);
        if (getHasAlert())
        {
            translator = EnumTranslator.Create(VmPauseStatus.class);
            setAlert(translator.get(vm.getVmPauseStatus()));
        }
        else
        {
            setAlert(null);
        }

        setHasDefaultHost(vm.getDedicatedVmForVds() != null);
        if (getHasDefaultHost())
        {
            Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + vm.getVdsGroupName() //$NON-NLS-1$
                    + " sortby name", SearchType.VDS), new AsyncQuery(this, //$NON-NLS-1$
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            VmGeneralModel model = (VmGeneralModel) target;
                            VM localVm = (VM) model.getEntity();
                            if (localVm == null)
                            {
                                return;
                            }
                            ArrayList<VDS> hosts =
                                    (ArrayList<VDS>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                            for (VDS host : hosts)
                            {
                                if (localVm.getDedicatedVmForVds() != null
                                        && host.getId().equals(localVm.getDedicatedVmForVds()))
                                {
                                    model.setDefaultHost(host.getVdsName());
                                    break;
                                }
                            }

                        }
                    }));
        }
        else
        {
            setDefaultHost("Any Host in Cluster"); //$NON-NLS-1$
        }
    }

    public void UpdateStorageDomain()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                VmGeneralModel vmGeneralModel = (VmGeneralModel) model;
                Iterable disks = (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                Iterator disksIterator = disks.iterator();
                if (disksIterator.hasNext())
                {
                    vmGeneralModel.setHasStorageDomain(true);

                    AsyncQuery _asyncQuery1 = new AsyncQuery();
                    _asyncQuery1.setModel(vmGeneralModel);
                    _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object ReturnValue1)
                        {
                            VmGeneralModel vmGeneralModel1 = (VmGeneralModel) model1;
                            StorageDomain storage =
                                    (StorageDomain) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                            vmGeneralModel1.setStorageDomain(storage.getStorageName());

                            vmGeneralModel1.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                        }
                    };

                    DiskImage firstDisk = (DiskImage) disksIterator.next();
                    StorageDomainQueryParametersBase params =
                            new StorageDomainQueryParametersBase(firstDisk.getStorageIds().get(0));
                    params.setRefresh(false);
                    Frontend.RunQuery(VdcQueryType.GetStorageDomainById, params, _asyncQuery1);
                }
                else
                {
                    vmGeneralModel.setHasStorageDomain(false);

                    vmGeneralModel.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                }
            }
        };

        VM vm = (VM) getEntity();

        GetAllDisksByVmIdParameters params = new GetAllDisksByVmIdParameters(vm.getId());
        params.setRefresh(false);
        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, params, _asyncQuery);
    }

    // TODO: Find a better place for this code. It must be something common,
    // allowing using of converters' code available in UICommon.
    public String PriorityToString(int value)
    {
        int highPriority = AsyncDataProvider.GetMaxVmPriority();
        int roundedPriority = AsyncDataProvider.RoundPriority(value);

        if (roundedPriority == 1)
        {
            return "Low"; //$NON-NLS-1$
        }
        else if (roundedPriority == AsyncDataProvider.GetMaxVmPriority() / 2)
        {
            return "Medium"; //$NON-NLS-1$
        }
        else if (roundedPriority == AsyncDataProvider.GetMaxVmPriority())
        {
            return "High"; //$NON-NLS-1$
        }
        else
        {
            return "Unknown"; //$NON-NLS-1$
        }
    }
}
