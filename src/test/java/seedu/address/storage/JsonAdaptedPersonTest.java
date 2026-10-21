package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+65-";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_GROUP = " ";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_SUBJECT = "CS2103T!";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_GROUP = "T11";
    private static final String VALID_TELEGRAM_HANDLE = BENSON.getTelegramHandle().toString();
    private static final String VALID_SUBJECT = "CS2103T";
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void constructorAndToModelType_nonDefaultSubjectAndGroup_preservesValues() throws Exception {
        Person student = new PersonBuilder(BENSON)
                .withSubject(VALID_SUBJECT)
                .withGroup(VALID_GROUP)
                .build();

        JsonAdaptedPerson jsonStudent = new JsonAdaptedPerson(student);

        assertEquals(student, jsonStudent.toModelType());
    }

    @Test
    public void toModelType_legacyPersonWithoutSubjectAndGroup_usesDefaults() throws Exception {
        JsonAdaptedPerson legacyPerson = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, null, null, VALID_TAGS);
        Person expectedPerson = new PersonBuilder(BENSON)
                .withSubject("General")
                .withGroup("General")
                .build();

        assertEquals(expectedPerson, legacyPerson.toModelType());
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, INVALID_SUBJECT, VALID_GROUP, VALID_TAGS);

        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidGroup_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_SUBJECT, INVALID_GROUP, VALID_TAGS);

        assertThrows(IllegalValueException.class, Group.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                null, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, null, VALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(
                VALID_NAME, VALID_PHONE, null, VALID_TELEGRAM_HANDLE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTelegramHandle_clearsTelegramHandle() throws Exception {
        String telegramHandle = "@alice / ! 用户\nsecond line";
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, telegramHandle, VALID_TAGS);
        assertEquals(new PersonBuilder(BENSON).withTelegramHandle("").build(), person.toModelType());
    }

    @Test
    public void toModelType_legacyPhysicalAddress_clearsTelegramHandle() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, "123 Clementi Road", VALID_TAGS);
        Person expectedPerson = new PersonBuilder(BENSON).withTelegramHandle("").build();

        assertEquals(expectedPerson, person.toModelType());
    }

    @Test
    public void toModelType_clearedOptionalFields_returnsPerson() throws Exception {
        Person clearedPerson = new PersonBuilder(BENSON).withPhone("").withTelegramHandle("").withTags().build();
        assertEquals(clearedPerson, new JsonAdaptedPerson(clearedPerson).toModelType());
    }

    @Test
    public void toModelType_nullTelegramHandle_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, TelegramHandle.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_TELEGRAM_HANDLE, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
