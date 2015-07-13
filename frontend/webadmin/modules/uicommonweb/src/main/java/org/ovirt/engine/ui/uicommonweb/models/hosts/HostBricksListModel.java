package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
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

        AsyncDataProvider.getInstance().getGlusterBricksForServer(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<GlusterBrickEntity> glusterBricks = (List<GlusterBrickEntity>) returnValue;
                Collections.sort(glusterBricks, new Linq.ServerBricksComparer());
                setItems(glusterBricks);
            }
        }), getEntity().getId());

    }

    @Override
    protected String getListName() {
        return "HostBricksListModel"; //$NON-NLS-1$
    }
}
