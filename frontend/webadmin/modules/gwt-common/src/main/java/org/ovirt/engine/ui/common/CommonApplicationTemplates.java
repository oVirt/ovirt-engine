package org.ovirt.engine.ui.common;

import com.google.gwt.safecss.shared.SafeStyles;
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

    /**
     * Creates a progress bar template.
     *
     * @param progress
     *            Progress value in percent.
     * @param text
     *            Text to show within the progress bar.
     */
    @Template("<div id=\"{3}\" class='{2}'>" +
            "<div style='{0} height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div></div>")
    SafeHtml progressBar(SafeStyles styles, String text, String cssClass, String id);

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

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextCardStatus(SafeHtml image, String text);

    @Template("Card Status: {0}")
    SafeHtml cardStatus(String status);

    @Template("Link State: {0}")
    SafeHtml linkState(String state);

    @Template("<i>{0}</i>")
    SafeHtml italicText(String text);

    @Template("<div style='min-width: 445px; max-width: 500px; width: 100%; border-bottom: 1px solid #acacac; float: left;'>" +
            "<div style='white-space: nowrap; overflow: hidden; text-overflow: ellipsis; float: left; max-width: 49%'>{0}</div>" +
            "<div style='width: 2%; float: left;'>:</div>" +
            "<div style='white-space: normal; color: #acacac; float: left;'>{1}</div>" +
            "</div>")
    SafeHtml typeAheadNameDescription(String name, String description);

    @Template("<div style='min-width: 445px; max-width: 500px; width: 100%; border-bottom: 1px solid #acacac; float: left;'>" +
            "<div style='white-space: nowrap; overflow: hidden; text-overflow: ellipsis; float: left; max-width: 49%; color: {2};'>{0}</div>" +
            "<div style='width: 2%; float: left;'>:</div>" +
            "<div style='white-space: normal; color: #acacac; float: left;'>{1}</div>" +
            "</div>")
    SafeHtml typeAheadNameDescriptionWithColor(String name, String description, String color);

    @Template("<table style='min-width: 445px; width: 100%; border-bottom: 1px solid #acacac;'><tr>" +
            "<td>{0}</td>" +
            "</tr></table>")
    SafeHtml typeAheadName(String name);

    @Template("<table style='min-width: 445px; width: 100%; border-bottom: 1px solid #acacac;'><tr>" +
            "<td>&nbsp</td>" +
            "</tr></table>")
    SafeHtml typeAheadEmptyContent();

    @Template("<span>{0} {1}</span>")
    SafeHtml iconWithText(SafeHtml icon, String text);

    @Template("<span>{0}</span>")
    SafeHtml text(String text);

    @Template("<div style='border-right: 0 solid #D7D7E1; height: 32px;'>{0}</div>")
    SafeHtml nonResizeableColumnHeader(SafeHtml text);

    @Template("<div class=\"{0}\" id=\"{1}\">{2}</div>")
    SafeHtml divWithStyle(String style, String id, SafeHtml text);

    @Template("<div style=\"{0}\" id=\"{1}\">{2}</div>")
    SafeHtml divWithStringStyle(String style, String id, SafeHtml text);

    @Template("<strong style='{1}'>{0}</strong>")
    SafeHtml strongTextWithColor(String text, SafeStyles color);

    @Template("<font style='{1}'>{0}</font>")
    SafeHtml coloredText(String text, SafeStyles color);

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
