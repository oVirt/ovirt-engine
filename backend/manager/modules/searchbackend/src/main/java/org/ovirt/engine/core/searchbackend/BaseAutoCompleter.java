package org.ovirt.engine.core.searchbackend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.compat.DayOfWeek;

public class BaseAutoCompleter implements IAutoCompleter {
    protected final Set<String> mVerbs = new HashSet<String>();
    protected final Map<String, List<String>> mVerbCompletion =
            new HashMap<String, List<String>>();

    private static final List<String> daysOfWeek = new ArrayList<String>();

    static {
        for(DayOfWeek day: DayOfWeek.values()) {
            daysOfWeek.add(day.toString());
        }
    }

    public BaseAutoCompleter() {

    }

    public BaseAutoCompleter(String text) {
        acceptAll(text);
        buildCompletions();
    }

    public BaseAutoCompleter(String... text) {
        acceptAll(text);
        buildCompletions();
    }

    public BaseAutoCompleter(String[] text, String[] noAutocomplete) {
        this(text);
        acceptAll(noAutocomplete);
    }

    protected final void acceptAll(final String... tokens) {
        Collections.addAll(mVerbs, tokens);
    }

    protected final void buildCompletions() {
        final List<String> emptyKeyList = new ArrayList<String>();
        for (String title : mVerbs) {
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

    @Override
    public final String[] getCompletion(String wordPart) {
        String[] retval = new String[0];
        if (mVerbCompletion.containsKey(wordPart.toUpperCase())) {
            List<String> curList = mVerbCompletion.get(wordPart.toUpperCase());
            retval = curList.toArray(new String[curList.size()]);
        }
        return retval;
    }

    @Override
    public final boolean validate(String text) {
        return (text != null) ? mVerbs.contains(text.toUpperCase()) : false;
    }

    @Override
    public final boolean validateCompletion(String text) {
        return mVerbCompletion.containsKey(text);
    }

    @Override
    public String changeCaseDisplay(String text) {
        if (daysOfWeek.contains(text)) {
            return text;
        }
        return text.toLowerCase();
    }
}
