package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person}'s name matches any keyword or contains any phrase.
 */
public class NameContainsSearchTermsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<String> phrases;

    /**
     * Creates a predicate with the given keywords and phrases.
     */
    public NameContainsSearchTermsPredicate(List<String> keywords, List<String> phrases) {
        this.keywords = List.copyOf(keywords);
        this.phrases = phrases.stream().map(NameContainsSearchTermsPredicate::normalize).toList();
    }

    @Override
    public boolean test(Person person) {
        String name = person.getName().fullName;
        if (keywords.stream().anyMatch(keyword -> StringUtil.containsWordIgnoreCase(name, keyword))) {
            return true;
        }

        String normalizedName = normalize(name);
        return phrases.stream().anyMatch(normalizedName::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NameContainsSearchTermsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords) && phrases.equals(otherPredicate.phrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords, phrases);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("keywords", keywords)
                .add("phrases", phrases)
                .toString();
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
