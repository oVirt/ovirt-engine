package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.core.compat.EnumCompat;
import org.ovirt.engine.core.compat.StringHelper;

public class BaseAutoCompleter implements IAutoCompleter {
    protected final Map<String, String> mVerbs = new HashMap<String, String>();
    protected final Map<String, List<String>> mVerbCompletion =
            new HashMap<String, List<String>>();

    public BaseAutoCompleter() {

    }

    public BaseAutoCompleter(String text) {
        mVerbs.put(text, text);
        buildCompletions();
    }

    public BaseAutoCompleter(String[] text) {
        for (String s : text) {
            mVerbs.put(s, s);
        }
        buildCompletions();
    }

    protected final void buildCompletions() {
        final List<String> emptyKeyList = new ArrayList<String>();
        for (String title : mVerbs.keySet()) {
            emptyKeyList.add(changeCaseDisplay(title));
            for (int idx = 1; idx <= title.length(); idx++) {
                String curKey = title.substring(0, idx);
                if (!mVerbCompletion.containsKey(curKey)) {
                    mVerbCompletion.put(curKey, new ArrayList<String>());
                }
                final List<String> curList = mVerbCompletion.get(curKey);
                curList.add(changeCaseDisplay(title));
            }
        }
        mVerbCompletion.put("", emptyKeyList);
        mVerbCompletion.put(" ", emptyKeyList);
    }

    public final String[] getCompletion(String wordPart) {
        String[] retval = new String[0];
        if (mVerbCompletion.containsKey(wordPart.toUpperCase())) {
            List<String> curList = mVerbCompletion.get(wordPart.toUpperCase());
            retval = new String[curList.size()];
            retval = curList.toArray(new String[] {});
        }
        return retval;
    }

    public final boolean validate(String text) {
        return (text != null) ? mVerbs.containsKey(text.toUpperCase()) : false;
    }

    public final boolean validateCompletion(String text) {
        return mVerbCompletion.containsKey(text);
    }

    public String changeCaseDisplay(String text) {
        for (String s : EnumCompat.GetNames(DayOfWeek.class)) {
            if (StringHelper.EqOp(text, s)) // days of the begin with capital
                                            // letter
            {
                return text;
            }
        }
        return text.toLowerCase();
    }
}
