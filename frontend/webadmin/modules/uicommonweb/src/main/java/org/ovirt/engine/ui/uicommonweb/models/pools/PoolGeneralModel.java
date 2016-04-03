package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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

@SuppressWarnings("unused")
public class PoolGeneralModel extends AbstractGeneralModel<VmPool> {

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

        AsyncQuery _asyncQuery = new AsyncQuery();

        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                // currently, only query that is being invoked asynchrounously in
                // this context is GetVmDataByPoolIdQuery. If more async queries will be needed,
                // refactor to "switch ... case...".
                if (result != null) {
                    setvm((VM) ((VdcQueryReturnValue) result).getReturnValue());
                }
                PoolGeneralModel poolGeneralModel = (PoolGeneralModel) model;
                if (getvm() != null) {
                    poolGeneralModel.setTemplate(vmTemplateNameRenderer.render(getvm()));
                    poolGeneralModel.setCpuInfo(ConstantsManager.getInstance().getMessages().cpuInfoLabel(
                            getvm().getNumOfCpus(),
                            getvm().getNumOfSockets(),
                            getvm().getCpuPerSocket(),
                            getvm().getThreadsPerCpu()));
                    poolGeneralModel.setMonitorCount(getvm().getNumOfMonitors());

                    poolGeneralModel.setOS(getvm().getVmOsId());

                    poolGeneralModel.setDefinedMemory(getvm().getVmMemSizeMb() + " MB"); //$NON-NLS-1$
                    poolGeneralModel.setMinAllocatedMemory(getvm().getMinAllocatedMem() + " MB"); //$NON-NLS-1$

                    Translator translator = EnumTranslator.getInstance();
                    setDefaultDisplayType(translator.translate(getvm().getDefaultDisplayType()));

                    poolGeneralModel.setOrigin(translator.translate(getvm().getOrigin()));
                    poolGeneralModel.setUsbPolicy(translator.translate(getvm().getUsbPolicy()));

                    setHasDomain(AsyncDataProvider.getInstance().isWindowsOsType(getvm().getVmOsId()));

                    setHasTimeZone(AsyncDataProvider.getInstance().isWindowsOsType(getvm().getVmOsId()));
                    poolGeneralModel.setTimeZone(getvm().getTimeZone());

                    poolGeneralModel.setIsStateless(!pool.isStateful());

                    poolGeneralModel.setQuotaName(getvm().getQuotaName());

                    poolGeneralModel.setHasDefaultHost(getvm().getDedicatedVmForVdsList().size() > 0);
                    if (poolGeneralModel.getHasDefaultHost()) {
                        AsyncQuery _asyncQuery1 = new AsyncQuery();
                        _asyncQuery1.setModel(poolGeneralModel);
                        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model1, Object ReturnValue1) {
                                PoolGeneralModel poolGeneralModel1 = (PoolGeneralModel) model1;
                                String defaultHost = "";
                                ArrayList<VDS> hosts = ((VdcQueryReturnValue) ReturnValue1).getReturnValue();
                                for (VDS host : hosts) {
                                    if (poolGeneralModel1.getvm().getDedicatedVmForVdsList().contains(host.getId())) {
                                        if (defaultHost.isEmpty()) {
                                            defaultHost = host.getName();
                                        } else {
                                            defaultHost += ", " + host.getName(); //$NON-NLS-1$
                                        }
                                    }
                                }
                                poolGeneralModel1.setDefaultHost(defaultHost);
                            }
                        };

                        Frontend.getInstance().runQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " //$NON-NLS-1$
                                + getvm().getClusterName() + " sortby name", SearchType.VDS), _asyncQuery1); //$NON-NLS-1$
                    }
                    else {
                        poolGeneralModel.setDefaultHost(ConstantsManager.getInstance()
                                .getConstants()
                                .anyHostInCluster());
                    }
                    if (getvm() != null) {
                        PoolGeneralModel.super.updateProperties(getvm().getId());
                    }
                }
                else {
                    poolGeneralModel.setTemplate(null);
                    poolGeneralModel.setCpuCount(0);
                    poolGeneralModel.setMonitorCount(0);
                    poolGeneralModel.setOS(0);
                    poolGeneralModel.setDefinedMemory(null);
                    poolGeneralModel.setMinAllocatedMemory(null);
                    poolGeneralModel.setDefaultDisplayType(null);
                    poolGeneralModel.setHasDomain(false);
                    poolGeneralModel.setDomain(null);
                    poolGeneralModel.setHasTimeZone(false);
                    poolGeneralModel.setTimeZone(null);
                    poolGeneralModel.setUsbPolicy(null);
                    poolGeneralModel.setDefaultHost(null);
                    poolGeneralModel.setIsStateless(!pool.isStateful());
                    poolGeneralModel.setGraphicsType(""); //$NON-NLS-1$

                    poolGeneralModel.getUpdateCompleteEvent().raise(this, EventArgs.EMPTY);
                }
            }
        };
        Frontend.getInstance().runQuery(VdcQueryType.GetVmDataByPoolId,
                new IdQueryParameters(pool.getVmPoolId()),
                _asyncQuery);
    }

}
