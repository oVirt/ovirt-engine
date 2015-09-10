/**
 * Extension API for 3rd party components.
 * <p>
 * Design mission is complete detachment between extension
 * implementation and core product.
 * </p>
 * <p>
 * Key players:
 * </p>
 * <ul>
 * <li>{@link org.ovirt.engine.api.extensions.ExtMap} - Type safe key/value map as a base to exchange information.</li>
 * <li>{@link org.ovirt.engine.api.extensions.ExtKey} - ExtMap key, it bundles UUID and type.</li>
 * <li>{@link org.ovirt.engine.api.extensions.Extension#invoke} - Command invocation.</li>
 * </ul>
 * <p>
 * Both core and extension should access only keys that are known, ignore any other keys.
 * Extension can assume that context is kept throughout the extension life cycle.
 * </p>
 */
package org.ovirt.engine.api.extensions;
