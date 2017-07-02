package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

/**
 * Model object representing List of Erratum for a Host.
 */
public class HostErrataListModel extends AbstractErrataListModel {

    public HostErrataListModel() {
        super();
        setApplicationPlace(WebAdminApplicationPlaces.hostGeneralErrataSubTabPlace);
    }

    @Override
    protected String getListName() {
        return "HostErrataListModel"; //$NON-NLS-1$
    }

    @Override
    protected QueryType getQueryType() {
        return QueryType.GetErrataForHost;
    }
}
