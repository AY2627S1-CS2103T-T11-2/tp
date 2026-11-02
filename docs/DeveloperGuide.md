---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutorTrack Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

---

## **Acknowledgements**

TutorTrack is based on [AddressBook-Level3](https://se-education.org/addressbook-level3/) by the [SE-EDU initiative](https://se-education.org). It uses [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), and [JUnit 5](https://junit.org/junit5/).

**AI-assisted development:** All team members used AI tools during TutorTrack's development. The tools used by the
team include ChatGPT and Claude Code. AI assistance was used across implementation, debugging, code review, test
development, and documentation. AI-assisted contributions were reviewed and verified before integration.

---

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

---

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The **_Architecture Diagram_** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

TutorTrack separates the interface, command processing, student data, and file access.

- **UI** receives commands and displays feedback and student records.
- **Logic** parses commands, executes them against the model, and saves student data.
- **Model** holds student records, preferences, and the current filtered view.
- **Storage** reads and writes JSON files.
- **Main/MainApp** creates and connects these components at startup.

`LogicManager` acts as the UI's command-processing facade. It coordinates parsing,
execution, and saving; individual commands implement the requested operation.
`AddressBookParser` selects a command-specific parser when one is needed.
Commands such as `help`, `list`, and `exit` are constructed directly.
`PersonTarget` is a selection strategy used by delete and bulk edit.
Its current implementation, `IndexPersonTarget`, selects students by displayed indexes.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.

- At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
- At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

- [**`UI`**](#ui-component): The UI of the App.
- [**`Logic`**](#logic-component): The command executor.
- [**`Model`**](#model-component): Holds the data of the App in memory.
- [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The _Sequence Diagram_ below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

- defines its _API_ in an `interface` with the same name as the Component.
- provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

- executes user commands using the `Logic` component.
- listens for changes to `Model` data so that the UI can be updated with the modified data.
- keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
- depends on some classes in the `Model` component because it displays `Person` objects from the model.
- uses `PersonCard` to hide empty Phone and Telegram handle labels; a person with no tags has no tag labels.

`MainWindow` owns one `HelpWindow`. `PersonListPanel` creates `PersonCard` instances for the JavaFX list cells that
are currently displayed, rather than retaining one card for every student.

#### Command history

`CommandHistory` stores up to 100 submitted commands independently of the JavaFX components. Consecutive exact duplicates
are stored only once, while commands that differ in case remain distinct. This allows its navigation behaviour to be tested
using regular unit tests.

`CommandHistory` manages stored commands, navigation, and the saved draft.
`CommandBox` handles input events, delegates history operations to `CommandHistory`, and displays the returned text. Its `isDisplayingHistoryEntry` flag prevents programmatic text changes from replacing the saved draft.
Consequently, edits made to a recalled command are preserved and restored when the user navigates past the newest stored command.

Non-empty submissions are recorded before execution, including failed commands. Successful execution clears the input, while failure retains the submitted text.

The overview shows how input, history, and execution work together. Displaying recalled text does not overwrite
the draft; editing that text does. Moving down past the newest entry restores the latest saved draft.

<puml src="diagrams/CommandHistoryOverview.puml" alt="Overview of command submission and history navigation" />

<panel header="Detailed command-history sequence">

<puml src="diagrams/CommandHistorySequenceDiagram.puml" alt="Command history navigation sequence diagram" />

</panel>

#### Help window

The `help` command returns a `CommandResult` requesting that the UI show help. `MainWindow` displays the command
feedback in `ResultDisplay`, then either shows the existing `HelpWindow` or brings it to the front. The window displays
a compact command summary and lets users copy the User Guide URL. The following sequence diagram shows this flow:

<puml src="diagrams/HelpCommandSequenceDiagram.puml" alt="Help command sequence diagram" />

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="900"/>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. `LogicManager` executes the command against `Model`. The command reads or updates student data. For example, `DeleteCommand` validates the displayed selection before applying a batch update.
1. After normal execution, `LogicManager` saves student data through `Storage`, then returns the `CommandResult` to the UI.

Clear-confirmation prompts return without saving. Parsing and command-validation failures also skip saving.

If saving fails after execution, the UI displays an error. The command's changes may already exist in memory; they are not rolled back.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="800"/>

How the parsing works:

- When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
- All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

#### Shared index and range parsing

Commands that operate on displayed persons can use `IndexRangeParser` to parse comma-separated indexes and inclusive index ranges.

For example, `1,3-5,7` represents displayed indexes 1, 3, 4, 5, and 7.

`IndexRangeParser` validates the expression, parses its indexes, sorts the result, and merges duplicate, overlapping, and adjacent ranges. It returns the canonical ranges in an immutable list. For example, `3-4,1,2-3,1,6,5` is canonicalized to `1-6`.

The parser does not check whether the selected indexes exist in the currently displayed person list. That list depends on the current model state and active filter, so bounds validation is performed when the resulting command executes.

Command-specific parsers translate parsing failures into their own feedback. `EditCommandParser` reports an invalid
displayed index for numeric selections containing zero, negative or overflowing indexes, or reversed ranges. Other
malformed expressions show the command usage. This allows commands to share the same index-expression syntax while
retaining command-specific feedback.

Both `DeleteCommandParser` and `EditCommandParser` report `Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX` for invalid numeric expressions, this numeric-expression check is intentionally present in both parsers.

`AddCommandParser` treats `h/TELEGRAM_HANDLE` as optional. `EditCommandParser` preserves the current value when `h/`
is omitted and rejects an empty `h/`; optional fields are cleared using `delete`. Both parsers explicitly reject the
legacy `a/` prefix.

`ParserUtil` performs shared contact-field normalization before constructing the corresponding model value object.
It trims names and collapses consecutive internal whitespace. `Name` permits alphanumeric words separated by single
spaces, hyphens or apostrophes within words, initials or abbreviations of up to three letters ending in a period
when followed by another word, and one uppercase `D/O` or `S/O` between name parts. Because `/` normally ends a
prefix, `ArgumentTokenizer.containsUnknownPrefix` treats an exact `D/O` or `S/O` token as name text only while `n/`
is the active prefix; elsewhere it is rejected as an unknown prefix. Any other casing is ambiguous because `s/` is
also the subject prefix: `n/Ravi s/o Kumar` could be a mistyped name or the name `Ravi` with the subject `o Kumar`, and
the input is identical either way. Previously the tokenizer chose the subject reading, silently changing both fields.
`AddCommandParser` and `EditCommandParser` therefore call
`ArgumentTokenizer.containsMiscasedNameRelationshipQualifier` before checking prefixes or field values
(`EditCommandParser` parses its index expression first). It reports a `d/o`, `s/O`, `S/o`
or similar token that follows the `n/` value and is followed by whitespace or the end of input, and the parser throws
`Name.MESSAGE_INVALID_RELATIONSHIP_QUALIFIER`. Rejecting is preferred over guessing because either guess can corrupt
a field without warning. The resulting restriction is that a subject consisting of, or starting with, the word `O`
(such as `O Level`) cannot be written immediately after the name; `s/` must come before `n/` or after another prefix.
`ParserUtil` also collapses repeated ordinary spaces in subjects and groups. Tabs are not collapsed, so a tab in a
subject is still rejected and a tab in a group is kept. The model value objects enforce maximum lengths of 100 characters for names, subjects, and
groups, 50 for tags, and 254 for email addresses, so command parsing and JSON reload apply the same constraints.
A data file containing a longer value therefore fails to load, as described in the Storage component. `Phone` requires exactly eight digits, accepts single spaces or hyphens
between digit groups, and stores numbers as `xxxx xxxx`. Country codes, `+`, and consecutive separators are rejected.
`TelegramHandle` treats an empty value as absent; a non-empty
value must begin with `@`, followed by 5 to 32 letters, digits, or underscores, with no leading underscore.

### Model component

**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="900" />

The `Model` component,

- stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
- represents a person's Telegram contact using `TelegramHandle`; an empty value represents an omitted handle.
- stores non-empty phone numbers as eight digits with a single space after the fourth digit (`xxxx xxxx`).
- identifies people by name using `Person.isSamePerson`. Identity comparison ignores letter case, while the `Name`
  value object retains case-sensitive equality and preserves the spelling entered for storage and display. Therefore,
  distinct people with the same name cannot coexist (issue #97 remains unresolved).
- stores tags in a `LinkedHashSet`, preserving entry order through parsing, editing, JSON storage, and display.
  `Tag.equals` and `hashCode` are case-insensitive; the first spelling supplied is retained when duplicates occur.
- stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
- stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
- does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T11-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

- can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
- is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
- depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`FileUtil` writes replacement content to a temporary file in the destination directory before moving it over the
destination. It uses an atomic replacement when supported by the file system and otherwise falls back to a regular
replacement. If writing or moving fails, it attempts to remove the temporary file.

`MainApp` stores student data in `data/tutortrack.json`. If that file does not exist but a data file from an earlier
version does (`data/addressbook.json` or `addressbook.json`), `MainApp#migrateLegacyDataFileIfNeeded` copies it to
`data/tutortrack.json` and keeps the original as a backup. If the copy fails, TutorTrack continues to use the legacy file.

`JsonAdaptedPerson` deliberately reads and writes a person's `TelegramHandle` using the existing JSON `"address"`
property; it does not introduce a `"telegramHandle"` property. When loading an older record, it replaces a non-empty
physical address that does not begin with `@` with an empty `TelegramHandle` in memory, while preserving
the person. Values beginning with `@` must also satisfy the current Telegram-handle format; otherwise, they are
cleared. This is not a migration and no backup is created. The original JSON remains unchanged until the next
successfully executed command causes `LogicManager` to save the in-memory data, at which point the old physical
address is overwritten with an empty `"address"` value.

JSON files that already contain names differing only by capitalization, such as `John Tan` and `john tan`, now fail
validation as duplicate data. Loading is all-or-nothing. If an existing data file cannot be loaded, for example
because of such duplicates, a value longer than its maximum length, malformed JSON, or a `null` entry in the
`persons` or `tags` array, `MainApp` does not create
`Logic` or the main window. Instead, it shows an error dialog naming the file and exits, so no command can save over
the original file. An automatic migration would risk merging distinct students, so recovery is manual: back up and
correct the file, or move it away to start again with sample data.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

---

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Delete command

The overview shows the successful execution paths. Index validation finishes before mutation; field clearing also
prepares every replacement before updating the model. A validation failure stops the flow without changing records
or saving. Whole-record deletion resets the filter, while field clearing keeps it. See the
[User Guide](UserGuide.md#correcting-delete-errors) for input examples and the corresponding messages.

<puml src="diagrams/DeleteCommandOverview.puml" alt="Overview of student deletion and optional-field clearing" />

The delete overview covers both whole-record deletion and optional-field clearing.

The detailed diagram below expands the successful `execute("delete 1,3-4")` path, including saving.
It assumes at least four students are displayed and no field prefixes are supplied.

<panel header="Detailed sequence: delete 1,3-4">

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1,3-4` Command" />

</panel>

`DeleteCommandParser` delegates its index-expression parsing to the shared `IndexRangeParser`, then constructs an `IndexPersonTarget` from the canonical ranges.

Each parsed component is represented by an `IndexRange`. An individual index is represented as a range whose start
and end are equal.

`IndexPersonTarget` sorts and merges duplicate, adjacent, or overlapping ranges. For example, `1-3,2-4,4` is
normalized to `1-4`. This ensures that each student is selected at most once.

Before deleting any students, `IndexPersonTarget` resolves and validates every index against a snapshot of the
currently displayed student list. Consequently, a command such as `delete 1,999` fails without deleting student 1
when displayed index 999 does not exist. This also applies to field clearing, for example `delete 1,999 p/`.

After validation, whole-record deletion calls `Model#deletePersonsInBatch` once, so the selected students are removed with a single observable-list update. The filter is then reset to show all remaining students.

`DeleteCommand` depends on the shared `PersonTarget` interface rather than directly on an index. This allows future
selection strategies to reuse the deletion workflow.

`delete` can also clear optional fields. When `p/`, `h/`, or `t/` is present, `DeleteCommandParser` creates a
`DeleteCommand.FieldsToDelete` record instead of deleting people. `p/` and `h/` must be empty and may each appear
once. An empty `t/` clears all tags and overrides named deletions, but supplied tag names must still pass validation.
Named tags are parsed into a set, so a repeated name counts once.
`DeleteCommand#deleteFields` builds every replacement `Person` before changing the model. If a named tag is missing
from any selected person, it throws a `CommandException` and nobody is changed. Unlike deleting people, clearing
fields keeps the current filter. Prepared replacements are applied through `Model#setPersonsInBatch` in one update.

### Edit command

`EditCommandParser` uses `IndexRangeParser` to obtain canonical ranges, then constructs an `IndexPersonTarget` to
check whether exactly one displayed index is selected. A single selection uses the index-based `EditCommand`
constructor and permits all fields. Multiple selections use the `PersonTarget` constructor and permit only subject,
group, and tags. Name, phone, email, and Telegram handle require a single selection; both the parser and the bulk
execution path enforce this restriction.

The parser rejects unknown prefixes, the legacy `a/` prefix, repeated non-tag prefixes, and commands with no field
to edit. Field validation and normalization follow the shared rules in the [Logic component](#logic-component).
Empty values are rejected; optional fields are cleared using `delete`.

The following sequence diagram shows successful parsing of `edit 1-3 g/T11 t/Online`. Field parsing is summarized
in a note; displayed-list bounds are checked during execution.

<puml src="diagrams/EditParsingSequenceDiagram.puml" width="900" alt="Parsing a bulk edit command" />

`EditPersonDescriptor` stores omitted replacement fields as `null` and tag operations as an ordered list, which is
empty when no tags are supplied. Both command constructors copy the descriptor defensively. `createEditedPerson`
preserves omitted fields and applies each `TagEdit` from left to right to a copy of the person's tags. Additions
preserve existing tags and their spelling. Renames match without regard to case, retain the source position, and
merge any existing destination tag using the replacement's spelling. A missing rename source throws a
`CommandException` before any model update.

For a single edit, the command validates the displayed index, prepares the replacement, and checks for a duplicate
using `Person.isSamePerson` before calling `Model#setPerson`. Names are compared without regard to case, so another
student's name is rejected while a capitalization-only change to the selected student's name is stored.

For a bulk edit, `IndexPersonTarget#resolve` validates every selected index against a snapshot of the displayed
list. The command prepares all replacement persons and the success result before calling `Model#setPersonsInBatch`,
which updates the observable list once. An invalid index or a missing rename source leaves every student unchanged.
Resolving the complete selection first also prevents filter changes from causing later students to be skipped.

The following sequence diagram continues the same example, assuming displayed indexes 1 to 3 exist. It focuses on
selection and batch replacement; parsing and saving are covered by the diagram above and `LogicManager` respectively.

<puml src="diagrams/EditSequenceDiagram.puml" width="800" alt="Executing a bulk edit command" />

Both single and bulk edits keep the current filter. Edited students that no longer match disappear from the
displayed list. `LogicManager` saves the address book after successful execution.

### Find command

`FindCommandParser` accepts the `n/`, `h/`, `s/`, `g/`, and `t/` prefixes. It rejects input that has no criterion,
a non-empty preamble, an unknown prefix, or a repeated prefix. Name input is split on commas. Each term is either an
unquoted keyword or a quoted phrase, and a blank phrase such as `" "` is rejected. Each normalized term is limited to
`Name.MAX_LENGTH`. Keywords use valid name tokens. A phrase must be a possible substring of a valid name; it may begin
or end at a valid punctuation boundary, such as `"Jean-"` for `Jean-Luc`. Prefix-like text inside quotations is kept
as phrase text before this validation, which allows relationship qualifiers such as `"Ravi S/O Kumar"` to be parsed
without treating `S/O` as the subject prefix.
`NameContainsSearchTermsPredicate` matches a keyword against whole name components, which are separated by spaces,
hyphens, apostrophes, or periods. It normalizes repeated whitespace and matches a phrase as a literal substring of
the name. Both comparisons ignore case, and a match against any supplied name term is sufficient.

`FindPredicate` combines the optional name predicate with the optional Telegram handle, subject, group, and tag
criteria. A person must satisfy every criterion type supplied. Telegram handles are matched exactly while ignoring
case. Subject and group comparisons ignore case and collapse repeated spaces in the stored value. New values are
already collapsed by `ParserUtil`, but records saved before that change may still contain repeated spaces, and the
JSON file is not rewritten when it is loaded. A tag expression separated by `,` uses `TagMatchMode.ANY`, while one
separated by `&` uses `TagMatchMode.ALL`; mixing both separators is rejected. `FindCommand` passes the predicate to
`Model#updateFilteredPersonList`, and the UI updates because it observes the filtered list.

### Clear confirmation

`AddressBookParser` turns `clear` into `ClearCommand(false)` and `clear confirm` into `ClearCommand(true)`; any other
argument is rejected. `LogicManager` records whether a confirmation is pending. An unconfirmed `ClearCommand` only
sets this flag and returns a prompt, so nothing is saved. `clear confirm` empties the student list only when the flag
is set; otherwise, it reports that no confirmation is pending. Any other input, including input that fails to parse,
resets the flag, so the confirmation must immediately follow `clear`.

The following low-level sequence diagram shows the three outcomes of the confirmation flow: requesting confirmation,
confirming a pending request, and attempting confirmation without a pending request. Saving occurs only after a
confirmed clear; the other two outcomes leave the model unchanged.

<puml src="diagrams/ClearConfirmationSequenceDiagram.puml" width="900" alt="Clear confirmation sequence diagram" />

---

## **Documentation, logging, testing, dev-ops**

- [Documentation guide](Documentation.md)
- [Testing guide](Testing.md)
- [Logging guide](Logging.md)
- [DevOps guide](DevOps.md)

---

## **Appendix: Requirements**

### Product scope

**Target user profile**:

- is a teaching assistant or tutor managing many students
- teaches one or more modules or tutorial groups
- needs to retrieve and update student particulars quickly
- prefers desktop applications
- can type quickly
- prefers typing to mouse interactions
- is reasonably comfortable using CLI-style commands

**Value proposition**:

TutorTrack helps teaching assistants manage student information efficiently in one place, including contact details, modules, and tutorial groups, using fast keyboard-based commands.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | Status | As a … | I want to … | So that I can… |
| --- | --- | --- | --- | --- |
| `* * *` | Implemented | new TA user | see usage instructions | refer to instructions when I forget how to use TutorTrack |
| `* * *` | Implemented | TA | add a student and their particulars | keep track of students under my care |
| `* * *` | Implemented | TA | edit a student's particulars | correct or update their information |
| `* * *` | Implemented | TA | delete a student | remove records I no longer need |
| `* * *` | Implemented | TA | clear all student records after confirmation | reset my records without accidentally deleting them |
| `* * *` | Implemented | TA | list all students | view all students I manage |
| `* * *` | Implemented | TA | organise students by tutorial group | manage students across different classes |
| `* * *` | Implemented | TA | find students from a tutorial group | view students belonging to that group |
| `* * *` | Implemented | TA | record a student's module | distinguish students from different modules |
| `* * *` | Implemented | TA | view a student's particulars in the displayed list or search results | retrieve their information quickly |
| `* *` | Implemented | TA | reuse a previously submitted command | repeat or amend a command efficiently |
| `* *` | Implemented | TA | copy a displayed email address, Telegram handle, or phone number | use contact details in another application |
| `* *` | Planned | TA | record tutorial participation | keep track of student participation |
| `* *` | Planned | TA | track consultation bookings | manage consultations with students |
| `*` | Planned | TA | record assessment grades | keep track of students' academic progress |
| `*` | Planned | TA | export student records | keep an external copy of tutoring records |

### Use cases

(For all use cases below, the **System** is the `TutorTrack` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add a student**

**MSS**

1. TA requests to add a student with a name, email, subject, and group, plus any optional particulars.
2. TutorTrack validates the supplied particulars.
3. TutorTrack adds the student.
4. TutorTrack confirms that the student was added.

Use case ends.

**Extensions**

- 2a. The supplied name or particulars are invalid.
  - 2a1. TutorTrack shows the corresponding error message.
  - Use case ends.

- 2b. A student with the same name, ignoring letter case, already exists.
  - 2b1. TutorTrack shows an error message.
  - Use case ends.

**Use case: Edit a single student's particulars**

**MSS**

1. TA requests to edit one student, identified by their index in the displayed list.
2. TutorTrack finds the student at that index.
3. TutorTrack validates the new particulars.
4. TutorTrack updates the specified particulars.
5. TutorTrack confirms that the student record was updated.

Use case ends.

**Extensions**

- 2a. The index is not in the displayed list.
  - 2a1. TutorTrack shows an error message.
  - Use case ends.

- 3a. One or more new particulars are invalid.
  - 3a1. TutorTrack shows the corresponding error message.
  - Use case ends.

- 3b. The new name is already used by another student, ignoring letter case.
  - 3b1. TutorTrack shows an error message.
  - Use case ends.

- 3c. A tag to rename does not exist after applying any preceding tag operations.
  - 3c1. TutorTrack shows an error message without changing any particulars.
  - Use case ends.

Capitalization-only name edits refer to the same selected student and are accepted. The replacement `Name` is stored,
so the newly entered capitalization appears in subsequent displays and saves.

**Use case: Edit multiple students' common particulars**

**MSS**

1. TA requests to edit students identified by displayed indexes or index ranges.
2. TutorTrack validates the index expression and supplied particulars.
3. TutorTrack resolves the indexes against the currently displayed student list.
4. TutorTrack updates the supplied subject, group, or tags for every selected student.
5. TutorTrack confirms which student records were updated.

Use case ends.

**Extensions**

- 2a. The index expression or a supplied particular is invalid.
  - 2a1. TutorTrack shows the corresponding error message.
  - Use case ends.

- 2b. No field to edit is supplied.
  - 2b1. TutorTrack informs the TA that at least one field must be provided.
  - Use case ends.

- 2c. A name, phone number, email address, or Telegram handle is supplied.
  - 2c1. TutorTrack informs the TA that only subject, group, and tags can be edited
    for multiple students.
  - Use case ends.

- 3a. One or more indexes do not exist in the currently displayed list.
  - 3a1. TutorTrack shows an error message without editing any student.
  - Use case ends.

- 4a. A tag to rename does not exist for a selected student after applying any preceding tag operations.
  - 4a1. TutorTrack shows an error message without editing any student.
  - Use case ends.

**Use case: Delete students**

**MSS**

1. TA requests to list or find students.
2. TutorTrack shows the matching students.
3. TA requests to delete one or more students by their displayed indexes or index ranges.
4. TutorTrack deletes the selected students and confirms which students were deleted.

Use case ends.

**Extensions**

- 2a. The list is empty.
  - Use case ends.

- 3a. The index expression is invalid, or an index is not in the displayed list.
  - 3a1. TutorTrack shows an error message without deleting any student.
  - Use case resumes at step 2.

**Use case: List students from a tutorial group**

**MSS**

1. TA requests to list students from a specified tutorial group.
2. TutorTrack validates the tutorial group.
3. TutorTrack finds students belonging to that tutorial group.
4. TutorTrack displays the matching students.

Use case ends.

**Extensions**

- 2a. The supplied tutorial group is invalid.
  - 2a1. TutorTrack shows an error message.
  - Use case ends.

- 3a. No students belong to the specified tutorial group.
  - 3a1. TutorTrack informs the TA that no students were found.
  - Use case ends.

**Use case: Clear all student records**

**MSS**

1. TA enters `clear`.
2. TutorTrack asks the TA to confirm the operation.
3. TA immediately enters `clear confirm`.
4. TutorTrack deletes all student records and confirms that they were cleared.

Use case ends.

**Extensions**

- 3a. The TA enters another command, or an invalid command.
  - 3a1. TutorTrack cancels the pending confirmation.
  - 3a2. TutorTrack does not delete any student records.
  - Use case ends.

- 3b. The TA enters `clear confirm` without first entering `clear`.
  - 3b1. TutorTrack reports that no clear confirmation is pending.
  - 3b2. TutorTrack does not delete any student records.
  - Use case ends.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should
    be able to accomplish most of the tasks faster using commands than using the mouse.
4.  Normal operations should complete within 1 second for 200 records.
5.  Should save data locally in a human-editable text file.
6.  Should preserve data after being closed and reopened.
7.  Should prevent partially written files during saving.
8.  Should provide clear confirmation for destructive operations.
9.  Should validate all user-entered and imported data.
10. Should not transmit student data to external services.
11. Should provide clear error messages and usage instructions.
12. Should support keyboard or command-based interaction for core tasks.
13. Should work on Windows, Linux, and macOS using Java 25.
14. Should work without an installer.
15. Should not require a remote server.
16. Should not exceed 100 MB.
17. Should remain usable at the required screen resolutions and display scales.
18. Should not require an internet connection for core functionality.

### Glossary

- **TA**: Teaching Assistant.
- **Tutorial Group**: A tutorial class or group that a student belongs to.
- **Module/Subject**: An academic course associated with a student, e.g. `CS1231S`.
- **Student particulars**: Information stored about a student, such as name, phone number, email, module/subject, and tutorial group.
- **CLI-style command**: A text command entered by the user to perform an action in TutorTrack.
- **Mainstream OS**: Windows, Linux, and macOS.
- **Displayed list**: The list of students currently shown, which may be filtered by `find`. Command indexes refer to this list.
- **Index range**: An inclusive range of displayed indexes written as `START_INDEX-END_INDEX`, for example `3-5`.

---

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

Unless a test says otherwise, run it in a new empty folder. This ensures TutorTrack starts with its six sample
students. The application saves changes to `data/tutortrack.json`, so simply closing and reopening it does not reset
the sample data. Treat each test case as independent unless it explicitly gives a sequence of commands.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more _exploratory_ testing.
</box>

### Launch and shutdown

1. Initial launch
   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file, or open a terminal in that folder and run `java -jar tutortrack.jar`.<br>
      Expected: The GUI opens with a set of sample students. The window size may not be optimal.

1. Saving window preferences
   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
      Expected: The most recent window size and location are retained.

### Deleting students

Run each delete test independently against a restored copy of the six sample students. Successful deletion changes
the displayed indexes, so do not reuse a previous test's modified data. Back up existing data before testing.

1. Deleting contacts while the full list is shown.
   1. Prerequisites: List all persons using the `list` command with all six sample students present.

   1. Test case: `delete 1`<br>
      Expected: The first student is deleted from the list. Feedback shows the deleted student's details.

   1. Test case: `delete 1,3,5-6`<br>
      Expected: The 1st, 3rd, 5th and 6th students from the original displayed list are deleted once each. The feedback message shows the deleted students' details.

   1. Test case: `delete 0`<br>
      Expected: No student is deleted. Feedback is `The student index provided is invalid.`

   1. Test cases: `delete -1`, `delete +1`, `delete 5-3`, and `delete 2147483648`<br>
      Expected: Each reports `The student index provided is invalid.` No student is changed.

   1. Test cases: `delete`, `delete x`, `delete 1,`, and `delete 1, 3`<br>
      Expected: Each reports `Invalid command format!` followed by delete usage. No student is changed.

   1. Test case: `delete 1,7`<br>
      Expected: Feedback is `The student index provided is invalid.` All six students, including student 1, remain.

1. Deleting from a filtered list
   1. Prerequisites: Run `find n/Alex` against the six sample students; only Alex Yeoh is displayed.

   1. Test case: `delete 2`<br>
      Expected: Feedback is `The student index provided is invalid.` No student is deleted, even though the full student list contains more than one student.

   1. Test case: `delete 1`<br>
      Expected: Alex Yeoh is deleted. The list then shows all five remaining students.

1. Clearing optional fields
   1. Prerequisites: Launch the application with the sample data and run `list`.

   1. Test case: `delete 1 p/ h/`<br>
      Expected: The first student's phone number and Telegram handle are removed. The student is not deleted.

   1. Test case: `delete 1 t/Missing`<br>
      Expected: No student is changed. The status message states that the tag does not exist.

   1. Test case: `delete 1,7 p/`<br>
      Expected: Feedback is `The student index provided is invalid.` Student 1's phone number is unchanged.

   1. Test case: `find n/Alex`, followed by `delete 1 p/`<br>
      Expected: Alex remains displayed without a phone number. The name filter stays active.

   1. Test case: `delete 1 t/ t/Missing`<br>
      Expected: All tags on the first student are cleared; the nonexistent named tag is overridden.

   1. Test case: `delete 1 t/ t/Invalid!`<br>
      Expected: The tag-format error is shown. No tags are cleared.

### Navigating command history

1. Prerequisites: Start a new session and submit `list`, then `find n/Alex`.
   1. Type `delete 1` without submitting, press Up twice, then Down twice.<br>
      Expected: The two submitted commands are recalled in order, then the unsubmitted draft `delete 1` is restored. No student is deleted.

   1. Recall a command, edit its text, navigate away, then move down past the newest command.<br>
      Expected: The edited draft is restored; recalling entries does not replace it.

   1. Submit `delete 0`, then press Up.<br>
      Expected: The index error appears, and the failed command can be recalled.

   1. Press Up beyond the oldest command or Down beyond the restored draft.<br>
      Expected: The input remains unchanged at the boundary.

   1. Close and reopen TutorTrack, then press Up in the empty command box.<br>
      Expected: No previous-session command is recalled.

### Adding students

1. Adding a student with all fields
   1. Test case: `add n/Alice Tan p/91234567 e/alice@example.com h/@alice_tan s/Mathematics g/Sec 3A t/Student`<br>
      Expected: Alice is added. The phone is displayed as `9123 4567`, and all supplied fields are shown.

1. Adding a student without optional fields
   1. Test case: `add n/Bob Lee e/bob@example.com s/Physics g/J1A`<br>
      Expected: Bob is added without a phone number, Telegram handle, or tags; no empty contact labels are shown.

1. Adding a student whose name contains initials or a short abbreviation
   1. Test case: `add n/Aisyah Bte. Ahmad e/aisyah@example.com s/Mathematics g/T11`<br>
      Expected: Aisyah is added with the name stored as `Aisyah Bte. Ahmad`.

   1. Test case: Replace the name above with `John..Tan`, `.John`, or `John.`.<br>
      Expected: No student is added. The status message shows the name constraints.

1. Adding a student whose name contains `D/O` or `S/O`
   1. Test case: `add n/Ravi S/O Kumar e/ravi@example.com s/CS2103T g/T11`<br>
      Expected: Ravi is added with the name `Ravi S/O Kumar` and the subject `CS2103T`.

   1. Test case: `add n/Ravi Kumar e/ravi2@example.com s/CS2103T g/T11 S/O`<br>
      Expected: No student is added. The status message shows the `add` command usage because `S/O` is not part of
      the name.

   1. Test case: `add n/Ali s/o Abu e/ali@example.com s/CS2103T g/T11`<br>
      Expected: No student is added. The status message asks for `D/O` or `S/O` in uppercase.

   1. Test case: `add s/O Level n/Ali Abu e/ali@example.com g/T11`<br>
      Expected: Ali is added with the subject `O Level`. Typing `s/O Level` directly after the name is rejected
      with the same uppercase message.

1. Normalizing subjects and groups, and validating Telegram handles
   1. Test case: `add n/Siti Rahman e/siti@example.com s/CS   2103T g/Sec  3A`, typing several spaces inside the
      subject and the group.<br>
      Expected: Siti is added with the subject `CS 2103T` and the group `Sec 3A`. `find s/cs 2103t` then shows her.

   1. Test case: `add n/Siti Lim e/sitilim@example.com h/siti_lim s/CS2103T g/T11`<br>
      Expected: No student is added. The status message shows the Telegram handle constraints. Using `h/@_siti`
      gives the same message.

1. Rejecting values longer than their maximum length
   1. Test case: `add n/Dan Ong e/dan@example.com s/CS2103T g/T11 t/` followed by 51 letters, for example
      `aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa`.<br>
      Expected: No student is added. The status message states that tags can have at most 50 characters.

1. Rejecting malformed input without changing data
   1. Test case: `add n/Charlie Lim e/charlie@example.com s/Mathematics g/T11 x/typo`<br>
      Expected: No student is added. The status message shows the `add` command usage because `x/` is unknown.

   1. Test case: `add n/Charlie Lim e/charlie@example.com s/Mathematics`<br>
      Expected: No student is added. The status message shows the `add` command usage because `g/` is missing.

   1. Test case: `add n/Charlie Lim e/charlie@example.com s/Mathematics s/Physics g/T11`<br>
      Expected: No student is added. The status message states that multiple values were specified for `s/`.

   1. Test case: After adding Alice Tan above, enter `add n/ALICE TAN e/alice2@example.com s/Physics g/T12`.<br>
      Expected: No student is added. The status message reports that the student already exists, because names are
      compared ignoring case.

### Editing students

1. Editing one student
   1. Prerequisites: Launch the application with the sample data and run `list`.

   1. Test case: `edit 2 n/Betsy Crower t/friends,Graduate`<br>
      Expected: The 2nd student is renamed to `Betsy Crower`, and the tag `friends` is renamed to `Graduate`. Other
      tags are unchanged.

   1. Test case: `edit 1 t/Missing,Graduate`<br>
      Expected: No student is changed. The status message states that the tag does not exist.

   1. Test case: `edit 1 p/`<br>
      Expected: No student is changed. The status message states that edit values cannot be empty.

   1. Test case: `edit 1 n/Ravi s/o Kumar`<br>
      Expected: No student is changed, including the subject. The status message asks for `D/O` or `S/O` in
      uppercase.

1. Editing several students
   1. Test case: `edit 1-3 g/T11 t/Online`<br>
      Expected: The first three students are moved to group `T11` and gain the tag `Online`.

   1. Prerequisites: Run `list`. Among the first three students, the first has the tag `friends` and at least one
      other does not, as with the sample students.

   1. Test case: `edit 1-3 g/T12 t/friends,Graduate`<br>
      Expected: No student is changed, including the first student's group and tags. The status message states
      that the tag does not exist.

   1. Test case: `edit 1-2 p/91234567`<br>
      Expected: No student is changed. The status message states that only subject, group, and tags can be edited
      for multiple students.

### Finding students

1. Finding students with keywords and phrases
   1. Prerequisites: Launch the application with the sample students.

   1. Test case: `find n/"Alex Yeoh",Li`<br>
      Expected: `Alex Yeoh` and `David Li` are displayed because each matches at least one search term.

   1. Test case: `find n/"Alex Yeoh","David Li"`<br>
      Expected: `Alex Yeoh` and `David Li` are displayed.

   1. Incorrect command: `find n/Alex,"Yeoh`<br>
      Expected: The displayed list remains unchanged. The status message shows the `find` command usage.

   1. Incorrect command: `find n/" "`<br>
      Expected: The displayed list remains unchanged. The status message shows the `find` command usage.

1. Finding students using other criteria
   1. Prerequisites: Launch the application with the sample students.

   1. Test case: `find h/@alex_yeoh`<br>
      Expected: Only `Alex Yeoh` is displayed because Telegram handles are matched exactly, ignoring case.

   1. Test case: `find s/General g/General t/friends`<br>
      Expected: `Alex Yeoh` and `Bernice Yu` are displayed because both students match every supplied criterion.

   1. Test case: `find t/friends&colleagues`<br>
      Expected: Only `Bernice Yu` is displayed because she has both tags.

   1. Test case: `find t/friends,colleagues`<br>
      Expected: Students having either the `friends` or `colleagues` tag are displayed.

   1. Incorrect command: `find t/friends,colleagues&family`<br>
      Expected: The displayed list remains unchanged. The status message shows the `find` command usage because
      comma and ampersand separators cannot be mixed.

   1. Incorrect command: `find t/friends t/colleagues`<br>
      Expected: The displayed list remains unchanged. The status message shows the `find` command usage because a
      criterion prefix cannot be repeated.

### Clearing all students

1. Clearing with confirmation
   1. Prerequisites: Launch the application with the sample students and run `list`.
   1. Test case: `clear`<br>
      Expected: No student is deleted. The status message asks you to enter `clear confirm`.

   1. Test case: `clear confirm`<br>
      Expected: No student is deleted. The status message states that no clear confirmation is pending.

   1. Test case: `clear`, then `list`, then `clear confirm`<br>
      Expected: No student is deleted. The status message states that no clear confirmation is pending.

   1. Test case: `clear`, then `clear confirm`<br>
      Expected: All students are deleted.

   1. Test case: `clear`, then enter `clear now`, then `clear confirm`<br>
      Expected: No student is deleted. `clear now` shows the command format error, and `clear confirm` states that no
      clear confirmation is pending.

   1. Test case: `clear`, then `clear confirm`, close TutorTrack, and relaunch it.<br>
      Expected: The displayed student list remains empty, confirming that the cleared data was saved.

### UI interactions

1. Opening and closing help

   1. Test case: Enter `help`.<br>
      Expected: The Help window opens with a command summary and a User Guide URL.

   1. Test case: With the main window focused, press <kbd>F1</kbd>, then choose **Help** > **Help** from the menu bar.<br>
      Expected: Both actions open the Help window, or bring it to the front if it is already open.

   1. Test case: In the Help window, press <kbd>Enter</kbd> or click **Copy URL**, then paste into a text editor.<br>
      Expected: The pasted text is the User Guide URL.

   1. Test case: Close the Help window and type a command without clicking the command box first.<br>
      Expected: The command box has focus and receives the typed command.

1. Navigating command history

   1. Test case: Enter `list`, then `help`, then click the command box and press the Up Arrow twice.<br>
      Expected: The command box first shows `help`, then `list`.

   1. Test case: Type `find n/Alex`, press the Up Arrow, then press the Down Arrow until past the newest command.<br>
      Expected: The command box restores `find n/Alex` as the unsubmitted draft.

1. Copying displayed student details

   1. Test case: Click the copy icon beside a displayed student's email address, Telegram handle, or phone number, then
      paste into a text editor.<br>
      Expected: The pasted text matches the selected detail.

### Saving data

1. Dealing with missing/corrupted data files
   1. Prerequisites: Launch TutorTrack and run `list` so that `data/tutortrack.json` is created, close it, and keep
      a copy of the file. Launching and closing TutorTrack without running a command does not create the file.

   1. Test case: In `data/tutortrack.json`, change one student's `"email"` value to `"invalid"`, save the file,
      and relaunch TutorTrack.<br>
      Expected: An error dialog names the data file and the reason. TutorTrack closes after the dialog is dismissed,
      and the data file is unchanged.

   1. Test case: In `data/tutortrack.json`, replace the contents with `{ "persons" : [ null ] }` and relaunch
      TutorTrack.<br>
      Expected: An error dialog names the data file. TutorTrack closes after the dialog is dismissed, and the data
      file is unchanged.

   1. Test case: Restore the copy. In `data/tutortrack.json`, change one student's `"address"` value, which stores
      the Telegram handle, to `"Blk 123 Clementi"` and relaunch TutorTrack.<br>
      Expected: All students load, and that student has no Telegram handle. The data file is unchanged until a command,
      such as `list`, runs successfully.

   1. Test case: Move `data/tutortrack.json` to another folder and relaunch TutorTrack.<br>
      Expected: TutorTrack starts with sample students. A new `data/tutortrack.json` is created only after a command,
      such as `list`, runs successfully.

   1. Test case: Close TutorTrack, rename `data/tutortrack.json` to `data/addressbook.json`, and relaunch TutorTrack.<br>
      Expected: The same students are shown. A new `data/tutortrack.json` is created and `data/addressbook.json` is kept.
