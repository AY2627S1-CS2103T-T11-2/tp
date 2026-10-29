package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.Person;

/** Provides the contact-card values used for copying and layout decisions. */
final class PersonCardData {

    static final int TELEGRAM_COLUMN_INDEX = 3;
    static final int PHONE_COLUMN_INDEX = 4;

    private PersonCardData() {}

    /** Returns the value that should be copied for the specified contact detail. */
    static String getCopyValue(Person person, CopyableDetail detail) {
        requireNonNull(person);
        requireNonNull(detail);

        return switch (detail) {
            case EMAIL -> person.getEmail().value;
            case TELEGRAM_HANDLE -> person.getTelegramHandle().value;
            case PHONE -> person.getPhone().value;
        };
    }

    /** Returns the column index where the phone section should appear. */
    static int getPhoneColumnIndex(Person person) {
        requireNonNull(person);
        return person.getTelegramHandle().isEmpty() ? TELEGRAM_COLUMN_INDEX : PHONE_COLUMN_INDEX;
    }

    /** Identifies a contact detail that can be copied from the card. */
    enum CopyableDetail {
        EMAIL,
        TELEGRAM_HANDLE,
        PHONE
    }
}
