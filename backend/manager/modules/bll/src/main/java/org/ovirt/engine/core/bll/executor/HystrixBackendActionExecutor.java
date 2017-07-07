package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@Alternative
public class HystrixBackendActionExecutor implements BackendActionExecutor {

    @Override
    public ActionReturnValue execute(final CommandBase<?> command) {
        final HystrixCommand.Setter setter = HystrixSettings.setter(command.getActionType().name());
        final HystrixCommand<ActionReturnValue> hystrixCommand = new HystrixCommand(setter) {
            @Override
            protected ActionReturnValue run() throws Exception {
                final ActionReturnValue returnValue = command.executeAction();
                if (returnValue.getSucceeded()) {
                    return returnValue;
                }
                // throw this so that hystrix can see that this command failed
                throw new ActionFailedException(returnValue);
            }
        };
        try {
            return hystrixCommand.execute();
        } catch (HystrixRuntimeException e) {
            // only thrown for hystrix, so catch it and proceed normally
            if (e.getCause() instanceof ActionFailedException) {
                return ((ActionFailedException) e.getCause()).getReturnValue();
            }
            throw e;
        }
    }

    private static class ActionFailedException extends Exception {

        private ActionReturnValue returnValue;

        public ActionFailedException(ActionReturnValue returnValue) {
            this.returnValue = returnValue;
        }

        public ActionReturnValue getReturnValue() {
            return returnValue;
        }
    }
}
