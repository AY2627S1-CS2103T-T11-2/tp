package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void containsUnknownPrefix_doubleSlashAfterUnknownPrefix_returnsTrue() {
        Prefix groupPrefix = new Prefix("g/");

        assertTrue(ArgumentTokenizer.containsUnknownPrefix(" g/T11 x//typo", groupPrefix));
    }

    @Test
    public void containsUnknownPrefix_embeddedSlashesAndUrl_returnsFalse() {
        Prefix groupPrefix = new Prefix("g/");

        assertFalse(ArgumentTokenizer.containsUnknownPrefix(" g/T11/T12", groupPrefix));
        assertFalse(ArgumentTokenizer.containsUnknownPrefix(" g/T11 https://example.com", groupPrefix));
    }

    @Test
    public void containsMiscasedNameRelationshipQualifier_miscasedQualifierInName_returnsTrue() {
        Prefix[] prefixes = {CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_EMAIL, CliSyntax.PREFIX_SUBJECT};

        for (String qualifier : List.of("s/o", "s/O", "S/o", "d/o", "D/o", "d/O")) {
            assertTrue(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(
                    " n/Ravi " + qualifier + " Kumar", prefixes), qualifier);
        }
        assertTrue(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi s/o", prefixes));
        assertTrue(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" e/x@example.com n/Ravi s/O Level",
                prefixes));
        assertTrue(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi S/O Kumar s/o", prefixes));
    }

    @Test
    public void containsMiscasedNameRelationshipQualifier_noMiscasedQualifierInName_returnsFalse() {
        Prefix[] prefixes = {CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_EMAIL, CliSyntax.PREFIX_SUBJECT};

        // Valid uppercase qualifiers are name text.
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi S/O Kumar", prefixes));
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Priya D/O Raman s/CS2103T",
                prefixes));
        // s/ that follows another prefix, or comes before n/, is an intentional subject.
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" s/o n/Ravi", prefixes));
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi e/x@example.com s/o",
                prefixes));
        // Longer words are not qualifiers.
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi s/Oracle", prefixes));
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi S/OAbu", prefixes));
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" n/Ravi s/o/x", prefixes));
        // Without n/ there is no name for the qualifier to belong to.
        assertFalse(ArgumentTokenizer.containsMiscasedNameRelationshipQualifier(" s/o", CliSyntax.PREFIX_SUBJECT));
    }

    @Test
    public void tokenizeIgnoringQuotedPrefixes_prefixInsideQuotes_keepsQuotedValueTogether() {
        Prefix namePrefix = new Prefix("n/");
        Prefix subjectPrefix = new Prefix("s/");
        String args = " n/\"ravi s/o kumar\" s/CS2103T";

        ArgumentMultimap result = ArgumentTokenizer.tokenizeIgnoringQuotedPrefixes(
                args, namePrefix, subjectPrefix);

        assertArgumentPresent(result, namePrefix, "\"ravi s/o kumar\"");
        assertArgumentPresent(result, subjectPrefix, "CS2103T");
    }

    @Test
    public void containsUnknownPrefixIgnoringQuotedPrefixes_prefixInsideQuotes_returnsFalse() {
        Prefix namePrefix = new Prefix("n/");

        assertFalse(ArgumentTokenizer.containsUnknownPrefixIgnoringQuotedPrefixes(
                "n/\"ravi x/typo kumar\"", namePrefix));
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

}
