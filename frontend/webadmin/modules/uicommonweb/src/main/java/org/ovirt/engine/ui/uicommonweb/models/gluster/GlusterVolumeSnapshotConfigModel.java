package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GlusterVolumeSnapshotConfigModel extends Model {
    private EntityModel<String> dataCenter;
    private EntityModel<String> clusterName;
    private EntityModel<String> volumeName;
    private ListModel<EntityModel<VolumeSnapshotOptionModel>> configOptions;
    private Map<String, String> existingVolumeConfigs = new HashMap<>();

    public EntityModel<String> getDataCenter() {
        return this.dataCenter;
    }

    public void setDataCenter(EntityModel<String> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public EntityModel<String> getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(EntityModel<String> cluster) {
        this.clusterName = cluster;
    }

    public EntityModel<String> getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(EntityModel<String> volumeName) {
        this.volumeName = volumeName;
    }

    public ListModel<EntityModel<VolumeSnapshotOptionModel>> getConfigOptions() {
        return configOptions;
    }

    public void setConfigOptions(ListModel<EntityModel<VolumeSnapshotOptionModel>> configOptions) {
        this.configOptions = configOptions;
    }

    private GlusterVolumeEntity selectedVolumeEntity;

    public GlusterVolumeEntity getSelectedVolumeEntity() {
        return this.selectedVolumeEntity;
    }

    public void setSelectedVolumeEntity(GlusterVolumeEntity volume) {
        this.selectedVolumeEntity = volume;
    }

    private boolean isVolumeTabAvailable;

    public boolean getIsVolumeTabAvailable() {
        return this.isVolumeTabAvailable;
    }

    public void setIsVolumeTabAvailable(boolean value) {
        this.isVolumeTabAvailable = value;
    }

    public String getExistingVolumeConfigValue(String cfgName) {
        return existingVolumeConfigs.get(cfgName);
    }

    public GlusterVolumeSnapshotConfigModel(GlusterVolumeEntity volumeEntity) {
        setSelectedVolumeEntity(volumeEntity);
        init();
    }

    private void init() {
        setDataCenter(new EntityModel<String>());
        setClusterName(new EntityModel<String>());
        setVolumeName(new EntityModel<String>());
        setConfigOptions(new ListModel<EntityModel<VolumeSnapshotOptionModel>>());
        populateConfigOptions();
    }

    public boolean validate() {
        boolean isValid = true;
        setMessage(null);
        Iterable<EntityModel<VolumeSnapshotOptionModel>> items = getConfigOptions().getItems();
        for (EntityModel<VolumeSnapshotOptionModel> model : items) {
            if (model.getEntity().getOptionValue().trim().length() == 0) {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .volumeSnapshotOptionValueEmpty(model.getEntity().getOptionName()));
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    private void populateConfigOptions() {
        startProgress();

        AsyncDataProvider.getInstance().getGlusterSnapshotConfig(new AsyncQuery<>(returnValue -> {
            Pair<List<GlusterVolumeSnapshotConfig>, List<GlusterVolumeSnapshotConfig>> configs =
                    returnValue.getReturnValue();
            Map<String, String> clusterConfigOptions = new HashMap<>();
            Map<String, String> volumeConfigOptions = new HashMap<>();
            for (GlusterVolumeSnapshotConfig config : configs.getFirst()) {
                clusterConfigOptions.put(config.getParamName(), config.getParamValue());
            }
            for (GlusterVolumeSnapshotConfig config : configs.getSecond()) {
                volumeConfigOptions.put(config.getParamName(), config.getParamValue());
            }
            List<EntityModel<VolumeSnapshotOptionModel>> coll = new ArrayList<>();
            for (Map.Entry<String, String> entry : volumeConfigOptions.entrySet()) {
                EntityModel<VolumeSnapshotOptionModel> cfgModel = new EntityModel<>();
                VolumeSnapshotOptionModel option = new VolumeSnapshotOptionModel();
                option.setOptionName(entry.getKey());
                option.setOptionValue(entry.getValue());
                option.setCorrespodingClusterValue(clusterConfigOptions.get(entry.getKey()));
                cfgModel.setEntity(option);
                existingVolumeConfigs.put(entry.getKey(), entry.getValue());
                coll.add(cfgModel);
            }

            getConfigOptions().setItems(coll);
        }),
                selectedVolumeEntity.getClusterId(),
                selectedVolumeEntity.getId());

        stopProgress();
    }
}
