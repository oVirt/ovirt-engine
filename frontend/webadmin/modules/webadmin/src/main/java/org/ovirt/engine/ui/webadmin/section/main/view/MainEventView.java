package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.searchbackend.AuditLogConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainEventPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainEventView extends AbstractMainWithDetailsTableView<AuditLog, EventListModel<Void>>
    implements MainEventPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainEventView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FlowPanel radioButtonPanel;

    @UiField
    FlowPanel tablePanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static final String BASIC_VIEW_MSG_COLUMN_WIDTH = "100%"; //$NON-NLS-1$
    private static final String ADV_VIEW_MSG_COLUMN_WIDTH = "175px"; //$NON-NLS-1$
    private AbstractTextColumn<AuditLog> basicMessageColumn;

    @Inject
    public MainEventView(MainModelProvider<AuditLog, EventListModel<Void>> modelProvider) {
        super(modelProvider);
        initTable();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        getTable().setTableOverhead(radioButtonPanel);

        tablePanel.add(getTable());
    }

    @UiHandler("basicViewButton")
    void onBasicView(ClickEvent event) {
        handleViewChange(false);
    }

    @UiHandler("advancedViewButton")
    void onAdvancedView(ClickEvent event) {
        handleViewChange(true);
    }

    void handleViewChange(boolean advancedViewEnabled) {
        if (advancedViewEnabled) {
            getTable().moveSortHeaderState(basicMessageColumn, advancedMessageColumn);
        } else {
            getTable().moveSortHeaderState(advancedMessageColumn, basicMessageColumn);
        }

        getTable().ensureColumnVisible(basicMessageColumn, constants.messageEvent(),
                !advancedViewEnabled,
                BASIC_VIEW_MSG_COLUMN_WIDTH);
        getTable().ensureColumnVisible(advancedMessageColumn, constants.messageEvent(),
                advancedViewEnabled,
                ADV_VIEW_MSG_COLUMN_WIDTH);
        getTable().ensureColumnVisible(logTypeColumn, constants.eventIdEvent(),
                advancedViewEnabled,
                "80px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(userColumn, constants.userEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(hostColumn, constants.hostEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(virtualMachineColumn, constants.vmEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "120px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(templateColumn, constants.templateEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(dataCenterColumn, constants.dcEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(storageColumn, constants.storageEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly),
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(clusterColumn, constants.clusterEvent(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(volumeColumn, constants.volumeEvent(),
                advancedViewEnabled && ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly),
                "120px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(corrIdColumn, constants.eventCorrelationId(),
                advancedViewEnabled,
                "120px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(originColumn, constants.eventOrigin(),
                advancedViewEnabled,
                "100px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(customEventIdColumn, constants.eventCustomEventId(),
                advancedViewEnabled,
                "140px"); //$NON-NLS-1$
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

        basicMessageColumn = createMessageColumn();
        getTable().addColumn(basicMessageColumn, constants.messageEvent(), BASIC_VIEW_MSG_COLUMN_WIDTH);
    }

    private static AbstractTextColumn<AuditLog> createMessageColumn() {
        AbstractTextColumn<AuditLog> messageColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getMessage();
            }
        };
        messageColumn.makeSortable(AuditLogConditionFieldAutoCompleter.MESSAGE);
        return messageColumn;
    }

    private static final AbstractTextColumn<AuditLog> advancedMessageColumn = createMessageColumn();

    private static final AbstractTextColumn<AuditLog> logTypeColumn = new AbstractTextColumn<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return String.valueOf(object.getLogTypeValue());
        }
    };

    private static final AbstractTextColumn<AuditLog> userColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.USER_NAME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getUserName();
        }
    };

    private static final AbstractTextColumn<AuditLog> hostColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_HOST);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVdsName();
        }
    };

    private static final AbstractTextColumn<AuditLog> virtualMachineColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VM);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVmName();
        }
    };

    private static final AbstractTextColumn<AuditLog> templateColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_TEMPLATE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getVmTemplateName();
        }
    };

    private static final AbstractTextColumn<AuditLog> dataCenterColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_DATACENTER);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getStoragePoolName();
        }
    };

    private static final AbstractTextColumn<AuditLog> storageColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_STORAGE);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getStorageDomainName();
        }
    };

    private static final AbstractTextColumn<AuditLog> clusterColumn = new AbstractTextColumn<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getClusterName();
        }
    };

    private static final AbstractTextColumn<AuditLog> volumeColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.EVENT_VOLUME);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getGlusterVolumeName();
        }
    };

    private static final AbstractTextColumn<AuditLog> corrIdColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.CORRELATION_ID);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getCorrelationId();
        }
    };

    private static final AbstractTextColumn<AuditLog> originColumn = new AbstractTextColumn<AuditLog>() {
        {
            makeSortable(AuditLogConditionFieldAutoCompleter.ORIGIN);
        }

        @Override
        public String getValue(AuditLog object) {
            return object.getOrigin();
        }
    };

    private static final AbstractTextColumn<AuditLog> customEventIdColumn = new AbstractTextColumn<AuditLog>() {
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
