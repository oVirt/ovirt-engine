package org.ovirt.engine.core.common.utils;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Queryable;

/**
 * A Pair that implements the {@link Queryable} interface, to enable returning it as the return value of a query.
 */
public class PairQueryable<T extends Queryable, K extends Queryable> extends Pair<T, K> implements Queryable {
    private static final long serialVersionUID = -8894728003078425184L;

    public PairQueryable() {
    }

    public PairQueryable(T first, K second) {
        super(first, second);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PairQueryable)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public PairQueryableId getQueryableId() {
        return new PairQueryableId(getMemberId(getFirst()), getMemberId(getSecond()));
    }

    private Object getMemberId(Queryable member) {
        if (member != null) {
            return member.getQueryableId();
        }
        return null;
    }

    private static class PairQueryableId extends Pair<Object, Object> {

        private static final long serialVersionUID = 3430689533159779008L;

        public PairQueryableId() {
            this(null, null);
        }

        public PairQueryableId(Object first, Object second) {
            super(first, second);
        }

        @Override
        public String toString() {
            return Objects.toString(getFirst()) + '.' + Objects.toString(getSecond());
        }
    }
}
