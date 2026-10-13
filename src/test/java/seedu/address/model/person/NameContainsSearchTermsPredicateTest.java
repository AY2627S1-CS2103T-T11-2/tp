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
    public void test_matchesPhrases_returnsTrue() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Tan"), List.of("Alice Chen"));

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Chen").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("Bob Tan").build()));
        assertTrue(new NameContainsSearchTermsPredicate(List.of(), List.of("Alice   Chen")).test(
                new PersonBuilder().withName("Alice Chen").build()));
    }

    @Test
    public void test_matchesNeitherKeywordNorPhrase_returnsFalse() {
        NameContainsSearchTermsPredicate predicate =
                new NameContainsSearchTermsPredicate(List.of("Tan"), List.of("Alice Chen"));

        assertFalse(predicate.test(new PersonBuilder().withName("Alice Chan").build()));
        assertFalse(new NameContainsSearchTermsPredicate(List.of("Alice"), List.of()).test(
                new PersonBuilder().withName("Alicea Chen").build()));
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
