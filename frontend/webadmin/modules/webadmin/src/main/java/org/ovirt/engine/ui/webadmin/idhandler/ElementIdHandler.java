package org.ovirt.engine.ui.webadmin.idhandler;

/**
 * Interface implemented by classes that generate and set DOM element IDs into appropriate fields of an owner.
 * <p>
 * Generated element IDs are guaranteed to be unique and deterministic within the context of the given owner type.
 * <p>
 * Implementations of this interface are intended to be generated for each specific owner type, for example:
 * 
 * <pre>
 * public class HelloWorld {
 * 
 *     interface MyIdHandler extends ElementIdHandler&lt;HelloWorld&gt; {
 *     }
 * 
 *     private static MyIdHandler idHandler = GWT.create(MyIdHandler.class);
 * 
 *     &#064;WithElementId
 *     Label label;
 * 
 *     public HelloWorld() {
 *         label = new Label(&quot;Hello World&quot;);
 *         idHandler.generateAndSetIds(this);
 *     }
 * 
 * }
 * </pre>
 * 
 * In the example above, the label widget's DOM element will have its ID set to {@code HelloWorld_label}.
 * 
 * @param <T>
 *            The type of an object that contains {@literal @WithElementId} fields.
 * 
 * @see WithElementId
 * @see HasElementId
 */
public interface ElementIdHandler<T> {

    /**
     * Generates and sets DOM element IDs into appropriate fields of the given root object.
     * 
     * @param owner
     *            The object whose {@literal @WithElementId} fields need to be processed.
     */
    void generateAndSetIds(T owner);

}
