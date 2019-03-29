package ducview;

import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JTextPane;

public class ScoreDialog extends javax.swing.JDialog {
    private DucView ducView;
    private JTextPane scoreTextPane;

    public ScoreDialog(DucView ducView) {
        super(ducView, "Peer annotation score", false);

        this.ducView = ducView;

        setDefaultCloseOperation(0);
        addWindowListener(new ScoreDialogWindowAdapter(this));
        this.scoreTextPane = new JTextPane() {
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }

        };
        this.scoreTextPane = new ScoreTextPane();
        this.scoreTextPane.setEditable(false);
        this.scoreTextPane.setContentType("text/html");
        getContentPane().add(new javax.swing.JScrollPane(this.scoreTextPane));
    }

    public void setText(String text) {
        this.scoreTextPane.setText(text);
    }

    private class ScoreDialogWindowAdapter extends java.awt.event.WindowAdapter {
        ScoreDialog scoreDlg;

        public ScoreDialogWindowAdapter(ScoreDialog scoreDlg) {
            this.scoreDlg = scoreDlg;
        }

        public void windowClosing(java.awt.event.WindowEvent e) {
            this.scoreDlg.setVisible(false);
            this.scoreDlg.ducView.fileShowPeerAnnotationScoreMenuItem.setSelected(false);
        }
    }

    private class ScoreTextPane extends JTextPane {


        public void setSize(Dimension d) {
            if (d.width < getParent().getSize().width) {
                d.width = getParent().getSize().width;
            }
            super.setSize(d);
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        private ScoreTextPane() {
        }
    }
}