package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.TelegramHandle;
import seedu.address.model.tag.Tag;

/**
 * Deletes selected people, or clears optional fields when field prefixes are supplied.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes students identified by indexes in the "
            + "displayed student list.\n"
            + "Parameters: INDEX_OR_RANGE[,INDEX_OR_RANGE]... [p/] [h/] [t/TAG]...\n"
            + "With field prefixes, clears phone, Telegram handle, or named tags instead of deleting students. "
            + "An empty t/ clears all tags, overriding named tag deletions. "
            + "Tag matching ignores case; missing tags reject the entire command. "
            + "Name, email, subject, and group cannot be deleted.\n"
            + "INDEX_OR_RANGE is either INDEX or START_INDEX-END_INDEX. "
            + "(indexes must be positive integers)\n"
            + "Spaces are not allowed within the expression.\n"
            + "Example: " + COMMAND_WORD + " 1,3-5,7";

    public static final String MESSAGE_DELETE_PERSONS_SUCCESS =
            "Deleted %1$d students:\n%2$s";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted student: %1$s";

    public static final String MESSAGE_DELETE_FIELDS_SUCCESS = "Cleared fields for %1$d students:\n%2$s";

    private final PersonTarget target;
    private final FieldsToDelete fields;

    /**
     * Creates a command that deletes the person at one displayed index.
     *
     * @param targetIndex Displayed index of the person to delete.
     */
    public DeleteCommand(Index targetIndex) {
        this(new IndexPersonTarget(
                List.of(IndexRange.single(targetIndex))));
    }

    /**
     * Creates a command that deletes people selected by {@code target}.
     *
     * @param target Selection strategy used when the command executes.
     */
    public DeleteCommand(PersonTarget target) {
        this(target, null);
    }

    /**
     * Creates a command that clears optional fields, or deletes whole contacts when fields is null.
     */
    public DeleteCommand(PersonTarget target, FieldsToDelete fields) {
        this.target = requireNonNull(target);
        this.fields = fields;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> peopleToDelete = target.resolve(model);
        if (fields != null) {
            return deleteFields(model, peopleToDelete);
        }

        for (Person person : peopleToDelete) {
            model.deletePerson(person);
        }

        CommandResult result;

        if (peopleToDelete.size() == 1) {
            result = new CommandResult(String.format(
                    MESSAGE_DELETE_PERSON_SUCCESS,
                    Messages.format(peopleToDelete.get(0))));
        } else {
            String deletedPeople = peopleToDelete.stream()
                    .map(Messages::format)
                    .collect(Collectors.joining("\n"));
            result = new CommandResult(String.format(
                    MESSAGE_DELETE_PERSONS_SUCCESS,
                    peopleToDelete.size(), deletedPeople));
        }

        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return result;
    }

    /**
     * Builds every replacement before changing the model so missing tags cannot cause partial deletion.
     * The current filter remains active, as for edit.
     */
    private CommandResult deleteFields(Model model, List<Person> people) throws CommandException {
        List<Person> updatedPeople = new ArrayList<>();
        for (Person person : people) {
            Set<Tag> tags = new LinkedHashSet<>(person.getTags());
            if (fields.allTags()) {
                tags.clear();
            } else {
                for (Tag tag : fields.tags()) {
                    if (!tags.remove(tag)) {
                        throw new CommandException(String.format(TagEdit.MESSAGE_TAG_NOT_FOUND, tag.tagName));
                    }
                }
            }
            updatedPeople.add(new Person(person.getName(), fields.phone() ? new Phone("") : person.getPhone(),
                    person.getEmail(), fields.telegramHandle() ? new TelegramHandle("") : person.getTelegramHandle(),
                    tags, person.getSubject(), person.getGroup()));
        }

        for (int i = 0; i < people.size(); i++) {
            model.setPerson(people.get(i), updatedPeople.get(i));
        }
        String summary = updatedPeople.stream().map(Messages::format).collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_DELETE_FIELDS_SUCCESS, updatedPeople.size(), summary));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return target.equals(otherDeleteCommand.target) && Objects.equals(fields, otherDeleteCommand.fields);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this).add("target", target);
        if (fields != null) {
            builder.add("fields", fields);
        }
        return builder.toString();
    }

    /**
     * Optional fields to clear. Tag names match without case; an all-tags request overrides named tags.
     */
    public record FieldsToDelete(boolean phone, boolean telegramHandle, boolean allTags, Set<Tag> tags) {
        /**
         * Creates an immutable request containing at least one optional field.
         */
        public FieldsToDelete {
            tags = allTags ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(tags));
            checkArgument(phone || telegramHandle || allTags || !tags.isEmpty(), "Specify at least one field.");
        }
    }
}
