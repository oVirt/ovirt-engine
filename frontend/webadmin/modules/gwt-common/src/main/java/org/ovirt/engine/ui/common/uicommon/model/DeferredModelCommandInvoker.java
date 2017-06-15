package org.ovirt.engine.ui.common.uicommon.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.core.client.Scheduler;

/**
 * Used to invoke UiCommon model {@linkplain UICommand commands} as GWT {@linkplain Scheduler#scheduleDeferred deferred
 * commands} that execute after the browser event loop returns.
 * <p>
 * Invoking model commands in a deferred way might be useful when there are other classes processing the model and those
 * commands should be executed only after the model has been fully processed.
 */
public class DeferredModelCommandInvoker {

    private static final Logger logger = Logger.getLogger(DeferredModelCommandInvoker.class.getName());

    private final Model model;

    public DeferredModelCommandInvoker(Model model) {
        this.model = model;
    }

    public void invokeCommand(UICommand command) {
        scheduleCommandExecution(command);
    }

    public void invokeDefaultCommand() {
        scheduleCommandExecution(model.getDefaultCommand());
    }

    public void invokeCancelCommand() {
        scheduleCommandExecution(model.getCancelCommand());
    }

    void scheduleCommandExecution(final UICommand command) {
        if (command != null) {
            Scheduler.get().scheduleDeferred(() -> {
                try {
                    executeCommand(command);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "UICommand execution failed", ex); //$NON-NLS-1$
                    commandFailed(command);
                } finally {
                    commandFinished(command);
                }
            });
        }
    }

    protected void executeCommand(UICommand command) {
        command.execute();
    }

    protected void commandFailed(UICommand command) {
        // No-op, override as necessary
    }

    protected void commandFinished(UICommand command) {
        // No-op, override as necessary
    }

}
