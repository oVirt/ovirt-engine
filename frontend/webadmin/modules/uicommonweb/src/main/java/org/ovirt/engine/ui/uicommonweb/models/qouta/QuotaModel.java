package org.ovirt.engine.ui.uicommonweb.models.qouta;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class QuotaModel extends EntityModel {

    UICommand editQuotaClusterCommand;
    UICommand editQuotaStorageCommand;

    EntityModel name;
    EntityModel description;
    ListModel dataCenter;

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

    public QuotaModel() {
        setEditQuotaClusterCommand(new UICommand("EditQuotaCluster", this));
        setEditQuotaStorageCommand(new UICommand("EditQuotaStorage", this));

        setName(new EntityModel());
        setDescription(new EntityModel());
        setDataCenter(new ListModel());

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
        quotaVdsGroup.setMemSizeMB((long) -1);
        quotaVdsGroup.setVirtualCpu(-1);
        quotaVdsGroup.setMemSizeMBUsage((long) 0);
        quotaVdsGroup.setVirtualCpuUsage(0);
        quotaClusterList.add(quotaVdsGroup);
        getQuotaClusters().setItems(quotaClusterList);

        ArrayList<QuotaStorage> quotaStorgaeList = new ArrayList<QuotaStorage>();
        QuotaStorage quotaStorage = new QuotaStorage();
        quotaStorage.setStorageSizeGB((long) -1);
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
        model.setTitle("Define Cluster Quota on Data Center");
        model.setEntity(object);
        if (object.getMemSizeMB() == null || object.getMemSizeMB() == -1) {
            model.getUnlimitedMem().setEntity(true);
        } else {
            model.getSpecificMem().setEntity(true);
            model.getSpecificMemValue().setEntity(object.getMemSizeMB().toString());
        }
        if (object.getVirtualCpu() == null || object.getVirtualCpu() == -1) {
            model.getUnlimitedCpu().setEntity(true);
        } else {
            model.getSpecificCpu().setEntity(true);
            model.getSpecificCpuValue().setEntity(object.getVirtualCpu().toString());
        }

        setWindow(model);

        UICommand command = new UICommand("OnEditClusterQuota", this);
        command.setTitle("OK");
        command.setIsDefault(true);
        model.getCommands().add(command);
        command = new UICommand("Cancel", this);
        command.setTitle("Cancel");
        model.getCommands().add(command);
    }

    public void editQuotaStorage(QuotaStorage object) {
        this.quotaStorage = object;
        getEditQuotaStorageCommand().Execute();
        EditQuotaStorageModel model = new EditQuotaStorageModel();
        model.setTitle("Define Storage Quota on Data Center");
        model.setEntity(object);
        if (object.getStorageSizeGB() == null || object.getStorageSizeGB() == -1) {
            model.getUnlimitedStorage().setEntity(true);
        } else {
            model.getSpecificStorage().setEntity(true);
            model.getSpecificStorageValue().setEntity(object.getStorageSizeGB().toString());
        }

        setWindow(model);

        UICommand command = new UICommand("OnEditStorageQuota", this);
        command.setTitle("OK");
        command.setIsDefault(true);
        model.getCommands().add(command);
        command = new UICommand("Cancel", this);
        command.setTitle("Cancel");
        model.getCommands().add(command);
    }

    private void onEditClusterQuota() {
        EditQuotaClusterModel model = (EditQuotaClusterModel) getWindow();
        if (!model.Validate()) {
            return;
        }
        if ((Boolean) model.getUnlimitedMem().getEntity()) {
            quotaCluster.setMemSizeMB((long) -1);
        } else {
            quotaCluster.setMemSizeMB(new Long((String) model.getSpecificMemValue().getEntity()));
        }

        if ((Boolean) model.getUnlimitedCpu().getEntity()) {
            quotaCluster.setVirtualCpu(-1);
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
            quotaStorage.setStorageSizeGB((long) -1);
        } else {
            quotaStorage.setStorageSizeGB(new Long((String) model.getSpecificStorageValue().getEntity()));
        }
        quotaStorage = null;
        setWindow(null);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.getName().equals("OnEditClusterQuota")) {
            onEditClusterQuota();
        } else if (command.getName().equals("OnEditStorageQuota")) {
            onEditStorageQuota();
        } else if (command.getName().equals("Cancel")) {
            setWindow(null);
        }
    }

    public boolean Validate() {
        LengthValidation lenValidation = new LengthValidation();
        lenValidation.setMaxLength(60);
        getName().setIsValid(true);
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), lenValidation });

        return getName().getIsValid();
    }
}
