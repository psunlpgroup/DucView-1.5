package ducview;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The DucView class creates the main JFrame window used to create pyramids and annotate peer summaries,
 * and calls all other classes in the ducview package.
 */
public class DucView extends javax.swing.JFrame implements java.awt.event.ActionListener, org.xml.sax.ErrorHandler {
    private SCUTextPane pyramidTextPane;
    private SCUTextPane peerTextPane;
    private SCUTextPane pyramidReferenceTextPane;
    protected SCUTree pyramidTree;
    protected SCUTree peerTree;
    private javax.swing.JLabel statusLbl; // Displays messages at bottom of DucView window
    private javax.swing.JButton addBtn;
    private javax.swing.JButton renameBtn;
    private javax.swing.JButton setLabelbtn;
    private javax.swing.JButton removeBtn;
    private javax.swing.JButton orderBtn;
    private javax.swing.JButton addBtn_peer;
    private javax.swing.JButton removeBtn_peer;
    private javax.swing.JButton orderBtn_peer;
    private javax.swing.JButton newBtn;
    private javax.swing.JButton collapseBtn;
    private javax.swing.JButton collapseBtn_peer;
    private javax.swing.JButton commentBtn;
    private javax.swing.JButton commentBtn_peer;
    protected javax.swing.JButton pyramidReferencePrevContributorBtn;
    protected javax.swing.JButton pyramidReferenceNextContributorBtn;
    protected javax.swing.JCheckBoxMenuItem fileShowPeerAnnotationScoreMenuItem;
    private int currentTextSize;
    private String defaultFilePath = System.getProperty("user.dir");
    private String defaultPANFileLocation = null;
    private boolean isPyramidLoaded = false;
    protected boolean isPeerLoaded = false;
    private boolean isPyramidModified = false;
    private boolean isPeerModified = false;
    private java.io.File pyramidFile = null;
    private java.io.File peerFile = null;
    private JMenuItem fileNewPyramidFromTextFileMenuItem;
    private JMenuItem fileNewPyramidFromFolderMenuItem;
    private JMenuItem fileLoadPyramidMenuItem;
    private JMenuItem fileSavePyramidMenuItem;
    private JMenuItem fileSavePyramidAsMenuItem;
    private JMenuItem fileClosePyramidMenuItem;
    private JMenuItem fileNewPeerAnnotationMenuItem;
    private JMenuItem fileSavePeerAnnotationMenuItem;
    private JMenuItem fileSavePeerAnnotationAsMenuItem;
    private JMenuItem fileClosePeerAnnotationMenuItem;
    private JMenuItem editUndoMenuItem;
    private JMenuItem editRedoMenuItem;
    private JMenuItem documentStartRegexMenuItem;
    private javax.swing.JPanel mainPanel;
    private static final String eol = System.getProperty("line.separator");
    private static final String titleString = "DucView v. 1.5";
    private static final String helpAbout = "v. 1.5 (c) 2019\nPennsylvania State University\nnlp.at.pennstate@gmail.com\n--------------------\nv. 1.4 (c) 2005\nColumbia University\nss1792@cs.columbia.edu\n--------------------\nFor help on using DucView, visit:\nhttp://www1.cs.columbia.edu/~ani/DUC2005/DUCView/\n--------------------\nTo view annotation guidelines, visit:\nhttp://personal.psu.edu/rjp49/DUC2006/2006-pyramid-guidelines.html";
    private static final String[] XMLScoreTags = {"totalscu", "uniquescu", "scusNotInPyr", "totalWeight", "maxScoreTotalscu", "qualityScore"}; // XML tags for peer annotation score
    private static final String[] optionalXMLScoreTags = {"averagescu", "maxScoreAveragescu", "coverageScore", "comprehensiveScore"}; // XML tags that only are only included when startDocumentPatternStr is not null
    protected int[] pyramidReferenceTextPaneHighlightIndexes = null;
    protected int currentPyramidReferenceTextPaneHighlightIndex = 0;
    protected ScoreDialog scoreDlg;
    private UndoController pyramidUndoController = new UndoController();
    private UndoController peerUndoController = new UndoController();
    private SearchDialog searchDialog;
    private String pyramidInputTextFile = null;
    private String peerInputTextFile = null;
    private javax.swing.JRadioButtonMenuItem dragScuMoveMenuItem; // Determines whether dragging SCUs moves it under the target SCU or merges it with the target SCU
    private javax.swing.JRadioButtonMenuItem dragScuMergeMenuItem;
    protected boolean draggingScuMove = true;
    String startDocumentPatternStr = null; // Regular expression that delimits model summaries
    int[] startDocumentIndexes = null; // Indices at which model summaries begin and end

    public DucView() {
        super(titleString);
        setDefaultCloseOperation(0);
        addWindowListener(new DucViewWindowAdapter(this));
        setResizable(true);

        javax.swing.JPanel contentPane = (javax.swing.JPanel) getContentPane();
        contentPane.setLayout(new java.awt.BorderLayout());
        this.currentTextSize = new javax.swing.JTextPane().getFont().getSize();


        contentPane.registerKeyboardAction(this, "find", javax.swing.KeyStroke.getKeyStroke(70, 2), 2); // Ctrl + F shortcut
        contentPane.registerKeyboardAction(this, "undo", javax.swing.KeyStroke.getKeyStroke(90, 2), 2); // Ctrl + Z shortcut
        contentPane.registerKeyboardAction(this, "redo", javax.swing.KeyStroke.getKeyStroke(89, 2), 2); // Ctrl + Y shortcut

        this.searchDialog = new SearchDialog(this);

        setJMenuBar(createMenuBar());

        this.mainPanel = new javax.swing.JPanel(new java.awt.CardLayout());


        this.pyramidTextPane = new SCUTextPane();

        javax.swing.JPanel pyramidSecondPanel = new javax.swing.JPanel();
        pyramidSecondPanel.setPreferredSize(new java.awt.Dimension(250, 500));
        pyramidSecondPanel.setLayout(new javax.swing.BoxLayout(pyramidSecondPanel, 1));


        javax.swing.JPanel pyramidButtonsPanel = new javax.swing.JPanel(new java.awt.GridLayout(2, 4));

        pyramidButtonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.newBtn = new javax.swing.JButton("New SCU");
        this.newBtn.setMnemonic('n');
        this.newBtn.setActionCommand("new");
        this.newBtn.addActionListener(this);
        this.newBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.newBtn);

