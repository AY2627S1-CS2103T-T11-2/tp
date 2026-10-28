package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TELEGRAM_HANDLE_BOB;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different Telegram handle -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTelegramHandle(VALID_TELEGRAM_HANDLE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void tagEdits_copiedAndComparedInOrder() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("Student").build();
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        assertTrue(descriptor.isAnyFieldEdited());
        assertFalse(descriptor.hasSinglePersonOnlyFields());
        assertEquals(descriptor, copy);
        assertThrows(UnsupportedOperationException.class, () -> copy.getTagEdits().clear());

        copy.setTagEdits(List.of(new TagEdit("Student", "Online")));
        assertFalse(descriptor.equals(copy));
        assertEquals(List.of(new TagEdit(null, "Student")), descriptor.getTagEdits());
    }

    @Test
    public void tagEdits_preserveOrderAndReplacementSpelling() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("Student", "Online").build();
        assertFalse(descriptor.equals(new EditPersonDescriptorBuilder().withTags("Online", "Student").build()));
        descriptor.setTagEdits(List.of(new TagEdit("Student", "ONLINE")));
        EditPersonDescriptor differentlyCased = new EditPersonDescriptor();
        differentlyCased.setTagEdits(List.of(new TagEdit("Student", "Online")));
        assertFalse(descriptor.equals(differentlyCased));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", telegramHandle="
                + editPersonDescriptor.getTelegramHandle().orElse(null) + ", subject="
                + editPersonDescriptor.getSubject().orElse(null) + ", group="
                + editPersonDescriptor.getGroup().orElse(null) + ", tagEdits="
                + editPersonDescriptor.getTagEdits() + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
