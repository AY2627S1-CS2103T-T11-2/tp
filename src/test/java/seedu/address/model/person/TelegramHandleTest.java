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

        // Valid Telegram handles have 5 to 32 alphanumeric or underscore characters after @
        assertTrue(TelegramHandle.isValidTelegramHandle("@alice_123"));
        assertTrue(TelegramHandle.isValidTelegramHandle("@CS2103TA"));
        assertTrue(TelegramHandle.isValidTelegramHandle("@a1234567890123456789012345678901"));

        // Invalid Telegram handles
        assertFalse(TelegramHandle.isValidTelegramHandle(" "));
        assertFalse(TelegramHandle.isValidTelegramHandle("alice_123"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@abcd"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@_alice"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@alice-tan"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@alice tan"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@alice\n_tan"));
        assertFalse(TelegramHandle.isValidTelegramHandle("@a12345678901234567890123456789012"));
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
