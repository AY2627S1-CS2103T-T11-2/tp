package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS =
            "Names should contain at most " + MAX_LENGTH + " characters. They may contain alphanumeric words "
                    + "or single-letter initials ending in a period, separated by single spaces, hyphens, or "
                    + "apostrophes, and may include D/O between words";

    /*
     * A separator must occur only between alphanumeric words. This prevents leading, trailing, and repeated
     * separators while allowing names such as {@code Jean-Luc} and {@code O'Connor}.
     */
    private static final String NAME_WORD = "(?:[\\p{Alnum}]+(?:[-'][\\p{Alnum}]+)*|[A-Za-z]\\.)";
    public static final String VALIDATION_REGEX = NAME_WORD + "(?: " + NAME_WORD + ")*"
            + "(?: D/O " + NAME_WORD + "(?: " + NAME_WORD + ")*)?";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
