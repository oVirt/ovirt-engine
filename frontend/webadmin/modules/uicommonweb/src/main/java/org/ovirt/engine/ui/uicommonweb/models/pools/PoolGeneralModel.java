package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class PoolGeneralModel extends EntityModel
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

    private VM privatevm;

    public VM getvm()
    {
        return privatevm;
    }

    public void setvm(VM value)
    {
        privatevm = value;
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
            OnPropertyChanged(new PropertyChangedEventArgs("Name"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Description"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("OS"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultDisplayType"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Origin"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Template"));
        }
    }

    private int cpuCount;

    public int getCpuCount()
    {
        return cpuCount;
    }

    public void setCpuCount(int value)
    {
        if (cpuCount != value)
        {
            cpuCount = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CpuCount"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("MonitorCount"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("DefinedMemory"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("MinAllocatedMemory"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasDomain"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasStorageDomain"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasTimeZone"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("UsbPolicy"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Domain"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("StorageDomain"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("TimeZone"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("CpuInfo"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("HasDefaultHost"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("DefaultHost"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsStateless"));
        }
    }

    static
    {
        UpdateCompleteEventDefinition = new EventDefinition("UpdateComplete", PoolGeneralModel.class);
    }

    public PoolGeneralModel()
    {
        setUpdateCompleteEvent(new Event(UpdateCompleteEventDefinition));

        setTitle("General");
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
        vm_pools pool = (vm_pools) getEntity();

        setName(pool.getvm_pool_name());
        setDescription(pool.getvm_pool_description());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                // currently, only query that is being invoked asynchrounously in
                // this context is GetAnyVmQuery. If more async queries will be needed,
                // refactor to "switch ... case...".
                setvm((VM) result);
                PoolGeneralModel poolGeneralModel = (PoolGeneralModel) model;
                if (getvm() != null)
                {
                    poolGeneralModel.setTemplate(getvm().getvmt_name());
                    poolGeneralModel.setCpuInfo(getvm().getnum_of_cpus() + " " + "(" + getvm().getnum_of_sockets()
                            + " Socket(s), " + getvm().getcpu_per_socket() + " Core(s) per Socket)");
                    poolGeneralModel.setMonitorCount(getvm().getnum_of_monitors());

                    Translator translator = EnumTranslator.Create(VmOsType.class);
                    poolGeneralModel.setOS(translator.get(getvm().getvm_os()));

                    poolGeneralModel.setDefinedMemory(getvm().getvm_mem_size_mb() + " MB");
                    poolGeneralModel.setMinAllocatedMemory(getvm().getMinAllocatedMem() + " MB");

                    translator = EnumTranslator.Create(DisplayType.class);
                    poolGeneralModel.setDefaultDisplayType(translator.get(getvm().getdefault_display_type()));

                    translator = EnumTranslator.Create(OriginType.class);
                    poolGeneralModel.setOrigin(translator.get(getvm().getorigin()));

                    translator = EnumTranslator.Create(UsbPolicy.class);
                    poolGeneralModel.setUsbPolicy(translator.get(getvm().getusb_policy()));

                    setHasDomain(DataProvider.IsWindowsOsType(getvm().getvm_os()));
                    poolGeneralModel.setDomain(getvm().getvm_domain());

                    setHasTimeZone(DataProvider.IsWindowsOsType(getvm().getvm_os()));
                    poolGeneralModel.setTimeZone(getvm().gettime_zone());

                    poolGeneralModel.setIsStateless(getvm().getis_stateless());

                    poolGeneralModel.setHasDefaultHost(getvm().getdedicated_vm_for_vds() != null);
                    if (poolGeneralModel.getHasDefaultHost())
                    {
                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(poolGeneralModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object model1, Object ReturnValue1)
                            {
                                PoolGeneralModel poolGeneralModel1 = (PoolGeneralModel) model1;
                                java.util.ArrayList<VDS> hosts =
                                        (java.util.ArrayList<VDS>) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                                for (VDS host : hosts)
                                {
                                    if (host.getvds_id().equals(poolGeneralModel1.getvm().getdedicated_vm_for_vds()))
                                    {
                                        poolGeneralModel1.setDefaultHost(host.getvds_name());
                                        break;
                                    }
                                }

                                poolGeneralModel1.UpdateStorageDomain();
                            }
                        };

                        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = "
                                + getvm().getvds_group_name() + " sortby name", SearchType.VDS), _asyncQuery1);
                    }
                    else
                    {
                        poolGeneralModel.setDefaultHost("Any Host in Cluster");

                        poolGeneralModel.UpdateStorageDomain();
                    }
                }
                else
                {
                    poolGeneralModel.setTemplate(null);
                    poolGeneralModel.setCpuCount(0);
                    poolGeneralModel.setMonitorCount(0);
                    poolGeneralModel.setOS(null);
                    poolGeneralModel.setDefinedMemory(null);
                    poolGeneralModel.setMinAllocatedMemory(null);
                    poolGeneralModel.setDefaultDisplayType(null);
                    poolGeneralModel.setStorageDomain(null);
                    poolGeneralModel.setHasStorageDomain(false);
                    poolGeneralModel.setHasDomain(false);
                    poolGeneralModel.setDomain(null);
                    poolGeneralModel.setHasTimeZone(false);
                    poolGeneralModel.setTimeZone(null);
                    poolGeneralModel.setUsbPolicy(null);
                    poolGeneralModel.setDefaultHost(null);
                    poolGeneralModel.setIsStateless(false);

                    poolGeneralModel.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                }
            }
        };
        AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
    }

    private void UpdateStorageDomain()
    {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                PoolGeneralModel poolGeneralModel = (PoolGeneralModel) model;
                Iterable disks = (Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                java.util.Iterator disksIterator = disks.iterator();
                if (disksIterator.hasNext())
                {
                    poolGeneralModel.setHasStorageDomain(true);

                    AsyncQuery _asyncQuery1 = new AsyncQuery();
                    _asyncQuery1.setModel(poolGeneralModel);
                    _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object ReturnValue1)
                        {
                            PoolGeneralModel poolGeneralModel1 = (PoolGeneralModel) model1;
                            storage_domains storage =
                                    (storage_domains) ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                            poolGeneralModel1.setStorageDomain(storage.getstorage_name());

                            poolGeneralModel1.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                        }
                    };

                    DiskImage firstDisk = (DiskImage) disksIterator.next();
                    Frontend.RunQuery(VdcQueryType.GetStorageDomainById,
                            new StorageDomainQueryParametersBase(firstDisk.getstorage_id().getValue()),
                            _asyncQuery1);
                }
                else
                {
                    poolGeneralModel.setHasStorageDomain(false);

                    poolGeneralModel.getUpdateCompleteEvent().raise(this, EventArgs.Empty);
                }
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId,
                new GetAllDisksByVmIdParameters(getvm().getvm_guid()),
                _asyncQuery);
    }

}
