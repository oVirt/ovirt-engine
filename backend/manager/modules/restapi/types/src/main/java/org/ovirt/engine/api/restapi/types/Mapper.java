package org.ovirt.engine.api.restapi.types;

public interface Mapper<F, T> {

    /**
     * Map from one type to another.
     *
     * @param from
     *            source object
     * @param template
     *            template object, or null
     * @return mapped object
     */
    T map(F from, T template);

}
