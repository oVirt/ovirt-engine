package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.AuditLogConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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

public class MainTabEventView extends AbstractMainTabTableView<AuditLog, EventListModel> implements MainTabEventPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabEventView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    RadioButton basicViewButton;

    @UiField
    RadioButton advancedViewButton;

    @UiField
    SimplePanel tablePanel;

    private final ApplicationConstants constants;

    private static final String BASIC_VIEW_MSG_COLUMN_WIDTH = "600px"; //$NON-NLS-1$
    private static final String ADV_VIEW_MSG_COLUMN_WIDTH = "150px"; //$NON-NLS-1$
    private TextColumnWithTooltip<AuditLog> messageColumn;

    @Inject
    public MainTabEventView(MainModelProvider<AuditLog, EventListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        this.constants = constants;
        initTable();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);

        tablePanel.setWidget(getTable());
        basicViewButton.setValue(true);
    }

    void localize(ApplicationConstants constants) {
        basicViewButton.setText(constants.eventBasicViewLabel());
        advancedViewButton.setText(constants.eventAdvancedViewLabel());
    }

    @UiHandler({ "basicViewButton", "advancedViewButton" })
    void handleViewButtonClick(ClickEvent event) {
        boolean advancedViewEnabled = advancedViewButton.getValue();

        getTable().ensureColumnPresent(AdvancedViewColumns.logTypeColumn, constants.eventIdEvent(),
                advancedViewEnabled,
                "80px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.userColumn, constants.userEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.hostColumn, constants.hostEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.virtualMachineColumn, constants.vmEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.templateColumn, constants.templateEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.dataCenterColumn, constants.dcEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.storageColumn, constants.storageEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.clusterColumn, constants.clusterEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.volumeColumn, constants.volumeEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly),
                "120px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.corrIdColumn, constants.eventCorrelationId(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.originColumn, constants.eventOrigin(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(AdvancedViewColumns.customEventIdColumn, constants.eventCustomEventId(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$

        getTable().setColumnWidth(messageColumn,
                advancedViewEnabled ? ADV_VIEW_MSG_COLUMN_WIDTH : BASIC_VIEW_MSG_COLUMN_WIDTH);
    }

    void initTable() {
        getTable().enableColumnResizing();

        getTable().addColumn(new AuditLogSeverityColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        logTimeColumn.makeSortable(AuditLogConditionFieldAutoCompleter.TIME);
        getTable().addColumn(logTimeColumn, constants.timeEvent(), "150px"); //$NON-NLS-1$

        messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
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

    public static final TextColumnWithTooltip<AuditLog> logTypeColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return String.valueOf(object.getlog_typeValue());
        }
    };

    public static final TextColumnWithTooltip<AuditLog> userColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.USER_NAME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getuser_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> hostColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_HOST);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getvds_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> virtualMachineColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VM);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getvm_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> templateColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_TEMPLATE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getvm_template_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> dataCenterColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_DATACENTER);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getstorage_pool_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> storageColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_STORAGE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getstorage_domain_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> clusterColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getvds_group_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> volumeColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VOLUME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getGlusterVolumeName();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> corrIdColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.CORRELATION_ID);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getCorrelationId();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> originColumn = new TextColumnWithTooltip<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.ORIGIN);
        }
        @Override
        public String getValue(AuditLog object) {
            return object.getOrigin();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> customEventIdColumn = new TextColumnWithTooltip<AuditLog>() {
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
