package org.ovirt.engine.core.utils.ovf;

/**
 * Interface for type conversation classes. Type conversion classes provide method to convert from a given object to a
 * string , and from a string (and a class that hints on the type of the converted result) to an object
 *
 */
public interface TypeConverter {

    /**
     * Returns a conversion of an object to String
     *
     * @param value
     *            to convert
     * @return value result of conversion (null if fails)
     */
    public String convert(Object value);

    /**
     * Converts the string representation to an object of a type which is provided by the clazz parameter
     *
     * @param value
     *            string to convert
     * @param clazz
     *            class of type
     * @return result of conversation (null if fails)
     */
    public Object convert(String value, Class<?> clazz);
}
