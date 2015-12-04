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
    public static final CorrectString englishText = new CorrectString("SomeText_-");

    @DataPoint
    public static final CorrectString slovakText = new CorrectString("ňejakýReťazeč_-");

    @DataPoint
    public static final CorrectString hungarianText = new CorrectString("körülröföghetetlenség_-");

    @DataPoint
    public static final CorrectString hebrewText = new CorrectString("שלוםעולם_-");

    @DataPoint
    public static final CorrectString chineseText = new CorrectString("你好世界_-");

    @DataPoint
    public static final CorrectString dotSign = new CorrectString(".");

    @DataPoint
    public static final IncorrectString atSign = new IncorrectString("@");

    @DataPoint
    public static final IncorrectString spaceSign = new IncorrectString(" ");

    @DataPoint
    public static final IncorrectString slashSign = new IncorrectString("\\");

    @DataPoint
    public static final IncorrectString apostropheSign = new IncorrectString("'");

    @Theory
    public void allCharsetCanPass(CorrectString correct) {
        assertThat("Check can not recognize all chars in a valid string '" + correct.text + "'",
                new Regex(REGEXP).isMatch(correct.text),
                is(true));
    }

    @Theory
    public void anyCharsetWithIncorrectPartCanNotPass(CorrectString correctPart, IncorrectString incorrectPart) {
        assertThat("Check can not recognize incorrect char in string incorrect string '" + correctPart.text
                + incorrectPart.text
                + "'", new Regex(REGEXP).isMatch(correctPart.text + incorrectPart.text), is(false));
        assertThat("Check can not recognize incorrect char in string incorrect string '" + incorrectPart.text
                + correctPart.text
                + "'", new Regex(REGEXP).isMatch(incorrectPart.text + correctPart.text), is(false));
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
