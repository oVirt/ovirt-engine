package org.ovirt.engine.ui.uicommonweb;

public interface DynamicMessages {

    /**
     * Get the application title using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The application title string.
     */
    String applicationTitle();

    /**
     * Get the application version message using the {@code Dictionary} in the host page. With a fall back to the
     * standard GWT Message.
     *
     * @param version
     *            The version string to use to replace the place holder in the message.
     * @return The version about message
     */
    String ovirtVersionAbout(String version);

    /**
     * Get the copy right notice using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The copy right notice string.
     */
    String copyRightNotice();

    /**
     * Get the feedback URL using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The feedback URL.
     */
    String feedbackUrl(String version);

    /**
     * Get the feedback link label using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The feedback link label.
     */
    String feedbackLinkLabel();

    /**
     * Get the feedback link tooltip using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The feedback link tooltip.
     */
    String feedbackLinkTooltip();

    /**
     * Get the guide URL using the {@code Dictionary} in the host page. Uses current locale (e.g. "en_US")
     * for placeholder {0}, if it exists. With a fall back to the standard GWT Constant.
     *
     * @return The guide URL.
     */
    String guideUrl();

    /**
     * Get the guide link label using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The guide link label.
     */
    String guideLinkLabel();

    /**
     *
     * Get the console client resources page url using the {@code Dictionary} in the host page. With a fall back to
     * the standard GWT Constant.
     *
     * @return The console client resources page url.
     */
    String consoleClientResources();

    /**
     *
     * Get the client resources link name using the {@code Dictionary} in the host page. With a fall back to
     * the standard GWT Constant.
     *
     * @return The client resources page name.
     */
    String clientResources();

    /**
     *
     * Get the console client resources link name using the {@code Dictionary} in the host page. With a fall back to
     * the standard GWT Constant.
     *
     * @return The console client resources page name.
     */
    String consoleClientResourcesUrl();

    /**
     *
     * Get the vendor URL using the {@code Dictionary} in the host page. With a fall back to
     * the standard GWT Constant.
     *
     * @return The vendor url.
     */
    String vendorUrl();

    /**
     * Get the application documentation title using the {@code Dictionary} in the host page. With a fall back to the standard GWT
     * Constant.
     *
     * @return The application documentation title string.
     */
    String applicationDocTitle();

}
