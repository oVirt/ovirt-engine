package org.ovirt.engine.api.restapi.resource.validation;

public interface Validator<E> {

    public void validateEnums(E entity);
}
