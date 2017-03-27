package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostBricksListModel extends SearchableListModel<VDS, GlusterBrickEntity> {

    public HostBricksListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().hostBricksTitle());
        setHelpTag(HelpTag.gluster_bricks);
        setHashName("gluster_bricks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        AsyncDataProvider.getInstance().getGlusterBricksForServer(
                new SetSortedRawItemsAsyncQuery(Comparator.comparing(GlusterBrickEntity::getVolumeName)),
                getEntity().getId());

    }

    @Override
    protected String getListName() {
        return "HostBricksListModel"; //$NON-NLS-1$
    }
}
