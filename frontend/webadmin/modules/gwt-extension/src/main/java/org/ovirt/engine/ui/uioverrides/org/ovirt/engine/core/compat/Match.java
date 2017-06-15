package org.ovirt.engine.core.compat;

import java.util.ArrayList;

import com.google.gwt.regexp.shared.MatchResult;

/**
 * Replacement for System.Text.RegularExpressions.Match
 */
public class Match {

    private final boolean success;
    private final String value;
    private final ArrayList<Match> groups = new ArrayList<>();

    public Match(MatchResult matchResult) {
        if (matchResult != null) {
            value = matchResult.getGroup(0);
            for (int i = 1; i <= matchResult.getGroupCount(); i++) {
                groups.add(new Match(matchResult.getGroup(i)));
            }
            success = true;
        } else {
            value = "";
            success = false;
        }
    }

    public Match(String value) {
        this.value = value;
        this.success = true;
    }

    public ArrayList<Match> groups() {
        return groups;
    }

    public String getValue() {
        return value;
    }

    public boolean success() {
        return success;
    }

}
