package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void equals_ignoresCase() {
        Tag tag = new Tag("Student");
        assertTrue(tag.equals(tag));
        assertTrue(tag.equals(new Tag("student")));
        assertTrue(new Tag("student").equals(tag));
        assertFalse(tag.equals(new Tag("Teacher")));
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("Student"));
        assertEquals(tag.hashCode(), new Tag("STUDENT").hashCode());
    }

    @Test
    public void duplicateTags_preserveFirstSpelling() {
        var tags = new HashSet<>(List.of(new Tag("Student"), new Tag("student")));
        assertEquals(1, tags.size());
        assertEquals("Student", tags.iterator().next().tagName);
    }

}
