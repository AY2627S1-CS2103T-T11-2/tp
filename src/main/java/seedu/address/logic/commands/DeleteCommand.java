package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes people selected using its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes people identified by indexes in the "
            + "displayed person list.\n"
            + "Parameters: INDEX_OR_RANGE[,INDEX_OR_RANGE]...\n"
            + "INDEX_OR_RANGE is either INDEX or START_INDEX-END_INDEX. "
            + "(indexes must be positive integers)\n"
            + "Spaces are not allowed within the expression.\n"
            + "Example: " + COMMAND_WORD + " 1,3-5,7";

    public static final String MESSAGE_DELETE_PERSONS_SUCCESS =
            "Deleted %1$d persons:\n%2$s";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    private final PersonTarget target;

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
        this.target = requireNonNull(target);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> peopleToDelete = target.resolve(model);

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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return target.equals(otherDeleteCommand.target);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("target", target)
                .toString();
    }
}
