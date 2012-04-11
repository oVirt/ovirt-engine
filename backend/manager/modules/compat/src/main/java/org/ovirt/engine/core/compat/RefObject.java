// ----------------------------------------------------------------------------------------
// Copyright (c) 2006 - 2009 Tangible Software Solutions Inc.
// This class can be used by anyone provided that the copyright notice remains intact.
//
// This class is used to simulate the ability to pass arguments by reference in Java.
// ----------------------------------------------------------------------------------------
package org.ovirt.engine.core.compat;

import java.io.Serializable;

@Deprecated
public final class RefObject<T> implements Serializable {
    private static final long serialVersionUID = -6587265469544116899L;

    public T argvalue;

    public RefObject() {
    }

    public RefObject(T refarg) {
        argvalue = refarg;
    }
}
