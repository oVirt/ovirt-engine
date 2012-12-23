package org.ovirt.engine.ui.common;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationTemplates extends SafeHtmlTemplates {

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

    @Template("<span style='height:22px; width: 22px; vertical-align: middle; text-align: center; display: table-cell;' title='{1}'>{0}</span>")
    SafeHtml imageWithTitle(SafeHtml image, String title);

    @Template("<table cellspacing='0' cellpadding='0'><tr>" +
            "<td style='background: url({2});width:2px;'></td>" +
            "<td style='text-align:center;'>" +
            "<div class='{5} {6}' style='background: url({3}) repeat-x; height: 20px;'>" +
            "<span style='vertical-align: middle; margin-right: 3px; line-height: 20px;'>{0}</span>{1}</div>" +
            "</td>" +
            "<td style='background: url({4});width:2px;'></td>" +
            "</tr></table>")
    SafeHtml dialogButton(SafeHtml image, String text, String start, String stretch,
            String end, String contentStyleName, String customContentStyleName);

    @Template("<ul style='margin-top:0'>{0}</ul>")
    SafeHtml unsignedList(SafeHtml list);

    @Template("<li>{0}</li>")
    SafeHtml listItem(SafeHtml item);

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

}
