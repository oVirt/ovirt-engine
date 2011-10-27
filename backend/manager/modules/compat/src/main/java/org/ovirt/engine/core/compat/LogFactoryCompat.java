package org.ovirt.engine.core.compat;

import org.apache.commons.logging.LogFactory;

public class LogFactoryCompat /* extends LogFactory */{

    // @Override
    // public Object getAttribute(String arg0) {
    // throw new NotImplementedException();
    // }
    //
    // @Override
    // public String[] getAttributeNames() {
    // throw new NotImplementedException();
    // }
    //
    // @Override
    // public Log getInstance(Class clazz) throws LogConfigurationException {
    // return LogFactory.getLog(clazz) ;
    // }
    //
    // @Override
    // public Log getInstance(String name) throws LogConfigurationException {
    // return LogFactory.getLog(name) ;
    // }
    //
    // @Override
    // public void release() {
    // throw new NotImplementedException();
    // }
    //
    // @Override
    // public void removeAttribute(String arg0) {
    // throw new NotImplementedException();
    //
    // }
    //
    // @Override
    // public void setAttribute(String arg0, Object arg1) {
    // throw new NotImplementedException();
    //
    // }

    public static LogCompat getLog(Class loggedClass) {
        return new LogCompat(LogFactory.getLog(loggedClass));
    }
}
