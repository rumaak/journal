# Journal
Journal desktop application written in Java using JavaFX library.

### Goal
Write a JavaFX application for note taking, a journal. Concrete specification [here](https://github.com/rumaak/journal/wiki/Task-specification).

### Installation, run
- Clone this repository
- Download JavaFX 15 (eg. from [here](https://gluonhq.com/products/javafx/)) for your OS
- Unzip and copy `lib` folder into `external_lib`
    - in the Windows distribution, there is also folder `bin` - copy it too
- Run `ant run` in root directory of this repository
- (Optional) Run `ant doc` to generate `javadoc` documentation

### Usage
A short user guide describing how the application is supposed to be used.

##### General use
- Run `ant run`
- If running for the first time, a directory chooser window will pop up
    - select a directory inside which a journal will be stored (journals are compatible, an existing one can be selected if journal was already used there)
    - to change / remove journal directory location, see below
- On the left side of window a journal tree can be seen, on the right side an editor should show up
- Adding a new group / note
    - select a group under which you wish to add a new group / note
    - click on `add group` / `add note` button above tree view of journal (tooltips can help recognizing the buttons)
    - a new group / note with default name will be created in selected group
- Rename a group / note
    - select a group / note
    - click on `rename` button
    - edit name, press `enter`
- Delete a group / button
    - select a group / note
    - click the `delete` button
- Note editing
    - to start editing a note, select it in journal tree and click into editor
    - write, style a note as you wish
    - add an image if desired using `add image` button
    - when done editing, press ctrl+s or `save` button to save the note

##### Change / remove journal
- navigate to the directory where journal is stored
- delete everything inside the directory (or move it to new location)
- in the local repository clone, delete .config file


### Example
To ilustrate how the journal application is used, let us go over two simple usage examples.

##### Example of existing journal
In the root directory of this repository, there is an example journal already set up (test_journal), select it and click `open`.

TODO add image




##### Example creating new journal
