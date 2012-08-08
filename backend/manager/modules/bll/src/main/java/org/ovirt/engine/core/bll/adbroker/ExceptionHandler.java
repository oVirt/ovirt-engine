package org.ovirt.engine.core.bll.adbroker;

public interface ExceptionHandler<T,P> {

    public T handle(Exception e, P params);

}

