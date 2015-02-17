package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GlusterVolumeSnapshotListModel extends SearchableListModel<GlusterVolumeEntity, GlusterVolumeSnapshotEntity> {
    @Override
    public String getListName() {
        return "GlusterVolumeSnapshotListModel"; //$NON-NLS-1$
    }

    public GlusterVolumeSnapshotListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().snapshotsTitle());
        setHelpTag(HelpTag.volume_snapshots);
        setHashName("volume_snapshots");//$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterVolumeSnapshotsForVolume(new AsyncQuery(this,
                new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<GlusterVolumeSnapshotEntity> snapshots =
                                (ArrayList<GlusterVolumeSnapshotEntity>) returnValue;
                        Collections.sort(snapshots, new Linq.GlusterVolumeSnapshotComparer());
                        setItems(snapshots);
                    }
                }), getEntity().getId());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
    }
}
