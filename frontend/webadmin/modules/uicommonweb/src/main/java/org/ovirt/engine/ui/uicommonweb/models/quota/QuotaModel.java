package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class QuotaModel extends EntityModel {

    UICommand editQuotaClusterCommand;
    UICommand editQuotaStorageCommand;

    EntityModel name;
    EntityModel description;
    ListModel dataCenter;

    EntityModel graceStorage;
    EntityModel thresholdStorage;
    EntityModel graceCluster;
    EntityModel thresholdCluster;

    EntityModel specificClusterQuota;
    EntityModel globalClusterQuota;

    EntityModel specificStorageQuota;
    EntityModel globalStorageQuota;

    ListModel quotaClusters;
    ListModel quotaStorages;

    ListModel allDataCenterClusters;
    ListModel allDataCenterStorages;

    private QuotaStorage quotaStorage;
    private QuotaVdsGroup quotaCluster;

    public UICommand getEditQuotaClusterCommand() {
        return editQuotaClusterCommand;
    }

    public void setEditQuotaClusterCommand(UICommand editQuotaClusterCommand) {
        this.editQuotaClusterCommand = editQuotaClusterCommand;
    }

    public UICommand getEditQuotaStorageCommand() {
        return editQuotaStorageCommand;
    }

    public void setEditQuotaStorageCommand(UICommand editQuotaStorageCommand) {
        this.editQuotaStorageCommand = editQuotaStorageCommand;
    }

    public ListModel getQuotaClusters() {
        return quotaClusters;
    }

    public void setQuotaClusters(ListModel quotaClusters) {
        this.quotaClusters = quotaClusters;
    }

    public ListModel getQuotaStorages() {
        return quotaStorages;
    }

    public void setQuotaStorages(ListModel quotaStorages) {
        this.quotaStorages = quotaStorages;
    }

    public ListModel getAllDataCenterClusters() {
        return allDataCenterClusters;
    }

    public void setAllDataCenterClusters(ListModel allDataCenterClusters) {
        this.allDataCenterClusters = allDataCenterClusters;
    }

    public ListModel getAllDataCenterStorages() {
        return allDataCenterStorages;
    }

    public void setAllDataCenterStorages(ListModel allDataCenterStorages) {
        this.allDataCenterStorages = allDataCenterStorages;
    }

    public EntityModel getSpecificClusterQuota() {
        return specificClusterQuota;
    }

    public void setSpecificClusterQuota(EntityModel specificClusterQuota) {
        this.specificClusterQuota = specificClusterQuota;
    }

    public EntityModel getGlobalClusterQuota() {
        return globalClusterQuota;
    }

    public void setGlobalClusterQuota(EntityModel globalClusterQuota) {
        this.globalClusterQuota = globalClusterQuota;
    }

    public EntityModel getSpecificStorageQuota() {
        return specificStorageQuota;
    }

    public void setSpecificStorageQuota(EntityModel specificStorageQuota) {
        this.specificStorageQuota = specificStorageQuota;
    }

    public EntityModel getGlobalStorageQuota() {
        return globalStorageQuota;
    }

    public void setGlobalStorageQuota(EntityModel globalStorageQuota) {
        this.globalStorageQuota = globalStorageQuota;
    }

    public EntityModel getName() {
        return name;
    }

    public void setName(EntityModel name) {
        this.name = name;
    }

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    public ListModel getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel dataCenter) {
        this.dataCenter = dataCenter;
    }

    public EntityModel getGraceStorage() {
        return graceStorage;
    }

    public void setGraceStorage(EntityModel graceStorage) {
        this.graceStorage = graceStorage;
    }

    public EntityModel getThresholdStorage() {
        return thresholdStorage;
    }

    public void setThresholdStorage(EntityModel thresholdStorage) {
        this.thresholdStorage = thresholdStorage;
    }

    public EntityModel getGraceCluster() {
        return graceCluster;
    }

    public void setGraceCluster(EntityModel graceCluster) {
        this.graceCluster = graceCluster;
    }

    public EntityModel getThresholdCluster() {
        return thresholdCluster;
    }

    public void setThresholdCluster(EntityModel thresholdCluster) {
        this.thresholdCluster = thresholdCluster;
    }

    public QuotaModel() {
        setEditQuotaClusterCommand(new UICommand("EditQuotaCluster", this)); //$NON-NLS-1$
        setEditQuotaStorageCommand(new UICommand("EditQuotaStorage", this)); //$NON-NLS-1$

        setName(new EntityModel());
        setDescription(new EntityModel());
        setDataCenter(new ListModel());

        setGraceCluster(new EntityModel());
        getGraceCluster().setEntity(20);
        setThresholdCluster(new EntityModel());
        getThresholdCluster().setEntity(80);
        setGraceStorage(new EntityModel());
        getGraceStorage().setEntity(20);
        setThresholdStorage(new EntityModel());
        getThresholdStorage().setEntity(80);

        setGlobalClusterQuota(new EntityModel());
        setSpecificClusterQuota(new EntityModel());
        getGlobalClusterQuota().setEntity(true);
        getSpecificClusterQuota().setEntity(false);
        getGlobalClusterQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getGlobalClusterQuota().getEntity() == true) {
                    getSpecificClusterQuota().setEntity(false);
                }
            }
        });
        getSpecificClusterQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getSpecificClusterQuota().getEntity() == true) {
                    getGlobalClusterQuota().setEntity(false);
                }
            }
        });

        setGlobalStorageQuota(new EntityModel());
        setSpecificStorageQuota(new EntityModel());
        getGlobalStorageQuota().setEntity(true);
        getSpecificStorageQuota().setEntity(false);
        getGlobalStorageQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getGlobalStorageQuota().getEntity() == true) {
                    getSpecificStorageQuota().setEntity(false);
                }
            }
        });
        getSpecificStorageQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) getSpecificStorageQuota().getEntity() == true) {
                    getGlobalStorageQuota().setEntity(false);
                }
            }
        });

        setQuotaClusters(new ListModel());
        setQuotaStorages(new ListModel());

        ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
        QuotaVdsGroup quotaVdsGroup = new QuotaVdsGroup();
        quotaVdsGroup.setMemSizeMB(QuotaVdsGroup.UNLIMITED_MEM);
        quotaVdsGroup.setVirtualCpu(QuotaVdsGroup.UNLIMITED_VCPU);
        quotaVdsGroup.setMemSizeMBUsage((long) 0);
        quotaVdsGroup.setVirtualCpuUsage(0);
        quotaClusterList.add(quotaVdsGroup);
        getQuotaClusters().setItems(quotaClusterList);

        ArrayList<QuotaStorage> quotaStorgaeList = new ArrayList<QuotaStorage>();
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setStorageSizeGB(QuotaStorage.UNLIMITED);
        quotaStorage.setStorageSizeGBUsage(0.0);
        quotaStorgaeList.add(quotaStorage);
        getQuotaStorages().setItems(quotaStorgaeList);

        setAllDataCenterClusters(new ListModel());
        setAllDataCenterStorages(new ListModel());
    }

    public void editQuotaCluster(QuotaVdsGroup object) {
        this.quotaCluster = object;
        getEditQuotaClusterCommand().Execute();

        EditQuotaClusterModel model = new EditQuotaClusterModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().defineClusterQuotaOnDataCenterTitle());
        model.setEntity(object);
        if (object.getMemSizeMB() == null || object.getMemSizeMB().equals(QuotaVdsGroup.UNLIMITED_MEM)) {
            model.getUnlimitedMem().setEntity(true);
        } else {
            model.getSpecificMem().setEntity(true);
            model.getSpecificMemValue().setEntity(object.getMemSizeMB().toString());
        }
        if (object.getVirtualCpu() == null || object.getVirtualCpu().equals(QuotaVdsGroup.UNLIMITED_VCPU)) {
            model.getUnlimitedCpu().setEntity(true);
        } else {
            model.getSpecificCpu().setEntity(true);
            model.getSpecificCpuValue().setEntity(object.getVirtualCpu().toString());
        }

        setWindow(model);

        UICommand command = new UICommand("OnEditClusterQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        model.getCommands().add(command);
    }

    public void editQuotaStorage(QuotaStorage object) {
        this.quotaStorage = object;
        getEditQuotaStorageCommand().Execute();
        EditQuotaStorageModel model = new EditQuotaStorageModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().defineStorageQuotaOnDataCenterTitle());
        model.setEntity(object);
        if (object.getStorageSizeGB() == null || object.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)) {
            model.getUnlimitedStorage().setEntity(true);
        } else {
            model.getSpecificStorage().setEntity(true);
            model.getSpecificStorageValue().setEntity(object.getStorageSizeGB().toString());
        }

        setWindow(model);

        UICommand command = new UICommand("OnEditStorageQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        model.getCommands().add(command);
    }

    private void onEditClusterQuota() {
        EditQuotaClusterModel model = (EditQuotaClusterModel) getWindow();
        if (!model.Validate()) {
            return;
        }
        if ((Boolean) model.getUnlimitedMem().getEntity()) {
            quotaCluster.setMemSizeMB(QuotaVdsGroup.UNLIMITED_MEM);
        } else {
            quotaCluster.setMemSizeMB(new Long((String) model.getSpecificMemValue().getEntity()));
        }

        if ((Boolean) model.getUnlimitedCpu().getEntity()) {
            quotaCluster.setVirtualCpu(QuotaVdsGroup.UNLIMITED_VCPU);
        } else {
            quotaCluster.setVirtualCpu(new Integer((String) model.getSpecificCpuValue().getEntity()));
        }
        quotaCluster = null;
        setWindow(null);
    }

    private void onEditStorageQuota() {
        EditQuotaStorageModel model = (EditQuotaStorageModel) getWindow();
        if (!model.Validate()) {
            return;
        }
        if ((Boolean) model.getUnlimitedStorage().getEntity()) {
            quotaStorage.setStorageSizeGB(QuotaStorage.UNLIMITED);
        } else {
            quotaStorage.setStorageSizeGB(new Long((String) model.getSpecificStorageValue().getEntity()));
        }
        quotaStorage = null;
        setWindow(null);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.getName().equals("OnEditClusterQuota")) { //$NON-NLS-1$
            onEditClusterQuota();
        } else if (command.getName().equals("OnEditStorageQuota")) { //$NON-NLS-1$
            onEditStorageQuota();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

    public boolean Validate() {
        LengthValidation lenValidation = new LengthValidation();
        lenValidation.setMaxLength(60);
        getName().setIsValid(true);
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), lenValidation });

        IValidation[] graceValidationArr =
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, Integer.MAX_VALUE) };

        IValidation[] thresholdValidationArr =
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, 100) };

        getGraceCluster().ValidateEntity(graceValidationArr);
        getGraceStorage().ValidateEntity(graceValidationArr);
        getThresholdCluster().ValidateEntity(thresholdValidationArr);
        getThresholdStorage().ValidateEntity(thresholdValidationArr);

        boolean graceThreshold = getGraceCluster().getIsValid() &
                getGraceStorage().getIsValid() &
                getThresholdCluster().getIsValid() &
                getThresholdStorage().getIsValid();

        return getName().getIsValid() & graceThreshold & ValidateNotEmpty();
    }

    static final IValidation quotaEmptyValidation = new IValidation() {

        @Override
        public ValidationResult validate(Object value) {
            ValidationResult result = new ValidationResult();
            result.setSuccess(false);
            result.getReasons().clear();
            result.getReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .quotaIsEmptyValidation());
            return result;
        }
    };

    private boolean ValidateNotEmpty() {
        getSpecificClusterQuota().setIsValid(true);
        getSpecificStorageQuota().setIsValid(true);
        if ((Boolean) getGlobalClusterQuota().getEntity() || (Boolean) getGlobalStorageQuota().getEntity()) {
            return true;
        }

        if (getAllDataCenterClusters().getItems() != null) {
            for (QuotaVdsGroup quotaVdsGroup : (ArrayList<QuotaVdsGroup>) getAllDataCenterClusters().getItems()) {
                if (quotaVdsGroup.getMemSizeMB() != null) {
                    return true;
                }
            }
        }

        if (getAllDataCenterStorages().getItems() != null) {
            for (QuotaStorage quotaStorage : (ArrayList<QuotaStorage>) getAllDataCenterStorages().getItems()) {
                if (quotaStorage.getStorageSizeGB() != null) {
                    return true;
                }
            }
        }

        getSpecificClusterQuota().ValidateEntity(new IValidation[] { quotaEmptyValidation });
        getSpecificStorageQuota().ValidateEntity(new IValidation[] { quotaEmptyValidation });

        return false;
    }

    public Integer getGraceClusterAsInteger() {
        return parseInt(getGraceCluster().getEntity());
    }

    public Integer getGraceStorageAsInteger() {
        return parseInt(getGraceStorage().getEntity());
    }

    public Integer getThresholdClusterAsInteger() {
        return parseInt(getThresholdCluster().getEntity());
    }

    public Integer getThresholdStorageAsInteger() {
        return parseInt(getThresholdStorage().getEntity());
    }

    private Integer parseInt(Object entity) {
        if (entity instanceof Integer) {
            return (Integer) entity;
        }
        if (entity == null || !(entity instanceof String)) {
            return null;
        }
        String text = (String) entity;

        try {
            return new Integer(text);
        } catch (Exception e) {
            return null;
        }
    }
}
