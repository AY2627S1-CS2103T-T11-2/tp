package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests whether a {@code Person} matches optional name, Telegram handle, subject, group, and tag criteria.
 * A person must match every type of criterion that is present. Multiple tags are matched using the supplied
 * {@link TagMatchMode}.
 */
public class FindPredicate implements Predicate<Person> {
    /** Defines how multiple tags in a find criterion are combined. */
    public enum TagMatchMode {
        ANY,
        ALL
    }

    private final Optional<Predicate<Person>> namePredicate;
    private final Optional<TelegramHandle> telegramHandle;
    private final Optional<Subject> subject;
    private final Optional<Group> group;
    private final Set<Tag> tags;
    private final TagMatchMode tagMatchMode;

    /**
     * Creates a predicate using at least one name, subject, group, or tag criterion.
     * This overload preserves callers that do not use Telegram handle filtering.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<Subject> subject,
            Optional<Group> group, Set<Tag> tags) {
        this(namePredicate, Optional.empty(), subject, group, tags, TagMatchMode.ANY);
    }

    /**
     * Creates a predicate using at least one criterion and the specified tag matching mode.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<Subject> subject,
            Optional<Group> group, Set<Tag> tags, TagMatchMode tagMatchMode) {
        this(namePredicate, Optional.empty(), subject, group, tags, tagMatchMode);
    }

    /**
     * Creates a predicate that can include a Telegram handle criterion.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<TelegramHandle> telegramHandle,
            Optional<Subject> subject, Optional<Group> group, Set<Tag> tags) {
        this(namePredicate, telegramHandle, subject, group, tags, TagMatchMode.ANY);
    }

    /**
     * Creates a predicate using at least one criterion and the specified tag matching mode.
     */
    public FindPredicate(Optional<Predicate<Person>> namePredicate, Optional<TelegramHandle> telegramHandle,
            Optional<Subject> subject, Optional<Group> group, Set<Tag> tags, TagMatchMode tagMatchMode) {
        requireNonNull(namePredicate);
        requireNonNull(telegramHandle);
        requireNonNull(subject);
        requireNonNull(group);
        requireNonNull(tags);
        requireNonNull(tagMatchMode);
        if (namePredicate.isEmpty() && telegramHandle.isEmpty() && subject.isEmpty()
                && group.isEmpty() && tags.isEmpty()) {
            throw new IllegalArgumentException("At least one criterion must be provided");
        }
        this.namePredicate = namePredicate;
        this.telegramHandle = telegramHandle;
        this.subject = subject;
        this.group = group;
        this.tags = Set.copyOf(tags);
        this.tagMatchMode = tagMatchMode;
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        boolean matchesName = namePredicate.map(predicate -> predicate.test(person)).orElse(true);
        boolean matchesTelegramHandle = telegramHandle
                .map(value -> value.value.equalsIgnoreCase(person.getTelegramHandle().value))
                .orElse(true);
        boolean matchesSubject = subject
                .map(value -> value.subject.equalsIgnoreCase(person.getSubject().subject))
                .orElse(true);
        boolean matchesGroup = group
                .map(value -> value.group.equalsIgnoreCase(person.getGroup().group))
                .orElse(true);
        boolean matchesTags = tags.isEmpty() || switch (tagMatchMode) {
            case ANY -> tags.stream().anyMatch(tag -> personHasTag(person, tag));
            case ALL -> tags.stream().allMatch(tag -> personHasTag(person, tag));
        };
        return matchesName && matchesSubject && matchesGroup && matchesTags && matchesTelegramHandle;
    }

    private boolean personHasTag(Person person, Tag tag) {
        return person.getTags().stream()
                .anyMatch(personTag -> tag.tagName.equalsIgnoreCase(personTag.tagName));
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
                && telegramHandle.equals(otherPredicate.telegramHandle)
                && subject.equals(otherPredicate.subject)
                && group.equals(otherPredicate.group)
                && tags.equals(otherPredicate.tags)
                && tagMatchMode == otherPredicate.tagMatchMode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("namePredicate", namePredicate)
                .add("telegramHandle", telegramHandle)
                .add("subject", subject)
                .add("group", group)
                .add("tags", tags)
                .add("tagMatchMode", tagMatchMode)
                .toString();
    }
}
