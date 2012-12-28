package org.ovirt.engine.core.common.utils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.compat.Regex;

@RunWith(Theories.class)
public class I18NRegexpTest {

    private static final String REGEXP = ValidationUtils.NO_SPECIAL_CHARACTERS_I18N;

    @DataPoint
    public static CorrectString englishText = new CorrectString("SomeText_-");

    @DataPoint
    public static CorrectString slovakText = new CorrectString("ňejakýReťazeč_-");

    @DataPoint
    public static CorrectString hungarianText = new CorrectString("körülröföghetetlenség_-");

    @DataPoint
    public static CorrectString hebrewText = new CorrectString("שלוםעולם_-");

    @DataPoint
    public static CorrectString chineseText = new CorrectString("你好世界_-");

    @DataPoint
    public static CorrectString dotSign = new CorrectString(".");

    @DataPoint
    public static IncorrectString atSign = new IncorrectString("@");

    @DataPoint
    public static IncorrectString spaceSign = new IncorrectString(" ");

    @DataPoint
    public static IncorrectString slashSign = new IncorrectString("\\");

    @DataPoint
    public static IncorrectString apostropheSign = new IncorrectString("'");

    @Theory
    public void allCharsetCanPass(CorrectString correct) {
        assertThat("Check can not recognize all chars in a valid string '" + correct.text + "'",
                new Regex(REGEXP).IsMatch(correct.text),
                is(true));
    }

    @Theory
    public void anyCharsetWithIncorrectPartCanNotPass(CorrectString correctPart, IncorrectString incorrectPart) {
        assertThat("Check can not recognize incorrect char in string incorrect string '" + correctPart.text
                + incorrectPart.text
                + "'", new Regex(REGEXP).IsMatch(correctPart.text + incorrectPart.text), is(false));
        assertThat("Check can not recognize incorrect char in string incorrect string '" + incorrectPart.text
                + correctPart.text
                + "'", new Regex(REGEXP).IsMatch(incorrectPart.text + correctPart.text), is(false));
    }

}

class CorrectString {
    String text;

    public CorrectString(String text) {
        this.text = text;
    }
}

class IncorrectString {
    String text;

    public IncorrectString(String text) {
        this.text = text;
    }
}
