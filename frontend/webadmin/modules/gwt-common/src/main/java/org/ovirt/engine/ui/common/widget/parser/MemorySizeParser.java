package org.ovirt.engine.ui.common.widget.parser;

import java.text.ParseException;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;

public class MemorySizeParser implements Parser<Integer> {
    private static final CommonApplicationConstants constant = AssetProvider.getConstants();

    @Override
    public Integer parse(CharSequence text) throws ParseException {
        MatchResult match = RegExp.compile("^(\\d+)\\s*(\\w*)$").exec(text.toString().trim()); //$NON-NLS-1$
        if (match == null) {
            return 0;
        }
        String prefix = match.getGroup(1);
        String suffix = match.getGroup(2);
        Integer size = null;

        try {
            size = Integer.parseInt(prefix);
        } catch (NumberFormatException e) {
            return 0;
        }

        if (suffix.equalsIgnoreCase(constant.sizeTB()) || suffix.equalsIgnoreCase(constant.sizeT()) ||
                suffix.equalsIgnoreCase(constant.sizeTiB())) {
            return size * 1024 * 1024;
        } else if (suffix.equalsIgnoreCase(constant.sizeGB()) || suffix.equalsIgnoreCase(constant.sizeG()) ||
                suffix.equalsIgnoreCase(constant.sizeGiB())) {
            return size * 1024;
        } else if (suffix.equalsIgnoreCase(constant.sizeMB()) || suffix.equalsIgnoreCase(constant.sizeM()) ||
                suffix.equalsIgnoreCase(constant.sizeMiB())) {
            return size;
        } else if (StringHelper.isNullOrEmpty(suffix)) {
            return size;
        } else {
            return 0; // disallow garbled suffixes
        }
    }

}
