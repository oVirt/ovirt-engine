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
    protected final Set<String> verbs = new HashSet<>();
    protected final Map<String, List<String>> verbCompletion = new HashMap<>();

    private static final List<String> daysOfWeek = new ArrayList<>();

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
        Collections.addAll(verbs, tokens);
    }

    protected final void buildCompletions() {
        final List<String> emptyKeyList = new ArrayList<>();
        for (String title : verbs) {
            emptyKeyList.add(changeCaseDisplay(title));
            for (int idx = 1; idx <= title.length(); idx++) {
                String curKey = title.substring(0, idx);
                if (!verbCompletion.containsKey(curKey)) {
                    verbCompletion.put(curKey, new ArrayList<String>());
                }
                final List<String> curList = verbCompletion.get(curKey);
                curList.add(changeCaseDisplay(title));
            }
        }
        verbCompletion.put("", emptyKeyList);
        verbCompletion.put(" ", emptyKeyList);
    }

    @Override
    public final String[] getCompletion(String wordPart) {
        String[] retval = new String[0];
        if (verbCompletion.containsKey(wordPart.toUpperCase())) {
            List<String> curList = verbCompletion.get(wordPart.toUpperCase());
            retval = curList.toArray(new String[curList.size()]);
        }
        return retval;
    }

    @Override
    public final boolean validate(String text) {
        return (text != null) ? verbs.contains(text.toUpperCase()) : false;
    }

    @Override
    public final boolean validateCompletion(String text) {
        return verbCompletion.containsKey(text);
    }

    @Override
    public String changeCaseDisplay(String text) {
        if (daysOfWeek.contains(text)) {
            return text;
        }
        return text.toLowerCase();
    }
}
