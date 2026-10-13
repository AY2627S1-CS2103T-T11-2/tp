---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutorTrack User Guide

TutorTrack is a **desktop application for tutors to manage student contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, TutorTrack can help you manage student contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T11-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   - `list` : Lists all students.

   - `add n/John Doe p/98765432 e/johnd@example.com a/Clementi s/Mathematics g/Sec 3A` : Adds a student named `John Doe` to TutorTrack.

   * `delete 2,4-6` : Deletes the 2nd, 4th, 5th and 6th contacts shown in the current list.

   - `clear` : Deletes all contacts.

   - `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

---

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

- Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

- Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

- Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

- Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

- Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

- If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a student to TutorTrack.

Format: `add n/NAME p/PHONE e/EMAIL a/ADDRESS [s/SUBJECT] [g/GROUP] [t/TAG]...`

* `n/NAME`, `p/PHONE`, `e/EMAIL`, and `a/ADDRESS` are required.
* `s/SUBJECT`, `g/GROUP`, and `t/TAG` are optional.
* If `s/SUBJECT` is omitted, the subject is set to `General`.
* If `g/GROUP` is omitted, the group is set to `General`.
* A student can have zero or more tags. To add multiple tags, repeat the `t/TAG` parameter.
* Parameters can be supplied in any order.

The following validation rules apply:

* Names and subjects must be non-blank and contain only alphanumeric characters and spaces.
* Phone numbers must contain only digits and be at least 3 digits long.
* Emails must use the `local-part@domain` format. The local part must start and end with an alphanumeric character;
  `+`, `_`, `.`, and `-` are allowed only between alphanumeric characters. Domain labels may be separated by periods,
  may contain hyphens only between alphanumeric characters, and the final label must be at least 2 characters long.
* Addresses and groups can contain any characters but must not be blank.
* Tags must be non-blank and contain only alphanumeric characters.
* Leading and trailing whitespace around each value is ignored.

After leading and trailing whitespace is removed, TutorTrack considers two students duplicates when their names are
exactly the same, including letter case. If an existing student has the same name, the new student is rejected even if
the other details are different.

<box type="tip" seamless>

**Tip:** Use `s/` and `g/` when the subject or group is known. Otherwise, TutorTrack records `General`, and the details can
be updated later using the `edit` command.
</box>

Examples:
* `add n/Alice Tan p/91234567 e/alice@example.com a/Clementi s/Mathematics g/Sec 3A t/Student`
* `add n/Bob Lee p/92345678 e/bob@example.com a/Tampines s/Physics g/J1A t/Student t/Online`
* `add n/Charlie Lim p/93456789 e/charlie@example.com a/Bishan` adds a student whose subject and group are both
  `General` and who has no tags.

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [s/SUBJECT] [g/GROUP] [t/TAG]... `

- Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
- At least one of the optional fields must be provided.
- Existing values will be updated to the input values.
- When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
- To remove all of a person's tags, enter `t/` without a tag after it.

Examples:

- `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
- `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons: `find`

Finds persons by name, containing any of the given keywords or quoted phrases, subject, group or a combination of these criteria.

Formats:

- `find SEARCH_TERM [MORE_SEARCH_TERMS] [s/SUBJECT] [g/GROUP]`, where a search term is a `KEYWORD` or `"PHRASE"`

- All searches are case-insensitive; for example, `hans` matches `Hans` and `g/t11` matches `T11`.
- Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
- Only full words match; for example, `Han` does not match `Hans`.
- Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.
- A quoted phrase matches names containing the phrase, ignoring case.
- Subject and group filters require an exact match, ignoring letter case.
- When name, subject, or group criteria are combined, a person must match every supplied criterion (an `AND` search).

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
* `find "Alex Yeoh"` returns names containing the phrase `Alex Yeoh`, ignoring case.
* `find "Alex Yeoh" David` returns names containing either the phrase `Alex Yeoh` or the keyword `David`.
* `find s/CS2103T` returns persons whose subject is `CS2103T`.
* `find g/T11` returns persons whose group is `T11`.
* `find s/CS2103T g/T11` returns persons in group `T11` whose subject is `CS2103T`.
* `find "John" s/CS3434` returns persons whose names contain `John` and whose subject is `CS3434`.
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting contacts: `delete`

Deletes one or more contacts from the address book.

Format: `delete INDEX_OR_RANGE[,INDEX_OR_RANGE]...`

* `INDEX_OR_RANGE` is either an individual `INDEX` or an inclusive range written as `START_INDEX-END_INDEX`.

* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...
* Commas can be used to combine individual indexes and ranges.
* Spaces are not allowed around commas or hyphens. For example, use `delete 1,3-5` not `delete 1, 3 - 5`.
* Duplicate and overlapping indexes are deleted only once. For example, `delete 1-3,2-4` deletes contacts 1 through 4.
* All indexes are validated before any contact is deleted. If any index is invalid, no contacts are deleted.

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.
* `list` followed by `delete 1,3,5` deletes the 1st, 3rd and 5th person in the address book.
* `list` followed by `delete 1,3-5,6` deletes the 1st, 3rd, 4th, 5th and 6th person in the address book.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

---

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

---

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

---

## Command summary

| Action | Format / Example |
| ------ | ---------------- |
| **Add** | `add n/NAME p/PHONE e/EMAIL a/ADDRESS [s/SUBJECT] [g/GROUP] [t/TAG]...` |
| **Clear** | `clear` |
| **Delete** | `delete INDEX_OR_RANGE[,INDEX_OR_RANGE]...`<br> e.g., `delete 1,3-5,7` |
| **Edit** | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [s/SUBJECT] [g/GROUP] [t/TAG]...` |
| **Find** | `find KEYWORD [MORE_KEYWORDS] [s/SUBJECT] [g/GROUP]` or `find "PHRASE" [s/SUBJECT] [g/GROUP]` |
| **List** | `list` |
| **Help** | `help` |
| **Exit** | `exit` |
