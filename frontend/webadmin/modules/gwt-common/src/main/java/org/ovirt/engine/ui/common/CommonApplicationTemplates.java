package org.ovirt.engine.ui.common;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationTemplates extends SafeHtmlTemplates {

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

    @Template("<div style='border-right: 1px solid #D7D7E1; height: 32px;'>{0}</div>")
    SafeHtml nonResizeableColumnHeader(SafeHtml text);

    @Template("<div class=\"{0}\" id=\"{1}\">{2}</div>")
    SafeHtml divWithStyle(String style, String id, SafeHtml text);

    @Template("<strong style='color: {1};'>{0}</strong>")
    SafeHtml strongTextWithColor(String text, String color);

    @Template("<font style='color: {1};'>{0}</font>")
    SafeHtml coloredText(String text, String color);

    @Template("<p><hr size=\"2\" width=\"100%\"></p>")
    SafeHtml horizontalLine();

    @Template("If ovirt-engine has the user's password connect automatically "
            + "to the first running VM if there is one on User Portal login.")
    SafeHtml connectAutomaticallyMessage();

    @Template("Specify public key for SSH authentication. Used to access guest serial console.")
    SafeHtml consolePublicKeyMessage();

    @Template("<p>{0}</p>")
    SafeHtml paragraph(String text);

    @Template("Total number of vCPUs.<br/>Stands for number of<ul><li>sockets</li><li>cores per socket</li><li>threads per core</li></ul>")
    SafeHtml numOfCpuCoresTooltip();

    @Template("<i>Replace '&lthostname&gt' with the hostname of the Xen hypervisor in the libvirt URI. <br />" +
            "Only SSH transport is allowed for Xen import and the connection has to be without password. <br /> <br />" +
            "To disable SSH host key verification add 'no_verify' parameter to the URI, " +
            "i.e.: xen+ssh://root@xen.example.com?no_verify=1</i>")
    SafeHtml xenUriInfo();

    @Template("This is not the entire kernel command line. Parameters listed below will be added to default kernel parameters.")
    SafeHtml kernelCmdlineInfoIcon();

    @Template("Enables or disables host's IOMMU, allowing PCI passthrough devices to be added to guests. Has no effect unless IOMMU is supported by the hardware and enabled in a firmware.")
    SafeHtml kernelCmdlineIommuInfoIcon();

    @Template("Enables or disables nested virtualization. Nested virtualization passes vmx/svm (hardware virtualization flag) to guest operating system. Requires vdsm 'nestedvt' hook.")
    SafeHtml kernelCmdlineKvmNestedInfoIcon();

    @Template("Enables or disables unsafe interrupt remapping. Should only be enabled if VMs with PCI passthrough devices cannot be started and 'dmesg' command indicates that it should be enabled (search for 'No interrupt remapping support.').")
    SafeHtml kernelCmdlineUnsafeInterruptsInfoIcon();

    @Template("Enables or disables reallocation of PCI bridge resources. Some firmware does not correctly handle SR-IOV devices, disallowing VF creation. Dynamic reallocation attempts to solve this problem. Does not help on unsupported hardware.")
    SafeHtml kernelCmdlinePciReallocInfoIcon();
}
