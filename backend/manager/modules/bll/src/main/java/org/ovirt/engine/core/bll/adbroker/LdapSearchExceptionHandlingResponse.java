package org.ovirt.engine.core.bll.adbroker;

public class LdapSearchExceptionHandlingResponse {
    private boolean tryNextServer;
    private Score serverScore;
    private Exception translatedException;

    public boolean isTryNextServer() {
        return tryNextServer;
    }

    public Score getServerScore() {
        return serverScore;
    }

    public Exception getTranslatedException() {
        return translatedException;
    }

    public LdapSearchExceptionHandlingResponse setTryNextServer(boolean tryNextServer) {
        this.tryNextServer = tryNextServer;
        return this;
    }

    public LdapSearchExceptionHandlingResponse setServerScore(Score serverScore) {
        this.serverScore = serverScore;
        return this;
    }

    public LdapSearchExceptionHandlingResponse setTranslatedException(Exception translatedException) {
        this.translatedException = translatedException;
        return this;
    }
}
