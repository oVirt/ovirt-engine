package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;

public class ScorableLDAPServer implements LDAPServer, Comparable<ScorableLDAPServer> {

    final private URI uri;
    private int score;

    public ScorableLDAPServer(URI uri) {
        this.uri = uri;
    }


    /**
     * Defensive copy of the URI. Prevents mutating the inner URI
     */
    @Override
    public URI getURI() {
        return URI.create(uri.toString());
    }

    @Override
    public int compareTo(ScorableLDAPServer o) {
        return o.getScore() - getScore();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

}
