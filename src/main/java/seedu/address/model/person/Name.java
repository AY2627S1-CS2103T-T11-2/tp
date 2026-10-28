package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names should contain alphanumeric words separated by single spaces, with hyphens or apostrophes "
                    + "within words. Initials or short abbreviations of up to three letters may end in a period "
                    + "when followed by another name, and names should not be blank";

    private static final String NAME_WORD = "[\\p{Alnum}]+(?:[-'][\\p{Alnum}]+)*";
    private static final String INITIAL_OR_ABBREVIATION = "[\\p{Alpha}]{1,3}\\.";

    /*
     * Hyphens and apostrophes must occur within an alphanumeric word. Periods are limited to initials and short
     * abbreviations that precede another name word. This prevents leading, trailing, and repeated separators while
     * allowing names such as {@code Jean-Luc}, {@code O'Connor}, {@code S. Jay}, and {@code Aisyah Bte. Ahmad}.
     */
    public static final String VALIDATION_REGEX =
            "(?:(?:" + NAME_WORD + "|" + INITIAL_OR_ABBREVIATION + ") )*" + NAME_WORD;

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
        return test.matches(VALIDATION_REGEX);
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
