package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
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
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String SUBJECT_DESC_CS2103T = " s/CS2103T";
    private static final String GROUP_DESC_T11 = " g/T11";
    private static final String SUBJECT_DESC_GENERAL = " s/General";
    private static final String GROUP_DESC_GENERAL = " g/General";

    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_subjectAndGroupPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withSubject("CS2103T").withGroup("T11").build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + SUBJECT_DESC_GENERAL + GROUP_DESC_GENERAL + TAG_DESC_FRIEND,
                new AddCommand(expectedPerson));

        // multiple tags - all accepted
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_GENERAL + GROUP_DESC_GENERAL + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_FRIEND;
        String requiredFieldsString = NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple subjects
        assertParseFailure(parser, validExpectedPersonString + " s/CS2101",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));

        // multiple groups
        assertParseFailure(parser, validExpectedPersonString + " g/T12",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE,
                        PREFIX_SUBJECT, PREFIX_GROUP));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // empty Telegram handle repeated before another value
        assertParseFailure(parser, EMPTY_TELEGRAM_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // empty Telegram handle repeated after another value
        assertParseFailure(parser, validExpectedPersonString + EMPTY_TELEGRAM_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // repeated empty optional values
        assertParseFailure(parser, requiredFieldsString + " p/ p/",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, requiredFieldsString + EMPTY_TELEGRAM_DESC + EMPTY_TELEGRAM_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        Person expectedPerson = new PersonBuilder(AMY)
                .withPhone("")
                .withAddress("")
                .withSubject("CS2100")
                .withGroup("T35")
                .withTags()
                .build();
        assertParseSuccess(parser, NAME_DESC_AMY + EMAIL_DESC_AMY + " s/CS2100" + " g/T35",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_EMAIL_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11,
                expectedMessage);

        // missing subject prefix
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + GROUP_DESC_T11, expectedMessage);

        // missing group prefix
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Email.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + INVALID_TAG_DESC + VALID_TAG_FRIEND,
                Tag.MESSAGE_CONSTRAINTS);

        // invalid subject
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + " s/CS2103T!" + GROUP_DESC_T11, Subject.MESSAGE_CONSTRAINTS);

        // invalid group
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + SUBJECT_DESC_CS2103T + " g/", Group.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + SUBJECT_DESC_CS2103T
                + GROUP_DESC_T11 + EMPTY_TELEGRAM_DESC, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
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
    public void parse_emptyOptionalFields_success() {
        Person expectedPerson = new PersonBuilder(BOB)
                .withPhone("")
                .withAddress("")
                .withSubject("CS2103T")
                .withGroup("T11")
                .withTags()
                .build();
        // Explicitly blank optional prefixes are equivalent to omitting them.
        assertParseSuccess(parser, NAME_DESC_BOB + " p/" + EMAIL_DESC_BOB
                        + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + EMPTY_TELEGRAM_DESC,
                new AddCommand(expectedPerson));

        // The tokenizer trims whitespace-only optional values to empty values.
        assertParseSuccess(parser, NAME_DESC_BOB + " p/    " + EMAIL_DESC_BOB
                        + SUBJECT_DESC_CS2103T + GROUP_DESC_T11 + " a/    ",
                new AddCommand(expectedPerson));
    }
}
