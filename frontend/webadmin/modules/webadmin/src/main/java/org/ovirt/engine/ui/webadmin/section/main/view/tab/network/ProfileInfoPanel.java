package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

public class ProfileInfoPanel extends TabLayoutPanel {

    private PermissionWithInheritedPermissionListModelTable<PermissionListModel> permissionsTable;
    private ApplicationConstants constants;

    public ProfileInfoPanel(PermissionWithInheritedPermissionListModelTable<PermissionListModel> permissionsTable,
            ApplicationConstants constants) {
        super(20, Unit.PX);

        this.constants = constants;
        this.permissionsTable = permissionsTable;

        permissionsTable.initTable(constants);
        initPanel();
        addStyle();
    }

    private void initPanel() {
        // Add Tabs
        add(new ScrollPanel(permissionsTable), constants.profilePermissions());
    }

    private void addStyle() {
        getElement().getStyle().setPosition(Position.STATIC);
    }

}
