package ducview;

import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class SearchDialog extends javax.swing.JDialog implements java.awt.event.ActionListener {
    private DucView ducView;
    private javax.swing.JTextField textField;
    private String text = "";
    private boolean infoOK = false;
    private JRadioButton inputTextBtn;
    private JRadioButton scuLabelBtn;

    public SearchDialog(DucView ducView) {
        super(ducView, "Find", true);

        this.ducView = ducView;

        setDefaultCloseOperation(1);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new javax.swing.BoxLayout(contentPane, 1));
        contentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));


        contentPane.registerKeyboardAction(this, "cancel", javax.swing.KeyStroke.getKeyStroke(27, 0), 2);

        JPanel labelPanel = new JPanel();
        labelPanel.add(new javax.swing.JLabel("Enter the regular expression to search for"));
        contentPane.add(labelPanel);


        this.textField = new javax.swing.JTextField() {
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        this.textField.addActionListener(this);
        this.textField.setActionCommand("ok");
        contentPane.add(this.textField);

        JPanel radioPanel = new JPanel();
        javax.swing.ButtonGroup buttonGroup = new javax.swing.ButtonGroup();

        this.inputTextBtn = new JRadioButton("Search text");
        this.inputTextBtn.setMnemonic('t');
        buttonGroup.add(this.inputTextBtn);
        radioPanel.add(this.inputTextBtn);

        this.scuLabelBtn = new JRadioButton("Search SCU labels");
        this.scuLabelBtn.setMnemonic('l');
        buttonGroup.add(this.scuLabelBtn);
        radioPanel.add(this.scuLabelBtn);

        contentPane.add(radioPanel);

        contentPane.add(javax.swing.Box.createVerticalStrut(5));

        JPanel buttonsPanel = new JPanel();

        JButton okBtn = new JButton("OK");
        okBtn.setActionCommand("ok");
        okBtn.addActionListener(this);
        buttonsPanel.add(okBtn);

        buttonsPanel.add(javax.swing.Box.createHorizontalStrut(10));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setActionCommand("cancel");
        cancelBtn.addActionListener(this);
        buttonsPanel.add(cancelBtn);

        okBtn.setPreferredSize(cancelBtn.getPreferredSize());

        buttonsPanel.setMaximumSize(buttonsPanel.getPreferredSize());
        contentPane.add(buttonsPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent e) {
                SearchDialog.this.textField.requestFocusInWindow();
            }

        });
        pack();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            this.infoOK = true;
            this.text = this.textField.getText();
        }
        hide();
    }

    public String getSearchString() {
        this.infoOK = false;
        this.textField.setText(this.text);
        this.textField.setSelectionStart(0);
        this.textField.setSelectionEnd(this.text.length());
        this.textField.requestFocus();

        if ((!this.inputTextBtn.isSelected()) && (!this.scuLabelBtn.isSelected())) {
            if (this.ducView.isPeerLoaded) {
                this.scuLabelBtn.setSelected(true);
            } else {
                this.inputTextBtn.setSelected(true);
            }
        }

        Dimension ducViewSize = this.ducView.getSize();
        java.awt.Point topLeft = this.ducView.getLocationOnScreen();
        Dimension mySize = getSize();
        //int x;
        int x;
        if (ducViewSize.width > mySize.width) {
            x = (ducViewSize.width - mySize.width) / 2 + topLeft.x;
        } else
            x = topLeft.x;
        //int y;
        int y;
        if (ducViewSize.height > mySize.height) {
            y = (ducViewSize.height - mySize.height) / 2 + topLeft.y;
        } else
            y = topLeft.y;
        setLocation(x, y);

        show();

        if (this.infoOK) {
            return this.text;
        }
        return null;
    }

    public boolean isSearchingText() {
        return this.inputTextBtn.isSelected();
    }

    public static void main(String[] s) {
        new SearchDialog(null).show();
    }
}