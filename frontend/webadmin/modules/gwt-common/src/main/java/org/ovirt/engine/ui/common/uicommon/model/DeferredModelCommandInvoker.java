package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Used to invoke UiCommon model {@linkplain UICommand commands} as GWT {@linkplain Scheduler#scheduleDeferred deferred
 * commands} that execute after the browser event loop returns.
 * <p>
 * Invoking model commands in a deferred way might be useful when there are other classes processing the model and those
 * commands should be executed only after the model has been fully processed.
 */
public class DeferredModelCommandInvoker {

    private final Model model;

    public DeferredModelCommandInvoker(Model model) {
        this.model = model;
    }

    public void invokeDefaultCommand() {
        invokeCommand(model.getDefaultCommand());
    }

    public void invokeCancelCommand() {
        invokeCommand(model.getCancelCommand());
    }

    void invokeCommand(final UICommand command) {
        if (command != null) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    command.Execute();
                }
            });
        }
    }

}
