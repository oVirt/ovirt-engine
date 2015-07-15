package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class SyntaxObject {
    protected SyntaxObjectType type = SyntaxObjectType.forValue(0);
    protected String body = null;
    protected int[] pos = new int[2];

    public SyntaxObject(SyntaxObjectType type, String body, int startPos, int endPos) {
        this.type = type;
        this.body = body;
        pos[0] = startPos;
        pos[1] = endPos;
    }

    public SyntaxObjectType getType() {
        return type;
    }

    public void setType(SyntaxObjectType value) {
        type = value;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String value) {
        body = value;
    }

    public int[] getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("body", body)
                .append("startPos", pos[0])
                .append("endPos", pos[1])
                .append("type", type)
                .build();
    }
}
