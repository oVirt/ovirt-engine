package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

/**
 * Model object representing List of errata (singular: Erratum) for a VM.
 *
 */
public class VmErrataListModel extends AbstractErrataListModel {

    public VmErrataListModel() {
        super();
        setApplicationPlace(WebAdminApplicationPlaces.virtualMachineErrataSubTabPlace);
    }

    @Override
    protected String getListName() {
        return "VmErrataListModel"; //$NON-NLS-1$
    }

    @Override
    protected QueryType getQueryType() {
        return QueryType.GetErrataForVm;
    }
}
