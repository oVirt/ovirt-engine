package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;

public class ConsoleButtonCell extends AbstractCell<UserPortalItemModel> {

    public static interface ConsoleButtonCommand {

        public void execute(UserPortalItemModel model);

    }

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div id=\"{0}\" title=\"{1}\" class=\"{2}\" data-class=\"consoleButton\" />")
        SafeHtml consoleButton(String id, String title, String className);

    }

    private final ConsoleUtils consoleUtils;

    private final ConsoleButtonCommand command;

    private final String enabledCss;

    private final String disabledCss;

    private final String title;

    // DOM element ID settings for the text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    private static CellTemplate template;

    public ConsoleButtonCell(ConsoleUtils consoleUtils,
            String enabledCss, String disabledCss,
            String title, ConsoleButtonCommand command) {
        super("click"); //$NON-NLS-1$
        this.consoleUtils = consoleUtils;
        this.enabledCss = SafeHtmlUtils.htmlEscape(enabledCss);
        this.disabledCss = SafeHtmlUtils.htmlEscape(disabledCss);
        this.title = SafeHtmlUtils.htmlEscape(title);
        this.command = command;

        // Delay cell template creation until the first time it's needed
        if (template == null) {
            template = GWT.create(CellTemplate.class);
        }
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent,
            final UserPortalItemModel model, NativeEvent event,
            ValueUpdater<UserPortalItemModel> valueUpdater) {
        super.onBrowserEvent(context, parent, model, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();

        // Skip events other than 'click'
        if (!"click".equals(event.getType())) { //$NON-NLS-1$
            return;
        }

        // Skip events that don't target consoleButton DIV element
        if (!DivElement.is(eventTarget) || !"consoleButton".equals(DivElement.as(eventTarget).getAttribute("data-class"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        if (isConsoleEnabled(model)) {
            // deferred because first the row has to be selected and then the console can be shown
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    command.execute(model);
                }
            });
        }
    }

    @Override
    public void render(Context context, UserPortalItemModel model, SafeHtmlBuilder sb) {
        sb.append(template.consoleButton(
                ElementIdUtils.createTableCellElementId(elementIdPrefix, columnId, context),
                title,
                isConsoleEnabled(model) ? enabledCss : disabledCss));
    }

    protected boolean isConsoleEnabled(UserPortalItemModel model) {
        ConsoleProtocol protocol = consoleUtils.determineConnectionProtocol(model);
        return consoleUtils.canShowConsole(protocol, model);
    }

}
