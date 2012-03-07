package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

public class UserPortalItemImageButtonColumn extends Column<UserPortalItemModel, UserPortalItemModel> {

    public UserPortalItemImageButtonColumn(Cell<UserPortalItemModel> cell) {
        super(cell);
    }

    @Override
    public UserPortalItemModel getValue(UserPortalItemModel object) {
        return object;
    }

}
