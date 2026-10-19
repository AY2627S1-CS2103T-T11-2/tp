package seedu.address.logic.commands;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects people from a model for a command to operate on.
 */
public interface PersonTarget {

    /**
     * Resolves this target against the current model state.
     *
     * @param model model containing the displayed persons
     * @return distinct selected persons in display order
     * @throws CommandException if the target cannot be resolved
     */
    List<Person> resolve(Model model) throws CommandException;
}
