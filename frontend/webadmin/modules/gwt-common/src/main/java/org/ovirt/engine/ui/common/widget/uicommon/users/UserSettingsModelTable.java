package org.ovirt.engine.ui.common.widget.uicommon.users;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserSettingsModel;

import com.google.gwt.event.shared.EventBus;

public class UserSettingsModelTable extends AbstractModelBoundTableWidget<DbUser, UserProfileProperty, UserSettingsModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public UserSettingsModelTable(SearchableTableModelProvider<UserProfileProperty, UserSettingsModel> modelProvider,
            EventBus eventBus,
            DetailActionPanelPresenterWidget<DbUser, UserProfileProperty, UserListModel, UserSettingsModel> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<UserProfileProperty> nameColumn = new AbstractTextColumn<UserProfileProperty>() {
            @Override
            public String getValue(UserProfileProperty property) {
                return property.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.name(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<UserProfileProperty> idColumn = new AbstractTextColumn<UserProfileProperty>() {
            @Override
            public String getValue(UserProfileProperty property) {
                return property.getPropertyId().toString();
            }
        };
        getTable().addColumn(idColumn, constants.propertyId(), "250px"); //$NON-NLS-1$

        AbstractTextColumn<UserProfileProperty> typeColumn = new AbstractTextColumn<UserProfileProperty>() {
            @Override
            public String getValue(UserProfileProperty property) {
                return property.getType().name();
            }
        };
        typeColumn.makeSortable();
        getTable().addColumn(typeColumn, constants.typePermission(), "110px"); //$NON-NLS-1$

        AbstractTextColumn<UserProfileProperty> contentColumn = new AbstractTextColumn<UserProfileProperty>() {
            @Override
            public String getValue(UserProfileProperty property) {
                return property.getContent();
            }
        };
        getTable().addColumn(contentColumn, constants.contentDisk(), "50%"); //$NON-NLS-1$
    }
}
