package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleProtocol;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleUtils;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ConsoleButtonCell extends AbstractCell<UserPortalItemModel> {

    private final ConsoleUtils consoleUtils;

    private final ConsoleButtonCommand command;

    private final String enabledCss;

    private final String disabledCss;

    private final String title;

    public ConsoleButtonCell(ConsoleUtils consoleUtils,
            String enabledCss,
            String disabledCss,
            String title,
            ConsoleButtonCommand command) {
        super("click");
        this.consoleUtils = consoleUtils;
        this.enabledCss = SafeHtmlUtils.htmlEscape(enabledCss);
        this.disabledCss = SafeHtmlUtils.htmlEscape(disabledCss);
        this.title = SafeHtmlUtils.htmlEscape(title);
        this.command = command;
    }

    public void onBrowserEvent(Context context,
            Element parent,
            final UserPortalItemModel model,
            NativeEvent event,
            ValueUpdater<UserPortalItemModel> valueUpdater) {
        super.onBrowserEvent(context, parent, model, event, valueUpdater);

        EventTarget eventTarget = event.getEventTarget();
        if (!Element.is(eventTarget)) {
            return;
        }

        if (!isConsoleEnabled(model)) {
            return;
        }

        if ("click".equals(event.getType())) {
            // deferred because first the row has to be selected and than the console can be shown
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
        if (isConsoleEnabled(model)) {
            sb.appendHtmlConstant("<div title=\"" + title + "\" class=\"" + enabledCss + "\" />");
        } else {
            sb.appendHtmlConstant("<div class=\"" + disabledCss + "\" />");
        }
    }

    protected boolean isConsoleEnabled(UserPortalItemModel model) {
        ConsoleProtocol protocol = consoleUtils.determineDefaultProtocol(model);
        return consoleUtils.canShowConsole(protocol, model);
    }

    public static interface ConsoleButtonCommand {
        public void execute(UserPortalItemModel model);
    }

}
