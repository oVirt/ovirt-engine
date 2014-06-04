package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ClusterVmListModel extends VmListModel
{

    @Override
    public VDSGroup getEntity()
    {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public void setEntity(VDSGroup value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            setSearchString("Vms: cluster=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VM);
        tempVar.setRefresh(getIsQueryFirstTime());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().execute();
        }
    }
}
