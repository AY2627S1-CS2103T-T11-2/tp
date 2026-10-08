package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsPhrasePredicateTest {

    @Test
    public void test_nameContainsPhrase_returnsTrue() {
        NameContainsPhrasePredicate predicate = new NameContainsPhrasePredicate("alice pauline");

        assertTrue(predicate.test(new PersonBuilder().withName("Alice Pauline").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("Sue Alice Pauline").build()));
        assertTrue(predicate.test(new PersonBuilder().withName("ALICE PAULINE").build()));
        assertTrue(new NameContainsPhrasePredicate("Alice   Pauline").test(
                new PersonBuilder().withName("Alice Pauline").build()));
    }

    @Test
    public void test_nameDoesNotContainPhrase_returnsFalse() {
        NameContainsPhrasePredicate predicate = new NameContainsPhrasePredicate("alice pauline");

        assertFalse(predicate.test(new PersonBuilder().withName("Alice Chan Pauline").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Kim Chi").build()));
    }

    @Test
    public void equals() {
        NameContainsPhrasePredicate firstPredicate = new NameContainsPhrasePredicate("first phrase");
        NameContainsPhrasePredicate firstPredicateCopy = new NameContainsPhrasePredicate("first phrase");
        NameContainsPhrasePredicate secondPredicate = new NameContainsPhrasePredicate("second phrase");

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(firstPredicateCopy));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(1));
        assertFalse(firstPredicate.equals(null));
        assertEquals(firstPredicate, firstPredicateCopy);
    }

    @Test
    public void toStringMethod() {
        NameContainsPhrasePredicate predicate = new NameContainsPhrasePredicate("phrase");
        String expected = NameContainsPhrasePredicate.class.getCanonicalName() + "{phrase=phrase}";

        assertEquals(expected, predicate.toString());
    }

}
