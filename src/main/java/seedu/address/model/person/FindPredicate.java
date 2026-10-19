package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests whether a {@code Person} matches optional name, subject, group, and tag criteria.
 * A person must match every type of criterion that is present. When multiple tags are supplied,
 * matching any one of them satisfies the tag criterion.
 */
public class FindPredicate implements Predicate<Person> {
    private final Optional<Predicate<Person>> namePredicate;
    private final Optional<Subject> subject;
    private final Optional<Group> group;
    private final Set<Tag> tags;

    /**
     * Creates a predicate using at least one name, subject, group, or tag criterion.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<Subject> subject,
            Optional<Group> group, Set<Tag> tags) {
        requireNonNull(namePredicate);
        requireNonNull(subject);
        requireNonNull(group);
        requireNonNull(tags);
        if (namePredicate.isEmpty() && subject.isEmpty() && group.isEmpty() && tags.isEmpty()) {
            throw new IllegalArgumentException("At least one criterion must be provided");
        }
        this.namePredicate = namePredicate;
        this.subject = subject;
        this.group = group;
        this.tags = Set.copyOf(tags);
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
        boolean matchesTags = tags.isEmpty() || tags.stream()
                .anyMatch(tag -> person.getTags().stream()
                .anyMatch(personTag -> tag.tagName.equalsIgnoreCase(personTag.tagName)));
        return matchesName && matchesSubject && matchesGroup && matchesTags;
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
                && group.equals(otherPredicate.group)
                && tags.equals(otherPredicate.tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("namePredicate", namePredicate)
                .add("subject", subject)
                .add("group", group)
                .add("tags", tags)
                .toString();
    }
}
