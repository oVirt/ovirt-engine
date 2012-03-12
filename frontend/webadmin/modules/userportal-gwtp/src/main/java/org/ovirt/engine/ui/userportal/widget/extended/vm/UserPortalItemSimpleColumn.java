package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/**
 * Simple column which only returns what it gets with no calculation around
 */
public class UserPortalItemSimpleColumn extends Column<UserPortalItemModel, UserPortalItemModel> {

    public UserPortalItemSimpleColumn(Cell<UserPortalItemModel> cell) {
        super(cell);
    }

    @Override
    public UserPortalItemModel getValue(UserPortalItemModel object) {
        return object;
    }

}
