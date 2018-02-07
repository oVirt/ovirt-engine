package org.ovirt.engine.core.common.flow;

/**
 * <pre>
 * The {@link Handler} interface allows chaining of activities in a tree-like structure.
 * It supports the following design goals:
 *
 * - divide business logic into modular, independent and reusable units
 * - allow easy implementation of complex flow charts and multiple-branch decision trees
 * - separate the business logic from the logic of the flow chart
 * - support AND OR NOT handlers, which can be used to combine other handlers
 * - be able to graphically view the whole tree using {@link Handler#print}
 * - accept a context object to share state between handlers
 * - instantiate the chain only once in the life time of the program, because all handlers
 *   are stateless - promotes dependency injection as well as less memory thrashing and gc
 *
 * A handler must return a signal about the outcome of its action when it is completed.
 * The signal is one of the {@link HandlerOutcome} entries. These signals may be interpreted
 * in different ways depending on the desired scenario and type of outcome.
 *
 * Some example usages:
 *
 * 1. Handlers transform or format data\state (e.g. similar to piped commands in linux cli):
 * - 'success' could mean 'no more handling required'
 * - 'failure' could mean 'cannot continue to next handler'
 * - 'neutral' could mean 'continue to next handler'.
 *
 * 2. Handlers are validators of user input:
 * - 'success' could mean 'current validation passed'
 * - 'failure' could mean 'current validation failed'
 * - 'neutral' might mean 'cannot decide'.
 *
 * 3. Handlers are filters of events (e.g. similar to firewall rules):
 * - 'success' could mean 'event is accepted - no other filters need be consulted'
 * - 'failure' could mean 'event is denied - no other filters need be consulted'
 * - 'neutral' could mean 'event should be passed to other filters. if there are no other filters,
 *   it is accepted'
 *
 * </pre>
 *
 * @param <C> the context which holds the state of the whole operation
 */
public interface Handler<C extends Context> {

    /**
     * Handle the business logic of the current node in the {@link Flow}.
     * The context C is passed between all handlers which make
     * up the flow, and should be the only object which holds
     * the state of the flow. Handlers can get and set state
     * on it, and share state among themselves with it.
     *
     * @return an outcome which is used by the process() method to determine
     * which handler to execute next.
     */
    HandlerOutcome handle(C ctx)  throws Exception;

    /**
     * Process the flow:
     * run the handle() method of this handler and branch to the next handler
     * according to the {@link HandlerOutcome} returned by the handle() method
     * @param ctx the state of the whole flow
     */
    void process(C ctx);

    /**
     * The next handler to process if this handler is successful
     * @param successHandler the next handler
     * @return this handler
     */
    Handler<C> setOnSuccess(Handler<C> successHandler);

    /**
     * The next handler to process if this handler is neutral about the outcome.
     * @param neutralHandler the next handler
     * @return this handler
     */
    Handler<C> setOnNeutral(Handler<C> neutralHandler);

    /**
     * The next handler to process if this handler fails
     * @param failureHandler the next handler
     * @return this handler
     */
    Handler<C> setOnFailure(Handler<C> failureHandler);

    /**
     * The next handler to process if this handler throws an exception
     * @param exceptionHandler the next handler
     * @return this handler
     */
    Handler<C> setOnException(Handler<C> exceptionHandler);

    /**
     * @return name of this handler
     */
    String getName();

    /**
     * set name of this handler
     */
    void setName(String name);

    /**
     * Recursively print a digraph representation of this handler
     * and subsequent ones. The output of this method may be inserted
     * into a digraph viewer (for example http://www.webgraphviz.com)
     * which displays the flow graph visually in order to analyze the
     * correctness of the flow.
     * @param sb accumulator of the output
     */
    void print(StringBuilder sb);
}
