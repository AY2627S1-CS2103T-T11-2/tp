package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardDataTest {

    @Test
    void getCopyValue_validPerson_returnsContactValues() {
        Person person = new PersonBuilder()
                .withEmail("person@example.com")
                .withTelegramHandle("@person_handle")
                .withPhone("91234567")
                .build();

        assertEquals("person@example.com", PersonCardData.getCopyValue(
                person, PersonCardData.CopyableDetail.EMAIL));
        assertEquals("@person_handle", PersonCardData.getCopyValue(
                person, PersonCardData.CopyableDetail.TELEGRAM_HANDLE));
        assertEquals("91234567", PersonCardData.getCopyValue(
                person, PersonCardData.CopyableDetail.PHONE));
    }

    @Test
    void getPhoneColumnIndex_telegramPresent_returnsPhoneColumn() {
        Person person = new PersonBuilder().withTelegramHandle("@person_handle").build();

        assertEquals(PersonCardData.PHONE_COLUMN_INDEX, PersonCardData.getPhoneColumnIndex(person));
    }

    @Test
    void getPhoneColumnIndex_telegramAbsent_returnsTelegramColumn() {
        Person person = new PersonBuilder().withTelegramHandle("").build();

        assertEquals(PersonCardData.TELEGRAM_COLUMN_INDEX, PersonCardData.getPhoneColumnIndex(person));
    }
}
