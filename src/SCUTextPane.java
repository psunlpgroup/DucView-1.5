package ducview;

import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.*;

public class SCUTextPane extends javax.swing.JTextPane implements javax.swing.event.CaretListener, java.awt.event.ActionListener {
    private boolean ignoreNextCaretUpdate = false;
    private boolean ignoreAllCaretUpdates = false;
    private String selectedText = null;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private short[] selectionIndexes;
    private short[] highlightIndexes;
    private SCUTree tree = null;
    private JPopupMenu popupMenu;

    public SCUTextPane() {
        setEditable(false);
        addCaretListener(this);
        setPreferredSize(new java.awt.Dimension(500, 500));
        addStyle("plain", null);
        Style grayedStyle = addStyle("grayed", null);
        StyleConstants.setForeground(grayedStyle, java.awt.Color.blue);
        Style selectedStyle = addStyle("selected", null);
        StyleConstants.setBackground(selectedStyle, UIManager.getLookAndFeelDefaults().getColor("TextArea.selectionBackground"));


        StyleConstants.setForeground(selectedStyle, UIManager.getLookAndFeelDefaults().getColor("TextArea.selectionForeground"));


        Style undelinedStyle = addStyle("highlighted", null);
        StyleConstants.setBackground(undelinedStyle, new java.awt.Color(255, 255, 100));

        this.popupMenu = new JPopupMenu("SCUs in which this text appears");
    }

    public void setTree(SCUTree tree) {
        this.tree = tree;
    }

    public void updateSelectedStyle() {
        Style selectedStyle = addStyle("selected", null);
        StyleConstants.setBackground(selectedStyle, UIManager.getLookAndFeelDefaults().getColor("TextArea.selectionBackground"));


        StyleConstants.setForeground(selectedStyle, UIManager.getLookAndFeelDefaults().getColor("TextArea.selectionForeground"));
    }


    public String getSelectedText() {
        return this.selectedText;
    }

    public int getSelectionStartIndex() {
        return this.selectionStart;
    }

    public int getSelectionEndIndex() {
        return this.selectionEnd;
    }

    public void setIgnoreNextCaretUpdate() {
        this.ignoreNextCaretUpdate = true;
    }

