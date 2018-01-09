package org.ovirt.engine.ui.uicommonweb.models.vms.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VnicProfileMappingModel extends Model {

    private static final String OK_COMMAND = "OK"; //$NON-NLS-1$
    private static final VnicProfileMappingItemComparator
            VNIC_PROFILE_MAPPING_MODEL_COMPARATOR = new VnicProfileMappingItemComparator();

    private final ListModel<Cluster> targetCluster;

    private final ListModel<VnicProfileMappingItem> mappingModelRows;

    private final RegisterEntityModel originModel;

    private final Map<Cluster, List<VnicProfileMappingItem>> shownMappingRows;

    private Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfiles;

    public VnicProfileMappingModel(RegisterEntityModel originModel,
            Map<Cluster, Set<VnicProfileMappingEntity>> externalVnicProfiles) {
        this.originModel = originModel;
        this.externalVnicProfiles = externalVnicProfiles;
        this.mappingModelRows = new ListModel<>();
        this.targetCluster = new ListModel<>();
        this.shownMappingRows = new HashMap<>();
    }

    @Override
    public void initialize() {
        super.initialize();

        initTargetClusters();
        addCommands();
    }

    private void initTargetClusters() {
        targetCluster.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateMappingRows());
        targetCluster.setItems(externalVnicProfiles.keySet(), Linq.firstOrNull(targetCluster.getItems()));
    }

    private void addCommands() {
        final UICommand okCommand = UICommand.createDefaultOkUiCommand(OK_COMMAND, this);
        getCommands().add(okCommand);
        final UICommand CancelCommand = UICommand.createCancelUiCommand(CANCEL_COMMAND, this);
        getCommands().add(CancelCommand);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (CANCEL_COMMAND.equals(command.getName())) {
            closeDialog();
        } else if (OK_COMMAND.equals(command.getName())) {
            mergeShownRows();
            originModel.setMappingChangeConfirmed(true);
            closeDialog();
        }
    }

    private void mergeShownRows() {
        for (Entry<Cluster, List<VnicProfileMappingItem>> showCluster : shownMappingRows.entrySet()) {
            final Cluster cluster = showCluster.getKey();
            final List<VnicProfileMappingItem> showClusterRows = showCluster.getValue();
            final Set<VnicProfileMappingEntity> existingMappings;
            if (externalVnicProfiles.containsKey(cluster)) {
                existingMappings = externalVnicProfiles.get(cluster);
            } else {
                existingMappings = new HashSet<>();
                externalVnicProfiles.put(cluster, existingMappings);
            }
            for (VnicProfileMappingItem shownRow : showClusterRows) {
                final VnicProfileMappingEntity shownMapping = shownRow.getEntity();
                addOrReplace(existingMappings, shownMapping);
            }
        }
    }

    private <T> void addOrReplace(Set<T> set, T e) {
        // warning: the remove() and add() methods of the Set use the equals of
        // {@link VnicProfileMappingEntity} which only compares the source profile
        set.remove(e);
        set.add(e);
    }

    private void closeDialog() {
        originModel.setWindow(null);
    }

    private void updateMappingRows() {

        startProgress();

        AsyncDataProvider.getInstance().getVnicProfilesByClusterId(
                new AsyncQuery<>(returnValue -> {
                    final List<VnicProfileView> vnicProfiles = new ArrayList<>();
                    vnicProfiles.add(VnicProfileView.EMPTY);
                    vnicProfiles.addAll(returnValue);
                    Collections.sort(vnicProfiles, Linq.VnicProfileViewComparator);

                    populateMappingRows(vnicProfiles);

                    stopProgress();
                }),
                targetCluster.getSelectedItem().getId());
    }

    private void populateMappingRows(List<VnicProfileView> targetVnicProfiles) {
        final Cluster selectedCluster = targetCluster.getSelectedItem();
        final List<VnicProfileMappingItem> mappingItems;
        if (shownMappingRows.containsKey(selectedCluster)) {
            mappingItems = shownMappingRows.get(selectedCluster);
        } else {
            final Set<VnicProfileMappingEntity> clusterMappings = externalVnicProfiles.get(selectedCluster);
            mappingItems = new ArrayList<>();

            for (VnicProfileMappingEntity mappingEntity : clusterMappings) {
                mappingItems.add(createVnicProfileMappingItem(targetVnicProfiles, mappingEntity));
            }
            Collections.sort(mappingItems, VNIC_PROFILE_MAPPING_MODEL_COMPARATOR);
            shownMappingRows.put(selectedCluster, mappingItems);
        }
        mappingModelRows.setItems(mappingItems);
    }

    private VnicProfileMappingItem createVnicProfileMappingItem(List<VnicProfileView> targetVnicProfiles,
            VnicProfileMappingEntity mappingEntity) {
        final VnicProfileMappingItem vnicProfileMappingItem =
                new VnicProfileMappingItem(mappingEntity, targetVnicProfiles);
        vnicProfileMappingItem.initialize();
        return vnicProfileMappingItem;
    }

    // in use by view
    @SuppressWarnings("unused")
    public ListModel<Cluster> getTargetCluster() {
        return targetCluster;
    }

    public ListModel<VnicProfileMappingItem> getMappingModelRows() {
        return mappingModelRows;
    }

    @Override public String getTitle() {
        return ConstantsManager.getInstance().getConstants().vnicProfilesMapping();
    }
}
