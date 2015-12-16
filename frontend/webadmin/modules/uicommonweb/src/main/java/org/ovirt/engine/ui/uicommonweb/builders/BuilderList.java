package org.ovirt.engine.ui.uicommonweb.builders;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of a list which operates on Builder.
 * Adds operations head() and tail().
 *
 * @param <S> the source
 * @param <D> the destination
 */
public class BuilderList<S, D> extends LinkedList<Builder<S, D>> {

    private static final long serialVersionUID = 9191043684640470136L;

    public BuilderList() {
    }

    public BuilderList(List<Builder<S, D>> builders) {
        for (Builder<S, D> builder : builders) {
            add(builder);
        }
    }

    public BuilderList(Builder<S, D>[] builders) {
        this(Arrays.asList(builders));
    }

    /**
     * Returns a new shallow copy of this list with appended parameter
     */
    @SuppressWarnings("unchecked")
    public BuilderList<S, D> append(Builder<S, D> builder) {
        // working on the shallow copy of the list to not affect the original one
        List<Builder<S, D>> enriched = new BuilderList<>(subList(0, size())).subList(0, size());
        Collections.addAll(enriched, builder);

        return new BuilderList<>(enriched);
    }

    /**
     * Returns the first element of the list. If there is no first element, a null object (EmptyBuilder) will be returned.
     */
    public Builder<S, D> head() {
        if (size() != 0) {
            return iterator().next();
        }

        return new EmptyBuilder();
    }

    /**
     * Returns a new list composed of all elements of the current list excluding the first one.
     * If there is only one or zero elements, an empty list is returned
     */
    public BuilderList<S, D> tail() {
        if (size() >= 2) {
            return new BuilderList<>(subList(1, size()));
        }

        // the tail of the empty list and of the list containing one element is empty
        return new BuilderList<>();
    }

    /**
     * The Null object of the builders. It terminates the iteration by not calling the rest.
     */
    class EmptyBuilder implements Builder<S, D> {

        @Override
        public void build(S source, D destination, BuilderList<S, D> rest) {

        }

    }

}
