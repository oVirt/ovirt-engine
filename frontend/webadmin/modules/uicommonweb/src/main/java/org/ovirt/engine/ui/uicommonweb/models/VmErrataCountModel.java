package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * Model object representing counts (summary info) about errata for a VM.
 *
 * @see org.ovirt.engine.core.common.businessentities.Erratum
 * @see org.ovirt.engine.core.common.businessentities.ErrataCounts
 */
public class VmErrataCountModel extends AbstractErrataCountModel {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    @Override
    protected void showErrataListWithDetailsPopup(String filterCommand) {
        super.showErrataListWithDetailsPopup(filterCommand, messages.errataForVm(getEntity().getName()));
    }

    @Override
    protected QueryType getQueryType() {
        return QueryType.GetErrataCountsForVm;
    }

}
