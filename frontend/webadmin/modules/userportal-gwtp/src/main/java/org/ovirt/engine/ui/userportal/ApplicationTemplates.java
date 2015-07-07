package org.ovirt.engine.ui.userportal;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface ApplicationTemplates extends CommonApplicationTemplates {

    @Template("<span id='{0}' style='font-size: 14px; font-family: Arial,sans-serif; font-weight: bold;'>{1}</span>")
    SafeHtml vmNameCellItem(String id, String name);

    @Template(" ({0})")
    SafeHtml vmDescriptionCellItem(String description);


    @Template("<table style=\"font-size:11px; border-collapse: collapse;\">\n" +
            "          <tr style=\"background-color: #333333;\">\n" +
            "                <th style=\"text-align:left; padding: 3px;\" >{0}</th>\n" +
            "                <th style=\"padding: 3px;\">&nbsp;</th>\n" +
            "                <th style=\"text-align:right; padding: 3px;\" >{1}</th>\n" +
            "          </tr>\n" +
            "          <tr style=\"padding: 3px;\">\n" +
            "                <td style=\"padding: 3px;\">{2}</td>\n" +
            "                <td style=\"text-align:right; padding: 3px; padding-left: 18px; \" >{3}%</td>\n" +
            "                <td style=\"text-align:right; padding: 3px; padding-left: 18px; \" >{4}</td>\n" +
            "          </tr>\n" +
            "          <tr>\n" +
            "                <td style=\"padding: 3px;\">{5}</td>\n" +
            "                <td style=\"text-align:right; padding: 3px;\" >{6}%</td>\n" +
            "                <td style=\"text-align:right; padding: 3px;\" >{7}</td>\n" +
            "          </tr>\n" +
            "         <tr>\n" +
            "                <td style=\"padding: 3px;\">{8}</td>\n" +
            "                <td style=\"text-align:right; padding: 3px;\" >{9}%</td>\n" +
            "                <td style=\"text-align:right; padding: 3px;\" >{10}</td>\n" +
            "          </tr>\n" +
            "         <tr style=\"background-color: #333333; \">\n" +
            "                <td style=\"border-bottom:1px solid #ffffff; padding: 3px;\">{11}</td>\n" +
            "                <td style=\"text-align:right; border-bottom:1px solid #ffffff; padding: 3px;\" >{12}%</td>\n" +
            "                <td style=\"text-align:right; border-bottom:1px solid #ffffff; padding: 3px;\" >{13}</td>\n" +
            "          </tr>\n" +
            "        </table>\n")
    SafeHtml quotaForUserBarToolTip(String quotaLabel, String quota,
            String totalUsageLabel, int totalUsagePercentage, String totalUsage,
            String usedByYouLabel, int usedByYouPercentage, String usedByYou,
            String usedByOthersLabel, int usedByOthersPercentage, String usedByOthers,
            String freeLabel, int freePercentage, String free);

    @Template("<div>{0}</div>")
    SafeHtml userMessageOfTheDay(String title);
}
