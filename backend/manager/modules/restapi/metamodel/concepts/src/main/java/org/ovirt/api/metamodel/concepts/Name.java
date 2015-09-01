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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.join;

/**
 * This class represents a name formed of multiple words. It is intended to simplify the use of different strategies for
 * representing names as strings, like using different separators or using camel case. The words that form the name are
 * stored separated, so there is no need to parse the name each time that the words are needed.
 */
public class Name implements Comparable<Name> {
    /**
     * The list of words of this name.
     */
    private ArrayList<String> words = new ArrayList<>(1);

    /**
     * Creates a new word using the given list of words.
     *
     * @param theWords the words that will be used to construct the name
     */
    public Name(String... theWords) {
        words.ensureCapacity(theWords.length);
        for (String theWord : theWords) {
            words.add(theWord.toLowerCase());
        }
    }

    /**
     * Creates a new word using the given list of words.
     *
     * @param theWords the words that will be used to construct the name
     */
    public Name(List<String> theWords) {
        words.ensureCapacity(theWords.size());
        words.addAll(theWords);
    }

    /**
     * Returns the list of worlds of this name. The returned list is a copy of the one used internally, so any changes
     * to it won't have any effect on this name, and changes in the name won't affect the returned list.
     */
    public List<String> getWords() {
        return new ArrayList<>(words);
    }

    /**
     * Replaces the list of words of this name. The list passed as parameter isn't modified or referenced, instead
     * its contents are copied, so any later changes to it won't have any effect on this name.
     *
     * @param newWords the list of words that will replace the words of this name
     */
    public void setWords(List<String> newWords) {
        words.clear();
        for (String newWord : newWords) {
            words.add(newWord.toLowerCase());
        }
    }

    /**
     * Adds a new word to the end of the list of words of this name.
     *
     * @param newWord the world that will be added
     */
    public void addWord(String newWord) {
        words.add(newWord.toLowerCase());
    }

    /**
     * Returns a sequential {@code Stream} with the list of words of the name as source.
     */
    public Stream<String> words() {
        return words.stream();
    }

    /**
     * Returns a string representation of this name, consisting on the list of words of the name separated by dashes.
     */
    @Override
    public String toString() {
        return join("-", words);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Name) {
            Name that = (Name) obj;
            return this.words.equals(that.words);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return words.hashCode();
    }

    @Override
    public int compareTo(Name that) {
        return this.toString().compareTo(that.toString());
    }
}
