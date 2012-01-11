package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.FullDateTimeColumn;

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

    @Inject
    public MainTabEventView(MainModelProvider<AuditLog, EventListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
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

        getTable().ensureColumnPresent(AdvancedViewColumns.logTypeColumn, "Event ID", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.userColumn, "User", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.hostColumn, "Host", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.virtualMachineColumn, "Virtual Machine", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.templateColumn, "Template", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.dataCenterColumn, "Data Center", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.storageColumn, "Storage", advancedViewEnabled);
        getTable().ensureColumnPresent(AdvancedViewColumns.clusterColumn, "Cluster", advancedViewEnabled);
    }

    void initTable() {
        getTable().addColumn(new AuditLogSeverityColumn(), "", "30px");

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        getTable().addColumn(logTimeColumn, "Time");

        TextColumnWithTooltip<AuditLog> messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
            }
        };
        getTable().addColumn(messageColumn, "Message");
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
        @Override
        public String getValue(AuditLog object) {
            return object.getuser_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> hostColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getvds_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> virtualMachineColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getvm_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> templateColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getvm_template_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> dataCenterColumn = new TextColumnWithTooltip<AuditLog>() {
        @Override
        public String getValue(AuditLog object) {
            return object.getstorage_pool_name();
        }
    };

    public static final TextColumnWithTooltip<AuditLog> storageColumn = new TextColumnWithTooltip<AuditLog>() {
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

}
