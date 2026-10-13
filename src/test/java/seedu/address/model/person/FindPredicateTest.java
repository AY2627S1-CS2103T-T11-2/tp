package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FindPredicateTest {

    @Test
    public void constructor_noCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty()));
    }

    @Test
    public void test_allCriteriaMatch_returnsTrue() {
        Person person = new PersonBuilder().withName("John Doe")
                .withSubject("CS3434").withGroup("T11").build();
        Predicate<Person> namePredicate = new NameContainsSearchTermsPredicate(List.of("John"), List.of());

        FindPredicate predicate = new FindPredicate(Optional.of(namePredicate),
                Optional.of(new Subject("cs3434")), Optional.of(new Group("t11")));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_anyCriterionDoesNotMatch_returnsFalse() {
        Person person = new PersonBuilder().withName("John Doe")
                .withSubject("CS3434").withGroup("T11").build();

        assertFalse(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11"))).test(person));
        assertFalse(new FindPredicate(Optional.empty(),
                Optional.of(new Subject("CS2103T")), Optional.of(new Group("T11"))).test(person));
        assertFalse(new FindPredicate(Optional.empty(),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T12"))).test(person));
    }

    @Test
    public void equals() {
        FindPredicate predicate = new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")));

        assertEquals(predicate, predicate);
        assertEquals(predicate, new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("not a predicate"));
        assertFalse(predicate.equals(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")))));
    }
}
