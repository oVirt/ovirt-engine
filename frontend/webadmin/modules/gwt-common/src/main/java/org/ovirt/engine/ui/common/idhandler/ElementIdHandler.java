package org.ovirt.engine.ui.common.idhandler;

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
 *     interface MyIdHandler extends ElementIdHandler&lt;HelloWorld&gt; {}
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
 * <p>
 * In case there are multiple instances of the same owner type displayed on the page, you can extend IDs generated for
 * those instances during runtime, for example:
 *
 * <pre>
 * public class HelloWorld {
 *
 *     ...
 *
 *     &#064;WithElementId
 *     Label label;
 *
 *     public HelloWorld(String extension) {
 *         label = new Label(&quot;Hello World&quot;);
 *         idHandler.setIdExtension(extension);
 *         idHandler.generateAndSetIds(this);
 *     }
 *
 * }
 *
 * public class MyApplication {
 *
 *     public void initialize() {
 *         new HelloWorld("One");
 *         new HelloWorld("Two");
 *     }
 *
 * }
 * </pre>
 *
 * This will cause label widgets to have following IDs set on their DOM elements:
 * <p>
 * <ul>
 * <li>{@code HelloWorld_label_One} for {@code HelloWorld} instance "One"
 * <li>{@code HelloWorld_label_Two} for {@code HelloWorld} instance "Two"
 * </ul>
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

    /**
     * Extends generated DOM element IDs with the given value at runtime.
     * <p>
     * This can be helpful when there are multiple instances of the same owner type displayed on the page.
     * <p>
     * Providing {@code null} or empty String has no effect.
     *
     * @param extension
     *            String value to append to DOM element IDs.
     */
    void setIdExtension(String extension);

}
