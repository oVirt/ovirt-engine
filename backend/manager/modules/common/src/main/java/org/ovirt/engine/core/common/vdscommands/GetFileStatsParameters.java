package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetFileStatsParameters extends IrsBaseVDSCommandParameters {

    private Guid spUUID;
    private Guid sdUUID;
    private String pattern;
    private boolean caseSensitive;

    public GetFileStatsParameters() {}

    public GetFileStatsParameters(Guid spUUID, Guid sdUUID, String pattern, boolean caseSensitive) {
        super(spUUID);
        this.sdUUID = sdUUID;
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
    }

    public Guid getSpUUID() {
        return spUUID;
    }

    public void setSpUUID(Guid spUUID) {
        this.spUUID = spUUID;
    }

    public Guid getSdUUID() {
        return sdUUID;
    }

    public void setSdUUID(Guid sdUUID) {
        this.sdUUID = sdUUID;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
}
