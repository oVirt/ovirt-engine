package org.ovirt.engine.ui.uicommonweb.builders;


/**
 * Base synchronous implementation of the {@link Builder}. It takes care of the boilerplate invocation of the rest and lets it's descendants
 * to only take care of copying parameters.
 * <p>
 * Use this only when the descendant class does not do any async call. Otherwise implement the {@link Builder} directly
 */
public abstract class BaseSyncBuilder<S, D> implements SyncBuilder<S, D> {

    @Override
    public void build(S source, D destination, BuilderList<S, D> rest) {
        build(source, destination);

        rest.head().build(source, destination, rest.tail());
    }

    /**
     * Builds the backend model from frontend the one
     *
     * @param frontend source
     * @param backend destination
     */
    protected abstract void build(S source, D destination);

}
