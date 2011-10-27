package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;

public class BaseAutoCompleter implements IAutoCompleter {
    protected final java.util.HashMap<String, String> mVerbs = new java.util.HashMap<String, String>();
    protected final java.util.HashMap<String, java.util.ArrayList<String>> mVerbCompletion =
            new java.util.HashMap<String, java.util.ArrayList<String>>();

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

    protected void buildCompletions() {
        java.util.ArrayList<String> emptyKeyList = new java.util.ArrayList<String>();
        for (String title : mVerbs.keySet()) {
            emptyKeyList.add(changeCaseDisplay(title));
            for (int idx = 1; idx <= title.length(); idx++) {
                String curKey = title.substring(0, idx);
                if (!mVerbCompletion.containsKey(curKey)) {
                    java.util.ArrayList<String> newList = new java.util.ArrayList<String>();
                    mVerbCompletion.put(curKey, newList);
                }
                java.util.ArrayList<String> curList = mVerbCompletion.get(curKey);
                curList.add(changeCaseDisplay(title));
            }
        }
        mVerbCompletion.put("", emptyKeyList);
        mVerbCompletion.put(" ", emptyKeyList);
    }

    public String[] getCompletion(String wordPart) {
        String[] retval = new String[0];
        if (mVerbCompletion.containsKey(wordPart.toUpperCase())) {
            java.util.ArrayList<String> curList = mVerbCompletion.get(wordPart.toUpperCase());
            retval = new String[curList.size()];
            retval = curList.toArray(new String[] {});
        }
        return retval;
    }

    public boolean validate(String text) {
        return (text != null) ? mVerbs.containsKey(text.toUpperCase()) : false;
    }

    public boolean validateCompletion(String text) {
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
