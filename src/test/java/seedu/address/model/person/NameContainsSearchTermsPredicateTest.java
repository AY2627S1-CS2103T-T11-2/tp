package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsSearchTermsPredicateTest {

    @Test
    public void test_matchesKeywords_returnsTrue() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Alice", "Bob"), List.of());

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("Carol Bob").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("aLIce Carol").build()));
    }

    @Test
    public void test_keywordMatchesHyphenatedAndApostrophizedNameComponents_returnsTrue() {
        Person person = new PersonBuilder().withName("Jean-Luc O'Connor").build();

        assertTrue(new NameContainsSearchTermsPredicate(List.of("Jean"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of("Luc"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of("Connor"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of("Jean-Luc"), List.of()).test(person));
    }

    @Test
    public void test_matchesPhrases_returnsTrue() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Tan"), List.of("Alice Chen"));

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Chen").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("Bob Tan").build()));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("Alice   Chen")).test(
                new PersonBuilder().withName("Alice Chen").build()));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("Binte D/O")).test(
                new PersonBuilder().withName("Abdul Binte D/O Rahmud").build()));
    }

    @Test
    public void test_keywordRequiresComponentWhilePhraseMatchesSubstring() {
        Person person = new PersonBuilder().withName("Hans Jean-Luc").build();

        assertFalse(new NameContainsSearchTermsPredicate(List.of("Han"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("Han")).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("aNS jeAn-l")).test(person));
        assertFalse(new NameContainsSearchTermsPredicate(List.of(), List.of("Hans Jean L")).test(person));
    }

    @Test
    public void test_mixedKeywordAndPhraseUsesOrMatching() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Bob"), List.of("lex Ye"));

        assertTrue(predicate.test(new PersonBuilder().withName("Alex Yeoh").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("Bob Tan").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Chen").build()));
    }

    @Test
    public void test_nameWithInitial_returnsTrue() {
        Person person = new PersonBuilder().withName("S. Jay").build();

        assertTrue(new NameContainsSearchTermsPredicate(List.of("S"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of("S."), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of("Jay"), List.of()).test(person));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("S. Jay")).test(person));
    }

    @Test
    public void test_abbreviationWithoutPeriod_matchesAbbreviationComponent() {
        Person person = new PersonBuilder().withName("df. f").build();

        assertTrue(new NameContainsSearchTermsPredicate(List.of("df"), List.of()).test(person));
    }

    @Test
    public void test_matchesNeitherKeywordNorPhrase_returnsFalse() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Tan"), List.of("Alice Chen"));

        assertFalse(predicate.test(new PersonBuilder().withName("Alice Chan").build()));
        assertFalse(new NameContainsSearchTermsPredicate(List.of("Alice"), List.of()).test(
                new PersonBuilder().withName("Alicea Chen").build()));
        assertFalse(new NameContainsSearchTermsPredicate(List.of("ean"), List.of()).test(
                new PersonBuilder().withName("Jean-Luc O'Connor").build()));
    }

    @Test
    public void equals() {
        NameContainsSearchTermsPredicate firstPredicate =
                new NameContainsSearchTermsPredicate(List.of("first"), List.of("first phrase"));
        NameContainsSearchTermsPredicate firstPredicateCopy =
                new NameContainsSearchTermsPredicate(List.of("first"), List.of("first phrase"));
        NameContainsSearchTermsPredicate normalizedFirstPredicate =
                new NameContainsSearchTermsPredicate(List.of("first"), List.of("FIRST   PHRASE"));
        NameContainsSearchTermsPredicate secondPredicate =
                new NameContainsSearchTermsPredicate(List.of("second"), List.of("second phrase"));

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(firstPredicateCopy));
        assertEquals(firstPredicate, normalizedFirstPredicate);
        assertEquals(firstPredicate.hashCode(), firstPredicateCopy.hashCode());
        assertEquals(firstPredicate.hashCode(), normalizedFirstPredicate.hashCode());
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(1));
        assertFalse(firstPredicate.equals(null));
    }

    @Test
    public void toStringMethod() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("keyword"), List.of("phrase"));
        String expected = NameContainsSearchTermsPredicate.class.getCanonicalName()
                + "{keywords=[keyword], phrases=[phrase]}";

        assertEquals(expected, predicate.toString());
    }
}
