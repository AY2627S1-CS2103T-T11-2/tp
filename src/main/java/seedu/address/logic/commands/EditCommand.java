package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TELEGRAM_HANDLE;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits one or more students identified by indexes in the displayed student list. "
            + "Name, phone, email, and Telegram handle can only be edited when one student is selected.\n"
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX_OR_RANGE[,INDEX_OR_RANGE]... "
            + "Omitted fields stay unchanged; empty values are rejected. "
            + "Use t/TAG to add a tag, or t/OLD_TAG,NEW_TAG to rename one. "
            + "Tag operations run left to right and matching ignores case.\n"
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_TELEGRAM_HANDLE + "TELEGRAM_HANDLE] "
            + "[" + PREFIX_SUBJECT + "SUBJECT] "
            + "[" + PREFIX_GROUP + "GROUP] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1,3-5 "
            + PREFIX_SUBJECT + "CS3240 "
            + PREFIX_GROUP + "T11 "
            + PREFIX_TAG + "student";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited student: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_EMPTY_FIELD = "Edit values cannot be empty. Use delete to clear fields.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This student already exists in TutorTrack.";
    public static final String MESSAGE_EDIT_PERSONS_SUCCESS = "Edited %1$d students:\n%2$s";
    public static final String MESSAGE_INVALID_BULK_EDIT = "Only subject, group, and tags can be edited "
            + "for multiple students.";

    private final Index index;
    private final PersonTarget target;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.target = null;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    /**
     * Creates a command that edits multiple selected persons.
     *
     * @param target persons to edit
     * @param editPersonDescriptor details to edit
     */
    public EditCommand(PersonTarget target, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(target);
        requireNonNull(editPersonDescriptor);

        this.index = null;
        this.target = target;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        int personCountBeforeEdit = model.getAddressBook().getPersonList().size();

        CommandResult result = target == null ? executeSingleEdit(model) : executeBulkEdit(model);

        assert model.getAddressBook().getPersonList().size() == personCountBeforeEdit
                : "Editing a person must preserve the number of people";

        return result;
    }


    /**
     * Executes the existing single-person edit workflow.
     */
    private CommandResult executeSingleEdit(Model model)
            throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased()
                >= lastShownList.size()) {
            throw new CommandException(
                    Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson =
                createEditedPerson(
                        personToEdit,
                        editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);

        return new CommandResult(String.format(
                MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson)));
    }

    /**
     * Executes a bulk edit after validating every selected person.
     */
    private CommandResult executeBulkEdit(Model model)
            throws CommandException {
        List<Person> peopleToEdit = target.resolve(model);

        if (editPersonDescriptor.hasSinglePersonOnlyFields()) {
            throw new CommandException(MESSAGE_INVALID_BULK_EDIT);
        }

        List<Person> editedPeople = new ArrayList<>();
        for (Person person : peopleToEdit) {
            editedPeople.add(createEditedPerson(person, editPersonDescriptor));
        }

        String editedPeopleSummary = editedPeople.stream()
                .map(Messages::format)
                .collect(Collectors.joining("\n"));

        CommandResult result = new CommandResult(String.format(
                MESSAGE_EDIT_PERSONS_SUCCESS,
                editedPeople.size(),
                editedPeopleSummary));

        for (int i = 0; i < peopleToEdit.size(); i++) {
            model.setPerson(peopleToEdit.get(i), editedPeople.get(i));
        }

        return result;
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        requireNonNull(personToEdit);
        requireNonNull(editPersonDescriptor);

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        TelegramHandle updatedTelegramHandle = editPersonDescriptor.getTelegramHandle()
                .orElse(personToEdit.getTelegramHandle());
        Subject updatedSubject = editPersonDescriptor.getSubject().orElse(personToEdit.getSubject());
        Group updatedGroup = editPersonDescriptor.getGroup().orElse(personToEdit.getGroup());
        Set<Tag> updatedTags = new LinkedHashSet<>(personToEdit.getTags());
        for (TagEdit tagEdit : editPersonDescriptor.getTagEdits()) {
            updatedTags = tagEdit.applyTo(updatedTags);
        }

        return new Person(updatedName, updatedPhone, updatedEmail, updatedTelegramHandle, updatedTags,
                updatedSubject, updatedGroup);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return Objects.equals(index, otherEditCommand.index)
                && Objects.equals(target, otherEditCommand.target)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);

        if (target == null) {
            builder.add("index", index);
        } else {
            builder.add("target", target);
        }

        return builder.add("editPersonDescriptor", editPersonDescriptor).toString();
    }

    /**
     * Stores replacement field values and ordered tag additions or renames.
     * Null field values mean unchanged. Tag operations preserve all unrelated tags.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Subject subject;
        private Group group;
        private TelegramHandle telegramHandle;
        private List<TagEdit> tagEdits = List.of();

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * An immutable copy of the ordered tag edits is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setSubject(toCopy.subject);
            setGroup(toCopy.group);
            setTelegramHandle(toCopy.telegramHandle);
            setTagEdits(toCopy.tagEdits);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, subject, group, telegramHandle)
                    || !tagEdits.isEmpty();
        }

        /**
         * Returns whether this descriptor contains a field that can only be edited
         * when exactly one person is selected.
         */
        public boolean hasSinglePersonOnlyFields() {
            return name != null
                    || phone != null
                    || email != null
                    || telegramHandle != null;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setTelegramHandle(TelegramHandle telegramHandle) {
            this.telegramHandle = telegramHandle;
        }

        public Optional<TelegramHandle> getTelegramHandle() {
            return Optional.ofNullable(telegramHandle);
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
        }

        public Optional<Subject> getSubject() {
            return Optional.ofNullable(subject);
        }

        public void setGroup(Group group) {
            this.group = group;
        }

        public Optional<Group> getGroup() {
            return Optional.ofNullable(group);
        }


        /**
         * Sets the tags to add, preserving their entry order.
         */
        public void setTags(Set<Tag> tags) {
            tagEdits = tags.stream().map(tag -> new TagEdit(null, tag.tagName)).toList();
        }

        public void setTagEdits(List<TagEdit> tagEdits) {
            this.tagEdits = List.copyOf(tagEdits);
        }

        public List<TagEdit> getTagEdits() {
            return tagEdits;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(subject, otherEditPersonDescriptor.subject)
                    && Objects.equals(group, otherEditPersonDescriptor.group)
                    && Objects.equals(telegramHandle, otherEditPersonDescriptor.telegramHandle)
                    && tagEdits.equals(otherEditPersonDescriptor.tagEdits);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("telegramHandle", telegramHandle)
                    .add("subject", subject)
                    .add("group", group)
                    .add("tagEdits", tagEdits)
                    .toString();
        }
    }
}
