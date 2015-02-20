package org.ovirt.engine.ui.uicommonweb.builders;

/**
 * Equivalent of {@link CompositeBuilder} purely for synchronously running builders (i.e. descendants of
 * {@link SyncBuilder}).
 */
public class CompositeSyncBuilder<S, D> extends CompositeBuilder<S, D> implements SyncBuilder<S, D> {

    public CompositeSyncBuilder(SyncBuilder<S, D>... builders) {
        super(builders);
    }
}
