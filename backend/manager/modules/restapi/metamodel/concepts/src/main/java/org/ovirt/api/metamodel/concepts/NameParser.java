/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.api.metamodel.concepts;

public class NameParser {
    /**
     * Separates the given text into words, using the given separator character, and creates a new name containing
     * those words. For example, to convert the text {@code my_favorite_fruit} into a name the method can be used
     * as follows:
     *
     * <pre>
     * Name name = parseUsingSeparator("my_favorite_fruit", '_');
     * </pre>
     *
     * @param text the text to process
     * @param separator the character that separates words
     * @return the name composed of the words extracted from the text
     */
    public static Name parseUsingSeparator(String text, char separator) {
        Name name = new Name();
        String[] words = text.split("[" + separator + "]");
        for (String word : words) {
            name.addWord(word);
        }
        return name;
    }

    /**
     * Separates the given text into words, using the case transitions as separators, and creates a new name containing
     * those words.
     *
     * @param text the text to process
     * @return
     */
    public static Name parseUsingCase(String text) {
        Name name = new Name();
        StringBuilder wordBuffer = new StringBuilder();
        Boolean previousCase = false;
        for (char current : text.toCharArray()) {
            boolean currentCase = Character.isUpperCase(current);
            if (currentCase != previousCase) {
                if (wordBuffer.length() > 0) {
                    name.addWord(wordBuffer.toString());
                    wordBuffer.setLength(0);
                }
            }
            wordBuffer.append(current);
        }
        if (wordBuffer.length() > 0) {
            name.addWord(wordBuffer.toString());
        }
        return name;
    }
}
