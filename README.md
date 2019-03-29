# DucView
DucView is a tool for creating and using pyramids, a method for summary content annotation. <br />
A pyramid is a model predicting the distribution of information content in summaries. Various summaries of the
same source material will typically have some content that overlaps, and some content that does not. The pyramid
ranks the importance of each content unit based on the frequency in "wise crowd" or "model" summaries (summaries judged to
be a gold standard). The pyramid can then be used to judge other summaries, called "peer" summaries.

## Requirements
Java 5 or higher.

## Features
Create pyramids and export them in XML. <br />
Annotate and score peer summaries and export them in XML.

## How to use?
First, refer to the [Annotation Instructions](http://personal.psu.edu/rjp49/DUC2006/2006-pyramid-guidelines.html)
for guides on annotating pyramids and peer summaries.<br />

DUCView is an annotation tool that has had two uses: the creation of pyramids from model summaries, and the annotation of peer summaries against an existing pyramid.

To start creating a new pyramid, select File > Pyramid > New. Select either a single text file with several model summaries,
or a folder containing several text files with one model summary in each file.
- Folder was selected: DucView will identify any text file within the selected folder as a model summary and concatenate the files into a single file for you.
- File was selected: The file must contain model summaries delimited by a common symbol, and it must be identified in Options > Document Header RegEx so DucView knows how many summaries are there.


Now you can begin annotating each model summary according to the pyramid annotation guidelines to create the pyramid.
The model summaries appear on the left, and the pyramid on the right pane.
To create an SCU (content unit) highlight text on the left pane and press the "New SCU" button.
If other model summaries have a similar SCU, highlight the SCU in the pyramid and click "Add Contributor" to note the overlap of the SCUs.
An explanation of all buttons is listed in the "Buttons" section.
Annotate as much of the model summaries as possible, and then save the pyramid in an XML format as a .pyr file using File > Pyramid > Save. <br />

To start annotating a peer summary, you must already have a pyramid open. Select File > Peer Annotation > New and
choose a text file containing the peer summary to be annotated. This will open a new view in DucView with three panes instead of two.
The peer summary will be on the left, the pyramid in the middle, and the model summaries on the left.
Highlight text in the peer summary and click the "Add Contributor" button while the pyramid SCU is highlighed in the middle
to match content in the peer summaries with SCUs in the pyramid. If an SCU in the peer summary is not found in the pyramid,
it should still be scored. Select the SCU "All non-matching SCUs go here" and add the contributor.
Once all SCUs in the peer summary have been annotated, save the peer annotation in an XML format as a .pyr file using File > Peer Annotation > Save.
The XML contains the pyramid, the peer annotation, and the score of the peer summary.

### DucView: Pyramid View Diagram
![Pyramid View](Images/pyramid_diagram.png?raw=true "Pyramid View")

### DucView: Peer Annotation View Diagram
![Peer Annotation View](Images/peer_annotation_diagram.png?raw=true "Peer Annotation View")


#### Drop Down Menus:
- File: For starting a new pyramid, either by reading in a text file or a folder containing the model summaries, or for starting a new peer annotation by first loading an existing pyramid, then reading in a new peer summary. For loading, saving, or closing the annotation files you are working on. User can also display score for peer annotations.
- Edit: There are find, undo and redo functions.
- Options: "Text Size" and "Look and Feel" are self-explanatory. During pyramid annotation, SCUs in the right pane can be dragged, either to move to a new location in the tree (e.g., to group similar SCUs together for ease of reference), or to merge two SCUs. See warning for an explanation of "Document Header RegEx," and the regular expression to use.
- Help: About DucView
#### Status Bar:
- A bar at the bottom of the window for displaying important information and errors.
#### Left Pane:
- During pyramid annotation, displays the file of model summaries. During peer annotation, displays the peer summary.
- Warning: When you read in a new text file containing the model summaries, you must select the "Options" drop down menu, then select "Document Header RegEx" and enter a regular expression for the summary separator you find in the *txt file.
- Searchable, using drop down menu "Edit > Find". Note that you can search on the model text, or on the SCU labels.
#### Center Pane:
- This applies only to peer annotation; displays the list of SCU labels along with the SCU weights.
#### Right Pane:
- During pyramid annotation, displays the tree of SCUs created by the annotator.
- During peer annotation, displays the text of the model summaries; when an SCU in the center pane is selected, its contributors are highlighted in the right pane.
#### Buttons:
- New SCU: This applies only to pyramid annotation; after user selects text in the right pane, creates a new SCU in the left pane.
- Add Contributor: This button has two functions during pyramid annotation. It will add a contributor to an existing SCU, after user simultaneously selects text in the right pane, and an SCU in the left pane. For discontinuous contributors, it will add selected text to a selected contributor (select the contributor, not the SCU label to add a discontinuous contributor).
  - During peer annotation, select this button after selecting some text and the SCU label it matches.
- Change Label: This applies only to pyramid annotation; allows user to edit the label
- Set SCU Label: This applies only to pyramid annotation; after selecting a contributor label, hit this button to copy the contributor label to the SCU label.
- Remove: Remove a selected SCU, or selected contributor.
- Order: Orders the list of SCUs by weight (descending), and within each weight, alphabetically. If you have used "Options > Dragging SCU > Moves it under target SCU" to create your own ordering, hitting the "Order" button will override it.
- Collapse: Collapses the tree of SCUs so that only the labels are visible.
- Comment: For user notes on SCUs or contributors; appears in the SCU tree as an asterisk on SCU or contributor labels; visible by mousing over the asterisk.
- < and >: This applies only to peer annotation; cycle through the contributors to the selected SCU label in the model summaries on the right pane.
#### Shortcuts:
- Many buttons and drop down menu items can be accessed using key shortcuts consisting of ALT + KEY, where KEY is the underlined letter on the button or menu item. For instance, ALT + N will work as the "New SCU" button. Ctrl + F, Ctrl + Z, and CTRL + Y can be used to find, undo, and redo, respectively.

## Class documentation
This section contains an overview of the most important classes in the project and their functions. <br />

#### DucView
- Contains the main function and calls all the other classes
- Provides GUI functions using the Swing Java library
- Parses and creates XML files, defines the DTD
- Calculates peer summary scores
#### SCU
- Defines an SCU with an ID, label, and a comment
#### SCUContributor
- Defines an SCU contributor - a portion of the text from a summary that makes up an SCU
- Includes a list of the contributor's SCUContributorParts, which may be non-adjacent in the text
#### SCUContributorPart
- Defines a part of an SCU contributor
- Includes the starting and ending indices of the SCU contributor in the summary, as well as the text
#### SCUTextPane
- Defines the left, center, and right panes of DucView
- Includes functions for displaying and selecting text
#### SCUTree
- Defines a tree for the SCUs in a pyramid or a peer annotation
- Includes functions for obtaining, ordering, comparing, selecting, highlighting, dragging, scrolling, and dropping SCUs
#### SearchDialog
- Enables searching of text and SCU labels
#### ScoreDialog
- Displays the HTML of a summary's score, generated in the DucView class
