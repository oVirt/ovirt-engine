package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;

import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;

public interface ApplicationTemplates extends CommonApplicationTemplates {

    @Template("<div>" +
            "<div style='{1} float:right; text-align: right;'>{2}%</div>" +
            "<div style='width: 100%; margin: auto;'>" +
                "<img style='border-left: 1px solid #c0c0c0; border-bottom: 1px solid #c0c0c0;' src='{0}' />" +
            "</div>" +
            "</div>")
    SafeHtml lineChart(SafeUri chartData, SafeStyles styles, int percentage);

    @Template("<div>" +
            "<div style='{0} float:right;'>--</div>" +
            "</div>")
    SafeHtml lineChartWithoutImage(SafeStyles styles);

    @Template("<div id=\"{3}\" title='{2}' class='engine-progress-box'>" +
            "<div style='{0} height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div></div>")
    SafeHtml glusterCapcityProgressBar(SafeStyles styles, String sizeString, String toolTip, String id);

    /**
     * Creates a tree-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node text
     */
    @Template("<span style='position: relative; bottom: 1px;'>{0}</span>" +
            "<span id='{2}'>{1}</span>")
    SafeHtml treeItem(SafeHtml imageHtml, String text, String id);

    /**
     * Creates a bookmark-item HTML
     *
     * @param text
     *            the bookmark text
     */
    @Template("<span id='{0}' style='display: inline-block; padding: 2px;'>{1}</span>")
    SafeHtml bookmarkItem(String id, String text);

    @Template("<span style='position: relative; white-space: nowrap;'><span>{0}</span>{1} Alerts</span>")
    SafeHtml alertFooterHeader(SafeHtml imageHtml, int alertCount);

    @Template("<div id=\"{2}\" style=\"text-align: center;\" data-status=\"{3}\">{0}{1}</div>")
    SafeHtml statusWithAlertTemplate(SafeHtml statusImage, SafeHtml alertImage, String id, String status);

    @Template("<i class= \"fa {0}\" style= \"font-size:16px;color:orange;\"></i>")
    SafeHtml iconTypeAlertTemplate(String css);

    @Template("<div id=\"{1}\" style=\"text-align: center;\" data-status=\"{2}\">{0}</div>")
    SafeHtml statusTemplate(SafeHtml statusImage, String id, String status);

    @Template("<div title='{1}' id=\"{2}\" style=\"text-align: center;\">{0}</div>")
    SafeHtml imageWithHoverTextAndContainerId(SafeHtml statusImage, String hoverText, String id);

    @Template("<span><span style='position: absolute; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{1}</span>"
            + "<span style='position: relative; white-space: normal; height: 14px; line-height: 14px;'>{0}</span></span>")
    SafeHtml lockedStatusTemplate(SafeHtml lockImage, SafeHtml statusImage);

    @Template("<button type='button' tabindex='-1' style='float: right; height: 20px;'>"
            +
            "<span style='position: relative; left: 0px; top: -5px; width: 100%; font-size: 10px;'>{0}</span></button>")
    SafeHtml actionButtonText(String text);

    @Template("<span style=\"top: -3px; left: 3px; position: relative;\">{0}</span>")
    SafeHtml textForCheckBoxHeader(String text);

    @Template("<span style=\"top: -2px; left: 3px; position: relative;\">{0}</span>")
    SafeHtml textForCheckBox(String text);

    @Template("{0} <span style='font-weight:bold; color: red;'>{1}</span>")
    SafeHtml blackRedBold(String black, String redBold);

    @Template("{0} <span style='font-weight:bold;'>{1}</span> {2}")
    SafeHtml middleBold(String start, String middle, String end);

