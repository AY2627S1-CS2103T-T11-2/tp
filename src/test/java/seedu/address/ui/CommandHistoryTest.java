package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private static final int MAX_COMMAND_HISTORY = 100;

    private final CommandHistory commandHistory = new CommandHistory();

    @Test
    public void add_nullCommand_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> commandHistory.add(null));
    }

    @Test
    public void getPrevious_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), commandHistory.getPrevious());
    }

    @Test
    public void getNext_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), commandHistory.getNext());
    }

    @Test
    public void getPrevious_singleCommand_returnsCommand() {
        commandHistory.add("list");

        assertEquals(Optional.of("list"), commandHistory.getPrevious());
    }

    @Test
    public void getPrevious_atOldestCommand_returnsEmptyWithoutMoving() {
        commandHistory.add("list");
        commandHistory.getPrevious();

        assertEquals(Optional.empty(), commandHistory.getPrevious());
        assertEquals(Optional.of(""), commandHistory.getNext());
    }

    @Test
    public void getPrevious_multipleCommands_returnsCommandsFromNewestToOldest() {
        commandHistory.add("first");
        commandHistory.add("second");
        commandHistory.add("third");

        assertEquals(Optional.of("third"), commandHistory.getPrevious());
        assertEquals(Optional.of("second"), commandHistory.getPrevious());
        assertEquals(Optional.of("first"), commandHistory.getPrevious());
    }

    @Test
    public void getNext_withoutBackwardNavigation_returnsEmpty() {
        commandHistory.add("list");

        assertEquals(Optional.empty(), commandHistory.getNext());
    }

    @Test
    public void getNext_afterBackwardNavigation_returnsCommandsFromOldestToNewest() {
        commandHistory.add("first");
        commandHistory.add("second");
        commandHistory.add("third");
        commandHistory.getPrevious();
        commandHistory.getPrevious();
        commandHistory.getPrevious();

        assertEquals(Optional.of("second"), commandHistory.getNext());
        assertEquals(Optional.of("third"), commandHistory.getNext());
    }

    @Test
    public void getNext_afterNewestCommand_returnsBlankEntry() {
        commandHistory.add("list");
        commandHistory.getPrevious();

        assertEquals(Optional.of(""), commandHistory.getNext());
    }

    @Test
    public void getNext_afterBackwardNavigation_returnsSavedDraft() {
        commandHistory.add("list");
        commandHistory.saveDraft("edit");
        commandHistory.getPrevious();

        assertEquals(Optional.of("edit"), commandHistory.getNext());
    }

    @Test
    public void saveDraft_afterEditingHistoryEntry_restoresEditedDraft() {
        commandHistory.add("list");
        commandHistory.saveDraft("original draft");

        assertEquals(Optional.of("list"), commandHistory.getPrevious());

        commandHistory.saveDraft("edited historical command");

        assertEquals(
                Optional.of("edited historical command"),
                commandHistory.getNext());
    }

    @Test
    public void navigation_caseCorrectionAndDraftCompletion_preservesExpectedHistory() {
        commandHistory.add("List");
        commandHistory.add("list");

        commandHistory.saveDraft("edit");
        assertEquals(Optional.of("list"), commandHistory.getPrevious());
        assertEquals(Optional.of("edit"), commandHistory.getNext());

        commandHistory.add("edit 1-2 g/somegrp");

        assertEquals(Optional.of("edit 1-2 g/somegrp"), commandHistory.getPrevious());
        assertEquals(Optional.of("list"), commandHistory.getPrevious());
        assertEquals(Optional.of("List"), commandHistory.getPrevious());
    }

    @Test
    public void add_afterSavingDraft_discardsDraft() {
        commandHistory.add("list");
        commandHistory.saveDraft("edit");
        commandHistory.getPrevious();
        commandHistory.getNext();

        commandHistory.add("edit 1-2 g/somegrp");
        commandHistory.getPrevious();

        assertEquals(Optional.of(""), commandHistory.getNext());
    }

    @Test
    public void saveDraft_afterReturningToNewest_replacesPreviousDraft() {
        commandHistory.add("list");

        commandHistory.saveDraft("first draft");
        commandHistory.getPrevious();
        commandHistory.getNext();

        commandHistory.saveDraft("updated draft");
        commandHistory.getPrevious();

        assertEquals(Optional.of("updated draft"), commandHistory.getNext());
    }

    @Test
    public void saveDraft_nullDraft_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> commandHistory.saveDraft(null));
    }

    @Test
    public void getNext_atBlankEntry_returnsEmptyWithoutMoving() {
        commandHistory.add("list");
        commandHistory.getPrevious();
        commandHistory.getNext();

        assertEquals(Optional.empty(), commandHistory.getNext());
        assertEquals(Optional.of("list"), commandHistory.getPrevious());
    }

    @Test
    public void add_consecutiveDuplicate_storesOnlyOneCopy() {
        commandHistory.add("list");
        commandHistory.add("list");

        assertEquals(Optional.of("list"), commandHistory.getPrevious());
        assertEquals(Optional.empty(), commandHistory.getPrevious());
    }

    @Test
    public void add_consecutiveDuplicateWithDifferentCase_storesBothCommands() {
        commandHistory.add("LiSt");
        commandHistory.add("list");

        assertEquals(Optional.of("list"), commandHistory.getPrevious());
        assertEquals(Optional.of("LiSt"), commandHistory.getPrevious());
        assertEquals(Optional.empty(), commandHistory.getPrevious());
    }

    @Test
    public void add_nonConsecutiveDuplicate_storesBothCopies() {
        commandHistory.add("list");
        commandHistory.add("clear");
        commandHistory.add("list");

        assertEquals(Optional.of("list"), commandHistory.getPrevious());
        assertEquals(Optional.of("clear"), commandHistory.getPrevious());
        assertEquals(Optional.of("list"), commandHistory.getPrevious());
    }

    @Test
    public void add_afterBackwardNavigation_resetsNavigationToNewestCommand() {
        commandHistory.add("first");
        commandHistory.add("second");
        commandHistory.getPrevious();
        commandHistory.getPrevious();

        commandHistory.add("third");

        assertEquals(Optional.of("third"), commandHistory.getPrevious());
    }

    @Test
    public void add_consecutiveDuplicateAfterBackwardNavigation_resetsNavigationToBlankEntry() {
        commandHistory.add("first");
        commandHistory.add("second");
        commandHistory.getPrevious();

        commandHistory.add("second");

        assertEquals(Optional.empty(), commandHistory.getNext());
        assertEquals(Optional.of("second"), commandHistory.getPrevious());
    }

    @Test
    public void add_moreThanMaximumCapacity_discardsOldestCommands() {
        for (int i = 0; i <= MAX_COMMAND_HISTORY; i++) {
            commandHistory.add("command " + i);
        }

        for (int i = MAX_COMMAND_HISTORY; i >= 1; i--) {
            assertEquals(Optional.of("command " + i), commandHistory.getPrevious());
        }
        assertEquals(Optional.empty(), commandHistory.getPrevious());
    }

    @Test
    public void add_commandsDifferingOnlyByWhitespace_storesBothCommands() {
        commandHistory.add("list");
        commandHistory.add("list ");

        assertEquals(Optional.of("list "), commandHistory.getPrevious());
        assertEquals(Optional.of("list"), commandHistory.getPrevious());
    }
}
