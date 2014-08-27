package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
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
        getGeoRepSessionSummary().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if(geoRepSessionSummary != null && geoRepSessionSummary.getSelectedItem() != null) {
                    onPropertyChanged(new PropertyChangedEventArgs("selectedSessionSummaryRow"));//$NON-NLS-1$
                }
            }
        });
    }
}
