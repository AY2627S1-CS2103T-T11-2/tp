package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.LinkedHashSet;
import java.util.Set;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.tag.Tag;

/**
 * An ordered tag addition (null oldName) or rename. Replacement spelling is significant.
 */
public record TagEdit(String oldName, String newName) {

    public static final String MESSAGE_TAG_NOT_FOUND = "Tag does not exist: %1$s";

    /**
     * Creates a validated addition or rename.
     */
    public TagEdit {
        requireNonNull(newName);
        checkArgument(Tag.isValidTagName(newName), Tag.MESSAGE_CONSTRAINTS);
        checkArgument(oldName == null || Tag.isValidTagName(oldName), Tag.MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns edited tags without modifying the original set. Renames retain the source position,
     * removing any existing destination tag so that the replacement spelling wins when merging.
     *
     * @throws CommandException if the tag to rename does not exist
     */
    public Set<Tag> applyTo(Set<Tag> tags) throws CommandException {
        Tag replacement = new Tag(newName);
        Set<Tag> updatedTags = new LinkedHashSet<>(tags);
        if (oldName == null) {
            updatedTags.add(replacement);
            return updatedTags;
        }

        Tag original = new Tag(oldName);
        if (!tags.contains(original)) {
            throw new CommandException(String.format(MESSAGE_TAG_NOT_FOUND, oldName));
        }

        updatedTags.clear();
        for (Tag tag : tags) {
            if (tag.equals(original)) {
                updatedTags.add(replacement);
            } else if (!tag.equals(replacement)) {
                updatedTags.add(tag);
            }
        }
        return updatedTags;
    }
}
