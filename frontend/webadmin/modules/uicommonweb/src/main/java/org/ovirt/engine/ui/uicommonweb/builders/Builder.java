package org.ovirt.engine.ui.uicommonweb.builders;

/**
 * The base of all the builders. It contains only method- build. This method get an additional paramater (except the
 * obvious source and destination) - the rest of the builders. When this builder is done it's work, it has to invoke the
 * builders following this one. This explicit invocation is done because this builder can do some async call and invoke
 * the next builder only in case it finished it's work.
 *
 * @param <S>
 *            Source
 * @param <D>
 *            Destination
 */
public interface Builder<S, D> {

    void build(S source, D destination, BuilderList<S, D> rest);
}
