package org.ovirt.engine.core.common.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Eases implementation of {@code toString()} methods.
 *
 * TODO: When our business entities won't be compiled with GWT compiler this class could be replaced/refactored using
 * TODO: commons-lang {@code ToStringBuilder} or guava {@code MoreObjects.ToStringHelper}
 */
public class ToStringBuilder {
    static final char CLASS_NAME_SUFFIX = ':';
    static final char ATTRIBUTES_LIST_PREFIX = '{';
    static final char ATTRIBUTES_LIST_SUFFIX = '}';
    static final String ATTRIBUTES_SEPARATOR =  ", ";
    static final String NAME_VALUE_SEPARATOR = "='";
    static final char VALUE_SUFFIX = '\'';
    static final String FILTERED_CONTENT = "***";

    /**
     * Buffer to store class name and its attributes
     */
    private final StringBuilder buffer;

    /**
     * Indicates if at least one attribute was already appended {@code false} or not {@code true}
     */
    private boolean noAttributes;

    private ToStringBuilder() {
        this(null);
    }

    private ToStringBuilder(Class<?> clazz) {
        buffer = new StringBuilder();
        noAttributes = true;
        appendClassName(clazz);
    }

    /**
     * Creates builder instance for specified class
     */
    public static ToStringBuilder forClass(Class<?> clazz) {
        return new ToStringBuilder(clazz);
    }

    /**
     * Creates builder instance for specified instance
     */
    public static ToStringBuilder forInstance(Object instance) {
        return new ToStringBuilder(instance == null ? null : instance.getClass());
    }

    /**
     * Appends {@code boolean} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final boolean value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code boolean[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final boolean[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code byte} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final byte value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code byte[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final byte[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code char} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final char value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code char[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final char[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code double} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final double value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code double[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final double[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code float} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final float value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code float[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final float[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code int} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final int value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code int[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final int[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code long} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final long value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code long[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final long[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code Object} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final Object value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code Object[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final Object[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code short} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final short value) {
        appendAttribute(name, String.valueOf(value));
        return this;
    }

    /**
     * Appends {@code short[]} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder append(final String name, final short[] value) {
        appendAttribute(name, Arrays.toString(value));
        return this;
    }

    /**
     * Appends {@code Collection} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public <T> ToStringBuilder append(final String name, final Collection<T> value) {
        appendAttribute(name, value == null ? null : Arrays.toString(value.toArray()));
        return this;
    }

    /**
     * Appends {@code Map} attribute with specified {@code name} and {@code value}
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public <K, V> ToStringBuilder append(final String name, final Map<K, V> value) {
        append(name, value == null ? null : value.entrySet());
        return this;
    }

    /**
     * Appends {@code String} attribute with specified {@code name} and its filtered {@code value}:
     * <ul>
     *     <li>If value is not {@code null}, then {@code getFilteredContent()} is appended as value</li>
     *     <li>Otherwise {@code null} is appended as value</li>
     * </ul>
     *
     * @param name
     *            attribute name
     * @param value
     *            attribute value
     * @return this
     */
    public ToStringBuilder appendFiltered(final String name, final String value) {
        appendAttribute(name, filterValue(value));
        return this;
    }

    /**
     * Saves specified class and its attributes into {@code String}
     */
    public String build() {
        final int bufferLength = buffer.length();
        if (bufferLength > 0) {
           final char lastChar = buffer.charAt(bufferLength - 1);
           if (lastChar == VALUE_SUFFIX) {
               // at least one attribute was added to list
               buffer.append(ATTRIBUTES_LIST_SUFFIX);
           } else if (lastChar == CLASS_NAME_SUFFIX) {
               // only class name was added to buffer
               buffer.append(ATTRIBUTES_LIST_PREFIX);
               buffer.append(ATTRIBUTES_LIST_SUFFIX);
           }
        }
        return buffer.toString();
    }

    /**
     * Appends attribute with specified name and value to buffer
     */
    private void appendAttribute(String name, String value) {
        appendSeparatorOrPrefix();
        buffer.append(name);
        buffer.append(NAME_VALUE_SEPARATOR);
        buffer.append(value);
        buffer.append(VALUE_SUFFIX);
    }

    /**
     * Appends attributes separator or attributes list prefix when needed
     */
    private void appendSeparatorOrPrefix() {
        if (noAttributes) {
            // append prefix of attributes list
            buffer.append(ATTRIBUTES_LIST_PREFIX);
            noAttributes = false;
        } else {
            // except for 1st attribute we need to append separator
            buffer.append(ATTRIBUTES_SEPARATOR);
        }
    }

    /**
     * Appends name of specified class to buffer
     */
    private void appendClassName(Class<?> clazz) {
        if (clazz != null) {
            buffer.append(clazz.getSimpleName());
            buffer.append(CLASS_NAME_SUFFIX);
        }
    }

    /**
     * Filters specified value
     */
    private String filterValue(String value) {
        return value == null ? null : FILTERED_CONTENT;
    }

    /**
     * @return String that is under construction via this builder
     * @see #build()
     */
    @Override
    public String toString() {
        return build();
    }
}
