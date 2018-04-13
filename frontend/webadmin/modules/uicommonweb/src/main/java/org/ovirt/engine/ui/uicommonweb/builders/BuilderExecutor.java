package org.ovirt.engine.ui.uicommonweb.builders;

import java.util.Arrays;
import java.util.List;

/**
 * Class to invoke the builders in a convenient way.
 * <p>
 * It has two kind of constructors
 * <li>synchronous - the one without the callback. Use this constructor only when all the builders in a chain are
 * synchronous
 * <li>asynchronous - the one with the callback. Use it if any of the builders in the chain are doing some asynchronous
 * calls
 *
 * @param <S>
 *            Source
 * @param <D>
 *            Destination
 */
public class BuilderExecutor<S, D> {

    private BuilderList<S, D> builders;

    /**
     * Helper method that constructs the @{link BuilderExecutor} from given builders and immediately utilizes him
     * to perform build from <code>source</code> to <code>destination</code>.
     */
    public static <S, D> void build(S source, D destination, SyncBuilder<S, D>... builders) {
        new BuilderExecutor<>(builders).build(source, destination);
    }

    /**
     * Asynchronous version of this class. Use it when any builder in the chain is asynchronous
     *
     * @param callback
     *            when all builders finishes the work, this callback is invoked
     * @param builders
     *            the chan in builders
     */
    public BuilderExecutor(BuilderExecutionFinished<S, D> callback, Builder<S, D>... builders) {
        this(callback, Arrays.asList(builders));
    }

    /**
     * Sync version of this class. Use it when all the builders in the chain are synchronous.
     *
     * @param builders
     *            the chain of builders
     */
    public BuilderExecutor(Builder<S, D>... builders) {
        this.builders = new BuilderList<>(builders);
    }

    /**
     * @see BuilderExecutionFinishedCaller
     */
    public BuilderExecutor(BuilderExecutionFinished<S, D> callback, List<Builder<S, D>> builders) {
        this.builders = new BuilderList<>(builders).append(new BuilderExecutionFinishedCaller(callback));
    }

    public void build(S source, D destination) {
        builders.head().build(source, destination, builders.tail());
    }

    public static interface BuilderExecutionFinished<S, D> {
        void finished(S source, D destination);
    }

    // just chains together the last builder in the chain with the callback
    private class BuilderExecutionFinishedCaller implements Builder<S, D> {

        private BuilderExecutionFinished<S, D> finishedCallback;

        public BuilderExecutionFinishedCaller(BuilderExecutionFinished<S, D> finishedCallback) {
            super();
            this.finishedCallback = finishedCallback;
        }

        @Override
        public void build(S source, D destination, BuilderList<S, D> rest) {
            finishedCallback.finished(source, destination);
        }

    }
}