        this.addBtn = new javax.swing.JButton("Add Contributor");
        this.addBtn.setMnemonic('a');
        this.addBtn.setActionCommand("add");
        this.addBtn.addActionListener(this);
        this.addBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.addBtn);

        this.renameBtn = new javax.swing.JButton("Change Label");
        this.renameBtn.setMnemonic('c');
        this.renameBtn.setActionCommand("rename");
        this.renameBtn.addActionListener(this);
        this.renameBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.renameBtn);

        this.setLabelbtn = new javax.swing.JButton("Set SCU Label");
        this.setLabelbtn.setMnemonic('s');
        this.setLabelbtn.setActionCommand("setLabel");
        this.setLabelbtn.addActionListener(this);
        this.setLabelbtn.setEnabled(false);
        pyramidButtonsPanel.add(this.setLabelbtn);

        this.removeBtn = new javax.swing.JButton("Remove");
        this.removeBtn.setMnemonic('r');
        this.removeBtn.setActionCommand("remove");
        this.removeBtn.addActionListener(this);
        this.removeBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.removeBtn);

        this.orderBtn = new javax.swing.JButton("Order");
        this.orderBtn.setMnemonic('o');
        this.orderBtn.setActionCommand("order");
        this.orderBtn.addActionListener(this);
        this.orderBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.orderBtn);

        this.collapseBtn = new javax.swing.JButton("Collapse");
        this.collapseBtn.setMnemonic('l');
        this.collapseBtn.setActionCommand("collapse");
        this.collapseBtn.addActionListener(this);
        this.collapseBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.collapseBtn);

        this.commentBtn = new javax.swing.JButton("Comment");
        this.commentBtn.setMnemonic('m');
        this.commentBtn.setActionCommand("comment");
        this.commentBtn.addActionListener(this);
        this.commentBtn.setEnabled(false);
        pyramidButtonsPanel.add(this.commentBtn);

        pyramidButtonsPanel.setMaximumSize(pyramidButtonsPanel.getPreferredSize());

        pyramidSecondPanel.add(pyramidButtonsPanel);

        this.pyramidTree = new SCUTree(this);
        this.pyramidTree.setSCUTextPane(this.pyramidTextPane);
        this.pyramidTextPane.setTree(this.pyramidTree);
        pyramidSecondPanel.add(new javax.swing.JScrollPane(this.pyramidTree));

        javax.swing.JSplitPane pyramidSplitPane = new javax.swing.JSplitPane(1, new javax.swing.JScrollPane(this.pyramidTextPane), pyramidSecondPanel);


        pyramidSplitPane.setResizeWeight(0.5D);

        this.mainPanel.add(pyramidSplitPane, "pyramid");


        this.peerTextPane = new SCUTextPane();

        javax.swing.JPanel peerSecondPanel = new javax.swing.JPanel();
        peerSecondPanel.setPreferredSize(new java.awt.Dimension(250, 500));
        peerSecondPanel.setLayout(new javax.swing.BoxLayout(peerSecondPanel, 1));

        javax.swing.Box peerButtonsPanel = javax.swing.Box.createHorizontalBox();

        peerButtonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.addBtn_peer = new javax.swing.JButton("Add Contributor");
        this.addBtn_peer.setMnemonic('a');
        this.addBtn_peer.setActionCommand("add");
        this.addBtn_peer.addActionListener(this);
        this.addBtn_peer.setEnabled(false);
        peerButtonsPanel.add(this.addBtn_peer);

        this.removeBtn_peer = new javax.swing.JButton("Remove");
        this.removeBtn_peer.setMnemonic('r');
        this.removeBtn_peer.setActionCommand("remove");
        this.removeBtn_peer.addActionListener(this);
        this.removeBtn_peer.setEnabled(false);
        peerButtonsPanel.add(this.removeBtn_peer);

        this.orderBtn_peer = new javax.swing.JButton("Order");
        this.orderBtn_peer.setMnemonic('o');
        this.orderBtn_peer.setActionCommand("order");
        this.orderBtn_peer.addActionListener(this);
        peerButtonsPanel.add(this.orderBtn_peer);

        this.collapseBtn_peer = new javax.swing.JButton("Collapse");
        this.collapseBtn_peer.setMnemonic('l');
        this.collapseBtn_peer.setActionCommand("order");
        this.collapseBtn_peer.addActionListener(this);
        peerButtonsPanel.add(this.collapseBtn_peer);

        this.commentBtn_peer = new javax.swing.JButton("Comment");
        this.commentBtn_peer.setMnemonic('m');
        this.commentBtn_peer.setActionCommand("comment");
        this.commentBtn_peer.addActionListener(this);
        this.commentBtn_peer.setEnabled(false);
        peerButtonsPanel.add(this.commentBtn_peer);

        peerSecondPanel.add(peerButtonsPanel);

        this.peerTree = new SCUTree(this);
        this.peerTree.setSCUTextPane(this.peerTextPane);
        this.peerTextPane.setTree(this.peerTree);

        peerSecondPanel.add(new javax.swing.JScrollPane(this.peerTree));

        javax.swing.JPanel peerThirdPanel = new javax.swing.JPanel();
        peerThirdPanel.setPreferredSize(new java.awt.Dimension(250, 500));
        peerThirdPanel.setLayout(new javax.swing.BoxLayout(peerThirdPanel, 1));

        javax.swing.Box pyramidReferenceButtonsPanel = javax.swing.Box.createHorizontalBox();

        pyramidReferenceButtonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));


        this.pyramidReferencePrevContributorBtn = new javax.swing.JButton("   <   ");
        this.pyramidReferencePrevContributorBtn.setMnemonic('<');
        this.pyramidReferencePrevContributorBtn.setActionCommand("pyramidReferencePrevContributor");

        this.pyramidReferencePrevContributorBtn.addActionListener(this);
        this.pyramidReferencePrevContributorBtn.setEnabled(false);
        pyramidReferenceButtonsPanel.add(this.pyramidReferencePrevContributorBtn);

        this.pyramidReferenceNextContributorBtn = new javax.swing.JButton("   >   ");
        this.pyramidReferenceNextContributorBtn.setMnemonic('>');
        this.pyramidReferenceNextContributorBtn.setActionCommand("pyramidReferenceNextContributor");

        this.pyramidReferenceNextContributorBtn.addActionListener(this);
        this.pyramidReferenceNextContributorBtn.setEnabled(false);
        pyramidReferenceButtonsPanel.add(this.pyramidReferenceNextContributorBtn);

        peerThirdPanel.add(pyramidReferenceButtonsPanel);

        this.pyramidReferenceTextPane = new SCUTextPane();
        this.pyramidReferenceTextPane.setHighlighter(null);
        peerThirdPanel.add(new javax.swing.JScrollPane(this.pyramidReferenceTextPane));
        this.peerTree.setPyramidReferenceTextPane(this.pyramidReferenceTextPane);

        javax.swing.JSplitPane peerSplitPane1 = new javax.swing.JSplitPane(1, peerSecondPanel, peerThirdPanel);

        peerSplitPane1.setResizeWeight(0.5D);

        javax.swing.JSplitPane peerSplitPane2 = new javax.swing.JSplitPane(1, new javax.swing.JScrollPane(this.peerTextPane), peerSplitPane1);


        peerSplitPane2.setResizeWeight(0.5D);

        this.mainPanel.add(peerSplitPane2, "peer");

        contentPane.add(this.mainPanel, "Center");

        this.statusLbl = new javax.swing.JLabel("Ready");
        this.statusLbl.setBorder(javax.swing.BorderFactory.createBevelBorder(1));
        this.statusLbl.setPreferredSize(new Dimension(1, 24));
        this.statusLbl.setFont(this.statusLbl.getFont().deriveFont(14.0f));
        this.statusLbl.setForeground(new Color(0,0,153));
        contentPane.add(this.statusLbl, "South");

        pack();


        peerSplitPane2.setDividerLocation(Math.round((peerSplitPane2.getSize().width - peerSplitPane2.getInsets().left - peerSplitPane2.getInsets().right) / 3 + peerSplitPane2.getInsets().left));


        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2, getWidth(), getHeight());


        this.scoreDlg = new ScoreDialog(this);
    }

    public static void main(String[] args) throws Exception {
        DucView ducView = new DucView();

        if (args.length > 0) {

            for (int i = 0; i < args.length; i++) {
                try {
                    if (i > 0) {
                        System.out.println();
                    }
                    System.out.println("-------------------\n" + args[i] + "\n-------------------\n");

                    org.w3c.dom.Document doc = ducView.makeDocument(new java.io.File(args[i]));
                    ducView.loadTree((org.w3c.dom.Element) doc.getElementsByTagName("pyramid").item(0), false);
                    ducView.loadTree((org.w3c.dom.Element) doc.getElementsByTagName("annotation").item(0), true);
                    ducView.isPyramidLoaded = true;
                    ducView.isPeerLoaded = true;
                    String regexStr = null;
                    try {
                        regexStr = doc.getElementsByTagName("startDocumentRegEx").item(0).getFirstChild().getNodeValue();
                    } catch (NullPointerException ex) {
                    }
                    if (regexStr != null) {
                        ducView.initializeStartDocumentIndexes(regexStr);
                    }
                    System.out.println(ducView.getScore().replaceAll("<.*?>", ""));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ducView.dispose();
            }

        } else {
            ducView.setVisible(true);
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand().equals("helpAbout")) {
            JTextArea help = new JTextArea(helpAbout);
            help.setEditable(false);
            help.setOpaque(false);
            javax.swing.JOptionPane.showMessageDialog(this, help, "About DucView v. 1.5", 1);

        } else if (e.getActionCommand().equals("fileNewPyramidFromTextFile")) {

            if ((!this.isPyramidModified) || (saveModifiedPyramid())) {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(this.defaultFilePath);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogTitle("Choose the initial text file");
                if (chooser.showOpenDialog(this) == 0) {
                    try {
                        if (chooser.getSelectedFile().isFile()) {
                            this.pyramidInputTextFile = chooser.getSelectedFile().getName();
                            this.pyramidTextPane.loadFile(chooser.getSelectedFile());

                            this.startDocumentPatternStr = null;
                            this.startDocumentIndexes = null;

                            this.pyramidTree.reset();
                            this.setTitle(titleString + " - Pyramid: " + chooser.getSelectedFile());
                            msg("Loaded file " + chooser.getSelectedFile());
                            this.defaultFilePath = chooser.getSelectedFile().getCanonicalPath();
                            this.pyramidFile = null;
                            setPyramidLoaded(true);
                        }
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        msg(ex.getMessage());
                    }
                }
            }
        } else if (e.getActionCommand().equals("fileNewPyramidFromFolder")) {

            if ((!this.isPyramidModified) || (saveModifiedPyramid())) {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(this.defaultFilePath);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.addChoosableFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory()) { return true; }
                        else { return false; }
                    }
                    public String getDescription() {
                        return "Folders";
                    }
                });
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setDialogTitle("Choose a folder containing text files");
                if (chooser.showOpenDialog(this) == 0) {
                    try {
                        File[] txtFiles = chooser.getSelectedFile().listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String filename) {
                                return filename.endsWith(".txt");
                            }
                        });
                        if (txtFiles.length > 0) {
                            StringBuffer buffer = new StringBuffer();
                            String line;
                            for (File file : txtFiles) {
                                buffer.append("-------------------- < " + file.getName() + " > -----");

                                // Make the header width equal based on file name length
                                int fileNameWidth = SwingUtilities.computeStringWidth(pyramidTextPane.getFontMetrics(pyramidTextPane.getFont()), file.getName());
                                int remainingWidth = SwingUtilities.computeStringWidth(pyramidTextPane.getFontMetrics(pyramidTextPane.getFont()), "-----------------------------------") - fileNameWidth;
                                int dashWidth = SwingUtilities.computeStringWidth(pyramidTextPane.getFontMetrics(pyramidTextPane.getFont()), "-");
                                for (int i = 0; i < remainingWidth; i += dashWidth) {
                                    buffer.append("-");
                                }

                                buffer.append("\n\n");
                                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
                                while ((line = reader.readLine()) != null) {
                                    buffer.append(line);
                                    buffer.append("\n");
                                }
                                buffer.append("\n\n");
                            }
                            this.pyramidInputTextFile = chooser.getSelectedFile().getName();
                            this.pyramidTextPane.loadText(buffer.toString().replaceAll("\n\n\n\n+-----", "\n\n\n-----")); // Get rid of any new lines at ends of .txt files

                            this.startDocumentPatternStr = "-----+ <.*> -----+";
                            initializeStartDocumentIndexes(this.startDocumentPatternStr);

                            this.pyramidTree.reset();
                            this.setTitle(titleString + " - Pyramid: " + chooser.getSelectedFile());
                            msg("Loaded folder " + chooser.getSelectedFile() + ". Successfully loaded " + this.startDocumentIndexes.length + " *.txt files.");
                            this.defaultFilePath = chooser.getSelectedFile().getCanonicalPath();
                            this.pyramidFile = null;
                            setPyramidLoaded(true);
                        } else {
                            showError("Error", "Error: no *.txt files found in " + chooser.getSelectedFile().getCanonicalPath() + ".");
                            msg("No *.txt files found in " + chooser.getSelectedFile().getCanonicalPath() + ".");
                        }
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        msg(ex.getMessage());
                    }
                }
            }
        }
        if (e.getActionCommand().equals("fileNewPeerAnnotation")) {

            if ((!this.isPeerModified) || (saveModifiedPeer())) {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(this.defaultFilePath);
                chooser.setDialogTitle("Choose the initial text file");
                if (chooser.showOpenDialog(this) == 0) {
                    try {
                        this.peerInputTextFile = chooser.getSelectedFile().getName();
                        this.peerTextPane.loadFile(chooser.getSelectedFile());
                        this.peerTree.reset();
                        this.setTitle(titleString + " - Peer annotation: " + chooser.getSelectedFile());
                        msg("Loaded file " + chooser.getSelectedFile());
                        this.defaultFilePath = chooser.getSelectedFile().getCanonicalPath();
                        this.peerFile = null;
                        java.util.Enumeration scuNodeEnum = this.pyramidTree.getRootNode().children();
                        while (scuNodeEnum.hasMoreElements()) {
                            javax.swing.tree.DefaultMutableTreeNode origSCUNode = (javax.swing.tree.DefaultMutableTreeNode) scuNodeEnum.nextElement();

                            SCU origScu = (SCU) origSCUNode.getUserObject();
                            int numContributors = origSCUNode.getChildCount();


                            this.peerTree.insertNodeInto(new javax.swing.tree.DefaultMutableTreeNode(new SCU(origScu.getId(), "(" + numContributors + ") " + origScu.toString())), this.peerTree.getRootNode());
                        }


                        this.peerTree.insertNodeInto(new javax.swing.tree.DefaultMutableTreeNode(new SCU(0, "All non-matching SCUs go here")), this.peerTree.getRootNode());

                        this.peerTree.order();

                        if (this.peerTree.getRootNode().getChildCount() > 1) {
                            this.orderBtn_peer.setEnabled(true);
                        } else {
                            this.orderBtn_peer.setEnabled(false);
                        }

                        this.pyramidReferenceTextPane.loadText(this.pyramidTextPane.getText());
                        setPeerLoaded(true);
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        msg(ex.getMessage());
                    }
                }
            }
        } else if (e.getActionCommand().equals("fileLoadPyramid")) {

            if ((!this.isPyramidModified) || (saveModifiedPyramid())) {
                javax.swing.JFileChooser chooser = new DucViewFileChooser(this.defaultFilePath, false, true);
                if (chooser.showOpenDialog(this) == 0) {
                    try {
                        this.defaultFilePath = chooser.getSelectedFile().getCanonicalPath();
                        org.w3c.dom.Document doc = makeDocument(chooser.getSelectedFile());
                        loadTree((org.w3c.dom.Element) doc.getElementsByTagName("pyramid").item(0), false);
                        this.pyramidFile = chooser.getSelectedFile();

                        String regexStr = null;
                        try {
                            regexStr = doc.getElementsByTagName("startDocumentRegEx").item(0).getFirstChild().getNodeValue();
                        } catch (NullPointerException ex) {
                        }
                        if ((regexStr == null) || (initializeStartDocumentIndexes(regexStr))) {
                            setPyramidLoaded(true);
                            this.setTitle(titleString + " - Pyramid: " + chooser.getSelectedFile());
                            msg("Loaded " + chooser.getSelectedFile());
                        } else {
                            this.pyramidTextPane.setText("");
                            this.pyramidTree.reset();
                            setPyramidLoaded(false);
                            msg("Error loading " + chooser.getSelectedFile());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        msg(ex.getMessage());
                    }
                }
            }
        } else if (e.getActionCommand().equals("fileLoadPeerAnnotation")) {

            if (((!this.isPeerModified) || (saveModifiedPeer())) && ((!this.isPyramidModified) || (saveModifiedPyramid()))) {

                javax.swing.JFileChooser chooser = new DucViewFileChooser(this.defaultFilePath, false, false);
                if (chooser.showOpenDialog(this) == 0) {
                    try {
                        this.defaultFilePath = chooser.getSelectedFile().getCanonicalPath();
                        org.w3c.dom.Document doc = makeDocument(chooser.getSelectedFile());

                        loadTree((org.w3c.dom.Element) doc.getElementsByTagName("pyramid").item(0), false);
                        loadTree((org.w3c.dom.Element) doc.getElementsByTagName("annotation").item(0), true);
                        this.pyramidReferenceTextPane.loadText(this.pyramidTextPane.getText());
                        this.peerFile = chooser.getSelectedFile();

                        String regexStr = null;
                        try {
                            regexStr = doc.getElementsByTagName("startDocumentRegEx").item(0).getFirstChild().getNodeValue();
                        } catch (NullPointerException ex) {
                        }
                        if ((regexStr == null) || (initializeStartDocumentIndexes(regexStr))) {
                            setPyramidLoaded(true);
                            setPeerLoaded(true);
                            this.setTitle(titleString + " - Peer annotation: " + chooser.getSelectedFile());
                            msg("Loaded " + chooser.getSelectedFile());
                        } else {
                            this.pyramidTextPane.setText("");
                            this.pyramidReferenceTextPane.setText("");
                            this.peerTextPane.setText("");
                            this.pyramidTree.reset();
                            this.peerTree.reset();
                            setPeerLoaded(false);
                            setPyramidLoaded(false);
                            msg("Error loading " + chooser.getSelectedFile());
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        msg(ex.getMessage());
                    }
                }
            }
        } else if (e.getActionCommand().equals("fileSavePyramid")) {
            if (this.pyramidFile == null) {
                savePyramid(true);
            } else {
                savePyramid(false);
            }
        } else if (e.getActionCommand().equals("fileSavePyramidAs")) {
            savePyramid(true);
        } else if (e.getActionCommand().equals("fileSavePeerAnnotation")) {
            if (this.peerFile == null) {
                savePeer(true);
            } else {
                savePeer(false);
            }
        } else if (e.getActionCommand().equals("fileSavePeerAnnotationAs")) {
            savePeer(true);
        } else if (e.getActionCommand().equals("fileClosePyramid")) {
            if ((!this.isPyramidModified) || (saveModifiedPyramid())) {
                this.pyramidTextPane.loadText("");
                this.pyramidTree.reset();
                setPyramidLoaded(false);
                setTitle(titleString);
                msg("Closed " + this.defaultFilePath);
            }
        } else if (e.getActionCommand().equals("fileShowPeerAnnotationScore")) {
            this.scoreDlg.setVisible(this.fileShowPeerAnnotationScoreMenuItem.isSelected());
        } else if (e.getActionCommand().equals("fileClosePeerAnnotation")) {
            if ((!this.isPeerModified) || (saveModifiedPeer())) {
                this.peerTextPane.loadText("");
                this.peerTree.reset();
                setPeerLoaded(false);
                this.setTitle(titleString + " - Pyramid from: " + this.defaultFilePath);
                msg("Closed " + this.defaultFilePath);
            }
        } else if (e.getActionCommand().equals("pyramidReferenceNextContributor")) {
            this.pyramidReferenceTextPane.showText(this.pyramidReferenceTextPaneHighlightIndexes[(++this.currentPyramidReferenceTextPaneHighlightIndex)]);


            if (this.currentPyramidReferenceTextPaneHighlightIndex + 1 == this.pyramidReferenceTextPaneHighlightIndexes.length) {

                this.pyramidReferenceNextContributorBtn.setEnabled(false);
            }
            if (this.currentPyramidReferenceTextPaneHighlightIndex > 0) {
                this.pyramidReferencePrevContributorBtn.setEnabled(true);
            }
        } else if (e.getActionCommand().equals("pyramidReferencePrevContributor")) {
            this.pyramidReferenceTextPane.showText(this.pyramidReferenceTextPaneHighlightIndexes[(--this.currentPyramidReferenceTextPaneHighlightIndex)]);


            if (this.currentPyramidReferenceTextPaneHighlightIndex + 1 < this.pyramidReferenceTextPaneHighlightIndexes.length) {

                this.pyramidReferenceNextContributorBtn.setEnabled(true);
            }
            if (this.currentPyramidReferenceTextPaneHighlightIndex == 0) {
                this.pyramidReferencePrevContributorBtn.setEnabled(false);
            }
        } else if (e.getActionCommand().equals("new")) {
            if ((this.pyramidTextPane.getSelectedText() != null) && (this.pyramidTextPane.getSelectedText().length() > 0)) {

                if ((this.startDocumentIndexes != null) && (this.pyramidTextPane.getSelectionStartIndex() < this.startDocumentIndexes[0])) {
                    showError("Not in document", "The selection starts before the beginning of the first document");
                    return;
                }

                msg("Creating new SCU \"" + this.pyramidTextPane.getSelectedText() + "\"");


                javax.swing.tree.DefaultMutableTreeNode rootNode = this.pyramidTree.getRootNode();
                javax.swing.tree.DefaultMutableTreeNode scuNode = new javax.swing.tree.DefaultMutableTreeNode(new SCU(this.pyramidTextPane.getSelectedText()));

                this.pyramidTree.insertNodeInto(scuNode, rootNode);
                this.pyramidTree.setSelectionPath(new javax.swing.tree.TreePath(scuNode.getPath()));


                javax.swing.tree.DefaultMutableTreeNode contributorNode = new javax.swing.tree.DefaultMutableTreeNode(new SCUContributor(new SCUContributorPart(this.pyramidTextPane.getSelectionStartIndex(), this.pyramidTextPane.getSelectionEndIndex(), this.pyramidTextPane.getSelectedText())));


                this.pyramidTree.insertNodeInto(contributorNode, scuNode);
                this.pyramidTextPane.modifyTextSelection(this.pyramidTextPane.getSelectionStartIndex(), this.pyramidTextPane.getSelectionEndIndex(), true);

                setPyramidModified(true);
                if (rootNode.getChildCount() > 1) {
                    this.orderBtn.setEnabled(true);
                }
                if (rootNode.getChildCount() > 0) {
                    this.collapseBtn.setEnabled(true);
                }


            } else {
                javax.swing.tree.DefaultMutableTreeNode selectedNode = (javax.swing.tree.DefaultMutableTreeNode) this.pyramidTree.getLastSelectedPathComponent();

                if ((selectedNode != null) && (selectedNode.getLevel() == 2) && (selectedNode.getSiblingCount() > 1)) {

                    this.pyramidTree.removeNodeFromParent(selectedNode);
                    javax.swing.tree.DefaultMutableTreeNode newSCUNode = new javax.swing.tree.DefaultMutableTreeNode(new SCU(selectedNode.toString()));

                    this.pyramidTree.insertNodeInto(newSCUNode, this.pyramidTree.getRootNode());
                    this.pyramidTree.insertNodeInto(selectedNode, newSCUNode);
                    this.pyramidTree.expandTree();
                    this.pyramidTree.setSelectionPath(new javax.swing.tree.TreePath(newSCUNode.getPath()));
                } else {
                    showError("No text selected", "You must select some text (or an SCU contributor) before creating an SCU");
                }
            }
        } else if (e.getActionCommand().equals("add")) {
            SCUTree tree = getTree();
            SCUTextPane textPane = getTextPane();

            if ((textPane.getSelectedText() != null) && (textPane.getSelectedText().length() > 0)) {

                javax.swing.tree.DefaultMutableTreeNode selectedNode = (javax.swing.tree.DefaultMutableTreeNode) tree.getLastSelectedPathComponent();


                if (selectedNode.getLevel() == 1) {


                    if ((!this.isPeerLoaded) && (this.startDocumentIndexes != null)) {
                        int[] startDocumentIndexesTmp = new int[this.startDocumentIndexes.length + 1];
                        for (int i = 0; i < this.startDocumentIndexes.length; i++)
                            startDocumentIndexesTmp[i] = this.startDocumentIndexes[i];
                        startDocumentIndexesTmp[(startDocumentIndexesTmp.length - 1)] = Integer.MAX_VALUE;
                        int selectionStartIndex = textPane.getSelectionStartIndex();
                        if (selectionStartIndex < startDocumentIndexesTmp[0]) {
                            showError("Not in document", "The text selection starts before the beginning of the first document");
                            return;
                        }
                        for (int i = 0; i < startDocumentIndexesTmp.length - 1; i++) {
                            if ((selectionStartIndex >= startDocumentIndexesTmp[i]) && (selectionStartIndex < startDocumentIndexesTmp[(i + 1)])) {


                                java.util.Enumeration scuContribuorEnum = selectedNode.children();
                                while (scuContribuorEnum.hasMoreElements()) {
                                    SCUContributor contributor = (SCUContributor) ((javax.swing.tree.DefaultMutableTreeNode) scuContribuorEnum.nextElement()).getUserObject();


                                    java.util.Iterator scuContribuorPartEnum = contributor.elements();

                                    while (scuContribuorPartEnum.hasNext()) {
                                        SCUContributorPart scuContribuorPart = (SCUContributorPart) scuContribuorPartEnum.next();

                                        int partStartIndex = scuContribuorPart.getStartIndex();
                                        if ((partStartIndex >= startDocumentIndexesTmp[i]) && (partStartIndex < startDocumentIndexesTmp[(i + 1)])) {

                                            showError("Multiple contributor per SCU per document", "The current SCU already has a contributor from the selected document:\n" + scuContribuorPart.getText());


                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }


                    msg("Adding SCU contributor \"" + textPane.getSelectedText() + "\" to SCU \"" + selectedNode.toString() + "\"");


                    javax.swing.tree.DefaultMutableTreeNode contributorNode = new javax.swing.tree.DefaultMutableTreeNode(new SCUContributor(new SCUContributorPart(textPane.getSelectionStartIndex(), textPane.getSelectionEndIndex(), textPane.getSelectedText())));


                    tree.insertNodeInto(contributorNode, selectedNode);
                    textPane.modifyTextSelection(textPane.getSelectionStartIndex(), textPane.getSelectionEndIndex(), true);

                } else if (selectedNode.getLevel() == 2) {

                    msg("Adding SCU contributor part \"" + textPane.getSelectedText() + "\" to SCU contributor \"" + selectedNode.toString() + "\" from SCU \"" + selectedNode.getParent().toString() + "\"");


                    SCUContributor scuContributor = (SCUContributor) selectedNode.getUserObject();
                    scuContributor.add(new SCUContributorPart(textPane.getSelectionStartIndex(), textPane.getSelectionEndIndex(), textPane.getSelectedText()));


                    textPane.modifyTextSelection(textPane.getSelectionStartIndex(), textPane.getSelectionEndIndex(), true);


                    if (scuContributor.getNumParts() == 2) {
                        tree.insertNodeInto(new javax.swing.tree.DefaultMutableTreeNode(scuContributor.getSCUContributorPart(0)), selectedNode);


                        tree.insertNodeInto(new javax.swing.tree.DefaultMutableTreeNode(scuContributor.getSCUContributorPart(1)), selectedNode);

                    } else {
                        tree.insertNodeInto(new javax.swing.tree.DefaultMutableTreeNode(scuContributor.getSCUContributorPart(scuContributor.getNumParts() - 1)), selectedNode);
                    }
                }


                tree.valueChanged(null);
                if (this.isPeerLoaded) {
                    setPeerModified(true);
                    this.scoreDlg.setText(getScore());
                    this.orderBtn_peer.setEnabled(tree.getRootNode().getChildCount() > 1);
                    this.collapseBtn_peer.setEnabled(tree.getRootNode().getChildCount() > 0);
                } else {
                    setPyramidModified(true);
                    this.orderBtn.setEnabled(tree.getRootNode().getChildCount() > 1);
                    this.collapseBtn.setEnabled(tree.getRootNode().getChildCount() > 0);
                }

            } else {
                showError("No text selected", "You must select some text before adding a contributor");
            }
        } else if (e.getActionCommand().equals("rename")) {

            javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) this.pyramidTree.getLastSelectedPathComponent();

            SCU scu = (SCU) node.getUserObject();
            String scuLabel = scu.toString();
            String newSCULabel = (String) javax.swing.JOptionPane.showInputDialog(this, "Enter the label for the selected SCU", "Rename SCU", -1, null, null, scuLabel);


            if ((newSCULabel != null) && (newSCULabel.trim().length() > 0)) {
                scu.setLabel(newSCULabel);
                this.pyramidTree.nodeChanged(node);
                setPyramidModified(true);
            }
        } else if (e.getActionCommand().equals("setLabel")) {
            javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) this.pyramidTree.getLastSelectedPathComponent();

            SCUContributor scuContributor = (SCUContributor) node.getUserObject();
            SCU scu = (SCU) ((javax.swing.tree.DefaultMutableTreeNode) node.getParent()).getUserObject();
            scu.setLabel(scuContributor.toString().replaceAll("\\.\\.\\.", " "));
            this.pyramidTree.nodeChanged(node);
            setPyramidModified(true);
        } else if (e.getActionCommand().equals("remove")) {
            SCUTree tree = getTree();
            SCUTextPane textPane = getTextPane();

            javax.swing.tree.DefaultMutableTreeNode selectedNode = (javax.swing.tree.DefaultMutableTreeNode) tree.getLastSelectedPathComponent();


            if (selectedNode.getLevel() == 1) {


                tree.removeNodeFromParent(selectedNode);
                java.util.Enumeration nodeEnum = selectedNode.children();
                while (nodeEnum.hasMoreElements()) {
                    javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) nodeEnum.nextElement();
                    java.util.Iterator iterator = ((SCUContributor) node.getUserObject()).elements();
                    while (iterator.hasNext()) {
                        SCUContributorPart scuContributorPart = (SCUContributorPart) iterator.next();
                        textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), false);
                    }

                }
            } else if (selectedNode.getLevel() == 2) {

                if ((selectedNode.getSiblingCount() == 1) && (!this.isPeerLoaded)) {


                    tree.removeNodeFromParent((javax.swing.tree.DefaultMutableTreeNode) selectedNode.getParent());

                } else {
                    tree.removeNodeFromParent(selectedNode);
                }
                java.util.Iterator iterator = ((SCUContributor) selectedNode.getUserObject()).elements();
                while (iterator.hasNext()) {
                    SCUContributorPart scuContributorPart = (SCUContributorPart) iterator.next();
                    textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), false);
                }


            } else {
                javax.swing.tree.DefaultMutableTreeNode parent = (javax.swing.tree.DefaultMutableTreeNode) selectedNode.getParent();

                SCUContributorPart scuContributorPart = (SCUContributorPart) selectedNode.getUserObject();

                ((SCUContributor) parent.getUserObject()).removeSCUContributorPart(scuContributorPart);

                textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), false);

                tree.removeNodeFromParent(selectedNode);

                if (((SCUContributor) parent.getUserObject()).getNumParts() == 1) {
                    tree.removeNodeFromParent((javax.swing.tree.DefaultMutableTreeNode) parent.getChildAt(0));
                }
            }


            if (this.isPeerLoaded) {
                setPeerModified(true);
                this.scoreDlg.setText(getScore());
                this.orderBtn_peer.setEnabled(tree.getRootNode().getChildCount() > 1);
                this.collapseBtn_peer.setEnabled(tree.getRootNode().getChildCount() > 0);
            } else {
                setPyramidModified(true);
                this.orderBtn.setEnabled(tree.getRootNode().getChildCount() > 1);
                this.collapseBtn.setEnabled(tree.getRootNode().getChildCount() > 0);
            }
        } else if (e.getActionCommand().equals("order")) {
            if (this.isPeerLoaded) {
                this.peerTree.order();
                setPeerModified(true);
            } else {
                this.pyramidTree.order();
                setPyramidModified(true);
            }
        } else if (e.getActionCommand().equals("exit")) {
            java.awt.event.WindowListener[] listeners = getWindowListeners();
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].windowClosing(null);
            }
        } else if (e.getActionCommand().startsWith("Text Size")) {
            float fontSize = Float.parseFloat(e.getActionCommand().substring(10));
            this.pyramidTextPane.setFont(this.pyramidTextPane.getFont().deriveFont(fontSize));
            this.pyramidTree.setFont(this.pyramidTree.getFont().deriveFont(fontSize));
            this.pyramidTree.revalidate();
            this.peerTextPane.setFont(this.peerTextPane.getFont().deriveFont(fontSize));
            this.peerTree.setFont(this.peerTree.getFont().deriveFont(fontSize));
            this.peerTree.revalidate();
            this.pyramidReferenceTextPane.setFont(this.pyramidReferenceTextPane.getFont().deriveFont(fontSize));

        } else if (e.getActionCommand().startsWith("Look And Feel")) {
            try {
                javax.swing.UIManager.setLookAndFeel(e.getActionCommand().substring(14));
            } catch (Exception ex) {
                msg(ex.getMessage());
            }
            javax.swing.SwingUtilities.updateComponentTreeUI(this);
            this.pyramidTextPane.updateSelectedStyle();
        } else if (e.getActionCommand().equals("find")) {
            SCUTextPane textPane = getTextPane();
            SCUTree tree = getTree();

            String searchRegex = null;

            while (true) {
                searchRegex = this.searchDialog.getSearchString();

                if ((searchRegex == null) || (searchRegex.trim().length() == 0)) {
                    break;
                }

                java.util.regex.Pattern pattern;
                try {
                    pattern = java.util.regex.Pattern.compile(searchRegex, 2);
                } catch (java.util.regex.PatternSyntaxException ex) {
                    showError("Regular Expression Error", "Regular expression syntax error:\n" + ex.getMessage());
                    continue;
                }
                //continue;


                if (this.searchDialog.isSearchingText()) {
                    java.util.regex.Matcher matcher = pattern.matcher(textPane.getText());
                    if (matcher.find()) {
                        textPane.modifyTextHighlight(0, textPane.getText().length() - 1, false);
                        do {
                            textPane.modifyTextHighlight(matcher.start(), matcher.end(), true);
                        }
                        while (matcher.find());
                        break;
                    }


                    javax.swing.JOptionPane.showMessageDialog(this, "Your search did not produce any results", "No results", 1);


                } else {

                    if (tree.highlightSCUsNodesWithLabelmatchingPattern(pattern) != 0)
                        break;
                    javax.swing.JOptionPane.showMessageDialog(this, "Your search did not produce any results", "No results", 1);

                }


            }


        } else if ((e.getActionCommand().equals("undo")) || (e.getActionCommand().equals("redo"))) {
            //UndoController undoController;
            SCUTree tree;
            SCUTextPane textPane;
            UndoController undoController;
            if (this.isPeerLoaded) {
                tree = this.peerTree;
                textPane = this.peerTextPane;
                undoController = this.peerUndoController;
            } else {
                tree = this.pyramidTree;
                textPane = this.pyramidTextPane;
                undoController = this.pyramidUndoController;
            }
            //javax.swing.tree.DefaultMutableTreeNode rootNode;
            javax.swing.tree.DefaultMutableTreeNode rootNode;
            if (e.getActionCommand().equals("undo")) {
                rootNode = (javax.swing.tree.DefaultMutableTreeNode) undoController.undo();
            } else {
                rootNode = (javax.swing.tree.DefaultMutableTreeNode) undoController.redo();
            }
            tree.rebuildTree(rootNode);
            textPane.loadText(textPane.getText());


            java.util.Enumeration scuNodeEnum = tree.getRootNode().children();
            while (scuNodeEnum.hasMoreElements()) {
                javax.swing.tree.DefaultMutableTreeNode scuNode = (javax.swing.tree.DefaultMutableTreeNode) scuNodeEnum.nextElement();
                java.util.Enumeration scuContributorNodeEnum = scuNode.children();

                while (scuContributorNodeEnum.hasMoreElements()) {
                    SCUContributor scuContributor = (SCUContributor) ((javax.swing.tree.DefaultMutableTreeNode) scuContributorNodeEnum.nextElement()).getUserObject();


                    for (int i = 0; i < scuContributor.getNumParts(); i++) {
                        SCUContributorPart scuContributorPart = scuContributor.getSCUContributorPart(i);
                        textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);
                    }
                }
            }

            if (this.isPeerLoaded) {
                this.isPeerModified = true;
                this.fileSavePeerAnnotationMenuItem.setEnabled(true);
                this.fileSavePeerAnnotationAsMenuItem.setEnabled(true);
            } else {
                this.isPyramidModified = true;
                this.fileSavePyramidMenuItem.setEnabled(true);
                this.fileSavePyramidAsMenuItem.setEnabled(true);
            }
        } else if (e.getActionCommand().equals("collapse")) {
            javax.swing.JButton btn = getCollapseBtn();
            SCUTree tree = getTree();

            tree.collapseTree();
            btn.setText(" Expand ");
            btn.setMnemonic('p');
            btn.setActionCommand("expand");
        } else if (e.getActionCommand().equals("expand")) {
            javax.swing.JButton btn = getCollapseBtn();
            SCUTree tree = getTree();

            tree.expandTree();
            btn.setText("Collapse");
            btn.setMnemonic('l');
            btn.setActionCommand("collapse");
        } else if (e.getActionCommand().equals("comment")) {
            javax.swing.tree.DefaultMutableTreeNode selectedNode = (javax.swing.tree.DefaultMutableTreeNode) getTree().getLastSelectedPathComponent();


            if (selectedNode.getLevel() == 1) {
                SCU scu = (SCU) selectedNode.getUserObject();
                String input = (String) javax.swing.JOptionPane.showInputDialog(this, "Enter the comment", "Comment", -1, null, null, scu.getComment());

                if (input != null) {
                    scu.setComment(input.trim());
                    getTree().nodeChanged(selectedNode);
                    setPyramidModified(true);
                }
            } else {
                SCUContributor scuContributor = (SCUContributor) selectedNode.getUserObject();
                String input = (String) javax.swing.JOptionPane.showInputDialog(this, "Enter the comment", "Comment", -1, null, null, scuContributor.getComment());

                if (input != null) {
                    scuContributor.setComment(input.trim());
                    getTree().nodeChanged(selectedNode);
                    setPyramidModified(true);
                }
            }
        } else if (e.getActionCommand().startsWith("dragScu")) {
            this.draggingScuMove = e.getActionCommand().endsWith("Move");
        } else if (e.getActionCommand().equals("regex")) {
            String startDocumentPatternStrTmp = this.startDocumentPatternStr;

            do {
                startDocumentPatternStrTmp = (String) javax.swing.JOptionPane.showInputDialog(this, "Enter the regular expression for the beginning of a new document", "Document Header RegEx", -1, null, null, startDocumentPatternStrTmp);


                if (startDocumentPatternStrTmp == null) {
                    break;
                }
            } while (!initializeStartDocumentIndexes(startDocumentPatternStrTmp));

            javax.swing.JOptionPane.showMessageDialog(this, "Your regular expression found " + this.startDocumentIndexes.length + " documents", "RegEx Result", -1);
        }
    }


    private org.w3c.dom.Document makeDocument(java.io.File file)
            throws java.io.IOException, org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, javax.xml.parsers.FactoryConfigurationError {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        javax.xml.parsers.DocumentBuilder documdentBuilder = factory.newDocumentBuilder();
        documdentBuilder.setErrorHandler(this);
        return documdentBuilder.parse(file);
    }

    /**
     * Display a message at the bottom of the window
     *
     * @param text  the message to be displayed
     */
    private void msg(String text) {
        this.statusLbl.setText(text);
    }

    private String xmlize(String str) {
        return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\"", "&quot;").replaceAll("\n", "");
    }


    private boolean writeout(java.io.File file, boolean writePeer) {
        boolean success = true;
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));

            writer.println("<?xml version=\"1.0\"?>");
            writer.println("<!DOCTYPE " + (writePeer ? "peerAnnotation" : "pyramid") + " [");

            writer.println(writePeer ? getPeerDTD() : getPyramidDTD());
            writer.println("]>");
            writer.println();
            if (writePeer) {
                writer.println("<peerAnnotation>");
                writer.println("<pyramid>");
                writer.println(getXML(false));
                writer.println("</pyramid>");
                writer.println("<annotation>");
                writer.println(getXML(true));
                writer.println("</annotation>");
                writer.println("<score>");
                writer.println(getScoreXML());
                writer.println("</score>");
                writer.println("</peerAnnotation>");
                this.peerFile = file;
                setPeerModified(false);
            } else {
                writer.println("<pyramid>");
                writer.println(getXML(false));
                writer.println("</pyramid>");
                this.pyramidFile = file;
                setPyramidModified(false);
            }
            writer.close();
            msg("Saved " + file);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            msg(ex.getMessage());
            success = false;
        }
        return success;
    }

    /**
     * Write the DTD for the XML for a pyramid
     *
     * @return the DTD (string)
     */
    private String getPyramidDTD() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(" <!ELEMENT pyramid (startDocumentRegEx?,text,scu*)>").append(eol);
        buffer.append(" <!ELEMENT startDocumentRegEx (#PCDATA)>").append(eol);
        buffer.append(" <!ELEMENT text (line*)>").append(eol);
        buffer.append(" <!ELEMENT line (#PCDATA)>").append(eol);
        buffer.append(" <!ELEMENT scu (contributor)+>").append(eol);
        buffer.append(" <!ATTLIST scu uid CDATA #REQUIRED").append(eol);
        buffer.append("               label CDATA #REQUIRED").append(eol);
        buffer.append("               comment CDATA #IMPLIED>").append(eol);
        buffer.append(" <!ELEMENT contributor (part)+>").append(eol);
        buffer.append(" <!ATTLIST contributor label CDATA #REQUIRED").append(eol);
        buffer.append("                       comment CDATA #IMPLIED>").append(eol);
        buffer.append(" <!ELEMENT part EMPTY>").append(eol);
        buffer.append(" <!ATTLIST part label CDATA #REQUIRED").append(eol);
        buffer.append("                start CDATA #REQUIRED").append(eol);
        buffer.append("                end   CDATA #REQUIRED>").append(eol);

        return buffer.toString();
    }

    /**
     * Write the DTD for the XML for a peer annotation with a pyramid and score
     *
     * @return the DTD (string)
     */
    private String getPeerDTD() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(" <!ELEMENT peerAnnotation (pyramid,annotation,score?)>").append(eol);
        buffer.append(" <!ELEMENT annotation (text,peerscu+)>").append(eol);
        buffer.append(" <!ELEMENT peerscu (contributor)*>").append(eol);
        buffer.append(" <!ATTLIST peerscu uid CDATA #REQUIRED").append(eol);
        buffer.append("                   label CDATA #REQUIRED").append(eol);
        buffer.append("                   comment CDATA #IMPLIED>").append(eol);
        buffer.append(getPyramidDTD());
        buffer.append(" <!ELEMENT score (");
        for (int i = 0; i < XMLScoreTags.length; i++) {
            buffer.append(XMLScoreTags[i] + ", ");
        }
        for (int i = 0; i < optionalXMLScoreTags.length; i++) {
            buffer.append(optionalXMLScoreTags[i] + "?, ");
        }
        buffer.setLength(buffer.length() - 2); // Remove extra comma
        buffer.append(")>").append(eol);
        for (String tag : XMLScoreTags) {
            buffer.append(" <!ELEMENT " + tag + " (#PCDATA)>").append(eol);
        }
        for (String tag : optionalXMLScoreTags) {
            buffer.append(" <!ELEMENT " + tag + " (#PCDATA)>").append(eol);
        }

        return buffer.toString();
    }


    private String getXML(boolean getPeer) {
        StringBuffer buffer = new StringBuffer();
        SCUTextPane textPane;
        SCUTree tree;
        if (getPeer) {
            textPane = this.peerTextPane;
            tree = this.peerTree;
        } else {
            textPane = this.pyramidTextPane;
            tree = this.pyramidTree;
            if (this.startDocumentPatternStr != null) {
                buffer.append("<startDocumentRegEx><![CDATA[").append(this.startDocumentPatternStr).append("]]></startDocumentRegEx>").append(eol);
            }
        }

        buffer.append(" <text>").append(eol);
        String[] lines = textPane.getText().split("\n");
        for (int i = 0; i < lines.length; i++) {
            buffer.append("  <line>").append(xmlize(lines[i])).append("</line>").append(eol);
        }
        buffer.append(" </text>").append(eol);
        java.util.Enumeration scuNodesEnum = tree.getRootNode().children();
        boolean wroteSCU = false;
        while (scuNodesEnum.hasMoreElements()) {
            javax.swing.tree.DefaultMutableTreeNode scuNode = (javax.swing.tree.DefaultMutableTreeNode) scuNodesEnum.nextElement();

            SCU scu = (SCU) scuNode.getUserObject();

            if (wroteSCU) {
                buffer.append(eol);
            }
            wroteSCU = true;

            String scuComment = scu.getComment();
            if ((scuComment != null) && (scuComment.length() > 0)) {
                scuComment = " comment=\"" + xmlize(scuComment) + "\"";
            } else {
                scuComment = "";
            }
            buffer.append(" <").append(getPeer ? "peer" : "").append("scu uid=\"").append(scu.getId()).append("\" label=\"").append(xmlize(scu.toString())).append("\"").append(scuComment).append(">").append(eol);

            java.util.Enumeration scuContributorEnum = scuNode.children();
            while (scuContributorEnum.hasMoreElements()) {
                SCUContributor scuContributor = (SCUContributor) ((javax.swing.tree.DefaultMutableTreeNode) scuContributorEnum.nextElement()).getUserObject();


                String scuContributorComment = scuContributor.getComment();
                if ((scuContributorComment != null) && (scuContributorComment.length() > 0)) {
                    scuContributorComment = " comment=\"" + xmlize(scuContributorComment) + "\"";
                } else {
                    scuContributorComment = "";
                }
                buffer.append("  <contributor label=\"").append(xmlize(scuContributor.toString())).append("\"").append(scuContributorComment).append(">").append(eol);

                java.util.Iterator scuContributorParts = scuContributor.elements();
                while (scuContributorParts.hasNext()) {
                    SCUContributorPart scuContributorPart = (SCUContributorPart) scuContributorParts.next();

                    buffer.append("   <part label=\"").append(xmlize(scuContributorPart.toString())).append("\" start=\"").append(scuContributorPart.getStartIndex()).append("\" end=\"").append(scuContributorPart.getEndIndex()).append("\"/>").append(eol);
                }


                buffer.append("  </contributor>").append(eol);
            }
            buffer.append(" </").append(getPeer ? "peer" : "").append("scu>");
        }

        return buffer.toString();
    }

    /**
     * This function generates XML containing the following fields, as specified in XMLScoreTags and optionalXMLScoreTags:
     *
     * XMLScoreTags: always present
     *
     * "totalscu": the total number of SCUs found in the peer summary
     * "uniquescu": the number of unique SCUs from the pyramid that were found in the peer summary
     * "scusNotInPyr": the number of SCUs from the pyramid that were not found in the peer summary
     * "totalWeight": the total weight of SCUs found in the peer summary
     * "maxScoreTotalscu": the max possible score given the total weight and number of SCUs in the peer summary
     * "qualityScore": score representing the quality of the SCUs in the peer summary
     *
     * optionalXMLScoreTags: present in the score when the RegEx delimiting model summaries is specified
     *
     * "averagescu": the average number of SCUs in the model summaries
     * "maxScoreAveragescu": the max possible score given the total weight and average number of SCUs in the model summaries
     * "coverageScore": score representing the coverage of the SCUs in the peer summary
     * "comprehensiveScore": the harmonic mean of the quality score and the coverage score
     *
     * Regex is used to match only strings inside <b></b> HTML tags, since the getScore function formats the
     * score in an HTML table with the above score elements between the <b></b> tags.
     *
     * @return String containing XML of peer score
     */
    private String getScoreXML() {
        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile("<b>\\s*(.*)</b>");
        Matcher matcher = pattern.matcher(getScore());
        for (String tag : XMLScoreTags) {
            if (matcher.find()) {
                buffer.append(" <" + tag + ">" + matcher.group(1) + "</" + tag + ">").append(eol);
            }
        }
        for (String tag : optionalXMLScoreTags) {
            if (matcher.find()) {
                buffer.append(" <" + tag + ">" + matcher.group(1) + "</" + tag + ">").append(eol);
            }
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    protected void setPyramidModified(boolean isModified) {
        this.isPyramidModified = isModified;
        this.fileSavePyramidMenuItem.setEnabled(isModified);
        this.fileSavePyramidAsMenuItem.setEnabled(isModified);
        this.pyramidUndoController.add(deepCopy(this.pyramidTree.getRootNode()));
    }

    protected void setPeerModified(boolean isModified) {
        this.isPeerModified = isModified;
        this.fileSavePeerAnnotationMenuItem.setEnabled(isModified);
        this.fileSavePeerAnnotationAsMenuItem.setEnabled(isModified);
        this.peerUndoController.add(deepCopy(this.peerTree.getRootNode()));
    }

    private void setPyramidLoaded(boolean isLoaded) {
        this.isPyramidLoaded = isLoaded;
        this.fileClosePyramidMenuItem.setEnabled(isLoaded);
        this.fileNewPeerAnnotationMenuItem.setEnabled(isLoaded);
        this.newBtn.setEnabled(isLoaded);
        this.dragScuMoveMenuItem.setEnabled(isLoaded);
        this.dragScuMergeMenuItem.setEnabled(isLoaded);
        this.documentStartRegexMenuItem.setEnabled(isLoaded);

        if (isLoaded) {
            showCard("pyramid");
            this.pyramidUndoController.setActive(true);
            this.peerUndoController.setActive(false);
            this.pyramidUndoController.clear();
            this.pyramidUndoController.add(deepCopy(this.pyramidTree.getRootNode()));
        } else {
            this.pyramidUndoController.clear();
            this.pyramidUndoController.setActive(false);
            setPyramidModified(false);
            this.orderBtn.setEnabled(false);
            this.collapseBtn.setEnabled(false);
        }
    }

    private void setPeerLoaded(boolean isLoaded) {
        this.isPeerLoaded = isLoaded;

        this.fileNewPyramidFromTextFileMenuItem.setEnabled(!isLoaded);
        this.fileNewPyramidFromFolderMenuItem.setEnabled(!isLoaded);
        this.fileLoadPyramidMenuItem.setEnabled(!isLoaded);
        this.fileClosePyramidMenuItem.setEnabled(!isLoaded);
        this.fileShowPeerAnnotationScoreMenuItem.setEnabled(isLoaded);
        this.dragScuMoveMenuItem.setEnabled((!isLoaded) && (this.isPyramidLoaded));
        this.dragScuMergeMenuItem.setEnabled((!isLoaded) && (this.isPyramidLoaded));
        this.documentStartRegexMenuItem.setEnabled((!isLoaded) && (this.isPyramidLoaded));

        if (isLoaded) {
            showCard("peer");
            this.fileClosePeerAnnotationMenuItem.setEnabled(true);
            this.scoreDlg.setText(getScore());
            this.scoreDlg.pack();

            this.pyramidUndoController.setActive(false);
            this.peerUndoController.setActive(true);
            this.peerUndoController.clear();
            this.peerUndoController.add(deepCopy(this.peerTree.getRootNode()));
        } else {
            showCard("pyramid");
            this.fileClosePeerAnnotationMenuItem.setEnabled(false);
            setPeerModified(false);
            this.fileShowPeerAnnotationScoreMenuItem.setSelected(false);
            this.scoreDlg.setVisible(false);

            this.peerUndoController.clear();
            this.peerUndoController.setActive(false);
            this.pyramidUndoController.setActive(true);
        }
    }

    private boolean saveModifiedPyramid() {
        int choice = javax.swing.JOptionPane.showConfirmDialog(this, "Save changes to the loaded pyramid?", "Save changes", 1, 3);


        if (choice == 0) {
            if (this.pyramidFile == null) {
                return savePyramid(true);
            }


            return savePyramid(false);
        }

        if (choice == 1) {
            return true;
        }


        return false;
    }


    private boolean saveModifiedPeer() {
        int choice = javax.swing.JOptionPane.showConfirmDialog(this, "Save changes to the loaded peer annotation?", "Save changes", 1, 3);


        if (choice == 0) {
            if (this.peerFile == null) {
                return savePeer(true);
            }


            return savePeer(false);
        }

        if (choice == 1) {
            return true;
        }


        return false;
    }


    private boolean savePyramid(boolean useNewFile) {
        if (useNewFile) {
            javax.swing.JFileChooser chooser = new DucViewFileChooser(this.defaultFilePath, true, true);
            if (chooser.showSaveDialog(this) == 0) {
                return writeout(chooser.getSelectedFile(), false);
            }


            return false;
        }


        return writeout(this.pyramidFile, false);
    }


    private boolean savePeer(boolean useNewFile) {
        if (useNewFile) {
            javax.swing.JFileChooser chooser = new DucViewFileChooser(this.defaultFilePath, true, false);
            if (chooser.showSaveDialog(this) == 0) {
                return writeout(chooser.getSelectedFile(), true);
            }


            return false;
        }


        return writeout(this.peerFile, true);
    }


    private javax.swing.JMenuBar createMenuBar() {
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();

        javax.swing.JMenu fileMenu = new javax.swing.JMenu("File");
        fileMenu.setMnemonic('f');

        javax.swing.JMenu pyramidMenu = new javax.swing.JMenu("Pyramid");
        pyramidMenu.setMnemonic('y');

        javax.swing.JMenu newPyramidMenu = new javax.swing.JMenu("New...");
        newPyramidMenu.setMnemonic('n');

        this.fileNewPyramidFromTextFileMenuItem = new JMenuItem("From text file");
        this.fileNewPyramidFromTextFileMenuItem.setMnemonic('f');
        this.fileNewPyramidFromTextFileMenuItem.setActionCommand("fileNewPyramidFromTextFile");
        this.fileNewPyramidFromTextFileMenuItem.addActionListener(this);
        newPyramidMenu.add(this.fileNewPyramidFromTextFileMenuItem);

        this.fileNewPyramidFromFolderMenuItem = new JMenuItem("From folder of text files");
        this.fileNewPyramidFromFolderMenuItem.setMnemonic('r');
        this.fileNewPyramidFromFolderMenuItem.setActionCommand("fileNewPyramidFromFolder");
        this.fileNewPyramidFromFolderMenuItem.addActionListener(this);
        newPyramidMenu.add(this.fileNewPyramidFromFolderMenuItem);

        pyramidMenu.add(newPyramidMenu);

        this.fileLoadPyramidMenuItem = new JMenuItem("Load...");
        this.fileLoadPyramidMenuItem.setMnemonic('l');
        this.fileLoadPyramidMenuItem.setActionCommand("fileLoadPyramid");
        this.fileLoadPyramidMenuItem.addActionListener(this);
        pyramidMenu.add(this.fileLoadPyramidMenuItem);

        this.fileSavePyramidMenuItem = new JMenuItem("Save");
        this.fileSavePyramidMenuItem.setMnemonic('s');
        this.fileSavePyramidMenuItem.setActionCommand("fileSavePyramid");
        this.fileSavePyramidMenuItem.addActionListener(this);
        this.fileSavePyramidMenuItem.setEnabled(false);
        pyramidMenu.add(this.fileSavePyramidMenuItem);

        this.fileSavePyramidAsMenuItem = new JMenuItem("Save As...");
        this.fileSavePyramidAsMenuItem.setMnemonic('a');
        this.fileSavePyramidAsMenuItem.setActionCommand("fileSavePyramidAs");
        this.fileSavePyramidAsMenuItem.addActionListener(this);
        this.fileSavePyramidAsMenuItem.setEnabled(false);
        pyramidMenu.add(this.fileSavePyramidAsMenuItem);

        this.fileClosePyramidMenuItem = new JMenuItem("Close");
        this.fileClosePyramidMenuItem.setMnemonic('c');
        this.fileClosePyramidMenuItem.setActionCommand("fileClosePyramid");
        this.fileClosePyramidMenuItem.addActionListener(this);
        this.fileClosePyramidMenuItem.setEnabled(false);
        pyramidMenu.add(this.fileClosePyramidMenuItem);

        fileMenu.add(pyramidMenu);

        javax.swing.JMenu peerMenu = new javax.swing.JMenu("Peer Annotation");
        peerMenu.setMnemonic('e');

        this.fileNewPeerAnnotationMenuItem = new JMenuItem("New...");
        this.fileNewPeerAnnotationMenuItem.setMnemonic('n');
        this.fileNewPeerAnnotationMenuItem.setActionCommand("fileNewPeerAnnotation");
        this.fileNewPeerAnnotationMenuItem.addActionListener(this);
        this.fileNewPeerAnnotationMenuItem.setEnabled(false);
        peerMenu.add(this.fileNewPeerAnnotationMenuItem);

        JMenuItem fileLoadPeerAnnotationMenuItem = new JMenuItem("Load...");
        fileLoadPeerAnnotationMenuItem.setMnemonic('l');
        fileLoadPeerAnnotationMenuItem.setActionCommand("fileLoadPeerAnnotation");
        fileLoadPeerAnnotationMenuItem.addActionListener(this);
        peerMenu.add(fileLoadPeerAnnotationMenuItem);

        this.fileSavePeerAnnotationMenuItem = new JMenuItem("Save");
        this.fileSavePeerAnnotationMenuItem.setMnemonic('s');
        this.fileSavePeerAnnotationMenuItem.setActionCommand("fileSavePeerAnnotation");
        this.fileSavePeerAnnotationMenuItem.addActionListener(this);
        this.fileSavePeerAnnotationMenuItem.setEnabled(false);
        peerMenu.add(this.fileSavePeerAnnotationMenuItem);

        this.fileSavePeerAnnotationAsMenuItem = new JMenuItem("Save As...");
        this.fileSavePeerAnnotationAsMenuItem.setMnemonic('a');
        this.fileSavePeerAnnotationAsMenuItem.setActionCommand("fileSavePeerAnnotationAs");
        this.fileSavePeerAnnotationAsMenuItem.addActionListener(this);
        this.fileSavePeerAnnotationAsMenuItem.setEnabled(false);
        peerMenu.add(this.fileSavePeerAnnotationAsMenuItem);

        this.fileShowPeerAnnotationScoreMenuItem = new javax.swing.JCheckBoxMenuItem("Show Score");
        this.fileShowPeerAnnotationScoreMenuItem.setMnemonic('w');
        this.fileShowPeerAnnotationScoreMenuItem.setActionCommand("fileShowPeerAnnotationScore");

        this.fileShowPeerAnnotationScoreMenuItem.addActionListener(this);
        this.fileShowPeerAnnotationScoreMenuItem.setEnabled(false);
        peerMenu.add(this.fileShowPeerAnnotationScoreMenuItem);

        this.fileClosePeerAnnotationMenuItem = new JMenuItem("Close");
        this.fileClosePeerAnnotationMenuItem.setMnemonic('c');
        this.fileClosePeerAnnotationMenuItem.setActionCommand("fileClosePeerAnnotation");
        this.fileClosePeerAnnotationMenuItem.addActionListener(this);
        this.fileClosePeerAnnotationMenuItem.setEnabled(false);
        peerMenu.add(this.fileClosePeerAnnotationMenuItem);

        fileMenu.add(peerMenu);

        JMenuItem fileExitMenuItem = new JMenuItem("Exit");
        fileExitMenuItem.setMnemonic('x');
        fileExitMenuItem.setActionCommand("exit");
        fileExitMenuItem.addActionListener(this);
        fileMenu.add(fileExitMenuItem);

        menuBar.add(fileMenu);

        javax.swing.JMenu editMenu = new javax.swing.JMenu("Edit");
        editMenu.setMnemonic('e');

        JMenuItem editFindMenuItem = new JMenuItem("Find...       Ctrl+F");
        editFindMenuItem.setMnemonic('f');
        editFindMenuItem.setActionCommand("find");
        editFindMenuItem.addActionListener(this);
        editMenu.add(editFindMenuItem);

        this.editUndoMenuItem = new JMenuItem("Undo...     Ctrl+Z");
        this.editUndoMenuItem.setMnemonic('u');
        this.editUndoMenuItem.setActionCommand("undo");
        this.editUndoMenuItem.addActionListener(this);
        this.editUndoMenuItem.setEnabled(false);
        editMenu.add(this.editUndoMenuItem);

        this.editRedoMenuItem = new JMenuItem("Redo...     Ctrl+Y");
        this.editRedoMenuItem.setMnemonic('r');
        this.editRedoMenuItem.setActionCommand("redo");
        this.editRedoMenuItem.addActionListener(this);
        this.editRedoMenuItem.setEnabled(false);
        editMenu.add(this.editRedoMenuItem);

        menuBar.add(editMenu);

        javax.swing.JMenu optionsMenu = new javax.swing.JMenu("Options");
        optionsMenu.setMnemonic('o');

        javax.swing.JMenu textSizeSubmenu = new javax.swing.JMenu("Text Size");
        textSizeSubmenu.setMnemonic('t');

        int[] textSizes = {6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60};


        javax.swing.ButtonGroup sizeGroup = new javax.swing.ButtonGroup();
        for (int i = 0; i < textSizes.length; i++) {
            javax.swing.JRadioButtonMenuItem textSizeMenuItem = new javax.swing.JRadioButtonMenuItem(String.valueOf(textSizes[i]));

            textSizeMenuItem.setActionCommand("Text Size " + String.valueOf(textSizes[i]));
            textSizeMenuItem.addActionListener(this);
            sizeGroup.add(textSizeMenuItem);
            if (this.currentTextSize == textSizes[i]) {
                textSizeMenuItem.setSelected(true);
            }
            textSizeSubmenu.add(textSizeMenuItem);
        }
        optionsMenu.add(textSizeSubmenu);

        javax.swing.JMenu lookAndFeelSubmenu = new javax.swing.JMenu("Look And Feel");
        lookAndFeelSubmenu.setMnemonic(76);

        javax.swing.UIManager.LookAndFeelInfo[] installedLooks = javax.swing.UIManager.getInstalledLookAndFeels();

        javax.swing.ButtonGroup lookAndFeelGroup = new javax.swing.ButtonGroup();
        for (int i = 0; i < installedLooks.length; i++) {
            javax.swing.JRadioButtonMenuItem lookAndFeelMenuItem = new javax.swing.JRadioButtonMenuItem(installedLooks[i].getName());

            lookAndFeelMenuItem.setActionCommand("Look And Feel " + installedLooks[i].getClassName());

            lookAndFeelMenuItem.addActionListener(this);
            lookAndFeelGroup.add(lookAndFeelMenuItem);
            if (javax.swing.UIManager.getLookAndFeel().getName().equals(installedLooks[i].getName())) {
                lookAndFeelMenuItem.setSelected(true);
            }
            lookAndFeelSubmenu.add(lookAndFeelMenuItem);
        }

        optionsMenu.add(lookAndFeelSubmenu);

        javax.swing.JMenu draggingScuMenu = new javax.swing.JMenu("Dragging SCU");
        draggingScuMenu.setMnemonic('d');

        javax.swing.ButtonGroup dragScuGroup = new javax.swing.ButtonGroup();

        this.dragScuMoveMenuItem = new javax.swing.JRadioButtonMenuItem("Moves it under target SCU");
        this.dragScuMoveMenuItem.setMnemonic('o');
        this.dragScuMoveMenuItem.setActionCommand("dragScuMove");
        this.dragScuMoveMenuItem.addActionListener(this);
        this.dragScuMoveMenuItem.setSelected(true);
        this.dragScuMoveMenuItem.setEnabled(false);
        dragScuGroup.add(this.dragScuMoveMenuItem);
        draggingScuMenu.add(this.dragScuMoveMenuItem);

        this.dragScuMergeMenuItem = new javax.swing.JRadioButtonMenuItem("Merges it with target SCU");
        this.dragScuMergeMenuItem.setMnemonic('e');
        this.dragScuMergeMenuItem.setActionCommand("dragScuMerge");
        this.dragScuMergeMenuItem.addActionListener(this);
        this.dragScuMergeMenuItem.setEnabled(false);
        dragScuGroup.add(this.dragScuMergeMenuItem);
        draggingScuMenu.add(this.dragScuMergeMenuItem);

        optionsMenu.add(draggingScuMenu);

        this.documentStartRegexMenuItem = new JMenuItem("Document Header RegEx...");
        this.documentStartRegexMenuItem.setMnemonic('h');
        this.documentStartRegexMenuItem.setActionCommand("regex");
        this.documentStartRegexMenuItem.addActionListener(this);
        this.documentStartRegexMenuItem.setEnabled(false);
        optionsMenu.add(this.documentStartRegexMenuItem);

        menuBar.add(optionsMenu);

        javax.swing.JMenu helpMenu = new javax.swing.JMenu("Help");
        helpMenu.setMnemonic(72);

        JMenuItem helpAboutMenuItem = new JMenuItem("About...");
        helpAboutMenuItem.setMnemonic(65);
        helpAboutMenuItem.setActionCommand("helpAbout");
        helpAboutMenuItem.addActionListener(this);
        helpMenu.add(helpAboutMenuItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    public javax.swing.JButton getAddBtn() {
        if (this.isPeerLoaded) {
            return this.addBtn_peer;
        }


        return this.addBtn;
    }


    public javax.swing.JButton getRemoveBtn() {
        if (this.isPeerLoaded) {
            return this.removeBtn_peer;
        }


        return this.removeBtn;
    }


    public javax.swing.JButton getRenameBtn() {
        if (this.isPeerLoaded) {
            return null;
        }


        return this.renameBtn;
    }


    public javax.swing.JButton getSetLabelBtn() {
        if (this.isPeerLoaded) {
            return null;
        }


        return this.setLabelbtn;
    }


    public javax.swing.JButton getCollapseBtn() {
        if (this.isPeerLoaded) {
            return this.collapseBtn_peer;
        }
        return this.collapseBtn;
    }

    private SCUTree getTree() {
        if (this.isPeerLoaded) {
            return this.peerTree;
        }
        return this.pyramidTree;
    }

    private SCUTextPane getTextPane() {
        if (this.isPeerLoaded) {
            return this.peerTextPane;
        }
        return this.pyramidTextPane;
    }

    public javax.swing.JButton getCommentBtn() {
        if (this.isPeerLoaded) {
            return this.commentBtn_peer;
        }
        return this.commentBtn;
    }

    private void loadTree(org.w3c.dom.Element top, boolean loadPeer) {
        javax.swing.JButton collapseButton;
        SCUTextPane textPane;
        SCUTree tree;
        javax.swing.JButton orderButton;
        //javax.swing.JButton collapseButton;
        if (loadPeer) {
            textPane = this.peerTextPane;
            tree = this.peerTree;
            orderButton = this.orderBtn_peer;
            collapseButton = this.collapseBtn_peer;
        } else {
            textPane = this.pyramidTextPane;
            tree = this.pyramidTree;
            orderButton = this.orderBtn;
            collapseButton = this.collapseBtn;
        }

        org.w3c.dom.NodeList lineNodeList = top.getElementsByTagName("line");
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < lineNodeList.getLength(); i++) {
            if (lineNodeList.item(i).getFirstChild() != null) {
                buffer.append(lineNodeList.item(i).getFirstChild().getNodeValue());
            }
            buffer.append("\n");
        }

        textPane.loadText(buffer.toString());

        javax.swing.tree.DefaultMutableTreeNode rootNode = new javax.swing.tree.DefaultMutableTreeNode("Root Node");
        org.w3c.dom.NodeList scuNodeList = top.getElementsByTagName((loadPeer ? "peer" : "") + "scu");

        for (int scuCnt = 0; scuCnt < scuNodeList.getLength(); scuCnt++) {
            org.w3c.dom.Element scuElement = (org.w3c.dom.Element) scuNodeList.item(scuCnt);
            int id = Integer.parseInt(scuElement.getAttribute("uid"));
            String scuLabel = scuElement.getAttribute("label");
            String scuComment = scuElement.getAttribute("comment");
            org.w3c.dom.NodeList scuContributorNodeList = scuElement.getElementsByTagName("contributor");
            //javax.swing.tree.DefaultMutableTreeNode scuNode = new javax.swing.tree.DefaultMutableTreeNode(new SCU(id, "(" + scuContributorNodeList.getLength() + ") " + scuLabel, scuComment));
            javax.swing.tree.DefaultMutableTreeNode scuNode = new javax.swing.tree.DefaultMutableTreeNode(new SCU(id, scuLabel, scuComment));

            //org.w3c.dom.NodeList scuContributorNodeList = scuElement.getElementsByTagName("contributor");
            //int i = scuContributorNodeList.getLength();
            for (int scuContributorCnt = 0;
                 scuContributorCnt < scuContributorNodeList.getLength();
                 scuContributorCnt++) {
                org.w3c.dom.Element scuContributorElement = (org.w3c.dom.Element) scuContributorNodeList.item(scuContributorCnt);

                String scuContributorComment = scuContributorElement.getAttribute("comment");
                org.w3c.dom.NodeList scuContributorPartNodeList = scuContributorElement.getElementsByTagName("part");

                org.w3c.dom.Element scuContributorPartElement = (org.w3c.dom.Element) scuContributorPartNodeList.item(0);

                int startIndex = Integer.parseInt(scuContributorPartElement.getAttribute("start"));

                int endIndex = Integer.parseInt(scuContributorPartElement.getAttribute("end"));

                String label = scuContributorPartElement.getAttribute("label");
                SCUContributorPart scuContributorPart = new SCUContributorPart(startIndex, endIndex, label);

                textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);

                SCUContributor scuContributor = new SCUContributor(scuContributorPart, scuContributorComment);
                javax.swing.tree.DefaultMutableTreeNode scuContributorNode = new javax.swing.tree.DefaultMutableTreeNode(scuContributor);

                for (int scuContributorPartCnt = 1;
                     scuContributorPartCnt < scuContributorPartNodeList.getLength();
                     scuContributorPartCnt++) {
                    if (scuContributorPartCnt == 1) {

                        scuContributorNode.add(new javax.swing.tree.DefaultMutableTreeNode(scuContributorPart));
                    }
                    scuContributorPartElement = (org.w3c.dom.Element) scuContributorPartNodeList.item(scuContributorPartCnt);

                    startIndex = Integer.parseInt(scuContributorPartElement.getAttribute("start"));

                    endIndex = Integer.parseInt(scuContributorPartElement.getAttribute("end"));

                    label = scuContributorPartElement.getAttribute("label");
                    scuContributorPart = new SCUContributorPart(startIndex, endIndex, label);

                    textPane.modifyTextSelection(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);

                    scuContributor.add(scuContributorPart);
                    scuContributorNode.add(new javax.swing.tree.DefaultMutableTreeNode(scuContributorPart));
                }
                scuNode.add(scuContributorNode);
            }

            rootNode.add(scuNode);
        }
        tree.rebuildTree(rootNode);

        orderButton.setEnabled(tree.getRootNode().getChildCount() > 1);
        collapseButton.setEnabled(tree.getRootNode().getChildCount() > 0);
    }

    private void showCard(String cardname) {
        ((java.awt.CardLayout) this.mainPanel.getLayout()).show(this.mainPanel, cardname);
    }

    /**
     * Display an error message.
     *
     * @param title     the title of the error message
     * @param message   the contents of the error message
     */
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Get the score for the loaded peer annotation
     *
     * @return If a peer annotation is loaded, return a string formatted as a table with the fields:
     *
     * <table><tr><td>Number of unique contributing SCUs:</td> <td> <b>???</b></td></tr>
     * <tr><td>Number of SCUs not in the pyramid:</td> <td> <b>???</b></td></tr>
     * <tr><td>Number of SCUs with multiple contributors:</td> <td> <b>???</b></td></tr>
     * <tr><td>Total SCUs in peer:</td> <td><b>???</b></td></tr>
     * <tr><td>Total peer SCU weight:</td> <td><b>???</b></td></tr>
     * <tr><td>Maximum attainable score with 8 SCUs:</td> <td><b>???</b></td></tr>
     * <tr><td>Score: </td> <td><b>???</b></td></tr></table>
     */
    protected String getScore() {
        if (!this.isPeerLoaded) {
            return "Error: peer annotation is not loaded\n";
        }

        StringBuffer resultBuffer = new StringBuffer();

        int totalPyramidSCUContributors = 0;

        java.util.HashMap pyramidScuNumContributorsMap = new java.util.HashMap();
        java.util.Enumeration pyramidScuNodesEnum = this.pyramidTree.getRootNode().children();
        while (pyramidScuNodesEnum.hasMoreElements()) {
            javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) pyramidScuNodesEnum.nextElement();

            SCU scu = (SCU) node.getUserObject();
            pyramidScuNumContributorsMap.put(new Integer(scu.getId()), new Integer(node.getChildCount()));

            totalPyramidSCUContributors += node.getChildCount();
        }

        java.util.ArrayList peerSCUIds = new java.util.ArrayList();
        java.util.ArrayList peerSCUMultipleContributors = new java.util.ArrayList();

        java.util.HashMap peerScuNumContributorsMap = new java.util.HashMap();
        java.util.Enumeration peerScuNodesEnum = this.peerTree.getRootNode().children();

        while (peerScuNodesEnum.hasMoreElements()) {
            javax.swing.tree.DefaultMutableTreeNode node = (javax.swing.tree.DefaultMutableTreeNode) peerScuNodesEnum.nextElement();

            int childCount = node.getChildCount();
            SCU scu = (SCU) node.getUserObject();
            int scuId = scu.getId();
            if ((scuId != 0) && (childCount > 0)) {
                peerSCUIds.add(new Integer(scuId));
            }
            if ((scuId != 0) && (childCount > 1)) {
                peerSCUMultipleContributors.add(scu);
            }
            peerScuNumContributorsMap.put(new Integer(scuId), new Integer(node.getChildCount()));
        }


        int numerator = 0;
        java.util.Iterator peerSCUIdsIterator = peerSCUIds.iterator();
        while (peerSCUIdsIterator.hasNext()) {
            numerator += ((Integer) pyramidScuNumContributorsMap.get(peerSCUIdsIterator.next())).intValue();
        }


        //resultBuffer.append("<tr><td>Number of SCUs with multiple contributors:</td> <td> <b>" + peerSCUMultipleContributors.size() + "</b></td></tr>\n");


        int totalRepetativeSCUs = 0;

        if (peerSCUMultipleContributors.size() > 0) {
            //resultBuffer.append("<tr><td colspan='2'><table>");
            java.util.Iterator peerSCUMultipleContributorsIterator = peerSCUMultipleContributors.iterator();

            while (peerSCUMultipleContributorsIterator.hasNext()) {
                SCU scu = (SCU) peerSCUMultipleContributorsIterator.next();
                int numRepetitions = ((Integer) peerScuNumContributorsMap.get(new Integer(scu.getId()))).intValue() - 1;

                //resultBuffer.append("<tr><td><i> " + scu.toString().substring(scu.toString().indexOf(')') + 2) + "</i></td> <td>" + numRepetitions + "</td></tr>\n");


                totalRepetativeSCUs += numRepetitions;
            }
            //resultBuffer.append("<tr><td> total extra contributors: </td><td>" + totalRepetativeSCUs + "</td></tr></table></tr>\n");
        }


        int totalSCUsInPeer = peerSCUIds.size() + totalRepetativeSCUs + ((Integer) peerScuNumContributorsMap.get(new Integer(0))).intValue();


        resultBuffer.append("<table>");
        resultBuffer.append("<tr><td>Total SCUs in peer:</td> <td><b>" + totalSCUsInPeer + "</b></td></tr>\n");
        resultBuffer.append("<tr><td>Number of unique contributing SCUs:</td> <td> <b>" + peerSCUIds.size() + "</b></td></tr>\n");
        resultBuffer.append("<tr><td>Number of SCUs not in the pyramid:</td> <td> <b>" + peerScuNumContributorsMap.get(new Integer(0)) + "</b></td></tr>\n");

        resultBuffer.append("<tr><td>Total peer SCU weight:</td> <td><b>" + numerator + "</b></td></tr>\n");


        Integer[] peerNumContributors = (Integer[]) pyramidScuNumContributorsMap.values().toArray(new Integer[0]);

        java.util.Arrays.sort(peerNumContributors);

        int denominator = 0;

        int peerIndex = peerNumContributors.length - 1;
        for (int numSCUs = 0;
             (peerIndex >= 0) && (numSCUs < totalSCUsInPeer); numSCUs++) {
            denominator += peerNumContributors[peerIndex].intValue();
            peerIndex--;
        }

        if (denominator == 0) {
            resultBuffer.append("<tr><td>No SCUs were annotated</td></tr>\n");
        } else {
            resultBuffer.append("<tr><td>Maximum attainable score with " + totalSCUsInPeer + " SCUs:</td> <td><b> " + denominator + "</b></td></tr>\n<tr><td><i>Quality score: </i></td> <td><b>" + new java.text.DecimalFormat("#.####").format(numerator / (double) denominator) + "</b></td></tr>");

            if (this.startDocumentIndexes != null) {
                int model_denominator = 0;
                int num_model_scus = Math.round(totalPyramidSCUContributors / this.startDocumentIndexes.length);

                resultBuffer.append("\n<tr><td>Average SCUs in Model summary:</td> <td><b>" + num_model_scus + "</b></td></tr>\n");


                peerIndex = peerNumContributors.length - 1;
                for (int numSCUs = 0;
                     (peerIndex >= 0) && (numSCUs < num_model_scus); numSCUs++) {
                    model_denominator += peerNumContributors[peerIndex].intValue();
                    peerIndex--;
                }

                resultBuffer.append("<tr><td>Maximum attainable score with " + num_model_scus + " SCUs:</td> <td><b> " + model_denominator + "</b></td></tr>\n<tr><td><i>Coverage score: </i></td> <td><b>" + new java.text.DecimalFormat("#.####").format(Math.min(1, numerator / (double) model_denominator)) + "</b></td></tr>\n");

                double comprehensive_score = 2 / (1 / (numerator / (double) denominator) + 1 / Math.min(1, numerator / (double) model_denominator));

                resultBuffer.append("<tr><td><i>Comprehensive score: </i></td> <td><b>" + new java.text.DecimalFormat("#.####").format(comprehensive_score) + "</b></td></tr>");
            }
        }

        resultBuffer.append("</table>");
        return resultBuffer.toString();
    }

    /**
     * Show an error message when an XML parsing error occurs.
     *
     * @param ex An exception created during XML parsing.
     */
    public void error(org.xml.sax.SAXParseException ex) {
        String message = "XML Parsing error: " + ex.getMessage() + " at line " + ex.getLineNumber() + " col " + ex.getColumnNumber();

        System.err.println(message);
        msg(message);
        showError("Error", message);
    }


    public void fatalError(org.xml.sax.SAXParseException ex) {
        String message = "XML Parsing fatal error: " + ex.getMessage() + " at line " + ex.getLineNumber() + " col " + ex.getColumnNumber();

        System.err.println(message);
        msg(message);
        showError("Error", message);
    }


    public void warning(org.xml.sax.SAXParseException ex) {
        String message = "XML Parsing warning: " + ex.getMessage() + " at line " + ex.getLineNumber() + " col " + ex.getColumnNumber();

        System.err.println(message);
        msg(message);
    }

    private static Object deepCopy(Object orig) {
        Object obj = null;

        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();


            java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));

            obj = in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /**
     * Find the indices within the summary text loaded in pyramidTextPane that correspond to the
     * start of each wise crowd summary.
     *
     * @param regexStr a regular expression that delimits multiple text summaries
     * @return true if at least two documents were found using regexStr
     *         false if one or zero documents were found, or if regexStr was not a valid regular expression
     */
    private boolean initializeStartDocumentIndexes(String regexStr) {
        if (regexStr.trim().length() == 0) {
            showError("Regular Expression Error", "The regular expression is empty");
            return false;
        }

        java.util.regex.Pattern p;
        try {
            p = java.util.regex.Pattern.compile(regexStr);
        } catch (java.util.regex.PatternSyntaxException ex) {
            showError("Regular Expression Error", "The regular expression is invalid:\n" + ex.getMessage());
            return false;
        }

        java.util.regex.Matcher m = p.matcher(this.pyramidTextPane.getText());
        java.util.ArrayList indexes = new java.util.ArrayList();
        while (m.find()) {
            indexes.add(new Integer(m.start()));
        }
        if (indexes.isEmpty()) {
            showError("Regular Expression Error", "The regular expression did not match any text");
            return false;
        }
        if (indexes.size() == 1) {
            showError("Regular Expression Error", "The regular expression only found one document");
            return false;
        }

        this.startDocumentIndexes = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i++) {
            this.startDocumentIndexes[i] = ((Integer) indexes.get(i)).intValue();
        }
        this.startDocumentPatternStr = regexStr;
        return true;
    }

    private class DucViewWindowAdapter
            extends java.awt.event.WindowAdapter {
        private DucView ducView;

        public DucViewWindowAdapter(DucView ducView) {
            this.ducView = ducView;
        }

        public void windowClosing(java.awt.event.WindowEvent e) {
            if (((DucView.this.isPeerModified) && (!DucView.this.saveModifiedPeer())) || ((!DucView.this.isPyramidModified) || (DucView.this.saveModifiedPyramid()))) {

                this.ducView.dispose();
            }
        }
    }

    private class DucViewFileChooser extends javax.swing.JFileChooser {
        private boolean isSavingFile;
        private boolean isPyramid;

        public DucViewFileChooser(String currentDirectory, boolean isSavingFile, boolean isPyramid) {
            super();
            this.isSavingFile = isSavingFile;
            this.isPyramid = isPyramid;

            String defaultName = isPyramid ? DucView.this.pyramidInputTextFile : DucView.this.peerInputTextFile;
            if ((defaultName == null) || (defaultName.trim().length() == 0))
                defaultName = "untitled";
            defaultName = defaultName.replaceFirst("\\.txt$", "");
            defaultName = defaultName + (isPyramid ? ".pyr" : ".pan");

            if (isSavingFile) {
                setSelectedFile(new java.io.File(defaultName));
            }
            this.setFileFilter(new DucViewFileFilter());

        }

        public void approveSelection() {
            if ((this.isSavingFile) && (getSelectedFile().exists()) && (javax.swing.JOptionPane.showConfirmDialog(this, "The file " + getSelectedFile().getName() + " already exists, would you like to overwrite it?", "Overwrite?", 0, 2) != 0)) {


                return;
            }
            super.approveSelection();
        }

        private class DucViewFileFilter extends javax.swing.filechooser.FileFilter {

            public boolean accept(java.io.File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (DucView.DucViewFileChooser.this.isPyramid) {
                    return file.getName().endsWith(".pyr");
                }


                return file.getName().endsWith(".pan");
            }


            public String getDescription() {
                if (DucView.DucViewFileChooser.this.isPyramid) {
                    return "Pyramid Files (*.pyr)";
                }


                return "Peer Annotation Files (*.pan)";
            }

            private DucViewFileFilter() {
            }
        }
    }

    private class UndoController {

        private java.util.Vector states = new java.util.Vector();

        private boolean isUndoEnabled = false;
        private boolean isRedoEnabled = false;
        private boolean isActive = false;

        private int undoIndex = -1;

        private void expressGUI() {
            if (this.isActive) {
                DucView.this.editUndoMenuItem.setEnabled(this.isUndoEnabled);
                DucView.this.editRedoMenuItem.setEnabled(this.isRedoEnabled);
            }
        }


        public void setActive(boolean isActive) {
            this.isActive = isActive;
            expressGUI();
        }

        public void clear() {
            this.states.clear();
            this.undoIndex = -1;
            this.isUndoEnabled = (this.isRedoEnabled = false);
            expressGUI();
        }

        public void add(Object state) {
            this.undoIndex += 1;
            if (this.undoIndex < this.states.size())
                this.states.setSize(this.undoIndex);
            this.states.add(state);
            this.isUndoEnabled = (this.undoIndex > 0);
            this.isRedoEnabled = false;
            expressGUI();
        }


        public Object undo() {
            if (this.isUndoEnabled) {
                this.undoIndex -= 1;
                this.isUndoEnabled = (this.undoIndex > 0);
                this.isRedoEnabled = true;
                expressGUI();

                return DucView.deepCopy(this.states.get(this.undoIndex));
            }


            return null;
        }


        public Object redo() {
            if (this.isRedoEnabled) {
                this.undoIndex += 1;
                this.isUndoEnabled = true;
                this.isRedoEnabled = (this.states.size() > this.undoIndex + 1);
                expressGUI();

                return DucView.deepCopy(this.states.get(this.undoIndex));
            }


            return null;
        }

        private UndoController() {
        }
    }
}