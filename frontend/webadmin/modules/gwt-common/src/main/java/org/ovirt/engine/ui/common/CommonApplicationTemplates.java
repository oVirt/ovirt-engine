package org.ovirt.engine.ui.common;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationTemplates extends SafeHtmlTemplates {
    /*
     * NOTE TO DEVS
     *
     * This is NOT the place to put messages, you can put messages in either CommonApplicationConstants (if there are
     * no variables in your message) or CommonApplicationMessages (if there are variables in your message). Templates
     * are for creating formatting of your message which you then PASS to the template. No text in this file will get
     * translated at all.
     */

    // TODO this doesn't belong here
    public static final int TAB_BAR_HEIGHT = 24;

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; margin-left: 3px; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextButton(SafeHtml image, String text);

    @Template("<span><span style='position: relative; vertical-align: middle;'>{0}</span>" +
            "<span style='position: relative; margin-left: 3px; white-space: nowrap;'>{1}</span></span>")
    SafeHtml textImageButton(String text, SafeHtml image);

    @Template("<span><span style='position: relative; height: 22px; vertical-align: bottom; display: table-cell;'>{0}</span>"
            +
            "<span style='position: relative; padding-left: 3px; vertical-align: middle; display: table-cell;'>{1}</span></span>")
    SafeHtml dualImage(SafeHtml image1, SafeHtml image2);

    @Template("<span><span style='position: relative; height: 22px; vertical-align: bottom; display: table-cell;'>{0}</span>"
            +
            "<span style='position: relative; padding-left: 3px; vertical-align: middle; display: table-cell; width: 19px;'>{1}</span>"
            +
            "<span style='position: relative; padding-left: 3px; vertical-align: middle; display: table-cell;'>{2}</span></span>")
    SafeHtml tripleImage(SafeHtml image1, SafeHtml image2, SafeHtml image3);

    @Template("<span style='width: 18px; vertical-align: middle; text-align: center; display: table-cell;'>{0}</span>")
    SafeHtml tableHeaderImage(SafeHtml image);

    @Template("<span style='height:22px; width: 22px; vertical-align: middle; text-align: center;'>{0}</span>")
    SafeHtml tableHeaderInlineImage(SafeHtml image);

    @Template("<span style='height:22px; width: 22px; vertical-align: middle; text-align: center;'>{0}</span>")
    SafeHtml inlineImage(SafeHtml image);

    @Template("<table cellspacing='0' cellpadding='0'><tr>" +
            "<td style='background: url({2});width:2px;'></td>" +
            "<td style='text-align:center;'>" +
            "<div class='db_bg_image {5} {6}' style='background: url({3}) repeat-x; height: 20px;'>" +
            "<span style='vertical-align: middle; line-height: 20px;' class=\"db_image_container\">{0}</span><div class=\"db_text\">{1}</div></div>" +
            "</td>" +
            "<td style='background: url({4});width:2px;'></td>" +
            "</tr></table>")
    SafeHtml dialogButton(SafeHtml image, String text, String start, String stretch,
            String end, String contentStyleName, String customContentStyleName);

    /**
     * Creates a progress bar template.
     *
     * @param progress
     *            Progress value in percent.
     * @param text
     *            Text to show within the progress bar.
     */
    @Template("<div id=\"{4}\" class='{3}'>" +
            "<div style='background: {2}; width: {0}%; height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div></div>")
    SafeHtml progressBar(int progress, String text, String color, String style, String id);

    @Template("<ul style='margin-top:0'>{0}</ul>")
    SafeHtml unsignedList(SafeHtml list);

    @Template("<ul>{0}</ul>")
    SafeHtml unorderedList(SafeHtml items);

    @Template("<li>{0}</li>")
    SafeHtml listItem(SafeHtml item);

    @Template("<li>{0}</li>")
    SafeHtml listItem(String item);

    @Template("{0} <sub>{1}</sub>")
    SafeHtml sub(String main, String sub);

    @Template("<b><font style='{0}'>{1}</font></b>")
    SafeHtml snapshotDescription(String style, String description);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextCardStatus(SafeHtml image, String text);

    @Template("Card Status: {0}")
    SafeHtml cardStatus(String status);

    @Template("Link State: {0}")
    SafeHtml linkState(String state);

    @Template("<i>{0}</i>")
    SafeHtml italicText(String text);

    @Template("<table style='min-width: 445px; width: 100%; border-bottom: 1px solid #acacac;'><tr>" +
            "<td style='width: 49%;'>{0}</td>" +
            "<td style='width: 2%; border-left: 1px solid #acacac;'></td>" +
            "<td style='white-space: normal; width: 49%; color: #acacac;'>{1}</td>" +
            "</tr></table>")
    SafeHtml typeAheadNameDescription(String name, String description);

    @Template("<table style='min-width: 445px; width: 100%; border-bottom: 1px solid #acacac;'><tr>" +
            "<td>{0}</td>" +
            "</tr></table>")
    SafeHtml typeAheadName(String name);

    @Template("<table style='min-width: 445px; width: 100%; border-bottom: 1px solid #acacac;'><tr>" +
            "<td>&nbsp</td>" +
            "</tr></table>")
    SafeHtml typeAheadEmptyContent();

    @Template("<div style='width: {0}; font-style: italic;'>{1}</div>")
    SafeHtml italicFixedWidth(String pxWidth, String text);

    @Template("<span>{0} {1}</span>")
    SafeHtml iconWithText(SafeHtml icon, String text);

    @Template("<span>{0}</span>")
    SafeHtml text(String text);

    @Template("<div style='border-right: 0 solid #D7D7E1; height: 32px;'>{0}</div>")
    SafeHtml nonResizeableColumnHeader(SafeHtml text);

    @Template("<div class=\"{0}\" id=\"{1}\">{2}</div>")
    SafeHtml divWithStyle(String style, String id, SafeHtml text);

    @Template("<strong style='color: {1};'>{0}</strong>")
    SafeHtml strongTextWithColor(String text, String color);

    @Template("<font style='color: {1};'>{0}</font>")
    SafeHtml coloredText(String text, String color);

    @Template("<p><hr size=\"2\" width=\"100%\"></p>")
    SafeHtml horizontalLine();

    @Template("<p>{0}</p>")
    SafeHtml paragraph(String text);

    @Template("{0} Mbps")
    SafeHtml nicSpeed(int speed);

    @Template("{0} Pkts")
    SafeHtml dropRate(double speed);

    @Template("<div title=\"{0}\" style='white-space: nowrap; text-overflow: ellipsis; overflow: hidden;'>{0}</div>")
    SafeHtml textWithToolTip(String text);
}
