package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GlusterClusterSnapshotConfigModel extends Model {
    private EntityModel<String> dataCenter;
    private ListModel<Cluster> clusters;
    private ListModel<EntityModel<GlusterVolumeSnapshotConfig>> clusterConfigOptions;
    private Map<String, String> existingClusterConfigs = new HashMap<>();

    public EntityModel<String> getDataCenter() {
        return this.dataCenter;
    }

    public void setDataCenter(EntityModel<String> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ListModel<Cluster> getClusters() {
        return this.clusters;
    }

    public void setClusters(ListModel<Cluster> clusters) {
        this.clusters = clusters;
    }

    public ListModel<EntityModel<GlusterVolumeSnapshotConfig>> getClusterConfigOptions() {
        return clusterConfigOptions;
    }

    public void setClusterConfigOptions(ListModel<EntityModel<GlusterVolumeSnapshotConfig>> clusterConfigOptions) {
        this.clusterConfigOptions = clusterConfigOptions;
    }

    public String getExistingClusterConfigValue(String cfgName) {
        return existingClusterConfigs.get(cfgName);
    }

    public GlusterClusterSnapshotConfigModel() {
        init();
    }

    private void init() {
        setDataCenter(new EntityModel<String>());
        setClusters(new ListModel<Cluster>());
        getClusters().getSelectedItemChangedEvent().addListener((ev, sender, args) -> clusterSelectedItemChanged());
        setClusterConfigOptions(new ListModel<EntityModel<GlusterVolumeSnapshotConfig>>());
    }

    public boolean validate() {
        boolean isValid = true;
        setMessage(null);
        if(getClusterConfigOptions().getItems() == null || getClusterConfigOptions().getItems().isEmpty()) {
            setMessage(ConstantsManager.getInstance().getMessages().clusterSnapshotOptionNotExist());
            return false;
        }

        for (EntityModel<GlusterVolumeSnapshotConfig> model : getClusterConfigOptions().getItems()) {
            GlusterVolumeSnapshotConfig option = model.getEntity();
            if (option.getParamValue().trim().length() == 0) {
                setMessage(ConstantsManager.getInstance()
                        .getMessages()
                        .clusterSnapshotOptionValueEmpty(option.getParamName()));
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    private void clusterSelectedItemChanged() {
        Cluster selectedCluster = getClusters().getSelectedItem();
        if (selectedCluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterSnapshotConfig(new AsyncQuery<>(new AsyncCallback<QueryReturnValue>() {

            @Override
            public void onSuccess(QueryReturnValue returnValue) {
                Pair<List<GlusterVolumeSnapshotConfig>, List<GlusterVolumeSnapshotConfig>> configs =
                        returnValue.getReturnValue();
                if (configs != null) {
                    List<GlusterVolumeSnapshotConfig> clusterConfigOptions = configs.getFirst();
                    Collections.sort(clusterConfigOptions, Comparator.comparing(GlusterVolumeSnapshotConfig::getParamName));
                    setModelItems(getClusterConfigOptions(), clusterConfigOptions, existingClusterConfigs);
                } else {
                    getClusterConfigOptions().setItems(null);
                }
            }

            private void setModelItems(ListModel<EntityModel<GlusterVolumeSnapshotConfig>> listModel,
                    List<GlusterVolumeSnapshotConfig> cfgs, Map<String, String> fetchedCfgsBackup) {
                List<EntityModel<GlusterVolumeSnapshotConfig>> coll = new ArrayList<>();
                for (GlusterVolumeSnapshotConfig cfg : cfgs) {
                    EntityModel<GlusterVolumeSnapshotConfig> cfgModel = new EntityModel<>();
                    cfgModel.setEntity(cfg);
                    fetchedCfgsBackup.put(cfg.getParamName(), cfg.getParamValue());
                    coll.add(cfgModel);
                }

                // set the entity items
                listModel.setItems(coll);
            }
        }),
                selectedCluster.getId(),
                null);
    }
}
