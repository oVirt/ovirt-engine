package org.ovirt.engine.core.compat;

public class ApplicationException extends RuntimeException {

    public ApplicationException() {
    }

    // public ApplicationException(SerializationInfo info, StreamingContext
    // context) {
    // throw new NotImplementedException();
    // }
    //

    // public ApplicationException(String string, RuntimeException
    // baseException) {
    // super(string, baseException)
    // }
    //

    public ApplicationException(String string) {
        super(string);
    }

    public ApplicationException(String string, Throwable ex) {
        super(string, ex);
    }

    // public void GetObjectData(SerializationInfo info, StreamingContext
    // context) {
    // throw new NotImplementedException();
    // }
    //

}
