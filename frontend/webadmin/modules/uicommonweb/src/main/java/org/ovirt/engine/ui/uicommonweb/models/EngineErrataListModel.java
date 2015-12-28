package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

/**
 * Model object representing List of errata (singular: Erratum)  for the engine itself (aka System).
 *
 * @see {@link EngineErrataListModel}
 * @see {@link HostErrataListModel}
 * @see {@link VmErrataListModel}
 *
 */
public class EngineErrataListModel extends AbstractErrataListModel {

    public EngineErrataListModel() {
        super();
        setApplicationPlace(WebAdminApplicationPlaces.errataMainTabPlace);
    }

    @Override
    protected String getListName() {
        return "EngineErrataListModel"; //$NON-NLS-1$
    }

    @Override
    protected VdcQueryType getQueryType() {
        return VdcQueryType.GetErrataForEngine;
    }
}
