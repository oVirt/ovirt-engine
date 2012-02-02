package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends CommonApplicationTemplates {

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

    @Template("<li>{0}</li>")
    SafeHtml listItem(SafeHtml item);

    @Template("<span style='font-size: 14px; font-family: Arial,sans-serif; font-weight: bold;'>{0}</span>")
    SafeHtml vmNameCellItem(String name);

    @Template(" ({0})")
    SafeHtml vmDescriptionCellItem(String description);

}
