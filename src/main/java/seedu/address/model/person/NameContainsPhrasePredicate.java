package seedu.address.model.person;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a {@code Person}'s name contains a phrase, ignoring case.
 */
public class NameContainsPhrasePredicate implements Predicate<Person> {
    private final String phrase;

    public NameContainsPhrasePredicate(String phrase) {
        this.phrase = phrase;
    }

    @Override
    public boolean test(Person person) {
        return normalize(person.getName().fullName).contains(normalize(phrase));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsPhrasePredicate otherNameContainsPhrasePredicate)) {
            return false;
        }

        return phrase.equals(otherNameContainsPhrasePredicate.phrase);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("phrase", phrase).toString();
    }

    /**
     * Normalizes whitespace and letter casing for phrase matching.
     */
    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
