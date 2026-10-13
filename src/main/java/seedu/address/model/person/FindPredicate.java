package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person} matches optional name, subject, and group criteria.
 * A person must match every criterion that is present.
 */
public class FindPredicate implements Predicate<Person> {
    private final Optional<Predicate<Person>> namePredicate;
    private final Optional<Subject> subject;
    private final Optional<Group> group;

    /**
     * Creates a predicate using at least one name, subject, or group criterion.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<Subject> subject,
            Optional<Group> group) {
        requireNonNull(namePredicate);
        requireNonNull(subject);
        requireNonNull(group);
        if (namePredicate.isEmpty() && subject.isEmpty() && group.isEmpty()) {
            throw new IllegalArgumentException("At least one criterion must be provided");
        }
        this.namePredicate = namePredicate;
        this.subject = subject;
        this.group = group;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        boolean matchesName = namePredicate.map(predicate -> predicate.test(person)).orElse(true);
        boolean matchesSubject = subject
                .map(value -> value.subject.equalsIgnoreCase(person.getSubject().subject))
                .orElse(true);
        boolean matchesGroup = group
                .map(value -> value.group.equalsIgnoreCase(person.getGroup().group))
                .orElse(true);
        return matchesName && matchesSubject && matchesGroup;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FindPredicate otherPredicate)) {
            return false;
        }
        return namePredicate.equals(otherPredicate.namePredicate)
                && subject.equals(otherPredicate.subject)
                && group.equals(otherPredicate.group);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("namePredicate", namePredicate)
                .add("subject", subject)
                .add("group", group)
                .toString();
    }
}
