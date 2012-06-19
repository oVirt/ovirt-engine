package org.ovirt.engine.core.compat;

import java.util.ArrayList;

@Deprecated
public class MatchGroups extends ArrayList<Match> {
    private int size;

    public int size() {
        return this.size;
    }
}
