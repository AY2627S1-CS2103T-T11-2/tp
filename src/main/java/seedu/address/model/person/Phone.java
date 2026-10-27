package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {
    public static final String MESSAGE_CONSTRAINTS = "Phone numbers may be empty; otherwise, they must contain "
            + "exactly 8 digits, with at most one space or hyphen between digit groups. "
            + "They are stored as xxxx xxxx. Country codes and '+' are not allowed.";
    public static final String VALIDATION_REGEX = "\\d(?:[ -]?\\d){7}";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        String canonicalPhone = phone.replace(" ", "").replace("-", "");
        value = canonicalPhone.isEmpty() ? "" : canonicalPhone.substring(0, 4) + " " + canonicalPhone.substring(4);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.isEmpty() || test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if this phone number is absent.
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
