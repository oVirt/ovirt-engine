package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class PreviewSnapshotModel extends Model {
    private SnapshotModel snapshotModel;
    private ListModel snapshots;
    private Map<Guid, ListModel> diskSnapshotsMap;
    private Guid vmId;
    private Guid activeSnapshotId;

    public PreviewSnapshotModel() {
        setSnapshots(new ListModel());
        setDiskSnapshotsMap(new HashMap<Guid, ListModel>());
    }

    @Override
    public void initialize() {
        Frontend.getInstance().runQuery(VdcQueryType.GetAllVmSnapshotsFromConfigurationByVmId,
                new IdQueryParameters(vmId), new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue response = (VdcQueryReturnValue) returnValue;
                if (response != null && response.getSucceeded()) {
                    PreviewSnapshotModel previewSnapshotModel = (PreviewSnapshotModel) model;
                    ArrayList<SnapshotModel> snapshotModels = new ArrayList<>();
                    ArrayList<Snapshot> snapshots = response.getReturnValue();
                    previewSnapshotModel.sortSnapshots(snapshots);

                    Guid userSelectedSnapshotId = previewSnapshotModel.getSnapshotModel().getEntity().getId();

                    for (Snapshot snapshot : snapshots) {
                        SnapshotModel snapshotModel = new SnapshotModel();
                        snapshotModel.setEntity(snapshot);
                        snapshotModel.getMemory().setEntity(false);
                        snapshotModel.setDisks((ArrayList<DiskImage>) snapshot.getDiskImages());
                        snapshotModels.add(snapshotModel);

                        if (snapshot.getType() == Snapshot.SnapshotType.ACTIVE) {
                            activeSnapshotId = snapshot.getId();
                        }
                    }

                    previewSnapshotModel.getSnapshots().setItems(snapshotModels);
                    updateDiskSnapshotsMap();

                    // Update disk-snapshots map
                    updateDiskSnapshotsMap();

                    // First selecting the active snapshot for ensuring default disks selection
                    // (i.e. when some disks are missing from the selected snapshot,
                    // the corresponding disks from the active snapshot should be selected).
                    previewSnapshotModel.selectSnapshot(activeSnapshotId);

                    // Selecting the snapshot the was selected by the user
                    previewSnapshotModel.selectSnapshot(userSelectedSnapshotId);
                }
            }}));
    }

    // Sort snapshots by creation date (keep active snapshot on top)
    private void sortSnapshots(ArrayList<Snapshot> snapshots) {
        Collections.sort(snapshots, Collections.reverseOrder(new Linq.SnapshotByCreationDateCommparer() {
            @Override
            public int compare(Snapshot x, Snapshot y) {
                if (x.getType() == Snapshot.SnapshotType.ACTIVE) {
                    return 1;
                }
                if (y.getType() == Snapshot.SnapshotType.ACTIVE) {
                    return -1;
                }
                return super.compare(x, y);
            }
        }));
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
                }
                else {
                    disksListModel = new ListModel();
                    disksListModel.setItems(new ArrayList<>(Arrays.asList(diskImage)));
                }
                diskSnapshotsMap.put(diskImage.getId(), disksListModel);
            }
        }
    }

    public void clearSelection() {
        clearDisksSelection();
        clearMemorySelection();
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

        getSnapshots().setSelectedItem(snapshotModel);
        setSnapshotModel(snapshotModel);
        for (DiskImage diskImage : snapshotModel.getDisks()) {
            ListModel diskListModel = diskSnapshotsMap.get(diskImage.getId());
            diskListModel.setSelectedItem(diskImage);
        }
    }
}