    public void caretUpdate(javax.swing.event.CaretEvent e) {
        if (getHighlighter() == null) {
            return;
        }
        if (this.ignoreNextCaretUpdate) {
            this.ignoreNextCaretUpdate = false;
            return;
        }

        if ((this.ignoreAllCaretUpdates) || (getText() == null) || (getText().length() == 0)) {
            return;
        }

        if (getSelectionEnd() - getSelectionStart() > 0) {
            this.selectionStart = getSelectionStart();
            this.selectionEnd = getSelectionEnd();


            char[] buff = getText().replaceAll("[\\t\\n\\r\\f\\,\\.\\-\\:]", " ").toCharArray();


            if (buff[this.selectionStart] != ' ') {

                while ((this.selectionStart > 0) && (buff[(this.selectionStart - 1)] != ' ')) {
                    this.selectionStart -= 1;
                }
            }


            while ((this.selectionStart < buff.length - 1) && (buff[this.selectionStart] == ' ')) {
                this.selectionStart += 1;
            }


            if (buff[(this.selectionEnd - 1)] != ' ') {

                while ((this.selectionEnd < buff.length - 1) && (buff[this.selectionEnd] != ' ')) {
                    this.selectionEnd += 1;
                }
            }


            while ((this.selectionEnd > 0) && (buff[(this.selectionEnd - 1)] == ' ')) {
                this.selectionEnd -= 1;
            }

            this.selectedText = getText().substring(this.selectionStart, this.selectionEnd);
            this.ignoreNextCaretUpdate = true;
            select(getSelectionEnd(), getSelectionEnd());

        } else {
            this.selectedText = null;


            Vector scuNodes = this.tree.getSCUNodesContainingIndex(getSelectionStart());

            if (scuNodes.size() == 1) {
                this.tree.selectSCUNode(((SCU) scuNodes.get(0)).getId());
            } else if (scuNodes.size() > 1) {
                this.popupMenu.removeAll();

                java.util.Enumeration scuNodeEnum = scuNodes.elements();
                while (scuNodeEnum.hasMoreElements()) {
                    SCU scu = (SCU) scuNodeEnum.nextElement();
                    JMenuItem menuItem = new JMenuItem(scu.toString());
                    menuItem.setActionCommand(String.valueOf(scu.getId()));
                    menuItem.addActionListener(this);
                    this.popupMenu.add(menuItem);
                }
                this.popupMenu.pack();
                try {
                    java.awt.Rectangle rect = modelToView(getSelectionStart());
                    this.popupMenu.show(this, rect.x, rect.y);
                } catch (javax.swing.text.BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        updateTextColors();
    }

    public void loadFile(java.io.File file) throws java.io.IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append("\n");
        }
        loadText(buffer.toString());
        reader.close();
    }

    public void showText(final int position) {
        boolean prevVal = this.ignoreAllCaretUpdates;
        this.ignoreAllCaretUpdates = true;


        setCaretPosition(getText().length());
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SCUTextPane.this.setCaretPosition(position);
            }
        });
        this.ignoreAllCaretUpdates = false;
        this.ignoreAllCaretUpdates = prevVal;
        this.ignoreNextCaretUpdate = true;
    }

    public void loadText(String text) {
        this.ignoreAllCaretUpdates = true;
        setText(text);
        setCaretPosition(0);
        this.ignoreAllCaretUpdates = false;

        this.selectionIndexes = new short[getText().length()];
        for (int i = 0; i < this.selectionIndexes.length; i++) {
            this.selectionIndexes[i] = 0;
        }

        this.highlightIndexes = new short[getText().length()];
        for (int i = 0; i < this.highlightIndexes.length; i++) {
            this.highlightIndexes[i] = 0;
        }
    }

    public void modifyTextSelection(int startIndex, int endIndex, boolean selectionAdded) {
        int increment = selectionAdded ? 1 : -1;
        if (selectionAdded) {
            this.selectedText = null;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            int tmp35_33 = i;
            short[] tmp35_30 = this.selectionIndexes;
            tmp35_30[tmp35_33] = ((short) (tmp35_30[tmp35_33] + increment));
        }
        updateTextColors();
    }


    public void modifyTextHighlight(int startIndex, int endIndex, boolean selectionAdded) {
        int increment = selectionAdded ? 1 : -1;

        for (int i = startIndex; i <= endIndex; i++) {
            int tmp26_24 = i;
            short[] tmp26_21 = this.highlightIndexes;
            tmp26_21[tmp26_24] = ((short) (tmp26_21[tmp26_24] + increment));
            if (this.highlightIndexes[i] < 0) {
                this.highlightIndexes[i] = 0;
            }
        }
        updateTextColors();
    }

    private void updateTextColors() {
        getStyledDocument().setCharacterAttributes(0, getText().length(), getStyle("plain"), true);


        for (int i = 0; i < this.selectionIndexes.length; i++) {
            if (this.selectionIndexes[i] > 0) {
                int j;

                for (j = i + 1; (j < this.selectionIndexes.length) && (this.selectionIndexes[j] > 0); j++) {
                }


                getStyledDocument().setCharacterAttributes(i, j - i, getStyle("grayed"), true);

                i = j;
            }
        }

        for (int i = 0; i < this.highlightIndexes.length; i++) {
            if (this.highlightIndexes[i] > 0) {
                int j;

                for (j = i + 1; (j < this.highlightIndexes.length) && (this.highlightIndexes[j] > 0); j++) {
                }


                getStyledDocument().setCharacterAttributes(i, j - i - 1, getStyle("highlighted"), false);

                i = j;
            }
        }


        if ((getSelectedText() != null) && (getSelectedText().length() > 0)) {
            getStyledDocument().setCharacterAttributes(getSelectionStartIndex(), getSelectionEndIndex() - getSelectionStartIndex(), getStyle("selected"), true);
        }
    }


    public void actionPerformed(java.awt.event.ActionEvent e) {
        this.tree.selectSCUNode(Integer.parseInt(e.getActionCommand()));
    }
}