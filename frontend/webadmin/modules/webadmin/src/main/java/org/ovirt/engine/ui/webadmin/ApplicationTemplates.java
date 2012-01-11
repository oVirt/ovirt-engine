package org.ovirt.engine.ui.webadmin;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends SafeHtmlTemplates {

    /**
     * Creates a progress bar template.
     *
     * @param progress
     *            Progress value in percent.
     * @param text
     *            Text to show within the progress bar.
     */
    @Template("<div class='engine-progress-box'>" +
            "<div style='background: {2}; width: {0}%; height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div>" +
            "</div>")
    SafeHtml progressBar(int progress, String text, String color);

    /**
     * Creates a tree-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; bottom: 1px;'>{0}</span><span style='position: relative; bottom: 7px;'>{1}</span>")
    SafeHtml treeItem(SafeHtml imageHtml, String text);

    /**
     * Creates a bookmark-item HTML
     *
     * @param text
     *            the bookmark title
     */
    @Template("<span id='{0}' style='display: inline-block; padding: 5px;'>{1}</span>")
    SafeHtml bookmarkItem(String id, String text);

    /**
     * Creates a tag-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node title
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {3}; " +
            "bottom: 4px; padding: 0 3px; margin: 0 1px;  white-space: nowrap; background-color: {2};'>" +
            "<span style='position: relative; top: 1px;'>{0}</span> {1}</span>")
    SafeHtml tagItem(SafeHtml imageHtml, String text, String backgroundColor, String borderColor);

    /**
     * Creates a tag-button HTML
     *
     * @param imageHtml
     *            the image HTML
     * @return
     */
    @Template("<span style='position: relative; border: 1px solid {2}; visibility: {3};" +
            " bottom: 4px; padding: 0 3px; background-color: {1};'>{0}</span>")
    SafeHtml tagButton(SafeHtml imageHtml, String backgroundColor, String borderColor, String visibility);

    @Template("<span style='position: relative; white-space: nowrap;'><span>{0}</span>{1} Alerts</span>")
    SafeHtml alertFooterHeader(SafeHtml imageHtml, int alertCount);

    @Template("<span><span style='position: relative; vertical-align: middle;'>{0}</span>" +
            "<span style='position: relative; margin-left: 3px; white-space: nowrap;'>{1}</span></span>")
    SafeHtml imageTextButton(SafeHtml image, String text);

    @Template("<table cellspacing='0' cellpadding='0'><tr>" +
            "<td style='background: url({2});width:2px;'></td>" +
            "<td style='text-align:center;'>" +
            "<div class='{5} {6}' style='background: url({3}) repeat-x; height:20px;'>" +
            "<span style='vertical-align: middle; margin-right: 3px;'>{0}</span>{1}</div>" +
            "</td>" +
            "<td style='background: url({4});width:2px;'></td>" +
            "</tr></table>")
    SafeHtml dialogButton(SafeHtml image, String text, String start, String stretch,
            String end, String contentStyleName, String customContentStyleName);

    @Template("<ul style='margin-top:0'>{0}</ul>")
    SafeHtml unsignedList(SafeHtml list);

    @Template("<li>{0}")
    SafeHtml listItem(SafeHtml item);
}
