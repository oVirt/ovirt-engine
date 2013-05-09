package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.StringFormat;

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
        String retval = StringFormat.format("body = '%1$s' , startPos = %2$s , endPos = %3$s, type = %4$s", mBody,
                mPos[0], mPos[1], mType);
        return retval;
    }
}
