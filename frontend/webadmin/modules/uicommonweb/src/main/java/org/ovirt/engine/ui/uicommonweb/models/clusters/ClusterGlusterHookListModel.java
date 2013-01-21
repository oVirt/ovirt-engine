package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterGlusterHookListModel extends SearchableListModel {

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) super.getEntity();
    }

    public ClusterGlusterHookListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().glusterHooksTitle());
        setHashName("gluster_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.GlusterOnly);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        AsyncDataProvider.getGlusterHooks(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                List<GlusterHookEntity> glusterHooks = (List<GlusterHookEntity>) returnValue;
                setItems(glusterHooks);
            }
        }), getEntity().getId());
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    protected String getListName() {
        return "ClusterGlusterHookListModel"; //$NON-NLS-1$
    }
}
