package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class GroupTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Group(null));
    }

    @Test
    public void constructor_invalidGroup_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Group(""));
    }

    @Test
    public void isValidGroup() {
        assertFalse(Group.isValidGroup(""));
        assertFalse(Group.isValidGroup(" "));
        assertTrue(Group.isValidGroup("G01"));
        assertTrue(Group.isValidGroup("Tutorial Group 1"));
    }

    @Test
    public void equals() {
        Group group = new Group("G01");
        assertTrue(group.equals(new Group("G01")));
        assertTrue(group.equals(group));
        assertFalse(group.equals(null));
        assertFalse(group.equals("G01"));
        assertFalse(group.equals(new Group("G02")));
    }
}
