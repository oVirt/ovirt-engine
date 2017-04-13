package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VolumeGeoRepSessionDetailsModel extends Model{
    private ListModel<EntityModel<GlusterGeoRepSessionDetails>> geoRepSessionSummary;
    public ListModel<EntityModel<GlusterGeoRepSessionDetails>> getGeoRepSessionSummary() {
        return geoRepSessionSummary;
    }

    public void setGeoRepSessionSummary(ListModel<EntityModel<GlusterGeoRepSessionDetails>> geoRepSessionSummary) {
        this.geoRepSessionSummary = geoRepSessionSummary;
    }

    public VolumeGeoRepSessionDetailsModel() {
        setGeoRepSessionSummary(new ListModel<EntityModel<GlusterGeoRepSessionDetails>>());
        getGeoRepSessionSummary().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if(geoRepSessionSummary != null && geoRepSessionSummary.getSelectedItem() != null) {
                onPropertyChanged(new PropertyChangedEventArgs("selectedSessionSummaryRow"));//$NON-NLS-1$
            }
        });
    }
}
