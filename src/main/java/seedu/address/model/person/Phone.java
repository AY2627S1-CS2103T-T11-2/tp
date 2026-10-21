package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS = "Phone numbers may be empty; otherwise, they must contain at "
            + "least 3 digits and may use spaces, hyphens, and one leading '+' as separators.";
    public static final String VALIDATION_REGEX = "\\+?\\d(?:[ -]*\\d)*";
    private static final String CANONICAL_PHONE_REGEX = "\\+?\\d{3,}";
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
        assert canonicalPhone.isEmpty() || canonicalPhone.matches(CANONICAL_PHONE_REGEX)
                : "A valid phone number must have a canonical representation";
        value = canonicalPhone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        if (test.isEmpty()) {
            return true;
        }
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }
        String canonicalPhone = test.replace(" ", "").replace("-", "");
        return canonicalPhone.matches(CANONICAL_PHONE_REGEX);
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
