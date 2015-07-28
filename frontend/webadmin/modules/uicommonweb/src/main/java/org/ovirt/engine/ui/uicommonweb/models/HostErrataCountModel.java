package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * Model object representing counts (summary info) about errata for a Host.
 *
 * @see {@link Erratum}
 * @see {@link ErrataCounts}
 */
public class HostErrataCountModel extends AbstractErrataCountModel {

    private static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    @Override
    protected void showErrataListWithDetailsPopup() {

        if (getWindow() != null) {
            return;
        }

        HostErrataCountModel transferObj = new HostErrataCountModel();
        transferObj.setTitle(messages.errataForHost(getName()));
        transferObj.setGuid(getGuid());
        transferObj.setName(getName());

        setWindow(transferObj);
        initCommands(transferObj);
    }

    @Override
    protected VdcQueryType getQueryType() {
        return VdcQueryType.GetErrataCountsForHost;
    }
}
