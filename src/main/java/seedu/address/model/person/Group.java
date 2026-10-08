package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a person's group in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidGroup(String)}.
 */
public class Group {

    public static final String MESSAGE_CONSTRAINTS = "Groups can take any values, and should not be blank";

    /*
     * The first character of the group must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[^\\s].*";
    public final String group;

    /**
     * Constructs a {@code Group}.
     *
     * @param group A valid group.
     */
    public Group(String group) {
        requireNonNull(group);
        checkArgument(isValidGroup(group), MESSAGE_CONSTRAINTS);
        this.group = group;
    }

    public static boolean isValidGroup(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return group;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Group otherGroup)) {
            return false;
        }

        return group.equals(otherGroup.group);
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }
}
