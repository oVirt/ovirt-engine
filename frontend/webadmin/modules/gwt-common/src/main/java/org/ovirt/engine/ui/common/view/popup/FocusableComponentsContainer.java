package org.ovirt.engine.ui.common.view.popup;

/**
 * Interface implemented by widgets acting as containers holding focusable child components.
 */
public interface FocusableComponentsContainer {

    /**
     * Updates tab index for each child component that can gain focus, returning next available tab index.
     * <p>
     * {@code nextTabIndex} represents the currently available tab index value. Implementations should therefore assign
     * {@code nextTabIndex} to the first child component, {@code nextTabIndex+1} to the second one, etc. Method should
     * return should the next available tab index value.
     * <p>
     * For example:
     *
     * <pre>
     * public class ContainerImpl extends Composite implements FocusableComponentsContainer {
     *
     *     &#064;Override
     *     public int setTabIndexes(int nextTabIndex) {
     *         // childOne implements FocusableComponentsContainer
     *         nextTabIndex = childOne.setTabIndexes(nextTabIndex);
     *
     *         // childTwo implements Focusable
     *         childTwo.setTabIndex(nextTabIndex++);
     *
     *         return nextTabIndex;
     *     }
     *
     * }
     * </pre>
     *
     * @param nextTabIndex
     *            Currently available tab index value.
     * @return Next available tab index value.
     */
    public int setTabIndexes(int nextTabIndex);

}