    @Template("<span><span style='position: relative; margin-left: 20px; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='{2} ;position: relative; margin-left: 3px; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextSetupNetworkUsage(SafeHtml image, String text, SafeStyles color);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; margin-left: 3px; margin-right: 3px; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageWithText(SafeHtml image, String text);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
                    + "<span style='position: relative; margin-left: 3px; margin-right: 3px; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageWithSafeHtml(SafeHtml image, SafeHtml html);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; margin-left: 3px; margin-right: 3px; white-space: normal; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextSetupNetwork(SafeHtml image, SafeHtml text);

    @Template("<span>{0}</span><ul>{1}</ul>")
    SafeHtml unorderedListWithTitle(String title, SafeHtml listItems);

    @Template("<div style='{1} font-weight:bold; width:100%; padding:3px;'>{0}</div> ")
    SafeHtml titleSetupNetworkTooltip(String title, SafeStyles backgroundColor);

    @Template("<i>{0}<br />{1}</i>")
    SafeHtml italicTwoLines(String firstLine, String secondLine);

    @Template("<div style='{0} no-repeat;'></div>")
    SafeHtml image(SafeStyles imageStyles);

    @Template("<div style='line-height: 100%; text-align: center; vertical-align: middle;'>{0}</div>")
    SafeHtml image(SafeHtml statusImage);

    @Template("<div id=\"{1}\"style='line-height: 100%; text-align: center; vertical-align: middle;'>{0}</div>")
    SafeHtml imageWithId(SafeHtml statusImage, String id);

    @Template("<table id=\"{4}\"> <tr> " +
            "<td> <div>{0}</div> </td>" +
            "<td> {1} </td>" +
            "<td> <div> {2} </div> </td>" +
            "<td> {3} </td>" +
            "</tr> </table>")
    SafeHtml volumeBrickStatusTemplate(SafeHtml upImage, int upCount, SafeHtml downImage, int downCount, String id);

    @Template("<div style='line-height: 100%; text-align: center; vertical-align: middle;'>{0}</div>")
    SafeHtml volumeSnapshotsStatusTemplate(int snapshotCount);

    @Template("<div id=\"{1}\" style='line-height: 100%; text-align: center; vertical-align: middle; border: solid 1px transparent; '>{0}</div>")
    SafeHtml volumeActivityMenu(SafeHtml statusImage, String id);

    @Template("<div><table cellspacing='0' cellpadding='0' style='line-height: 5px; width: 100%;'>" +
            "<tr>" +
            "{1}" +
            "<td style='width: 30%; text-align: center;'>&#160;{2}&#160;</td>" +
            "{3}" +
            "</tr>" +
            "</table>" +
            "</div>")
    SafeHtml vmCountWithMigrations(
            String title,
            SafeHtml incomingMigrations,
            String vmCountStr,
            SafeHtml outgoingMigrations);

    @Template("<td style='text-align: right;'>{0}</td>" +
            "<td style='text-align: center; white-space: nowrap; width: 10%;'>" +
            "<div style='font-size: 10px; text-align: center;'>{1}</div>" +
            "<div style='width: 25px;'>{2}</div>" +
            "</td>" +
            "<td style='text-align: left;'>{3}</td>")
    SafeHtml vmCountInOutMigrations(String prefix, String vmCountStr, SafeHtml image, String postfix);

    @Template("<div style='width: 100%; float: left;'><div style='font-weight: bold; width: 100%;'>{0}</div><div style='width: 100%;'>{1}</div></div>")
    SafeHtml migrationPolicyDetails(String name, String description);

    @Template("<div style='max-width: 500px; word-wrap:break-word; font-style: italic;'>{0}</div>")
    SafeHtml italicWordWrapMaxWidth(String text);

    @Template("<div style='max-width: 500px; word-wrap:break-word; font-style: italic;'><b>{0}</b><br/>{1}</div>")
    SafeHtml italicWordWrapMaxWidthWithBoldTitle(String title, SafeHtml text);

    @Template("<div style='max-width: 250px; display:inline-block;'>{0}</div>")
    SafeHtml maxWidthNteworkItemPopup(String text);

    @Template("<span style='position: relative; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{1}</span>")
    SafeHtml textImageLabels(String text, SafeHtml image);

    @Template("<div id=\"{0}\" style='text-align: center;'>{1}</div>")
    SafeHtml hostAdditionalStatusIcon(String id, SafeHtml statusImage);

    @Template("<div style='padding-bottom: 5px;'>{0}</div>")
    SafeHtml hostAlertTooltip(String text);

    @Template("{0} {1} {2}")
    SafeHtml hostOutOfSyncPreviewSentence(SafeHtml host, SafeHtml outOfsyncSentence, SafeHtml dc);

    @Template("<div class='talign-center'>{0}</div>")
    SafeHtml networkDeviceStatusImg(SafeHtml imgHtml);

    @Template("<div class='talign-center'><span class='spinner spinner-xs spinner-inline valign-middle'></span>{0}...</div>")
    SafeHtml networkOperationInProgressDiv(String progressText);

    @Template("<div class='talign-center'>{0}&nbsp;&nbsp;&nbsp;<span class='spinner spinner-xs spinner-inline valign-middle'></span> {1}...</div>")
    SafeHtml networkDeviceStatusImgAndNetworkOperationInProgress(SafeHtml imgHtml, String progressText);

    @Template("<div class='networkUpdatingSpinner'><span class='spinner spinner-xs spinner-inline valign-middle'></span> {0}...</div>")
    SafeHtml networkUpdatingSpinner(String progressText);

    @Template("<span class=\"fa-chain-broken fa\" style=\"color: red\"></span>")
    SafeHtml brokenLinkRed();
}
