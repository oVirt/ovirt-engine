package org.ovirt.engine.core.bll.context;

public final class EngineContext implements Cloneable {

    private String sessionId;

    public EngineContext() {
    }

    public EngineContext(EngineContext engineContext) {
        sessionId = engineContext.sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EngineContext withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public EngineContext clone() {
        return new EngineContext(this);
    }

}
