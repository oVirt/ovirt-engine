package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

/**
 * A class representing extended fencing policy (extended in the meaning that not only host status and power management
 * settings will be used to decide to fence the host)
 */
public class FencingPolicy implements Serializable {
    /**
     * Skip fencing of host of it's connected to at least one storage domain.
     */
    private boolean skipFencingIfSDActive;

    public FencingPolicy() {
        skipFencingIfSDActive = false;
    }

    public FencingPolicy(FencingPolicy fencingPolicy) {
        if (fencingPolicy == null) {
            skipFencingIfSDActive = false;
        } else {
            skipFencingIfSDActive = fencingPolicy.skipFencingIfSDActive;
        }
    }

    public boolean isSkipFencingIfSDActive() {
        return skipFencingIfSDActive;
    }

    public void setSkipFencingIfSDActive(boolean skipFencingIfSDActive) {
        this.skipFencingIfSDActive = skipFencingIfSDActive;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FencingPolicy)) {
            return false;
        }
        FencingPolicy other = (FencingPolicy) obj;

        return skipFencingIfSDActive == other.skipFencingIfSDActive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (skipFencingIfSDActive ? 1231 : 1237);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ skipFencingIfSDActive=");
        sb.append(skipFencingIfSDActive);
        sb.append(" }");
        return sb.toString();
    }
}
