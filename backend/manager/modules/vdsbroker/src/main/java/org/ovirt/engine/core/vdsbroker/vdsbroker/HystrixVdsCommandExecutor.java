package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@Alternative
public class HystrixVdsCommandExecutor implements VdsCommandExecutor {

    @Override
    public VDSReturnValue execute(final VDSCommandBase<?> command, final VDSCommandType commandType) {
        final HystrixCommand.Setter setter;
        if (command.isAsync()) {
            setter = setter("AsyncVds" + commandType.name());
        } else {
            setter = setter("Vds" + commandType.name());
        }
        final HystrixCommand<VDSReturnValue> hystrixCommand = new HystrixCommand(setter) {
            @Override
            protected VDSReturnValue run() throws Exception {
                command.execute();
                if (command.isAsync()) {
                    // Don't check if the command succeeded to avoid waiting for the result of the asynchronous
                    // operation.
                    return command.getVDSReturnValue();
                } else if(command.getVDSReturnValue().getSucceeded()) {
                    return command.getVDSReturnValue();
                } else {
                    // throw this so that hystrix can see that this command failed
                    throw new ActionFailedException(command.getVDSReturnValue());
                }
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

    private HystrixCommand.Setter setter(final String key) {
        return HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(key)
        ).andCommandKey(
                HystrixCommandKey.Factory.asKey(key)
        ).andCommandPropertiesDefaults(
                HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(SEMAPHORE)
                        .withExecutionTimeoutEnabled(false)
                        .withCircuitBreakerEnabled(false)
                        .withFallbackEnabled(false)
                        .withMetricsRollingStatisticalWindowInMilliseconds(60000)
                        .withMetricsRollingStatisticalWindowBuckets(60)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(200));
    }

    private static class ActionFailedException extends Exception {

        VDSReturnValue returnValue;

        public ActionFailedException(VDSReturnValue returnValue) {
            this.returnValue = returnValue;
        }

        public VDSReturnValue getReturnValue() {
            return returnValue;
        }
    }
}
