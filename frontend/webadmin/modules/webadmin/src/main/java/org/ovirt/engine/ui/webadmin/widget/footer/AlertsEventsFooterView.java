package org.ovirt.engine.ui.webadmin.widget.footer;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider.AlertCountChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.FullDateTimeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class AlertsEventsFooterView extends Composite implements AlertCountChangeHandler {

    interface WidgetUiBinder extends UiBinder<Widget, AlertsEventsFooterView> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    SimplePanel tablePanel;

    @UiField
    SimplePanel widgetPanel;

    @UiField
    SimplePanel firstRowTablePanel;

    @UiField
    ToggleButton alertButton;

    @UiField
    ToggleButton eventButton;

    @UiField
    PushButton expandButton;

    SimpleActionTable<AuditLog> alertsTable;
    SimpleActionTable<AuditLog> eventsTable;
    SimpleActionTable<AuditLog> _alertsTable;
    SimpleActionTable<AuditLog> _eventsTable;

    private final ApplicationTemplates templates;
    private final SafeHtml alertImage;

    public AlertsEventsFooterView(AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            ApplicationResources resources,
            ApplicationTemplates templates) {
        this.templates = templates;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        initButtonHandlers();
        alertModelProvider.setAlertCountChangeHandler(this);

        alertsTable = new SimpleActionTable<AuditLog>(alertModelProvider, getTableResources());
        alertsTable.setBarStyle(style.barStyle());
        initTable(alertsTable);

        _alertsTable = new SimpleActionTable<AuditLog>(alertFirstRowModelProvider, getTableResources());
        _alertsTable.setBarStyle(style.barStyle());
        _alertsTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTable(_alertsTable);

        eventsTable = new SimpleActionTable<AuditLog>(eventModelProvider, getTableResources());
        eventsTable.setBarStyle(style.barStyle());
        initTable(eventsTable);

        _eventsTable = new SimpleActionTable<AuditLog>(eventFirstRowModelProvider, getTableResources());
        _eventsTable.setBarStyle(style.barStyle());
        _eventsTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTable(_eventsTable);

        alertButton.setValue(false);
        eventButton.setValue(true);
        tablePanel.clear();
        firstRowTablePanel.clear();
        tablePanel.add(eventsTable);
        firstRowTablePanel.add(_eventsTable);

        String image = AbstractImagePrototype.create(resources.alertConfigureImage()).getHTML();
        alertImage = SafeHtmlUtils.fromTrustedString(image);

        // no body is invoking the alert search (timer)
        alertModelProvider.getModel().Search();

        setAlertCount(0);
    }

    Resources getTableResources() {
        return GWT.<Resources> create(AlertsEventsFooterResources.class);
    }

    @Override
    public void onAlertCountChange(int count) {
        setAlertCount(count);
    }

    void setAlertCount(int count) {
        alertButton.setHTML(templates.alertFooterHeader(alertImage, count));
    }

    void initButtonHandlers() {
        alertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (alertButton.getValue()) {
                    eventButton.setValue(false);
                    tablePanel.clear();
                    tablePanel.add(alertsTable);

                    firstRowTablePanel.clear();
                    firstRowTablePanel.add(_alertsTable);
                }
                else {
                    alertButton.setValue(true);
                }
            }
        });

        eventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (eventButton.getValue()) {
                    alertButton.setValue(false);
                    tablePanel.clear();
                    tablePanel.add(eventsTable);

                    firstRowTablePanel.clear();
                    firstRowTablePanel.add(_eventsTable);
                }
                else {
                    eventButton.setValue(true);
                }
            }
        });

        expandButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String height = widgetPanel.getElement().getParentElement().getParentElement().getStyle().getHeight();
                int offset = 30;
                if (height.equals("30px")) {
                    offset = 162;
                }
                widgetPanel.getElement().getParentElement().getParentElement().getStyle().setHeight(offset, Unit.PX);
                widgetPanel.getElement().getParentElement().getParentElement().getStyle().setBottom(0, Unit.PX);
                Element e =
                        (Element) widgetPanel.getElement()
                                .getParentElement()
                                .getParentElement()
                                .getParentElement()
                                .getChild(2);
                e.getStyle().setBottom(offset, Unit.PX);
                e =
                        (Element) widgetPanel.getElement()
                                .getParentElement()
                                .getParentElement()
                                .getParentElement()
                                .getChild(3);
                e.getStyle().setBottom(offset + 8, Unit.PX);
            }
        });
    }

    void initTable(SimpleActionTable<AuditLog> table) {
        table.addColumn(new AuditLogSeverityColumn(), "", "40px");

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        table.addColumn(logTimeColumn, "Time", "160px");

        TextColumnWithTooltip<AuditLog> messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
            }
        };
        table.addColumn(messageColumn, "Message");
    }

    public interface AlertsEventsFooterResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/FotterHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    interface Style extends CssResource {

        String barStyle();

    }

}
