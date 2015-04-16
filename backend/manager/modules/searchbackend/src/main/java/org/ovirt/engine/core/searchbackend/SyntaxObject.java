package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class SyntaxObject {
    protected SyntaxObjectType mType = SyntaxObjectType.forValue(0);
    protected String mBody = null;
    protected int[] mPos = new int[2];

    public SyntaxObject(SyntaxObjectType type, String body, int startPos, int endPos) {
        mType = type;
        mBody = body;
        mPos[0] = startPos;
        mPos[1] = endPos;
    }

    public SyntaxObjectType getType() {
        return mType;
    }

    public void setType(SyntaxObjectType value) {
        mType = value;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String value) {
        mBody = value;
    }

    public int[] getPos() {
        return mPos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("body", mBody)
                .append("startPos", mPos[0])
                .append("endPos", mPos[1])
                .append("type", mType)
                .build();
    }
}
