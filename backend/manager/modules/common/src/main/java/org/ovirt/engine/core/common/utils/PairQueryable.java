package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

/**
 * A Pair that extends the {@link IVdcQueryable} class, to enable returning a Pair as the return value of a Query.
 */
public class PairQueryable<T extends IVdcQueryable, K extends IVdcQueryable> extends IVdcQueryable {
    private static final long serialVersionUID = -8894728003078425184L;

    private Pair<T, K> pair;

    public PairQueryable() {
    }

    public PairQueryable(T first, K second) {
        pair = new Pair<T, K>(first, second);
    }

    private Pair<T, K> getPair() {
        if (pair == null) {
            pair = new Pair<T, K>(null, null);
        }
        return pair;
    }

    public void setFirst(T value) {
        getPair().setFirst(value);
    }

    public T getFirst() {
        return getPair().getFirst();
    }

    public void setSecond(K value) {
        getPair().setSecond(value);
    }

    public K getSecond() {
        return getPair().getSecond();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getPair().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PairQueryable)) {
            return false;
        }
        PairQueryable other = (PairQueryable) obj;
        if (!getPair().equals(other.getPair())) {
            return false;
        }
        return true;
    }

    public Object getQueryableId() {
        return getMemberId(getFirst()) + '.' + getMemberId(getSecond());
    }

    private String getMemberId(IVdcQueryable member) {
        if (member != null) {
            return member.getQueryableId().toString();
        }
        return null;
    }
}
