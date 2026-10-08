package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a person's subject in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidSubject(String)}.
 */
public class Subject {

    public static final String MESSAGE_CONSTRAINTS =
            "Subjects should only contain alphanumeric characters and spaces, and should not be blank";
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";
    public final String subject;

    /**
     * Constructs a {@code Subject}.
     *
     * @param subject A valid subject.
     */
    public Subject(String subject) {
        requireNonNull(subject);
        checkArgument(isValidSubject(subject), MESSAGE_CONSTRAINTS);
        this.subject = subject;
    }

    public static boolean isValidSubject(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return subject;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Subject otherSubject)) {
            return false;
        }

        return subject.equals(otherSubject.subject);
    }

    @Override
    public int hashCode() {
        return subject.hashCode();
    }
}
