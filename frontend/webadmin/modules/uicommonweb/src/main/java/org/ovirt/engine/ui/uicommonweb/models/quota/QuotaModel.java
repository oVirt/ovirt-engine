package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaModel extends EntityModel<Quota> {

    UICommand editQuotaClusterCommand;
    UICommand editQuotaStorageCommand;

    EntityModel<String> name;
    EntityModel<String> description;
    ListModel<StoragePool> dataCenter;

    EntityModel<Boolean> copyPermissions;

    EntityModel<Integer> graceStorage;
    EntityModel<Integer> thresholdStorage;
    EntityModel<Integer> graceCluster;
    EntityModel<Integer> thresholdCluster;

    EntityModel<Boolean> specificClusterQuota;
    EntityModel<Boolean> globalClusterQuota;

    EntityModel<Boolean> specificStorageQuota;
    EntityModel<Boolean> globalStorageQuota;

    ListModel<QuotaCluster> quotaClusters;
    ListModel<QuotaStorage> quotaStorages;

    ListModel<QuotaCluster> allDataCenterClusters;
    ListModel<QuotaStorage> allDataCenterStorages;

    private QuotaStorage quotaStorage;
    private QuotaCluster quotaCluster;

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

    public ListModel<QuotaCluster> getQuotaClusters() {
        return quotaClusters;
    }

    public void setQuotaClusters(ListModel<QuotaCluster> quotaClusters) {
        this.quotaClusters = quotaClusters;
    }

    public ListModel<QuotaStorage> getQuotaStorages() {
        return quotaStorages;
    }

    public void setQuotaStorages(ListModel<QuotaStorage> quotaStorages) {
        this.quotaStorages = quotaStorages;
    }

    public ListModel<QuotaCluster> getAllDataCenterClusters() {
        return allDataCenterClusters;
    }

    public void setAllDataCenterClusters(ListModel<QuotaCluster> allDataCenterClusters) {
        this.allDataCenterClusters = allDataCenterClusters;
    }

    public ListModel<QuotaStorage> getAllDataCenterStorages() {
        return allDataCenterStorages;
    }

    public void setAllDataCenterStorages(ListModel<QuotaStorage> allDataCenterStorages) {
        this.allDataCenterStorages = allDataCenterStorages;
    }

    public EntityModel<Boolean> getSpecificClusterQuota() {
        return specificClusterQuota;
    }

    public void setSpecificClusterQuota(EntityModel<Boolean> specificClusterQuota) {
        this.specificClusterQuota = specificClusterQuota;
    }

    public EntityModel<Boolean> getGlobalClusterQuota() {
        return globalClusterQuota;
    }

    public void setGlobalClusterQuota(EntityModel<Boolean> globalClusterQuota) {
        this.globalClusterQuota = globalClusterQuota;
    }

    public EntityModel<Boolean> getSpecificStorageQuota() {
        return specificStorageQuota;
    }

    public void setSpecificStorageQuota(EntityModel<Boolean> specificStorageQuota) {
        this.specificStorageQuota = specificStorageQuota;
    }

    public EntityModel<Boolean> getGlobalStorageQuota() {
        return globalStorageQuota;
    }

    public void setGlobalStorageQuota(EntityModel<Boolean> globalStorageQuota) {
        this.globalStorageQuota = globalStorageQuota;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public EntityModel<Boolean> getCopyPermissions() {
        return copyPermissions;
    }

    public void setCopyPermissions(EntityModel<Boolean> copyPermissions) {
        this.copyPermissions = copyPermissions;
    }

    public EntityModel<Integer> getGraceStorage() {
        return graceStorage;
    }

    public void setGraceStorage(EntityModel<Integer> graceStorage) {
        this.graceStorage = graceStorage;
    }

    public EntityModel<Integer> getThresholdStorage() {
        return thresholdStorage;
    }

    public void setThresholdStorage(EntityModel<Integer> thresholdStorage) {
        this.thresholdStorage = thresholdStorage;
    }

    public EntityModel<Integer> getGraceCluster() {
        return graceCluster;
    }

    public void setGraceCluster(EntityModel<Integer> graceCluster) {
        this.graceCluster = graceCluster;
    }

    public EntityModel<Integer> getThresholdCluster() {
        return thresholdCluster;
    }

    public void setThresholdCluster(EntityModel<Integer> thresholdCluster) {
        this.thresholdCluster = thresholdCluster;
    }

    public QuotaModel() {
        setEditQuotaClusterCommand(new UICommand("EditQuotaCluster", this)); //$NON-NLS-1$
        setEditQuotaStorageCommand(new UICommand("EditQuotaStorage", this)); //$NON-NLS-1$

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setDataCenter(new ListModel<StoragePool>());
        setCopyPermissions(new EntityModel<>(false));
        getCopyPermissions().setIsAvailable(false); // visible on copy quota.
        setGraceCluster(new EntityModel<Integer>());
        getGraceCluster().setEntity(20);
        setThresholdCluster(new EntityModel<Integer>());
        getThresholdCluster().setEntity(80);
        setGraceStorage(new EntityModel<Integer>());
        getGraceStorage().setEntity(20);
        setThresholdStorage(new EntityModel<Integer>());
        getThresholdStorage().setEntity(80);

        setGlobalClusterQuota(new EntityModel<Boolean>());
        setSpecificClusterQuota(new EntityModel<Boolean>());
        getGlobalClusterQuota().setEntity(true);
        getSpecificClusterQuota().setEntity(false);
        getGlobalClusterQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getGlobalClusterQuota().getEntity()) {
                getSpecificClusterQuota().setEntity(false);
            }
        });
        getSpecificClusterQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getSpecificClusterQuota().getEntity()) {
                getGlobalClusterQuota().setEntity(false);
            }
        });

        setGlobalStorageQuota(new EntityModel<Boolean>());
        setSpecificStorageQuota(new EntityModel<Boolean>());
        getGlobalStorageQuota().setEntity(true);
        getSpecificStorageQuota().setEntity(false);
        getGlobalStorageQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getGlobalStorageQuota().getEntity()) {
                getSpecificStorageQuota().setEntity(false);
            }
        });
        getSpecificStorageQuota().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getSpecificStorageQuota().getEntity()) {
                getGlobalStorageQuota().setEntity(false);
            }
        });

        setQuotaClusters(new ListModel<QuotaCluster>());
        setQuotaStorages(new ListModel<QuotaStorage>());

        ArrayList<QuotaCluster> quotaClusterList = new ArrayList<>();
        QuotaCluster quotaCluster = new QuotaCluster();
        quotaCluster.setMemSizeMB(QuotaCluster.UNLIMITED_MEM);
        quotaCluster.setVirtualCpu(QuotaCluster.UNLIMITED_VCPU);
        quotaCluster.setMemSizeMBUsage((long) 0);
        quotaCluster.setVirtualCpuUsage(0);
        quotaClusterList.add(quotaCluster);
        getQuotaClusters().setItems(quotaClusterList);

        ArrayList<QuotaStorage> quotaStorgaeList = new ArrayList<>();
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setStorageSizeGB(QuotaStorage.UNLIMITED);
        quotaStorage.setStorageSizeGBUsage(0.0);
        quotaStorgaeList.add(quotaStorage);
        getQuotaStorages().setItems(quotaStorgaeList);

        setAllDataCenterClusters(new ListModel<QuotaCluster>());
        setAllDataCenterStorages(new ListModel<QuotaStorage>());
    }

    public void editQuotaCluster(QuotaCluster object) {
        this.quotaCluster = object;
        getEditQuotaClusterCommand().execute();

        EditQuotaClusterModel model = new EditQuotaClusterModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().defineClusterQuotaOnDataCenterTitle());
        model.setEntity(object);
        if (object.getMemSizeMB() == null || object.getMemSizeMB().equals(QuotaCluster.UNLIMITED_MEM)) {
            model.getUnlimitedMem().setEntity(true);
        } else {
            model.getSpecificMem().setEntity(true);
            model.getSpecificMemValue().setEntity(object.getMemSizeMB());
        }
        if (object.getVirtualCpu() == null || object.getVirtualCpu().equals(QuotaCluster.UNLIMITED_VCPU)) {
            model.getUnlimitedCpu().setEntity(true);
        } else {
            model.getSpecificCpu().setEntity(true);
            model.getSpecificCpuValue().setEntity(object.getVirtualCpu());
        }

        setWindow(model);

        UICommand command = UICommand.createDefaultOkUiCommand("OnEditClusterQuota", this); //$NON-NLS-1$
        model.getCommands().add(command);
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public void editQuotaStorage(QuotaStorage object) {
        this.quotaStorage = object;
        getEditQuotaStorageCommand().execute();
        EditQuotaStorageModel model = new EditQuotaStorageModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().defineStorageQuotaOnDataCenterTitle());
        model.setEntity(object);
        if (object.getStorageSizeGB() == null || object.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)) {
            model.getUnlimitedStorage().setEntity(true);
        } else {
            model.getSpecificStorage().setEntity(true);
            model.getSpecificStorageValue().setEntity(object.getStorageSizeGB());
        }

        setWindow(model);

        UICommand command = UICommand.createDefaultOkUiCommand("OnEditStorageQuota", this); //$NON-NLS-1$
        model.getCommands().add(command);
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onEditClusterQuota() {
        EditQuotaClusterModel model = (EditQuotaClusterModel) getWindow();
        if (!model.validate()) {
            return;
        }
        if (model.getUnlimitedMem().getEntity()) {
            quotaCluster.setMemSizeMB(QuotaCluster.UNLIMITED_MEM);
        } else {
            quotaCluster.setMemSizeMB(model.getSpecificMemValue().getEntity());
        }

        if (model.getUnlimitedCpu().getEntity()) {
            quotaCluster.setVirtualCpu(QuotaCluster.UNLIMITED_VCPU);
        } else {
            quotaCluster.setVirtualCpu(model.getSpecificCpuValue().getEntity());
        }
        quotaCluster = null;
        setWindow(null);
    }

    private void onEditStorageQuota() {
        EditQuotaStorageModel model = (EditQuotaStorageModel) getWindow();
        if (!model.validate()) {
            return;
        }
        if (model.getUnlimitedStorage().getEntity()) {
            quotaStorage.setStorageSizeGB(QuotaStorage.UNLIMITED);
        } else {
            quotaStorage.setStorageSizeGB(model.getSpecificStorageValue().getEntity());
        }
        quotaStorage = null;
        setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.getName().equals("OnEditClusterQuota")) { //$NON-NLS-1$
            onEditClusterQuota();
        } else if (command.getName().equals("OnEditStorageQuota")) { //$NON-NLS-1$
            onEditStorageQuota();
        } else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

    public boolean validate() {
        LengthValidation lenValidation = new LengthValidation();
        lenValidation.setMaxLength(60);
        getName().setIsValid(true);
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), lenValidation });

        IValidation[] graceValidationArr =
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, Integer.MAX_VALUE) };

        IValidation[] thresholdValidationArr =
                new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, 100) };

        getGraceCluster().validateEntity(graceValidationArr);
        getGraceStorage().validateEntity(graceValidationArr);
        getThresholdCluster().validateEntity(thresholdValidationArr);
        getThresholdStorage().validateEntity(thresholdValidationArr);

        boolean graceThreshold = getGraceCluster().getIsValid() &
                getGraceStorage().getIsValid() &
                getThresholdCluster().getIsValid() &
                getThresholdStorage().getIsValid();

        return getName().getIsValid() & graceThreshold & validateNotEmpty();
    }

    static final IValidation quotaEmptyValidation = value -> {
        ValidationResult result = new ValidationResult();
        result.setSuccess(false);
        result.getReasons().clear();
        result.getReasons().add(ConstantsManager.getInstance()
                .getConstants()
                .quotaIsEmptyValidation());
        return result;
    };

    private boolean validateNotEmpty() {
        getSpecificClusterQuota().setIsValid(true);
        getSpecificStorageQuota().setIsValid(true);
        if (getGlobalClusterQuota().getEntity() || getGlobalStorageQuota().getEntity()) {
            return true;
        }

        if (getAllDataCenterClusters().getItems() != null) {
            for (QuotaCluster quotaCluster : getAllDataCenterClusters().getItems()) {
                if (quotaCluster.getMemSizeMB() != null) {
                    return true;
                }
            }
        }

        if (getAllDataCenterStorages().getItems() != null) {
            for (QuotaStorage quotaStorage : getAllDataCenterStorages().getItems()) {
                if (quotaStorage.getStorageSizeGB() != null) {
                    return true;
                }
            }
        }

        getSpecificClusterQuota().validateEntity(new IValidation[] { quotaEmptyValidation });
        getSpecificStorageQuota().validateEntity(new IValidation[] { quotaEmptyValidation });

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
        if (!(entity instanceof String)) {
            return null;
        }
        String text = (String) entity;

        try {
            return Integer.valueOf(text);
        } catch (Exception e) {
            return null;
        }
    }
}
