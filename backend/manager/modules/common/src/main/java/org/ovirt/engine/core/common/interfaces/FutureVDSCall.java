package org.ovirt.engine.core.common.interfaces;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;

public interface FutureVDSCall<T> {

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    VDSReturnValue get();

    VDSReturnValue get(long timeout, TimeUnit unit) throws TimeoutException;

}
