package org.ovirt.engine.ui.gwtaop;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;

public class AspectJLogHandler implements IMessageHandler {

    @Override
    public boolean handleMessage(IMessage message) throws AbortException {
        return SYSTEM_OUT.handleMessage(message);
    }

    @Override
    public boolean isIgnoring(Kind kind) {
        return kind == IMessage.DEBUG;
    }

    @Override
    public void dontIgnore(Kind kind) {
    }

    @Override
    public void ignore(Kind kind) {
    }

}
