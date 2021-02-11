package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmTemplateNameRenderer;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.Translator;

public class PoolGeneralModel extends AbstractGeneralModel<VmPool> {

    public static final String ARCHITECTURE = "VmPoolArchitecture";//$NON-NLS-1$

    public static final String BIOS_TYPE = "VmPoolBiosType";//$NON-NLS-1$

    public static final EventDefinition updateCompleteEventDefinition;
    private Event<EventArgs> privateUpdateCompleteEvent;

    private static final VmTemplateNameRenderer vmTemplateNameRenderer = new VmTemplateNameRenderer();

    public Event<EventArgs> getUpdateCompleteEvent() {
        return privateUpdateCompleteEvent;
    }

    private void setUpdateCompleteEvent(Event<EventArgs> value) {
        privateUpdateCompleteEvent = value;
    }

    private VM privatevm;

    public VM getvm() {
        return privatevm;
    }

    public void setvm(VM value) {
        privatevm = value;
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

    private int os;

    public int getOS() {
        return os;
    }

    public void setOS(int value) {
        if (os != value) {
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

    private int cpuCount;

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int value) {
        if (cpuCount != value) {
            cpuCount = value;
            onPropertyChanged(new PropertyChangedEventArgs("CpuCount")); //$NON-NLS-1$
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

    private String quotaName;

    public void setQuotaName(String value) {
        if (!Objects.equals(defaultHost, value)) {
            this.quotaName = value;
            onPropertyChanged(new PropertyChangedEventArgs("QuotaName")); //$NON-NLS-1$
        }
    }

    public String getQuotaName() {
        return quotaName;
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

    static {
        updateCompleteEventDefinition = new EventDefinition("UpdateComplete", PoolGeneralModel.class); //$NON-NLS-1$
    }

    public PoolGeneralModel() {
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
        final VmPool pool = getEntity();

        setName(pool.getName());
        setDescription(pool.getVmPoolDescription());

        Frontend.getInstance().runQuery(QueryType.GetVmDataByPoolId,
                new IdQueryParameters(pool.getVmPoolId()),
                new AsyncQuery<>(new AsyncCallback<QueryReturnValue>() {
                    @Override
                    public void onSuccess(QueryReturnValue result) {
                        // currently, only query that is being invoked asynchrounously in
                        // this context is GetVmDataByPoolIdQuery. If more async queries will be needed,
                        // refactor to "switch ... case...".
                        if (result != null) {
                            setvm((VM) result.getReturnValue());
                        }
                        if (getvm() != null) {
                            setTemplate(vmTemplateNameRenderer.render(getvm()));
                            setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                                    getvm().getNumOfCpus(),
                                    getvm().getNumOfSockets(),
                                    getvm().getCpuPerSocket(),
                                    getvm().getThreadsPerCpu()));
                            setMonitorCount(getvm().getNumOfMonitors());

                            setOS(getvm().getVmOsId());
                            setArchitecture(getvm().getClusterArch());
                            setBiosType(getvm().getBiosType());

                            setDefinedMemory(getvm().getVmMemSizeMb() + " MB"); //$NON-NLS-1$
                            setMinAllocatedMemory(getvm().getMinAllocatedMem() + " MB"); //$NON-NLS-1$

                            Translator translator = EnumTranslator.getInstance();
                            setDefaultDisplayType(translator.translate(getvm().getDefaultDisplayType()));

                            setOrigin(translator.translate(getvm().getOrigin()));
                            setUsbPolicy(translator.translate(getvm().getUsbPolicy()));

                            setHasDomain(AsyncDataProvider.getInstance().isWindowsOsType(getvm().getVmOsId()));
                            setOptimizedForSystemProfile(translator.translate(getvm().getVmType()));
                            setHasTimeZone(AsyncDataProvider.getInstance().isWindowsOsType(getvm().getVmOsId()));
                            setTimeZone(getvm().getTimeZone());

                            setIsStateless(!pool.isStateful());

                            setQuotaName(getvm().getQuotaName());
                            setHasDefaultHost(getvm().getDedicatedVmForVdsList().size() > 0);
                            if (getHasDefaultHost()) {

                                Frontend.getInstance().runQuery(QueryType.Search, new SearchParameters("Host: cluster = " //$NON-NLS-1$
                                        + getvm().getClusterName() + " sortby name", SearchType.VDS), new AsyncQuery<QueryReturnValue>(returnValue1 -> { //$NON-NLS-1$
                                            String defaultHost1 = "";
                                            ArrayList<VDS> hosts = returnValue1.getReturnValue();
                                            for (VDS host : hosts) {
                                                if (getvm().getDedicatedVmForVdsList().contains(host.getId())) {
                                                    if (defaultHost1.isEmpty()) {
                                                        defaultHost1 = host.getName();
                                                    } else {
                                                        defaultHost1 += ", " + host.getName(); //$NON-NLS-1$
                                                    }
                                                }
                                            }
                                            setDefaultHost(defaultHost1);
                                        }));
                            } else {
                                setDefaultHost(ConstantsManager.getInstance()
                                        .getConstants()
                                        .anyHostInCluster());
                            }
                            if (getvm() != null) {
                                PoolGeneralModel.super.updateProperties(getvm().getId());
                            }
                        } else {
                            setTemplate(null);
                            setCpuCount(0);
                            setMonitorCount(0);
                            setOS(0);
                            setDefinedMemory(null);
                            setMinAllocatedMemory(null);
                            setDefaultDisplayType(null);
                            setHasDomain(false);
                            setDomain(null);
                            setHasTimeZone(false);
                            setTimeZone(null);
                            setUsbPolicy(null);
                            setDefaultHost(null);
                            setIsStateless(!pool.isStateful());
                            setGraphicsType(""); //$NON-NLS-1$
                            setOptimizedForSystemProfile(""); //$NON-NLS-1$

                            getUpdateCompleteEvent().raise(this, EventArgs.EMPTY);
                        }
                    }
                }));
    }

    @Override
    public void cleanup() {
        cleanupEvents(getUpdateCompleteEvent());
        super.cleanup();
    }
}
