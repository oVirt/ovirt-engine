package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.MigrateOnErrorOptions;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ClusterModel extends EntityModel
{

    private int privateServerOverCommit;

    public int getServerOverCommit()
    {
        return privateServerOverCommit;
    }

    public void setServerOverCommit(int value)
    {
        privateServerOverCommit = value;
    }

    private int privateDesktopOverCommit;

    public int getDesktopOverCommit()
    {
        return privateDesktopOverCommit;
    }

    public void setDesktopOverCommit(int value)
    {
        privateDesktopOverCommit = value;
    }

    private int privateDefaultMemoryOvercommit;

    public int getDefaultMemoryOvercommit()
    {
        return privateDefaultMemoryOvercommit;
    }

    public void setDefaultMemoryOvercommit(int value)
    {
        privateDefaultMemoryOvercommit = value;
    }

    private VDSGroup privateEntity;

    public VDSGroup getEntity()
    {
        return privateEntity;
    }

    public void setEntity(VDSGroup value)
    {
        privateEntity = value;
    }

    private boolean privateIsEdit;

    public boolean getIsEdit()
    {
        return privateIsEdit;
    }

    public void setIsEdit(boolean value)
    {
        privateIsEdit = value;
    }

    private boolean isCPUinitialized = false;

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private String privateOriginalName;

    public String getOriginalName()
    {
        return privateOriginalName;
    }

    public void setOriginalName(String value)
    {
        privateOriginalName = value;
    }

    private NGuid privateClusterId;

    public NGuid getClusterId()
    {
        return privateClusterId;
    }

    public void setClusterId(NGuid value)
    {
        privateClusterId = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    public void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    public void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
    }

    private ListModel privateCPU;

    public ListModel getCPU()
    {
        return privateCPU;
    }

    public void setCPU(ListModel value)
    {
        privateCPU = value;
    }

    private ListModel privateVersion;

    public ListModel getVersion()
    {
        return privateVersion;
    }

    public void setVersion(ListModel value)
    {
        privateVersion = value;
    }

    private boolean allowClusterWithVirtGlusterEnabled;

    public boolean getAllowClusterWithVirtGlusterEnabled()
    {
        return allowClusterWithVirtGlusterEnabled;
    }

    public void setAllowClusterWithVirtGlusterEnabled(boolean value)
    {
        allowClusterWithVirtGlusterEnabled = value;
        if (allowClusterWithVirtGlusterEnabled != value)
        {
            allowClusterWithVirtGlusterEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AllowClusterWithVirtGlusterEnabled")); //$NON-NLS-1$
        }
    }

    private EntityModel privateEnableOvirtService;

    public EntityModel getEnableOvirtService()
    {
        return privateEnableOvirtService;
    }

    public void setEnableOvirtService(EntityModel value)
    {
        this.privateEnableOvirtService = value;
    }

    private EntityModel privateEnableGlusterService;

    public EntityModel getEnableGlusterService() {
        return privateEnableGlusterService;
    }

    public void setEnableGlusterService(EntityModel value) {
        this.privateEnableGlusterService = value;
    }

    private EntityModel isImportGlusterConfiguration;

    public EntityModel getIsImportGlusterConfiguration() {
        return isImportGlusterConfiguration;
    }

    public void setIsImportGlusterConfiguration(EntityModel value) {
        this.isImportGlusterConfiguration = value;
    }

    private EntityModel glusterHostAddress;

    public EntityModel getGlusterHostAddress() {
        return glusterHostAddress;
    }

    public void setGlusterHostAddress(EntityModel glusterHostAddress) {
        this.glusterHostAddress = glusterHostAddress;
    }

    private EntityModel glusterHostFingerprint;

    public EntityModel getGlusterHostFingerprint() {
        return glusterHostFingerprint;
    }

    public void setGlusterHostFingerprint(EntityModel glusterHostFingerprint) {
        this.glusterHostFingerprint = glusterHostFingerprint;
    }

    private Boolean isFingerprintVerified;

    public Boolean isFingerprintVerified() {
        return isFingerprintVerified;
    }

    public void setIsFingerprintVerified(Boolean value) {
        this.isFingerprintVerified = value;
    }

    private EntityModel glusterHostPassword;

    public EntityModel getGlusterHostPassword() {
        return glusterHostPassword;
    }

    public void setGlusterHostPassword(EntityModel glusterHostPassword) {
        this.glusterHostPassword = glusterHostPassword;
    }

    private EntityModel privateOptimizationNone;

    public EntityModel getOptimizationNone()
    {
        return privateOptimizationNone;
    }

    public void setOptimizationNone(EntityModel value)
    {
        privateOptimizationNone = value;
    }

    private EntityModel privateOptimizationForServer;

    public EntityModel getOptimizationForServer()
    {
        return privateOptimizationForServer;
    }

    public void setOptimizationForServer(EntityModel value)
    {
        privateOptimizationForServer = value;
    }

    private EntityModel privateOptimizationForDesktop;

    public EntityModel getOptimizationForDesktop()
    {
        return privateOptimizationForDesktop;
    }

    public void setOptimizationForDesktop(EntityModel value)
    {
        privateOptimizationForDesktop = value;
    }

    private EntityModel privateOptimizationCustom;

    public EntityModel getOptimizationCustom()
    {
        return privateOptimizationCustom;
    }

    public void setOptimizationCustom(EntityModel value)
    {
        privateOptimizationCustom = value;
    }

    private EntityModel privateOptimizationNone_IsSelected;

    public EntityModel getOptimizationNone_IsSelected()
    {
        return privateOptimizationNone_IsSelected;
    }

    public void setOptimizationNone_IsSelected(EntityModel value)
    {
        privateOptimizationNone_IsSelected = value;
    }

    private EntityModel privateOptimizationForServer_IsSelected;

    public EntityModel getOptimizationForServer_IsSelected()
    {
        return privateOptimizationForServer_IsSelected;
    }

    public void setOptimizationForServer_IsSelected(EntityModel value)
    {
        privateOptimizationForServer_IsSelected = value;
    }

    private EntityModel privateOptimizationForDesktop_IsSelected;

    public EntityModel getOptimizationForDesktop_IsSelected()
    {
        return privateOptimizationForDesktop_IsSelected;
    }

    public void setOptimizationForDesktop_IsSelected(EntityModel value)
    {
        privateOptimizationForDesktop_IsSelected = value;
    }

    private EntityModel privateOptimizationCustom_IsSelected;

    public EntityModel getOptimizationCustom_IsSelected()
    {
        return privateOptimizationCustom_IsSelected;
    }

    public void setOptimizationCustom_IsSelected(EntityModel value)
    {
        privateOptimizationCustom_IsSelected = value;
    }

    private EntityModel privateCountThreadsAsCores;

    public EntityModel getCountThreadsAsCores()
    {
        return privateCountThreadsAsCores;
    }

    public void setCountThreadsAsCores(EntityModel value)
    {
        privateCountThreadsAsCores = value;
    }

    private EntityModel privateVersionSupportsCpuThreads;

    public EntityModel getVersionSupportsCpuThreads()
    {
        return privateVersionSupportsCpuThreads;
    }

    public void setVersionSupportsCpuThreads(EntityModel value)
    {
        privateVersionSupportsCpuThreads = value;
    }

    private EntityModel privateMigrateOnErrorOption_NO;

    public EntityModel getMigrateOnErrorOption_NO()
    {
        return privateMigrateOnErrorOption_NO;
    }

    public void setMigrateOnErrorOption_NO(EntityModel value)
    {
        privateMigrateOnErrorOption_NO = value;
    }

    private EntityModel privateMigrateOnErrorOption_YES;

    public EntityModel getMigrateOnErrorOption_YES()
    {
        return privateMigrateOnErrorOption_YES;
    }

    public void setMigrateOnErrorOption_YES(EntityModel value)
    {
        privateMigrateOnErrorOption_YES = value;
    }

    private EntityModel privateMigrateOnErrorOption_HA_ONLY;

    public EntityModel getMigrateOnErrorOption_HA_ONLY()
    {
        return privateMigrateOnErrorOption_HA_ONLY;
    }

    public void setMigrateOnErrorOption_HA_ONLY(EntityModel value)
    {
        privateMigrateOnErrorOption_HA_ONLY = value;
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
        }
    }

    private ClusterPolicyModel clusterPolicyModel;

    public ClusterPolicyModel getClusterPolicyModel() {
        return clusterPolicyModel;
    }

    public void setClusterPolicyModel(ClusterPolicyModel clusterPolicyModel) {
        this.clusterPolicyModel = clusterPolicyModel;
    }

    private MigrateOnErrorOptions migrateOnErrorOption = MigrateOnErrorOptions.values()[0];

    public MigrateOnErrorOptions getMigrateOnErrorOption()
    {
        if ((Boolean) getMigrateOnErrorOption_NO().getEntity() == true)
        {
            return MigrateOnErrorOptions.NO;
        }
        else if ((Boolean) getMigrateOnErrorOption_YES().getEntity() == true)
        {
            return MigrateOnErrorOptions.YES;
        }
        else if ((Boolean) getMigrateOnErrorOption_HA_ONLY().getEntity() == true)
        {
            return MigrateOnErrorOptions.HA_ONLY;
        }
        return MigrateOnErrorOptions.YES;
    }

    public void setMigrateOnErrorOption(MigrateOnErrorOptions value)
    {
        if (migrateOnErrorOption != value)
        {
            migrateOnErrorOption = value;

            // webadmin use.
            switch (migrateOnErrorOption)
            {
            case NO:
                getMigrateOnErrorOption_NO().setEntity(true);
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                break;
            case YES:
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_YES().setEntity(true);
                getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                break;
            case HA_ONLY:
                getMigrateOnErrorOption_NO().setEntity(false);
                getMigrateOnErrorOption_YES().setEntity(false);
                getMigrateOnErrorOption_HA_ONLY().setEntity(true);
                break;
            default:
                break;
            }
            OnPropertyChanged(new PropertyChangedEventArgs("MigrateOnErrorOption")); //$NON-NLS-1$
        }
    }

    private boolean privateisResiliencePolicyTabAvailable;

    public boolean getisResiliencePolicyTabAvailable()
    {
        return privateisResiliencePolicyTabAvailable;
    }

    public void setisResiliencePolicyTabAvailable(boolean value)
    {
        privateisResiliencePolicyTabAvailable = value;
    }

    public boolean getIsResiliencePolicyTabAvailable()
    {
        return getisResiliencePolicyTabAvailable();
    }

    public void setIsResiliencePolicyTabAvailable(boolean value)
    {
        if (getisResiliencePolicyTabAvailable() != value)
        {
            setisResiliencePolicyTabAvailable(value);
            OnPropertyChanged(new PropertyChangedEventArgs("IsResiliencePolicyTabAvailable")); //$NON-NLS-1$
        }
    }

    public int getMemoryOverCommit()
    {
        if ((Boolean) getOptimizationNone_IsSelected().getEntity())
        {
            return (Integer) getOptimizationNone().getEntity();
        }

        if ((Boolean) getOptimizationForServer_IsSelected().getEntity())
        {
            return (Integer) getOptimizationForServer().getEntity();
        }

        if ((Boolean) getOptimizationForDesktop_IsSelected().getEntity())
        {
            return (Integer) getOptimizationForDesktop().getEntity();
        }

        if ((Boolean) getOptimizationCustom_IsSelected().getEntity())
        {
            return (Integer) getOptimizationCustom().getEntity();
        }

        return AsyncDataProvider.GetClusterDefaultMemoryOverCommit();
    }

    public void setMemoryOverCommit(int value)
    {
        getOptimizationNone_IsSelected().setEntity(value == (Integer) getOptimizationNone().getEntity());
        getOptimizationForServer_IsSelected().setEntity(value == (Integer) getOptimizationForServer().getEntity());
        getOptimizationForDesktop_IsSelected().setEntity(value == (Integer) getOptimizationForDesktop().getEntity());

        if (!(Boolean) getOptimizationNone_IsSelected().getEntity()
                && !(Boolean) getOptimizationForServer_IsSelected().getEntity()
                && !(Boolean) getOptimizationForDesktop_IsSelected().getEntity())
        {
            getOptimizationCustom().setIsAvailable(true);
            getOptimizationCustom().setEntity(value);
            getOptimizationCustom_IsSelected().setIsAvailable(true);
            getOptimizationCustom_IsSelected().setEntity(true);
        }
    }

    public ClusterModel()
    {
        super();
    }

    public void Init(final boolean isEdit)
    {
        setIsEdit(isEdit);
        setName(new EntityModel());
        setDescription(new EntityModel());
        setClusterPolicyModel(new ClusterPolicyModel());
        setAllowClusterWithVirtGlusterEnabled(true);
        AsyncDataProvider.GetAllowClusterWithVirtGlusterEnabled(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                setAllowClusterWithVirtGlusterEnabled((Boolean) returnValue);
            }
        }));

        setEnableOvirtService(new EntityModel());
        setEnableGlusterService(new EntityModel());

        getEnableOvirtService().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!getAllowClusterWithVirtGlusterEnabled() && (Boolean) getEnableOvirtService().getEntity()) {
                    getEnableGlusterService().setEntity(Boolean.FALSE);
                }
            }
        });
        getEnableOvirtService().setEntity(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        getEnableOvirtService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.VirtOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));

        initImportCluster(isEdit);

        getEnableGlusterService().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!getAllowClusterWithVirtGlusterEnabled() && (Boolean) getEnableGlusterService().getEntity()) {
                    getEnableOvirtService().setEntity(Boolean.FALSE);
                }

                if (!isEdit
                        && getEnableGlusterService().getEntity() != null
                        && (Boolean) getEnableGlusterService().getEntity())
                {
                    getIsImportGlusterConfiguration().setIsAvailable(true);

                    getGlusterHostAddress().setIsAvailable(true);
                    getGlusterHostFingerprint().setIsAvailable(true);
                    getGlusterHostPassword().setIsAvailable(true);
                }
                else
                {
                    getIsImportGlusterConfiguration().setIsAvailable(false);
                    getIsImportGlusterConfiguration().setEntity(false);

                    getGlusterHostAddress().setIsAvailable(false);
                    getGlusterHostFingerprint().setIsAvailable(false);
                    getGlusterHostPassword().setIsAvailable(false);
                }
            }
        });

        getEnableGlusterService().setEntity(ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly);
        getEnableGlusterService().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly
                && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly));

        setOptimizationNone(new EntityModel());
        setOptimizationForServer(new EntityModel());
        setOptimizationForDesktop(new EntityModel());
        setOptimizationCustom(new EntityModel());

        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setOptimizationNone_IsSelected(tempVar);
        getOptimizationNone_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(false);
        setOptimizationForServer_IsSelected(tempVar2);
        getOptimizationForServer_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(false);
        setOptimizationForDesktop_IsSelected(tempVar3);
        getOptimizationForDesktop_IsSelected().getEntityChangedEvent().addListener(this);
        EntityModel tempVar4 = new EntityModel();
        tempVar4.setEntity(false);
        tempVar4.setIsAvailable(false);
        setOptimizationCustom_IsSelected(tempVar4);
        getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(this);

        EntityModel tempVar5 = new EntityModel();
        tempVar5.setEntity(false);
        setMigrateOnErrorOption_YES(tempVar5);
        getMigrateOnErrorOption_YES().getEntityChangedEvent().addListener(this);
        EntityModel tempVar6 = new EntityModel();
        tempVar6.setEntity(false);
        setMigrateOnErrorOption_NO(tempVar6);
        getMigrateOnErrorOption_NO().getEntityChangedEvent().addListener(this);
        EntityModel tempVar7 = new EntityModel();
        tempVar7.setEntity(false);
        setMigrateOnErrorOption_HA_ONLY(tempVar7);
        getMigrateOnErrorOption_HA_ONLY().getEntityChangedEvent().addListener(this);

        // Optimization methods:
        // default value =100;
        setDefaultMemoryOvercommit(AsyncDataProvider.GetClusterDefaultMemoryOverCommit());

        setCountThreadsAsCores(new EntityModel(AsyncDataProvider.GetClusterDefaultCountThreadsAsCores()));

        setVersionSupportsCpuThreads(new EntityModel(true));

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                clusterModel.setDesktopOverCommit((Integer) result);
                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.setModel(clusterModel);
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model1, Object result1)
                    {
                        ClusterModel clusterModel1 = (ClusterModel) model1;
                        clusterModel1.setServerOverCommit((Integer) result1);

                        // temp is used for conversion purposes
                        EntityModel temp;

                        temp = clusterModel1.getOptimizationNone();
                        temp.setEntity(clusterModel1.getDefaultMemoryOvercommit());
                        // res1, res2 is used for conversion purposes.
                        boolean res1 = clusterModel1.getDesktopOverCommit() != clusterModel1.getDefaultMemoryOvercommit();
                        boolean res2 = clusterModel1.getServerOverCommit() != clusterModel1.getDefaultMemoryOvercommit();
                        temp = clusterModel1.getOptimizationNone_IsSelected();
                        setIsSelected(res1 && res2);
                        temp.setEntity(getIsSelected());

                        temp = clusterModel1.getOptimizationForServer();
                        temp.setEntity(clusterModel1.getServerOverCommit());
                        temp = clusterModel1.getOptimizationForServer_IsSelected();
                        temp.setEntity(clusterModel1.getServerOverCommit() == clusterModel1.getDefaultMemoryOvercommit());

                        temp = clusterModel1.getOptimizationForDesktop();
                        temp.setEntity(clusterModel1.getDesktopOverCommit());
                        temp = clusterModel1.getOptimizationForDesktop_IsSelected();
                        temp.setEntity(clusterModel1.getDesktopOverCommit() == clusterModel1.getDefaultMemoryOvercommit());

                        temp = clusterModel1.getOptimizationCustom();
                        temp.setIsAvailable(false);
                        temp.setIsChangable(false);

                        if (clusterModel1.getIsEdit())
                        {
                            clusterModel1.postInit();
                        }

                    }
                };
                AsyncDataProvider.GetClusterServerMemoryOverCommit(_asyncQuery1);
            }
        };
        AsyncDataProvider.GetClusterDesktopMemoryOverCommit(_asyncQuery);

        setDataCenter(new ListModel());
        getDataCenter().getSelectedItemChangedEvent().addListener(this);
        getDataCenter().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);

        setCPU(new ListModel());
        getCPU().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setVersion(new ListModel());
        getVersion().getSelectedItemChangedEvent().addListener(this);
        getVersion().setIsAvailable(ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly);
        setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

        setIsGeneralTabValid(true);
        setIsResiliencePolicyTabAvailable(true);
    }

    private void initImportCluster(boolean isEdit)
    {
        setGlusterHostAddress(new EntityModel());
        getGlusterHostAddress().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                setIsFingerprintVerified(false);
                if (getGlusterHostAddress().getEntity() == null
                        || ((String) getGlusterHostAddress().getEntity()).trim().length() == 0) {
                    getGlusterHostFingerprint().setEntity(""); //$NON-NLS-1$
                    return;
                }
                fetchFingerprint((String) getGlusterHostAddress().getEntity());
            }
        });

        setGlusterHostFingerprint(new EntityModel());
        getGlusterHostFingerprint().setEntity(""); //$NON-NLS-1$
        setIsFingerprintVerified(false);
        setGlusterHostPassword(new EntityModel());

        setIsImportGlusterConfiguration(new EntityModel());
        getIsImportGlusterConfiguration().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (getIsImportGlusterConfiguration().getEntity() != null
                        && (Boolean) getIsImportGlusterConfiguration().getEntity())
                {
                    getGlusterHostAddress().setIsChangable(true);
                    getGlusterHostPassword().setIsChangable(true);
                }
                else
                {
                    getGlusterHostAddress().setIsChangable(false);
                    getGlusterHostPassword().setIsChangable(false);
                }
            }
        });

        getIsImportGlusterConfiguration().setIsAvailable(false);
        getGlusterHostAddress().setIsAvailable(false);
        getGlusterHostFingerprint().setIsAvailable(false);
        getGlusterHostPassword().setIsAvailable(false);

        getIsImportGlusterConfiguration().setEntity(false);
    }

    private void fetchFingerprint(String hostAddress) {
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                String fingerprint = (String) result;
                if (fingerprint != null && fingerprint.length() > 0)
                {
                    getGlusterHostFingerprint().setEntity(result);
                    setIsFingerprintVerified(true);
                }
                else
                {
                    getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance()
                            .getConstants()
                            .errorLoadingFingerprint());
                    setIsFingerprintVerified(false);
                }
            }
        };
        AsyncDataProvider.GetHostFingerprint(aQuery, hostAddress);
        getGlusterHostFingerprint().setEntity(ConstantsManager.getInstance().getConstants().loadingFingerprint());
    }

    private void postInit()
    {
        getDescription().setEntity(getEntity().getdescription());
        setMemoryOverCommit(getEntity().getmax_vds_memory_over_commit());

        getCountThreadsAsCores().setEntity((boolean) getEntity().getCountThreadsAsCores());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) result;

                clusterModel.getDataCenter().setItems(dataCenters);

                clusterModel.getDataCenter().setSelectedItem(null);
                for (storage_pool a : dataCenters)
                {
                    if (clusterModel.getEntity().getStoragePoolId() != null
                            && a.getId().equals(clusterModel.getEntity().getStoragePoolId()))
                    {
                        clusterModel.getDataCenter().setSelectedItem(a);
                        break;
                    }
                }
                clusterModel.getDataCenter().setIsChangable(clusterModel.getDataCenter().getSelectedItem() == null);

                clusterModel.setMigrateOnErrorOption(clusterModel.getEntity().getMigrateOnError());
            }
        };
        AsyncDataProvider.GetDataCenterList(_asyncQuery);

    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.SelectedItemChangedEventDefinition))
        {
            if (sender == getDataCenter())
            {
                StoragePool_SelectedItemChanged(args);
            }
            else if (sender == getVersion())
            {
                Version_SelectedItemChanged(args);
            }
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition))
        {
            EntityModel senderEntityModel = (EntityModel) sender;
            if ((Boolean) senderEntityModel.getEntity())
            {
                if (senderEntityModel == getOptimizationNone_IsSelected())
                {
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationForServer_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationForDesktop_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationCustom_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getOptimizationCustom_IsSelected())
                {
                    getOptimizationNone_IsSelected().setEntity(false);
                    getOptimizationForServer_IsSelected().setEntity(false);
                    getOptimizationForDesktop_IsSelected().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_YES())
                {
                    getMigrateOnErrorOption_NO().setEntity(false);
                    getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_NO())
                {
                    getMigrateOnErrorOption_YES().setEntity(false);
                    getMigrateOnErrorOption_HA_ONLY().setEntity(false);
                }
                else if (senderEntityModel == getMigrateOnErrorOption_HA_ONLY())
                {
                    getMigrateOnErrorOption_YES().setEntity(false);
                    getMigrateOnErrorOption_NO().setEntity(false);
                }
            }
        }
    }

    private void Version_SelectedItemChanged(EventArgs e)
    {
        Version version;
        if (getVersion().getSelectedItem() != null)
        {
            version = (Version) getVersion().getSelectedItem();
        }
        else
        {
            version = ((storage_pool) getDataCenter().getSelectedItem()).getcompatibility_version();
        }
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<ServerCpu> cpus = (ArrayList<ServerCpu>) result;

                ServerCpu oldSelectedCpu = (ServerCpu) clusterModel.getCPU().getSelectedItem();
                clusterModel.getCPU().setItems(cpus);

                clusterModel.getCPU().setSelectedItem(oldSelectedCpu != null ?
                        Linq.FirstOrDefault(cpus, new Linq.ServerCpuPredicate(oldSelectedCpu.getCpuName())) : null);

                if (clusterModel.getCPU().getSelectedItem() == null || !isCPUinitialized)
                {
                    InitCPU();
                }
            }
        };
        AsyncDataProvider.GetCPUList(_asyncQuery, version);

        // CPU Thread support is only available for clusters of version 3.2 or greater
        getVersionSupportsCpuThreads().setEntity(version.compareTo(Version.v3_2) >= 0);

    }

    private void InitCPU()
    {
        if (!isCPUinitialized && getIsEdit())
        {
            isCPUinitialized = true;
            getCPU().setSelectedItem(null);
            for (ServerCpu a : (ArrayList<ServerCpu>) getCPU().getItems())
            {
                if (StringHelper.stringsEqual(a.getCpuName(), getEntity().getcpu_name()))
                {
                    getCPU().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    private void StoragePool_SelectedItemChanged(EventArgs e)
    {
        // possible versions for new cluster (when editing cluster, this event won't occur)
        // are actually the possible versions for the data-center that the cluster is going
        // to be attached to.
        storage_pool selectedDataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (selectedDataCenter == null)
        {
            return;
        }
        if (selectedDataCenter.getstorage_pool_type() == StorageType.LOCALFS)
        {
            setIsResiliencePolicyTabAvailable(false);
        }
        else
        {
            setIsResiliencePolicyTabAvailable(true);
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                ClusterModel clusterModel = (ClusterModel) model;
                ArrayList<Version> versions = (ArrayList<Version>) result;
                clusterModel.getVersion().setItems(versions);
                if (!versions.contains(clusterModel.getVersion().getSelectedItem()))
                {
                    if (versions.contains(((storage_pool) clusterModel.getDataCenter().getSelectedItem()).getcompatibility_version()))
                    {
                        clusterModel.getVersion().setSelectedItem(((storage_pool) clusterModel.getDataCenter()
                                .getSelectedItem()).getcompatibility_version());
                    }
                    else
                    {
                        clusterModel.getVersion().setSelectedItem(Linq.SelectHighestVersion(versions));
                    }
                }
                else if (clusterModel.getIsEdit()) {
                    clusterModel.getVersion().setSelectedItem(Linq.FirstOrDefault(versions,
                            new Linq.VersionPredicate(clusterModel.getEntity().getcompatibility_version())));
                }
            }
        };
        AsyncDataProvider.GetDataCenterVersions(_asyncQuery, selectedDataCenter.getId());
    }

    public boolean Validate(boolean validateCpu)
    {
        return Validate(true, validateCpu);
    }

    public boolean Validate(boolean validateStoragePool, boolean validateCpu)
    {
        getName().ValidateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new LengthValidation(40),
                new I18NNameValidation() });

        if (validateStoragePool)
        {
            getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        if (validateCpu)
        {
            getCPU().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }
        else
        {
            getCPU().ValidateSelectedItem(new IValidation[] {});
        }

        getVersion().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        // TODO: async validation for webadmin
        // string name = (string)Name.Entity;

        // //Check name unicitate.
        // if (String.Compare(name, OriginalName, true) != 0 && !DataProvider.IsClusterNameUnique(name))
        // {
        // Name.IsValid = false;
        // Name.InvalidityReasons.Add("Name must be unique.");
        // }

        boolean validService = true;
        if (getEnableOvirtService().getIsAvailable() && getEnableGlusterService().getIsAvailable())
        {
            validService = ((Boolean) getEnableOvirtService().getEntity())
                            || ((Boolean) getEnableGlusterService().getEntity());
        }

        getGlusterHostAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        getGlusterHostPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

        if (!validService)
        {
            setMessage(ConstantsManager.getInstance().getConstants().clusterServiceValidationMsg());
        }
        else if (((Boolean) getIsImportGlusterConfiguration().getEntity()) && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && !isFingerprintVerified())
        {
            setMessage(ConstantsManager.getInstance().getConstants().fingerprintNotVerified());
        }
        else
        {
            setMessage(null);
        }

        setIsGeneralTabValid(getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid()
                && getVersion().getIsValid() && validService && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && ((Boolean) getIsImportGlusterConfiguration().getEntity() ? (getGlusterHostAddress().getIsValid()
                        && getGlusterHostPassword().getIsValid()
                        && isFingerprintVerified()) : true));

        return getClusterPolicyModel().Validate() && getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid()
                && getVersion().getIsValid() && validService && getGlusterHostAddress().getIsValid()
                && getGlusterHostPassword().getIsValid()
                && ((Boolean) getIsImportGlusterConfiguration().getEntity() ? (getGlusterHostAddress().getIsValid()
                        && getGlusterHostPassword().getIsValid()
                        && isFingerprintVerified()) : true);
    }

}
