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
            "Names should contain at most " + MAX_LENGTH + " characters, using alphanumeric words separated by "
                    + "single spaces, with hyphens or apostrophes "
                    + "within words. Initials or short abbreviations of up to three letters may end in a period "
                    + "when followed by another name, and D/O or S/O may appear between name parts";

    private static final String NAME_WORD = "[\\p{Alnum}]+(?:[-'][\\p{Alnum}]+)*";
    private static final String INITIAL_OR_ABBREVIATION = "[\\p{Alpha}]{1,3}\\.";
    private static final String NAME_PART = "(?:(?:" + NAME_WORD + "|" + INITIAL_OR_ABBREVIATION + ") )*"
            + NAME_WORD;

    /*
     * Hyphens and apostrophes must occur within an alphanumeric word. Periods are limited to initials and short
     * abbreviations that precede another name word. This prevents leading, trailing, and repeated separators while
     * allowing names such as {@code Jean-Luc}, {@code O'Connor}, {@code S. Jay}, and {@code Aisyah Bte. Ahmad}.
     * An uppercase {@code D/O} or {@code S/O} may appear once between name parts.
     */
    public static final String VALIDATION_REGEX = NAME_PART + "(?: [DS]/O " + NAME_PART + ")?";

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
