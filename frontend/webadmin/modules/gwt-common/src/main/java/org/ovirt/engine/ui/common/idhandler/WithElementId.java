package org.ovirt.engine.ui.common.idhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given field will have its DOM element ID set by an {@link ElementIdHandler} implementation.
 * <p>
 * Semantics of this annotation for different types are shown in the following table:
 * <p>
 *
 * <blockquote>
 * <table border="1" cellpadding="5" cellspacing="0">
 * <thead>
 * <tr>
 * <th>Field Type</th>
 * <th>Semantics</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@link HasElementId}</td>
 * <td>call {@link HasElementId#setElementId setElementId} (used with custom UI object types)</td>
 * </tr>
 * <tr>
 * <td>{@link com.google.gwt.user.client.ui.UIObject}</td>
 * <td>access element through {@linkplain com.google.gwt.user.client.ui.UIObject#getElement getElement} and set its ID using {@link com.google.gwt.user.client.Element#setId
 * setId}</td>
 * </tr>
 * <tr>
 * <td>{@link com.google.gwt.user.client.Element}</td>
 * <td>set element ID using {@link com.google.gwt.user.client.Element#setId setId}</td>
 * </tr>
 * </tbody>
 * </table>
 * </blockquote>
 *
 * <p>
 * Since {@link ElementIdHandler} implementations access field values directly through field declarations, annotated
 * fields should not be {@code private}.
 *
 * @see ElementIdHandler
 * @see HasElementId
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WithElementId {

    /**
     * Overrides the default field ID that is part of the resulting DOM element ID.
     * <p>
     * When not specified, the name of the annotated field will be taken as the field ID value.
     *
     * @return Custom field ID or an empty string to use the default value.
     */
    String value() default "";

    /**
     * If {@code true}, declared type of the given field will be recursively processed with regard to
     * {@literal @WithElementId} fields. If {@code false}, no further action will be taken on the field type.
     *
     * @return {@code true} if the field type should be recursively processed with regard to {@literal @WithElementId}
     *         fields, {@code false} otherwise.
     */
    boolean processType() default true;

}
