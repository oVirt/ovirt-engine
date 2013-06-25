package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.widget.table.AbstractActionTable;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class RolePresenterWidget extends PresenterWidget<RolePresenterWidget.ViewDef> {

    public interface ViewDef extends View {

        void setRoleModelProvider(RoleModelProvider modelProvider);

        SimpleActionTable<Role> getRoleTable();

        void setSubTabVisibility(boolean b);

        void setRolePermissionModelProvider(RolePermissionModelProvider permissionModelProvider);

        AbstractActionTable<permissions> getRolePermissionTable();

    }

    private final RoleModelProvider roleModelProvider;
    private final RolePermissionModelProvider permissionModelProvider;

    @Inject
    public RolePresenterWidget(EventBus eventBus, ViewDef view,
            RoleModelProvider modelProvider,
            RolePermissionModelProvider permissionModelProvider) {
        super(eventBus, view);
        this.roleModelProvider = modelProvider;
        this.permissionModelProvider = permissionModelProvider;

    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setRoleModelProvider(roleModelProvider);
        getView().setRolePermissionModelProvider(permissionModelProvider);
        registerHandler(getView().getRoleTable()
                .getSelectionModel()
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        roleModelProvider.setSelectedItems(getView().getRoleTable()
                                .getSelectionModel().getSelectedList());
                        if (getView().getRoleTable()
                                .getSelectionModel().getSelectedList().size() > 0) {
                            getView().setSubTabVisibility(true);
                        }
                        else {
                            getView().setSubTabVisibility(false);
                        }
                    }
                }));
        registerHandler(getView().getRolePermissionTable()
                .getSelectionModel()
                .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        permissionModelProvider.setSelectedItems(getView().getRolePermissionTable()
                                .getSelectionModel().getSelectedList());
                    }
                }));

    }
}
