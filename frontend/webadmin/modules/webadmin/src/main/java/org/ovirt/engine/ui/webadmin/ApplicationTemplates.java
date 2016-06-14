package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;

public interface ApplicationTemplates extends CommonApplicationTemplates {

    @Template("<div>" +
            "<div style='float:right; width: {2}px; color: {3}; font-weight: {4}; text-align: right'>{5}%</div>" +
            "<div style='margin-right: {1}px; width: 100%; margin: auto;'>" +
                "<img style='border-left: 1px solid #c0c0c0; border-bottom: 1px solid #c0c0c0;' src='{0}' />" +
            "</div>" +
            "</div>")
    SafeHtml lineChart(SafeUri chartData, int chartMarginRight, int textWidth, String textColor, String fontWeight, int percentage);

    @Template("<div>" +
            "<div style='float:right; width: {0}px; color: {1}; font-weight: {2}'>{3}%</div>" +
            "</div>")
    SafeHtml lineChartWithoutImage(int textWidth, String textColor, String fontWeight, int percentage);

    @Template("<div id=\"{4}\" title='{3}' class='engine-progress-box'>" +
            "<div style='background: {2}; width: {0}%; height: 100%'></div>" +
            "<div class='engine-progress-text'>{1}</div></div>")
    SafeHtml glusterCapcityProgressBar(int progress, String sizeString, String color, String toolTip, String id);

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

    /**
     * Creates a tag-item HTML
     *
     * @param imageHtml
     *            the image HTML
     * @param text
     *            the node text
     */
    @Template("<span style='position: relative; border: 1px solid {3}; " +
            "bottom: 4px; padding: 0 3px; margin: 0 1px;  white-space: nowrap; background-color: {2};'>" +
            "<span style='position: relative; top: 1px;' id='{4}'>{0}</span> {1}</span>")
    SafeHtml tagItem(SafeHtml imageHtml, String text, String backgroundColor, String borderColor, String id);

    /**
     * Creates a tag-button HTML
     *
     * @param imageHtml
     *            the image HTML
     */
    @Template("<span style='position: relative; left: -2px; border: 1px solid {2}; visibility: {3};" +
            " bottom: 4px; padding: 0 3px; background-color: {1};' id='{4}'>{0}</span>")
    SafeHtml tagButton(SafeHtml imageHtml, String backgroundColor, String borderColor, String visibility, String id);

    @Template("<span style='position: relative; white-space: nowrap;'><span>{0}</span>{1} Alerts</span>")
    SafeHtml alertFooterHeader(SafeHtml imageHtml, int alertCount);

    @Template("<table cellspacing='0' cellpadding='0'><tr>"
            +
            "<td><div style='background: url({2}); width: 4px; height: 20px; float:left;'></div>"
            +
            "<div class='{5}' style='background: url({3}) repeat-x; white-space: nowrap; height: 20px; line-height: 20px; padding-right: 4px;'>"
            +
            "<span style='vertical-align: middle; margin-right: 3px; line-height: 20px;'>{0}</span>{1}</div></td>" +
            "<td><div style='background: url({4}); width: 4px; height: 20px; float: right;'></div></td>" +
            "</tr></table>")
    SafeHtml alertEventButton(SafeHtml image, String text, String start, String stretch,
            String end, String contentStyleName);

    @Template("<div id=\"{2}\" style=\"text-align: center;\" data-status=\"{3}\">{0}{1}</div>")
    SafeHtml statusWithAlertTemplate(SafeHtml statusImage, SafeHtml alertImage, String id, String status);

    @Template("<div id=\"{1}\" style=\"text-align: center;\" data-status=\"{2}\">{0}</div>")
    SafeHtml statusTemplate(SafeHtml statusImage, String id, String status);

    @Template("<div title='{1}' id=\"{2}\" style=\"text-align: center;\">{0}</div>")
    SafeHtml imageWithHoverTextAndContainerId(SafeHtml statusImage, String hoverText, String id);

