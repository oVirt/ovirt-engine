package org.ovirt.engine.ui.uicommonweb.models.events;

public class SubTabEventListModel extends EventListModel {

    @Override
    protected void forceRefresh() {
        // enable refresh for the sub tab only when the entity is set up.
        if (getEntity() != null) {
            super.forceRefresh();
        }
    }
}
