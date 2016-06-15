package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * A class representing extended fencing policy (extended in the meaning that not only host status and power management
 * settings will be used to decide to fence the host)
 */
public class FencingPolicy implements Serializable {
    /**
     * Enable fencing
     */
    private boolean fencingEnabled;

    /**
     * Skip fencing of host of it's connected to at least one storage domain.
     */
    private boolean skipFencingIfSDActive;
    private boolean skipFencingIfConnectivityBroken;
    private int hostsWithBrokenConnectivityThreshold;

    private boolean skipFencingIfGlusterBricksUp;
    private boolean skipFencingIfGlusterQuorumNotMet;

    public FencingPolicy() {
        this(null);
    }

    public FencingPolicy(FencingPolicy fencingPolicy) {
        if (fencingPolicy == null) {
            fencingEnabled = true;
            skipFencingIfSDActive = false;
            skipFencingIfConnectivityBroken = false;
            hostsWithBrokenConnectivityThreshold = 50;
            skipFencingIfGlusterBricksUp = false;
            skipFencingIfGlusterQuorumNotMet = false;
        } else {
            fencingEnabled = fencingPolicy.fencingEnabled;
            skipFencingIfSDActive = fencingPolicy.skipFencingIfSDActive;
            skipFencingIfConnectivityBroken = fencingPolicy.skipFencingIfConnectivityBroken;
            hostsWithBrokenConnectivityThreshold = fencingPolicy.hostsWithBrokenConnectivityThreshold;
            skipFencingIfGlusterBricksUp = fencingPolicy.skipFencingIfGlusterBricksUp;
            skipFencingIfGlusterQuorumNotMet = fencingPolicy.skipFencingIfGlusterQuorumNotMet;
        }
    }

    public boolean isFencingEnabled() {
        return fencingEnabled;
    }

    public void setFencingEnabled(boolean fencingEnabled) {
        this.fencingEnabled = fencingEnabled;
    }

    public boolean isSkipFencingIfSDActive() {
        return skipFencingIfSDActive;
    }

    public void setSkipFencingIfSDActive(boolean skipFencingIfSDActive) {
        this.skipFencingIfSDActive = skipFencingIfSDActive;
    }

    public boolean isSkipFencingIfConnectivityBroken() {
        return skipFencingIfConnectivityBroken;
    }

    public void setSkipFencingIfConnectivityBroken(boolean skipFencingIfConnectivityBroken) {
        this.skipFencingIfConnectivityBroken = skipFencingIfConnectivityBroken;
    }

    public int getHostsWithBrokenConnectivityThreshold() {
        return hostsWithBrokenConnectivityThreshold;
    }

    public void setHostsWithBrokenConnectivityThreshold(int hostsWithBrokenConnectivityThreshold) {
        this.hostsWithBrokenConnectivityThreshold = hostsWithBrokenConnectivityThreshold;
    }

    public boolean isSkipFencingIfGlusterBricksUp() {
        return skipFencingIfGlusterBricksUp;
    }

    public void setSkipFencingIfGlusterBricksUp(boolean skipFencingIfGlusterBricksUp) {
        this.skipFencingIfGlusterBricksUp = skipFencingIfGlusterBricksUp;
    }

    public boolean isSkipFencingIfGlusterQuorumNotMet() {
        return skipFencingIfGlusterQuorumNotMet;
    }

    public void setSkipFencingIfGlusterQuorumNotMet(boolean skipFencingIfGlusterQuorumNotMet) {
        this.skipFencingIfGlusterQuorumNotMet = skipFencingIfGlusterQuorumNotMet;
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

        return fencingEnabled == other.fencingEnabled
                && skipFencingIfSDActive == other.skipFencingIfSDActive
                && skipFencingIfConnectivityBroken == other.skipFencingIfConnectivityBroken
                && hostsWithBrokenConnectivityThreshold == other.hostsWithBrokenConnectivityThreshold
                && skipFencingIfGlusterBricksUp == other.skipFencingIfGlusterBricksUp
                && skipFencingIfGlusterQuorumNotMet == other.skipFencingIfGlusterQuorumNotMet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fencingEnabled,
                skipFencingIfSDActive,
                skipFencingIfConnectivityBroken,
                hostsWithBrokenConnectivityThreshold,
                skipFencingIfGlusterBricksUp,
                skipFencingIfGlusterQuorumNotMet
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("fencingEnabled", fencingEnabled)
                .append("skipFencingIfSDActive", skipFencingIfSDActive)
                .append("skipFencingIfConnectivityBroken", skipFencingIfConnectivityBroken)
                .append("hostsWithBrokenConnectivityThreshold", hostsWithBrokenConnectivityThreshold)
                .append("skipFencingIfGlusterBricksUp", skipFencingIfGlusterBricksUp)
                .append("skipFencingIfGlusterQuorumNotMet", skipFencingIfGlusterQuorumNotMet)
                .build();
    }
}
