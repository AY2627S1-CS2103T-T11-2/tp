package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMPTY_TELEGRAM_DESC;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.GROUP_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_HANDLE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TELEGRAM_HANDLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GROUP_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_GROUP_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_HANDLE_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.IndexPersonTarget;
import seedu.address.logic.commands.IndexRange;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_repeatedPhoneSeparators_failure() {
        for (String phone : List.of("1--- --2 3", "9123--4567", "9123  4567", "9123 -4567")) {
            assertParseFailure(parser, "1 p/" + phone, Phone.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_hyphenatedFinalEmailLabel_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withEmail("alice@x.a-b").build();
        assertParseSuccess(parser, "1 e/alice@x.a-b", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_repeatedEmptyTags_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagRemovalCount(2).build();
        assertParseSuccess(parser, "1 t/ t/", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 t/   t/ \t t/", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withTagRemovalCount(3).build()));
        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON)));
        assertParseSuccess(parser, "1-2 t/ t/", new EditCommand(target, descriptor));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        for (String input : List.of("1 g/T11 x/typo", "1 x/typo g/T11", "1 g/T11 typo/value",
                "1 g/T11 X/typo", "1 g/T11 x/", "1 g/T11\tx/typo", "1-2 g/T11 x/typo")) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_groupContainingSlashes_success() {
        for (String group : List.of("T11/T12", "T11 x / typo", "T11 https://example.com")) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withGroup(group).build();
            assertParseSuccess(parser, "1 g/" + group, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_caseVariantTags_storesOneTag() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withTags("old").build());
        parser.parse("1 t/Student t/student t/STUDENT").execute(model);

        var tags = model.getFilteredPersonList().getFirst().getTags();
        assertEquals(1, tags.size());
        assertEquals("Student", tags.iterator().next().tagName);
    }

    @Test
    public void parse_emptyTag_removesOnlyLastTag() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withTags("Student", "Online").build());
        parser.parse("1 t/").execute(model);
        assertEquals(List.of("Student"),
                model.getFilteredPersonList().getFirst().getTags().stream().map(tag -> tag.tagName).toList());
    }

    @Test
    public void parse_repeatedEmptyTags_clearsExistingTags() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withTags("Student", "Online").build());
        parser.parse("1 t/ t/").execute(model);
        assertEquals(0, model.getFilteredPersonList().getFirst().getTags().size());
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1 h/alice_tan", TelegramHandle.MESSAGE_CONSTRAINTS); // missing leading @
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser,
                "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_TELEGRAM_HANDLE_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_legacyAddressPrefix_failure() {
        assertParseFailure(parser, "1 a/Clementi",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 a/",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + TELEGRAM_HANDLE_DESC_AMY
                + SUBJECT_DESC_AMY + GROUP_DESC_BOB
                + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withTelegramHandle(VALID_TELEGRAM_HANDLE_AMY)
                .withSubject(VALID_SUBJECT_AMY).withGroup(VALID_GROUP_BOB)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // Telegram handle
        userInput = targetIndex.getOneBased() + TELEGRAM_HANDLE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withTelegramHandle(VALID_TELEGRAM_HANDLE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // subject
        userInput = targetIndex.getOneBased() + SUBJECT_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withSubject(VALID_SUBJECT_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // group
        userInput = targetIndex.getOneBased() + GROUP_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withGroup(VALID_GROUP_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + TELEGRAM_HANDLE_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + TELEGRAM_HANDLE_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + TELEGRAM_HANDLE_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_TELEGRAM_HANDLE));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + EMPTY_TELEGRAM_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + EMPTY_TELEGRAM_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_TELEGRAM_HANDLE));
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTagRemovalCount(1).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleIndexesAllowedFields_success() {
        String userInput = "1-3"
                + SUBJECT_DESC_AMY
                + GROUP_DESC_AMY
                + TAG_DESC_FRIEND
                + TAG_DESC_FRIEND
                + TAG_DESC_HUSBAND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withSubject(VALID_SUBJECT_AMY)
                .withGroup(VALID_GROUP_AMY)
                .withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_THIRD_PERSON)));
        EditCommand expectedCommand = new EditCommand(target, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleIndexesDisallowedFields_failure() {
        List<String> disallowedFields = List.of(
                NAME_DESC_AMY,
                PHONE_DESC_AMY,
                EMAIL_DESC_AMY,
                TELEGRAM_HANDLE_DESC_AMY,
                " " + PREFIX_PHONE,
                EMPTY_TELEGRAM_DESC);

        for (String field : disallowedFields) {
            assertParseFailure(
                    parser,
                    "1-2" + field,
                    EditCommand.MESSAGE_INVALID_BULK_EDIT);
        }
    }

    @Test
    public void parse_multipleIndexesResetTags_success() {
        EditPersonDescriptor descriptor =
                new EditPersonDescriptorBuilder().withTagRemovalCount(1).build();

        IndexPersonTarget target = new IndexPersonTarget(List.of(
                new IndexRange(INDEX_FIRST_PERSON, INDEX_THIRD_PERSON)));

        assertParseSuccess(
                parser,
                "1-3" + TAG_EMPTY,
                new EditCommand(target, descriptor));
    }

    @Test
    public void parse_multipleGroupsOrSubjects_failure() {
        assertParseFailure(
                parser,
                "1-2" + GROUP_DESC_AMY + GROUP_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GROUP));

        assertParseFailure(
                parser,
                "1-2" + SUBJECT_DESC_AMY + SUBJECT_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
    }

    @Test
    public void parse_clearPhone_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("").build();
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        assertParseSuccess(parser, "1 p/", expectedCommand);
        assertParseSuccess(parser, "1 p/ \t ", expectedCommand);
    }

    @Test
    public void parse_clearTelegramHandle_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTelegramHandle("").build();
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        assertParseSuccess(parser, "1 h/", expectedCommand);
        assertParseSuccess(parser, "1 h/ \t ", expectedCommand);
    }

    @Test
    public void parse_clearAllOptionalFields_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone("").withTelegramHandle("").withTagRemovalCount(1).build();
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        assertParseSuccess(parser, "1 p/ h/ t/", expectedCommand);
        assertParseSuccess(parser, "1 t/ \t h/ \t p/ \t ", expectedCommand);
    }

    @Test
    public void parse_invalidTelegramHandle_failure() {
        String telegramHandle = "@alice_123 / ! 用户\nsecond line";
        assertParseFailure(parser, "1 h/" + telegramHandle, TelegramHandle.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_clearCompulsoryFields_failure() {
        assertParseFailure(parser, "1 n/", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 e/", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 s/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 g/", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 n/ \t ", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 e/ \t ", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 s/ \t ", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 g/ \t ", Group.MESSAGE_CONSTRAINTS);
    }
}
