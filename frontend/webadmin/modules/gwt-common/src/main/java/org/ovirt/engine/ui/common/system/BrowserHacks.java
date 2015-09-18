package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.common.uicommon.ClientAgentType;

import com.google.inject.Inject;

/**
 * "Keep calm and open Internet Explorer."
 * <p>
 * This class contains hacks to work around browser-specific quirks.
 */
public final class BrowserHacks {

    private static final String GWT_PLACEHOLDER_IMAGE = "clear.cache.gif"; //$NON-NLS-1$

    @Inject
    public BrowserHacks(ClientAgentType clientAgentType) {
        if (clientAgentType.isIE()) {
            fixIEImageSize(GWT_PLACEHOLDER_IMAGE);
        }
    }

    /**
     * IE automatically adds {@code width} and {@code height} attributes, with values
     * taken from {@code src} image data, when an {@code <img>} element is created via
     * {@code Image} constructor function. For example:
     *
     * <pre>
     *     var img = new Image();
     *     img.src = "foo.png";
     *     document.body.appendChild(img); // adds "width" and "height" automatically
     * </pre>
     *
     * This causes render issues for images that use {@code src} attribute for initial
     * placeholder image and CSS styling for the actual image. For example:
     *
     * <pre>
     *     &lt;style&gt;
     *         .fooImage {
     *             background: url("foo.png");
     *             width: 400px;
     *             height: 200px;
     *         }
     *     &lt;/style&gt;
     *
     *     &lt;img src="clear.cache.gif" class="fooImage"&gt;
     * </pre>
     *
     * In IE, above {@code <img>} element would have {@code width} and {@code height}
     * attributes, with values taken from {@code clear.cache.gif} placeholder image
     * data, added automatically:
     *
     * <pre>
     *     &lt;img src="clear.cache.gif" width="1" height="1" class="fooStyle"&gt;
     * </pre>
     *
     * To fix this quirk, {@code width} and {@code height} attributes of such images
     * are set to value {@code auto}:
     *
     * <pre>
     *     &lt;img src="clear.cache.gif" width="auto" height="auto" class="fooStyle"&gt;
     * </pre>
     */
    private native void fixIEImageSize(String placeholderImageUrl) /*-{
        var $ = $wnd.jQuery;

        // Using DOM mutation events to stay compatible with IE9+
        $('body').on('DOMNodeInserted', 'img', function (e) {
            var $img = $(e.target);

            if ($img.attr('src') == placeholderImageUrl) {
                setTimeout(function () {
                    $img.attr('width', 'auto');
                    $img.attr('height', 'auto');
                }, 1);
            }
        });
    }-*/;

}
