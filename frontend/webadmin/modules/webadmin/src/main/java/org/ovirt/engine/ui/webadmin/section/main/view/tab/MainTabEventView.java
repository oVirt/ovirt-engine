package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.AuditLogConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabEventView extends AbstractMainTabTableView<AuditLog, EventListModel<Void>>
    implements MainTabEventPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabEventView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    RadioButton basicViewButton;

    @UiField
    RadioButton advancedViewButton;

    @UiField
    SimplePanel tablePanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static final String BASIC_VIEW_MSG_COLUMN_WIDTH = "600px"; //$NON-NLS-1$
    private static final String ADV_VIEW_MSG_COLUMN_WIDTH = "150px"; //$NON-NLS-1$
    private AbstractTextColumn<AuditLog> messageColumn;

    @Inject
    public MainTabEventView(MainModelProvider<AuditLog, EventListModel<Void>> modelProvider) {
        super(modelProvider);
        initTable();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();

        tablePanel.setWidget(getTable());
        basicViewButton.setValue(true);
    }

    void localize() {
        basicViewButton.setText(constants.eventBasicViewLabel());
        advancedViewButton.setText(constants.eventAdvancedViewLabel());
    }

    @UiHandler({ "basicViewButton", "advancedViewButton" })
    void handleViewButtonClick(ClickEvent event) {
        boolean advancedViewEnabled = advancedViewButton.getValue();

        getTable().ensureColumnVisible(AdvancedViewColumns.logTypeColumn, constants.eventIdEvent(),
                advancedViewEnabled,
                "80px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.userColumn, constants.userEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.hostColumn, constants.hostEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.virtualMachineColumn, constants.vmEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.templateColumn, constants.templateEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.dataCenterColumn, constants.dcEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.storageColumn, constants.storageEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.clusterColumn, constants.clusterEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.volumeColumn, constants.volumeEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly),
                "120px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.corrIdColumn, constants.eventCorrelationId(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.originColumn, constants.eventOrigin(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(AdvancedViewColumns.customEventIdColumn, constants.eventCustomEventId(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$

        getTable().setColumnWidth(messageColumn,
                advancedViewEnabled ? ADV_VIEW_MSG_COLUMN_WIDTH : BASIC_VIEW_MSG_COLUMN_WIDTH);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AuditLogSeverityColumn severityColumn = new AuditLogSeverityColumn();
        severityColumn.setContextMenuTitle(constants.severityEvent());
        getTable().addColumn(severityColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> logTimeColumn = new AbstractFullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getLogTime();
            }
        };
        logTimeColumn.makeSortable(AuditLogConditionFieldAutoCompleter.TIME);
        getTable().addColumn(logTimeColumn, constants.timeEvent(), "175px"); //$NON-NLS-1$

        messageColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getMessage();
            }
        };
        messageColumn.makeSortable(AuditLogConditionFieldAutoCompleter.MESSAGE);
        getTable().addColumn(messageColumn, constants.messageEvent(), BASIC_VIEW_MSG_COLUMN_WIDTH);

        getTable().addActionButton(new WebAdminButtonDefinition<AuditLog>(constants.details(),
                CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getDetailsCommand();
            }
        });
    }

}

class AdvancedViewColumns {

    public static final AbstractTextColumn<AuditLog> logTypeColumn = new AbstractTextColumn<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return String.valueOf(object.getLogTypeValue());
        }
    };

    public static final AbstractTextColumn<AuditLog> userColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.USER_NAME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getUserName();
        }
    };

    public static final AbstractTextColumn<AuditLog> hostColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_HOST);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVdsName();
        }
    };

    public static final AbstractTextColumn<AuditLog> virtualMachineColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VM);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVmName();
        }
    };

    public static final AbstractTextColumn<AuditLog> templateColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_TEMPLATE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVmTemplateName();
        }
    };

    public static final AbstractTextColumn<AuditLog> dataCenterColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_DATACENTER);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getStoragePoolName();
        }
    };

    public static final AbstractTextColumn<AuditLog> storageColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_STORAGE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getStorageDomainName();
        }
    };

    public static final AbstractTextColumn<AuditLog> clusterColumn = new AbstractTextColumn<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getClusterName();
        }
    };

    public static final AbstractTextColumn<AuditLog> volumeColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VOLUME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getGlusterVolumeName();
        }
    };

    public static final AbstractTextColumn<AuditLog> corrIdColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.CORRELATION_ID);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getCorrelationId();
        }
    };

    public static final AbstractTextColumn<AuditLog> originColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.ORIGIN);
        }
        @Override
        public String getValue(AuditLog object) {
            return object.getOrigin();
        }
    };

    public static final AbstractTextColumn<AuditLog> customEventIdColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.CUSTOM_EVENT_ID);
        }

        @Override
        public String getValue(AuditLog object) {

            int id = object.getCustomEventId();
            return id >= 0 ? String.valueOf(id) : "";   //$NON-NLS-1$
        }
    };
}
