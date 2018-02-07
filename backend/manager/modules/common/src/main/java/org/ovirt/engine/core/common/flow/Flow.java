package org.ovirt.engine.core.common.flow;

/**
 * <pre>
 * A {@link Flow} and its {@Handler}s define a framework which is an implementation
 * of the 'chain of responsibility' design pattern. The framework provides an api for
 * building a graph of activities and then processing it, with each individual
 * {@link Handler} defining and executing a single unit of  business logic and signaling
 * the next in chain about its outcome.
 *
 * A {@link Context} object is passed from one handler to the next and holds the state
 * of the flow, enabling individual {@Handler}s to get\set\share state.
 *
 * The head of the {@link Flow} is the {@link Handler} which is executed first.
 * </pre>
 *
 * @see Handler for more design goals and usage examples
 */
public interface Flow<C extends Context> {

    /**
     * @return The first {@link Handler} in the flow graph,
     * which is processed first.
     */
    Handler<C> getHead();
}
