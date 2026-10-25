package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ALL;
import static seedu.address.model.person.FindPredicate.TagMatchMode.ANY;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class FindPredicateTest {

    @Test
    public void constructor_noCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Set.of()));
    }

    @Test
    public void test_allCriteriaMatch_returnsTrue() {
        Person person = new PersonBuilder().withName("John Doe")
                .withSubject("CS3434").withGroup("T11").withTags("needsHelp").build();
        Predicate<Person> namePredicate = new NameContainsSearchTermsPredicate(List.of("John"), List.of());

        FindPredicate predicate = new FindPredicate(Optional.of(namePredicate),
                Optional.of(new Subject("cs3434")), Optional.of(new Group("t11")),
                Set.of(new Tag("needsHelp")));

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_anyCriterionDoesNotMatch_returnsFalse() {
        Person person = new PersonBuilder().withName("John Doe")
                .withSubject("CS3434").withGroup("T11").build();

        assertFalse(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")), Set.of()).test(person));
        assertFalse(new FindPredicate(Optional.empty(),
                Optional.of(new Subject("CS2103T")), Optional.of(new Group("T11")), Set.of()).test(person));
        assertFalse(new FindPredicate(Optional.empty(),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T12")), Set.of()).test(person));
        assertFalse(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")),
                Set.of(new Tag("lowAttendance"))).test(person));
    }

    @Test
    public void test_anyTagMatches_returnsTrue() {
        Person person = new PersonBuilder().withTags("highParticipation").build();
        FindPredicate predicate = new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("highParticipation"), new Tag("lowParticipation")), ANY);

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_allTagsMatch_returnsTrue() {
        Person person = new PersonBuilder().withTags("highParticipation", "needsHelp").build();
        FindPredicate predicate = new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("highParticipation"), new Tag("needsHelp")), ALL);

        assertTrue(predicate.test(person));
    }

    @Test
    public void test_notAllTagsMatch_returnsFalse() {
        Person person = new PersonBuilder().withTags("highParticipation").build();
        FindPredicate predicate = new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("highParticipation"), new Tag("needsHelp")), ALL);

        assertFalse(predicate.test(person));
    }

    @Test
    public void test_tagWithDifferentCase_returnsTrue() {
        Person person = new PersonBuilder().withTags("highParticipation").build();
        FindPredicate predicate = new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of(new Tag("HIGHPARTICIPATION")));

        assertTrue(predicate.test(person));
    }

    @Test
    public void constructor_mutableTags_defensiveCopyCreated() {
        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("needsHelp"));
        FindPredicate predicate = new FindPredicate(Optional.empty(), Optional.empty(), Optional.empty(), tags);
        tags.clear();

        assertTrue(predicate.test(new PersonBuilder().withTags("needsHelp").build()));
    }

    @Test
    public void equals() {
        FindPredicate predicate = new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")), Set.of(new Tag("needsHelp")));

        assertEquals(predicate, predicate);
        assertEquals(predicate, new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")), Set.of(new Tag("needsHelp"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("not a predicate"));
        assertFalse(predicate.equals(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("Jane"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")), Set.of(new Tag("needsHelp")))));
        assertFalse(predicate.equals(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")),
                Set.of(new Tag("consultation")))));
        assertFalse(predicate.equals(new FindPredicate(
                Optional.of(new NameContainsSearchTermsPredicate(List.of("John"), List.of())),
                Optional.of(new Subject("CS3434")), Optional.of(new Group("T11")),
                Set.of(new Tag("needsHelp")), ALL)));
    }
}
