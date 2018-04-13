package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * Model object representing counts (summary info) about errata for a Host.
 *
 * @see org.ovirt.engine.core.common.businessentities.Erratum
 * @see org.ovirt.engine.core.common.businessentities.ErrataCounts
 */
public class HostErrataCountModel extends AbstractErrataCountModel {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    @Override
    protected void showErrataListWithDetailsPopup(String filterCommand) {
        super.showErrataListWithDetailsPopup(filterCommand, messages.errataForHost(getEntity().getName()));
    }

    @Override
    protected QueryType getQueryType() {
        return QueryType.GetErrataCountsForHost;
    }

}
