package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

/**
 * Model object representing List of errata (singular: Erratum)  for the engine itself (aka System).
 *
 * @see EngineErrataListModel
 * @see HostErrataListModel
 * @see VmErrataListModel
 *
 */
public class EngineErrataListModel extends AbstractErrataListModel {

    public EngineErrataListModel() {
        super();
        setApplicationPlace(WebAdminApplicationPlaces.errataMainPlace);
    }

    @Override
    protected String getListName() {
        return "EngineErrataListModel"; //$NON-NLS-1$
    }

    @Override
    protected QueryType getQueryType() {
        return QueryType.GetErrataForEngine;
    }

}
