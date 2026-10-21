package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an optional Telegram handle.
 * Guarantees: immutable; is valid as declared in {@link #isValidTelegramHandle(String)}
 */
public class TelegramHandle {

    public static final String MESSAGE_CONSTRAINTS = "Telegram handles must be empty or begin with '@' followed by "
            + "5 to 32 letters, digits, or underscores; the first character after '@' cannot be an underscore.";

    public static final String VALIDATION_REGEX = "|@[A-Za-z0-9][A-Za-z0-9_]{4,31}";

    public final String value;

    /**
     * Constructs an {@code TelegramHandle}.
     *
     * @param telegramHandle A valid Telegram handle.
     */
    public TelegramHandle(String telegramHandle) {
        requireNonNull(telegramHandle);
        checkArgument(isValidTelegramHandle(telegramHandle), MESSAGE_CONSTRAINTS);
        value = telegramHandle;
    }

    /**
     * Returns true if the given string is empty or is a valid Telegram username.
     */
    public static boolean isValidTelegramHandle(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if this Telegram handle is absent.
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
        if (!(other instanceof TelegramHandle otherTelegramHandle)) {
            return false;
        }

        return value.equals(otherTelegramHandle.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
