package org.ovirt.engine.ui.userportal.widget.table.column;

public interface OsTypeExtractor<T> {

    public abstract int extractOsType(T item);

}
