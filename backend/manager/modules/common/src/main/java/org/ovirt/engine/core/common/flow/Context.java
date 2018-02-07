package org.ovirt.engine.core.common.flow;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;


/**
 * Holds the state of a {@link Flow} and is passed to all
 * {@link Handler}s which make up the flow graph and should
 * use only the context to manage and share state.
 */
public class Context {

    protected String id;
    protected Exception exception;
    protected Stack<List<Object>> flowTrace;

    public Context () {
        flowTrace = new Stack<>();
    }

    public Context(String id) {
        this();
        this.id = id;
    }

    public Exception getException() {
        return exception;
    }

    public String getId() {
        return id;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * A trace of the flow and its outcomes in reverse order.
     * A sort of breadcrumbs trail of where the context was
     * employed and what was the outcome.
     * Can be used for:
     * - reporting results the user
     * - logging
     * - debugging
     * - testing
     *
     * Each element in the stack can hold a list of objects
     * that client code can use to add any useful info.
     *
     */
    public Stack<List<Object>> getTrace() {
        return flowTrace;
    }

    public Context trace(Object... objects) {
        flowTrace.push(Arrays.asList(objects));
        return this;
    }

    public boolean hasTrace() {
        return flowTrace != null && flowTrace.size() > 0;
    }

    public List<Object> peekTrace() {
        return flowTrace.peek();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Context)) {
            return false;
        }
        Context context = (Context) o;
        return Objects.equals(getId(), context.getId()) &&
                Objects.equals(getException(), context.getException()) &&
                Objects.equals(getTrace(), context.getTrace());
    }

    @Override public int hashCode() {
        return Objects.hash(getId(), getException(), flowTrace);
    }
}

