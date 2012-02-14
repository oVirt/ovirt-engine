package org.ovirt.engine.ui.webadmin.widget.footer;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider.AlertCountChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
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

    String buttonUpStart;
    String buttonUpStretch;
    String buttonUpEnd;
    String buttonDownStart;
    String buttonDownStretch;
    String buttonDownEnd;

    private final ApplicationTemplates templates;
    private final ApplicationResources resources;
    private final SafeHtml alertImage;

    public AlertsEventsFooterView(AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            ApplicationResources resources,
            ApplicationTemplates templates) {
        this.resources = resources;
        this.templates = templates;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        initButtonHandlers();
        alertModelProvider.setAlertCountChangeHandler(this);

        alertsTable = createActionTable(alertModelProvider);
        alertsTable.setBarStyle(style.barStyle());
        initTable(alertsTable);

        _alertsTable = createActionTable(alertFirstRowModelProvider);
        _alertsTable.setBarStyle(style.barStyle());
        _alertsTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTable(_alertsTable);

        eventsTable = createActionTable(eventModelProvider);
        eventsTable.setBarStyle(style.barStyle());
        initTable(eventsTable);

        _eventsTable = createActionTable(eventFirstRowModelProvider);
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

        updateButtonResources();
        updateEventsButton();
        setAlertCount(0);
    }

    SimpleActionTable<AuditLog> createActionTable(SearchableTabModelProvider<AuditLog, ?> modelProvider) {
        return new SimpleActionTable<AuditLog>(modelProvider, getTableResources(),
                ClientGinjectorProvider.instance().getEventBus(),
                ClientGinjectorProvider.instance().getClientStorage());
    }

    AlertsEventsFooterResources getTableResources() {
        return GWT.<AlertsEventsFooterResources> create(AlertsEventsFooterResources.class);
    }

    @Override
    public void onAlertCountChange(int count) {
        setAlertCount(count);
    }

    void setAlertCount(int count) {

        String countStr = count + " " + "Alerts";

        SafeHtml up = templates.alertEventButton(alertImage, countStr,
                buttonUpStart, buttonUpStretch, buttonUpEnd, style.alertButtonUpStyle());
        SafeHtml down = templates.alertEventButton(alertImage, countStr,
                buttonDownStart, buttonDownStretch, buttonDownEnd, style.alertButtonDownStyle());

        alertButton.getUpFace().setHTML(up);
        alertButton.getDownFace().setHTML(down);
    }

    void updateEventsButton() {
        String eventsGrayImageSrc = AbstractImagePrototype.create(resources.eventsGrayImage()).getHTML();
        SafeHtml eventsGrayImage = SafeHtmlUtils.fromTrustedString(eventsGrayImageSrc);

        SafeHtml up = templates.alertEventButton(eventsGrayImage, "Events",
                buttonUpStart, buttonUpStretch, buttonUpEnd, style.eventButtonUpStyle());
        SafeHtml down = templates.alertEventButton(eventsGrayImage, "Events",
                buttonDownStart, buttonDownStretch, buttonDownEnd, style.eventButtonDownStyle());

        eventButton.getUpFace().setHTML(up);
        eventButton.getDownFace().setHTML(down);
    }

    void updateButtonResources() {
        buttonUpStart = resources.footerButtonUpStart().getURL();
        buttonUpStretch = resources.footerButtonUpStretch().getURL();
        buttonUpEnd = resources.footerButtonUpEnd().getURL();
        buttonDownStart = resources.footerButtonDownStart().getURL();
        buttonDownStretch = resources.footerButtonDownStretch().getURL();
        buttonDownEnd = resources.footerButtonDownEnd().getURL();
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
                e.getStyle().setBottom(offset + 2, Unit.PX);
            }
        });
    }

    void initTable(SimpleActionTable<AuditLog> table) {
        table.addColumn(new AuditLogSeverityColumn(), "", "30px");

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

        String alertButtonUpStyle();

        String alertButtonDownStyle();

        String eventButtonUpStyle();

        String eventButtonDownStyle();
    }

}
