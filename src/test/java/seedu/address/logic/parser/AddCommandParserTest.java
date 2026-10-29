package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMPTY_TELEGRAM_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_HANDLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_HANDLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String SUBJECT_DESC_CS2103T = " s/CS2103T";
    private static final String GROUP_DESC_T11 = " g/T11";
    private static final String SUBJECT_DESC_GENERAL = " s/General";
    private static final String GROUP_DESC_GENERAL = " g/General";
    private static final String REQUIRED_BOB_FIELDS =
            NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11;

    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + TELEGRAM_HANDLE_DESC_BOB + SUBJECT_DESC_GENERAL + GROUP_DESC_GENERAL + TAG_DESC_FRIEND,
                new AddCommand(expectedPerson));

        Person expectedPersonMultipleTags = new PersonBuilder(BOB)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_HANDLE_DESC_BOB
                + SUBJECT_DESC_GENERAL + GROUP_DESC_GENERAL + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_requiredFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB)
                .withPhone("")
                .withTelegramHandle("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();

        assertParseSuccess(parser, REQUIRED_BOB_FIELDS, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameWithRelationshipQualifier_success() {
        Person expectedPerson = new PersonBuilder()
                .withName("Abdul Binte D/O Rahmud")
                .withPhone("")
                .withEmail("abdul@example.com")
                .withTelegramHandle("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();

        assertParseSuccess(parser,
                " n/Abdul Binte D/O Rahmud e/abdul@example.com s/CS2103T g/T11",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameWithSonOfQualifier_keepsSubjectSeparate() {
        Person expectedPerson = new PersonBuilder()
                .withName("Ravi S/O Kumar")
                .withPhone("")
                .withEmail("ravi@example.com")
                .withTelegramHandle("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();

        assertParseSuccess(parser, " n/Ravi S/O Kumar e/ravi@example.com s/CS2103T g/T11",
                new AddCommand(expectedPerson));
        assertParseSuccess(parser, " s/CS2103T g/T11 e/ravi@example.com n/Ravi S/O Kumar",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameWithInitials_success() {
        Person expectedPerson = new PersonBuilder(BOB)
                .withName("A. R. Rahman")
                .withPhone("")
                .withTelegramHandle("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();

        assertParseSuccess(parser, " n/A. R. Rahman" + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_relationshipQualifierOutsideName_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, " n/Abdul Rahmud e/abdul@example.com s/CS2103T g/T11 D/O", expectedMessage);
        assertParseFailure(parser, " n/Abdul D/ Rahmud e/abdul@example.com s/CS2103T g/T11", expectedMessage);
        assertParseFailure(parser, " n/Ravi Kumar e/ravi@example.com s/CS2103T g/T11 S/O", expectedMessage);
        assertParseFailure(parser, " n/Ravi S/Okay Kumar e/ravi@example.com s/CS2103T g/T11", expectedMessage);
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + " x/unknown", expectedMessage);
    }

    @Test
    public void parse_nameWithMalformedPeriods_failure() {
        assertParseFailure(parser, " n/John..Tan" + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyOptionalFields_success() {
        Person expectedPerson = new PersonBuilder(BOB)
                .withPhone("")
                .withTelegramHandle("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();

        assertParseSuccess(parser, NAME_DESC_BOB + " p/" + EMAIL_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + EMPTY_TELEGRAM_DESC, new AddCommand(expectedPerson));
        assertParseSuccess(parser, NAME_DESC_BOB + " p/    " + EMAIL_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + " h/    ", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validPerson = REQUIRED_BOB_FIELDS + PHONE_DESC_BOB + TELEGRAM_HANDLE_DESC_BOB + TAG_DESC_FRIEND;

        assertParseFailure(parser, NAME_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, PHONE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, EMAIL_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, TELEGRAM_HANDLE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM_HANDLE));
        assertParseFailure(parser, validPerson + " s/CS2101",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
        assertParseFailure(parser, validPerson + " g/T12",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));

        assertParseFailure(parser, validPerson + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + TELEGRAM_HANDLE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_TELEGRAM_HANDLE, PREFIX_SUBJECT, PREFIX_GROUP));

        assertParseFailure(parser, INVALID_NAME_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, INVALID_EMAIL_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, INVALID_PHONE_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, EMPTY_TELEGRAM_DESC + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM_HANDLE));
        assertParseFailure(parser, validPerson + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, validPerson + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, validPerson + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, validPerson + EMPTY_TELEGRAM_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM_HANDLE));
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + " p/ p/",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + EMPTY_TELEGRAM_DESC + EMPTY_TELEGRAM_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TELEGRAM_HANDLE));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, VALID_NAME_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + VALID_EMAIL_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + GROUP_DESC_T11, expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T, expectedMessage);
    }

    @Test
    public void parse_emptyRequiredValue_failure() {
        assertParseFailure(parser, " n/" + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + " e/" + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + " s/" + GROUP_DESC_T11,
                Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + " g/",
                Group.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + TELEGRAM_HANDLE_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + TELEGRAM_HANDLE_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + TELEGRAM_HANDLE_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + " h/alice_tan", TelegramHandle.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + TELEGRAM_HANDLE_DESC_BOB
                + INVALID_TAG_DESC + TAG_DESC_FRIEND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + " s/CS2103T!" + GROUP_DESC_T11,
                Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + " g/",
                Group.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T
                + GROUP_DESC_T11 + EMPTY_TELEGRAM_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + REQUIRED_BOB_FIELDS,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_legacyAddressPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + " a/Clementi", expectedMessage);
        assertParseFailure(parser, REQUIRED_BOB_FIELDS + " a/", expectedMessage);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        for (String input : List.of(REQUIRED_BOB_FIELDS + " x/typo",
                NAME_DESC_BOB + " x/typo" + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                REQUIRED_BOB_FIELDS + " typo/value",
                REQUIRED_BOB_FIELDS + " X/typo",
                REQUIRED_BOB_FIELDS + " x/",
                REQUIRED_BOB_FIELDS + "\tx/typo")) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_groupContainingSlashes_success() {
        for (String group : List.of("T11/T12", "T11 x / typo", "T11 https://example.com")) {
            Person expectedPerson = new PersonBuilder(BOB)
                    .withPhone("")
                    .withTelegramHandle("")
                    .withSubject("CS2103T")
                    .withGroup(group)
                    .withTags()
                    .build();
            assertParseSuccess(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + " g/" + group,
                    new AddCommand(expectedPerson));
        }
    }

    @Test
    public void parse_telegramHandlePresent_success() {
        Person expectedPerson = new PersonBuilder(BOB)
                .withPhone("")
                .withTelegramHandle("@alice_tan")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();
        assertParseSuccess(parser, REQUIRED_BOB_FIELDS + " h/@alice_tan", new AddCommand(expectedPerson));
    }
}
