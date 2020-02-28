package org.ovirt.engine.core.common.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.compat.Regex;

public class I18NRegexpTest {

    private static final String REGEXP = ValidationUtils.NO_SPECIAL_CHARACTERS_I18N;

    // Correct strings
    public static final String englishText = "SomeText_-";
    public static final String slovakText = "ňejakýReťazeč_-";
    public static final String hungarianText = "körülröföghetetlenség_-";
    public static final String hebrewText = "שלוםעולם_-";
    public static final String chineseText = "你好世界_-";
    public static final String dotSign = ".";
    public static List<String> correctStrings =
            Arrays.asList(englishText, slovakText, hungarianText, hebrewText, chineseText, dotSign);

    // Incorrect strings
    public static final String atSign = "@";
    public static final String spaceSign = " ";
    public static final String slashSign = "\\";
    public static final String apostropheSign = "'";
    public static final List<String> incorrectStrings =
            Arrays.asList(atSign, spaceSign, slashSign, apostropheSign);

    @ParameterizedTest
    @MethodSource
    public void allCharsetCanPass(String correct) {
        assertThat("Check can not recognize all chars in a valid string '" + correct + "'",
                new Regex(REGEXP).isMatch(correct),
                is(true));
    }

    public static Stream<String> allCharsetCanPass() {
        return correctStrings.stream();
    }

    @ParameterizedTest
    @MethodSource
    public void anyCharsetWithIncorrectPartCanNotPass(String str) {
        assertThat("Check can not recognize incorrect char in string incorrect string '" + str + "'",
                new Regex(REGEXP).isMatch(str), is(false));
    }

    /**
     * @return all the permutations of a correctString+incorrectString and incorrectString+correctString
     */
    public static Stream<String> anyCharsetWithIncorrectPartCanNotPass() {
        return correctStrings.stream().flatMap(c -> incorrectStrings.stream().flatMap(i -> Stream.of(c + i, i + c)));
    }
}