    @Template("<span><span style='position: absolute; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{1}</span>"
            + "<span style='position: relative; white-space: normal; height: 14px; line-height: 14px;'>{0}</span></span>")
    SafeHtml lockedStatusTemplate(SafeHtml lockImage, SafeHtml statusImage);

    @Template("<button type='button' tabindex='-1' style='float: right; height: 20px;'>"
            +
            "<span style='position: relative; left: 0px; top: -5px; width: 100%; font-family: arial; font-size: 10px;'>{0}</span></button>")
    SafeHtml actionButtonText(String text);

    @Template("<button type='button' tabindex='-1' style='background: url({0}) no-repeat; white-space: nowrap; height: 20px; width: 20px; line-height: 20px; float: right;'></button>")
    SafeHtml actionButtonImage(String image);

    @Template("<span style=\"top: -3px; left: 3px; position: relative;\">{0}</span>")
    SafeHtml textForCheckBoxHeader(String text);

    @Template("<span style=\"top: -2px; left: 3px; position: relative;\">{0}</span>")
    SafeHtml textForCheckBox(String text);

    @Template("{0} <span style='font-weight:bold; color: red;'>{1}</span>")
    SafeHtml blackRedBold(String black, String redBold);

    @Template("{0} <span style='font-weight:bold;'>{1}</span> {2}")
    SafeHtml middleBold(String start, String middle, String end);

    @Template("<span><span style='position: relative; margin-left: 20px; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='color:{2} ;position: relative; margin-left: 3px; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextSetupNetworkUsage(SafeHtml image, String text, String color);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; margin-left: 3px; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextSetupNetwork(SafeHtml image, String text);

    @Template("<span><span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; margin-left: 3px; margin-right: 3px; white-space: normal; height: 14px; line-height: 14px;'>{1}</span></span>")
    SafeHtml imageTextSetupNetwork(SafeHtml image, SafeHtml text);

    @Template("<div style='font-weight:bold; background-color:{1}; width:100%; padding:3px;'>{0}</div> ")
    SafeHtml titleSetupNetworkTooltip(String title, String backgroundColor);

    @Template("<i>{0}<br />{1}</i>")
    SafeHtml italicTwoLines(String firstLine, String secondLine);

    @Template("<div style='background: url({0}) no-repeat; height: {1}px; width: {2}px;'></div>")
    SafeHtml image(String url, int height, int width);

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

    @Template("<div style='max-width: 250px; display:inline-block;'>{0}</div>")
    SafeHtml maxWidthNteworkItemPopup(String text);

    @Template("<span style='position: relative; margin-right: 3px; white-space: nowrap; height: 14px; line-height: 14px;'>{0}</span>"
            + "<span style='position: relative; display: inline-block; vertical-align: top; height: 14px; line-height: 14px;'>{1}</span>")
    SafeHtml textImageLabels(String text, SafeHtml image);

    @Template("<div id=\"{0}\" style='text-align: center;'>{1}</div>")
    SafeHtml hostAdditionalStatusIcon(String id, SafeHtml statusImage);

    @Template("<p>Maximum bandwidth of both outgoing and incoming migrations per Host.</p>"
            + "<p><b>Auto</b> Bandwidth is copied from Migration Host Network QoS > Rate Limit if it exists. Otherwise "
            + "it is computed as minimum of link speeds of sending and receiving network interfaces if link speeds are "
            + "available. Otherwise it behaves as\"VDSM\" option.</p>"
            + "<p><b>Hypervisor default</b> Bandwidth is controlled by local VDSM setting on sending Host.</p>"
            + "<p><b>Custom</b> User defined.</p>")
    SafeHtml migrationBandwidthLimit();

    @Template("Defines under which conditions the virtual machines will be migrated.")
    SafeHtml clusterPopupResiliencePolicyInfo();

    @Template("{0} {1} {2}")
    SafeHtml hostOutOfSyncPreviewSentence(SafeHtml host, SafeHtml outOfsyncSentence, SafeHtml dc);
}
