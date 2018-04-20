package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class PreviewSnapshotModel extends Model {
    private SnapshotModel snapshotModel;
    private ListModel snapshots;
    private Map<Guid, ListModel> diskSnapshotsMap;
    private Map<Guid, Guid> snapshotLeaseDomainsMap;
    private Guid vmId;
    private Guid activeSnapshotId;

    public PreviewSnapshotModel() {
        setSnapshots(new ListModel());
        setDiskSnapshotsMap(new HashMap<Guid, ListModel>());
        setSnapshotLeaseDomainsMap(new HashMap<Guid, Guid>());
    }

    public PreviewSnapshotModel(VM vm, Guid userSelectedSnapshotId) {
        this();
        setVmId(vm.getId());
    }

    @Override
    public void initialize() {
        Frontend.getInstance().runQuery(QueryType.GetAllVmSnapshotsWithLeasesFromConfigurationByVmId,
                new IdQueryParameters(vmId), new AsyncQuery<QueryReturnValue>(response -> {
                    if (response != null && response.getSucceeded()) {
                        ArrayList<SnapshotModel> snapshotModels = new ArrayList<>();
                        Map<Snapshot, Guid> snapshotLeaseStorageDomaindIdMap = response.getReturnValue();
                        List<Snapshot> snapshots = new ArrayList<>(snapshotLeaseStorageDomaindIdMap.keySet());
                        sortSnapshots(snapshots);

                        Guid userSelectedSnapshotId = getSnapshotModel().getEntity().getId();

                        for (Snapshot snapshot : snapshots) {
                            Guid leaseStorageDomainId = snapshotLeaseStorageDomaindIdMap.get(snapshot);
                            SnapshotModel snapshotModel = new SnapshotModel();
                            snapshotModel.setEntity(snapshot);
                            snapshotModel.getMemory().setEntity(false);
                            snapshotModel.setDisks((ArrayList<DiskImage>) snapshot.getDiskImages());
                            snapshotModels.add(snapshotModel);

                            if (snapshot.getType() == Snapshot.SnapshotType.ACTIVE) {
                                activeSnapshotId = snapshot.getId();
                            }

                            if (leaseStorageDomainId != null) {
                                snapshotModel.getLeaseExists().setEntity(snapshot.getId() != activeSnapshotId);
                                getSnapshotLeaseDomainsMap().put(snapshot.getId(), leaseStorageDomainId);
                            } else {
                                snapshotModel.getLeaseExists().setEntity(null);
                                getSnapshotLeaseDomainsMap().put(snapshot.getId(), null);
                            }
                        }

                        getSnapshots().setItems(snapshotModels);
                        updateDiskSnapshotsMap();

                        // Update disk-snapshots map
                        updateDiskSnapshotsMap();

                        // First selecting the active snapshot for ensuring default disks selection
                        // (i.e. when some disks are missing from the selected snapshot,
                        // the corresponding disks from the active snapshot should be selected).
                        selectSnapshot(activeSnapshotId);

                        // Selecting the snapshot the was selected by the user
                        selectSnapshot(userSelectedSnapshotId);
                    }
                }));
    }

    // Sort snapshots by creation date (keep active snapshot on top)
    private void sortSnapshots(List<Snapshot> snapshots) {
        Collections.sort(snapshots,
                Comparator.comparing((Snapshot s) -> s.getType() == Snapshot.SnapshotType.ACTIVE).reversed()
                        .thenComparing(Linq.SnapshotByCreationDateCommparer));
    }

    public SnapshotModel getSnapshotModel() {
        return snapshotModel;
    }

    public void setSnapshotModel(SnapshotModel snapshotModel) {
        this.snapshotModel = snapshotModel;
    }

    public ListModel<SnapshotModel> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(ListModel snapshots) {
        this.snapshots = snapshots;
    }

    public Map<Guid, ListModel> getDiskSnapshotsMap() {
        return diskSnapshotsMap;
    }

    public void setDiskSnapshotsMap(Map<Guid, ListModel> diskSnapshotsMap) {
        this.diskSnapshotsMap = diskSnapshotsMap;
    }

    public Map<Guid, Guid> getSnapshotLeaseDomainsMap() {
        return snapshotLeaseDomainsMap;
    }

    public void setSnapshotLeaseDomainsMap(Map<Guid, Guid> snapshotLeaseDomainsMap) {
        this.snapshotLeaseDomainsMap = snapshotLeaseDomainsMap;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public List<DiskImage> getAllDisks() {
        Map<Guid, DiskImage> disksMap = new HashMap<>();

        for (SnapshotModel snapshotModel : (List<SnapshotModel>) snapshots.getItems()) {
            for (DiskImage disk : snapshotModel.getEntity().getDiskImages()) {
                if (!disksMap.containsKey(disk.getId())) {
                    disksMap.put(disk.getId(), disk);
                }
            }
        }

        return new ArrayList(disksMap.values());
    }

    public List<DiskImage> getSelectedDisks() {
        List<DiskImage> disks = new ArrayList<>();

        for (ListModel diskListModel : diskSnapshotsMap.values()) {
            DiskImage selectedImage = (DiskImage) diskListModel.getSelectedItem();
            if (selectedImage != null) {
                disks.add(selectedImage);
            }
        }

        return disks;
    }

    public boolean isSnapshotsContainsLeases() {
        return getSnapshots().getItems()
                .stream()
                .anyMatch(model -> model.getLeaseExists() != null);
    }

    public Guid getSelectedLease() {
        SnapshotModel selectedLeaseSnapshotModel = getSnapshots().getItems()
                .stream()
                .filter(model -> model.getLeaseExists().getEntity() != null)
                .filter(model -> model.getLeaseExists().getEntity())
                .findFirst()
                .orElse(null);

        return selectedLeaseSnapshotModel != null ?
                getSnapshotLeaseDomainsMap().get(selectedLeaseSnapshotModel.getEntity().getId()) :
                null;
    }

     private void updateDiskSnapshotsMap() {
        if (snapshots.getItems() == null) {
            return;
        }

        for (SnapshotModel snapshotModel : (List<SnapshotModel>) snapshots.getItems()) {
            for (DiskImage diskImage : snapshotModel.getEntity().getDiskImages()) {
                ListModel disksListModel;
                if (diskSnapshotsMap.containsKey(diskImage.getId())) {
                    disksListModel = diskSnapshotsMap.get(diskImage.getId());
                    ((ArrayList<DiskImage>) disksListModel.getItems()).add(diskImage);
                } else {
                    disksListModel = new ListModel();
                    disksListModel.setItems(new ArrayList<>(Arrays.asList(diskImage)));
                }
                diskSnapshotsMap.put(diskImage.getId(), disksListModel);
            }
        }
    }

    public void clearSelection(Guid selectedSnapshotModel) {
        clearDisksSelection();
        clearMemorySelection();
        clearLeaseSelection(selectedSnapshotModel);
    }

    public void clearDisksSelection() {
        for (ListModel diskListModel : diskSnapshotsMap.values()) {
            diskListModel.setSelectedItem(null);
        }
    }

    public void clearMemorySelection() {
        for (SnapshotModel snapshotModel : getSnapshots().getItems()) {
            snapshotModel.getMemory().setEntity(false);
        }
    }

    public void clearLeaseSelection(Guid selectedSnapshotModel) {
        for (SnapshotModel snapshotModel : getSnapshots().getItems()) {
            if (!selectedSnapshotModel.equals(snapshotModel.getEntity().getId())
                    && snapshotModel.getLeaseExists().getEntity() != null) {
                snapshotModel.getLeaseExists().setEntity(false);
            }
        }
    }

    public SnapshotModel getSnapshotModelById(Guid id) {
        for (SnapshotModel snapshotModel : getSnapshots().getItems()) {
            if (snapshotModel.getEntity().getId().equals(id)) {
                return snapshotModel;
            }
        }
        return null;
    }

    public SnapshotModel getActiveSnapshotModel() {
        return getSnapshotModelById(activeSnapshotId);
    }

    public void selectSnapshot(Guid id) {
        SnapshotModel snapshotModel = getSnapshotModelById(id);

        if (snapshotModel == null) {
            return;
        }

        for (SnapshotModel model : getSnapshots().getItems()) {
            if (model.getEntity().getId().equals(id) && model.getLeaseExists().getEntity() != null) {
                model.getLeaseExists().setEntity(true);
            } else if (model.getLeaseExists().getEntity() != null) {
                model.getLeaseExists().setEntity(false);
            }
        }

        getSnapshots().setSelectedItem(snapshotModel);
        setSnapshotModel(snapshotModel);
        for (DiskImage diskImage : snapshotModel.getDisks()) {
            ListModel diskListModel = diskSnapshotsMap.get(diskImage.getId());
            diskListModel.setSelectedItem(diskImage);
        }
    }
}
