package org.ovirt.engine.core.compat;

import java.util.ArrayList;
import java.util.regex.MatchResult;

// I believe this is a wrapper for java.util.regex.MatchResult
/**
 * @deprecated Use {@link MatchResult} directly instead.
 */
@Deprecated
public class Match {
    private String value;
    private boolean success;
    private ArrayList<Match> groups;

    public Match(MatchResult mr, boolean success) {
        groups = new ArrayList<>();
        if (success) {
            this.success = success;
            for (int x = 1; x <= mr.groupCount(); x++) {
                groups.add(new Match(mr.group(x)));
            }
            value = mr.group();
        } else {
            value = null;
        }
    }

    public Match(String value) {
        this.value = value;
        this.success = true;
    }

    public String getValue() {
        return value;
    }

    public ArrayList<Match> groups() {
        return groups;
    }

    public boolean success() {
        return success;
    }
}
