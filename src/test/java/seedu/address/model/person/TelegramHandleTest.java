package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TelegramHandleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TelegramHandle(null));
    }

    @Test
    public void constructor_emptyTelegramHandle_success() {
        assertEquals("", new TelegramHandle("").value);
    }

    @Test
    public void isValidTelegramHandle() {
        // null Telegram handle
        assertThrows(NullPointerException.class, () -> TelegramHandle.isValidTelegramHandle(null));

        // Empty Telegram handles are valid
        assertTrue(TelegramHandle.isValidTelegramHandle(""));

        // Non-empty Telegram handles must begin with @; remaining text is unrestricted
        assertTrue(TelegramHandle.isValidTelegramHandle("@alice_123 / ! Unicode: 用户\nsecond line"));
        assertTrue(TelegramHandle.isValidTelegramHandle("@"));

        // Invalid Telegram handles
        assertFalse(TelegramHandle.isValidTelegramHandle(" "));
        assertFalse(TelegramHandle.isValidTelegramHandle("alice_123"));
        assertFalse(TelegramHandle.isValidTelegramHandle("Blk 456, Den Road, #01-355"));
    }

    @Test
    public void equals() {
        TelegramHandle telegramHandle = new TelegramHandle("@valid_handle");

        // same values -> returns true
        assertTrue(telegramHandle.equals(new TelegramHandle("@valid_handle")));

        // same object -> returns true
        assertTrue(telegramHandle.equals(telegramHandle));

        // null -> returns false
        assertFalse(telegramHandle.equals(null));

        // different types -> returns false
        assertFalse(telegramHandle.equals(5.0f));

        // different values -> returns false
        assertFalse(telegramHandle.equals(new TelegramHandle("@other_valid_handle")));
    }
}
