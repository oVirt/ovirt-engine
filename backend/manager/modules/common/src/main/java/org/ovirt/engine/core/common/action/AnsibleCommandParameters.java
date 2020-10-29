package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class AnsibleCommandParameters extends ActionParametersBase {

    private int lastEventId;
    private String logFile;
    private String playUuid;
    private StringBuilder stringBuilder;
    private Guid hostId;
    private String playAction;
    private Map<String, Object> variables;

    public int getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(int lastEventId) {
        this.lastEventId = lastEventId;
    }

    public String getPlayUuid() {
        return playUuid;
    }

    public void setPlayUuid(String playUuid) {
        this.playUuid = playUuid;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public void setStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public void setPlayAction(String playAction) {
        this.playAction = playAction;
    }

    public String getPlayAction() {
        return playAction;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
