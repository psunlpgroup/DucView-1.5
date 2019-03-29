package ducview;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SCUTree extends javax.swing.JTree implements javax.swing.event.TreeSelectionListener, java.util.Comparator, java.awt.dnd.DragGestureListener, java.awt.dnd.DragSourceListener, java.awt.dnd.DropTargetListener, java.awt.dnd.Autoscroll, javax.swing.event.TreeExpansionListener {
    private DefaultTreeModel treeModel;
    private DucView ducView;
    private SCUTextPane textPane = null;
    private SCUTextPane pyramidReferenceTextPane = null;
    private static DataFlavor SCUContributorListTransferableDataFlavor = null;
    private boolean noScrollOnNextNodeSelection = false;
    private Vector highlightedNodes = new Vector();

    public SCUTree(DucView ducView) {
        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Root Node"));
        setModel(this.treeModel);
        this.ducView = ducView;
        setRootVisible(false);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(1);
        setEditable(false);
        addTreeSelectionListener(this);
        addTreeExpansionListener(this);
        setCellRenderer(new SCUTreeCellRenderer());
        javax.swing.ToolTipManager.sharedInstance().registerComponent(this);

        if (SCUContributorListTransferableDataFlavor == null) {
            try {
                SCUContributorListTransferableDataFlavor = new DataFlavor("application/x-java-jvm-local-objectref;class=" + SCUContributorListTransferable.class.getName());

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        java.awt.dnd.DragSource.getDefaultDragSource();
        java.awt.dnd.DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, 2, this);

        new java.awt.dnd.DropTarget(this, this);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void reset() {
        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Root Node"));
        setModel(this.treeModel);
    }

    public void rebuildTree(javax.swing.tree.TreeNode root) {
        this.treeModel.setRoot(root);
        expandTree();
    }

    public void expandTree() {
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
        scrollRowToVisible(0);
    }

    public void collapseTree() {
        for (int i = 0; i < getRowCount(); i++) {
            collapseRow(i);
        }
        scrollRowToVisible(0);
    }

    public void setSCUTextPane(SCUTextPane textPane) {
        this.textPane = textPane;
    }

    public void setPyramidReferenceTextPane(SCUTextPane pyramidReferenceTextPane) {
        this.pyramidReferenceTextPane = pyramidReferenceTextPane;
    }

    public DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode) this.treeModel.getRoot();
    }


    public void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent) {
        insertNodeInto(newChild, parent, parent.getChildCount());
    }


    public void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent, int index) {
        this.noScrollOnNextNodeSelection = true;
        this.treeModel.insertNodeInto(newChild, parent, index);
        scrollPathToVisible(new TreePath(newChild.getPath()));
    }

    public void nodeChanged(javax.swing.tree.TreeNode node) {
        this.treeModel.nodeChanged(node);
    }

    public void removeNodeFromParent(DefaultMutableTreeNode node) {
        this.treeModel.removeNodeFromParent(node);
    }

    public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();


        while (this.highlightedNodes.size() > 0) {
            DefaultMutableTreeNode highlightedNode = (DefaultMutableTreeNode) this.highlightedNodes.remove(0);

            nodeChanged(highlightedNode);
        }

        this.textPane.modifyTextHighlight(0, this.textPane.getText().length() - 1, false);
        if (this.pyramidReferenceTextPane != null) {
            this.pyramidReferenceTextPane.modifyTextHighlight(0, this.pyramidReferenceTextPane.getText().length() - 1, false);
        }


        JButton addBtn = this.ducView.getAddBtn();
        JButton removeBtn = this.ducView.getRemoveBtn();
        JButton renameBtn = this.ducView.getRenameBtn();
        JButton setLabelBtn = this.ducView.getSetLabelBtn();
        JButton commentBtn = this.ducView.getCommentBtn();

        if (node == null) {
            addBtn.setEnabled(false);
            removeBtn.setEnabled(false);
            commentBtn.setEnabled(false);
            if (renameBtn != null) {
                renameBtn.setEnabled(false);
                setLabelBtn.setEnabled(false);
            }


        } else {
            if (this.pyramidReferenceTextPane != null) {
                SCU scu = (SCU) ((DefaultMutableTreeNode) node.getPath()[1]).getUserObject();
                if (scu.getId() != 0) {

                    ArrayList highlightIndexes = new ArrayList();
                    Enumeration pyramidSCUs = this.ducView.pyramidTree.getRootNode().children();
                    while (pyramidSCUs.hasMoreElements()) {
                        DefaultMutableTreeNode scuNode = (DefaultMutableTreeNode) pyramidSCUs.nextElement();

                        if (((SCU) scuNode.getUserObject()).getId() == scu.getId()) {

                            Enumeration pyramidScuContributorNodeEnum = scuNode.children();
                            while (pyramidScuContributorNodeEnum.hasMoreElements()) {
                                Iterator pyramidSCUContributorIterator = ((SCUContributor) ((DefaultMutableTreeNode) pyramidScuContributorNodeEnum.nextElement()).getUserObject()).elements();


                                while (pyramidSCUContributorIterator.hasNext()) {
                                    SCUContributorPart scuContributorPart = (SCUContributorPart) pyramidSCUContributorIterator.next();

                                    this.pyramidReferenceTextPane.modifyTextHighlight(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);


                                    highlightIndexes.add(new Integer(scuContributorPart.getStartIndex()));
                                }
                            }

                            java.util.Collections.sort(highlightIndexes);
                            this.ducView.pyramidReferenceTextPaneHighlightIndexes = new int[highlightIndexes.size()];

                            for (int i = 0; i < highlightIndexes.size(); i++) {
                                this.ducView.pyramidReferenceTextPaneHighlightIndexes[i] = ((Integer) highlightIndexes.get(i)).intValue();
                            }

                            this.ducView.currentPyramidReferenceTextPaneHighlightIndex = 0;
                            this.pyramidReferenceTextPane.showText(this.ducView.pyramidReferenceTextPaneHighlightIndexes[0]);


                            this.ducView.pyramidReferencePrevContributorBtn.setEnabled(false);
                            this.ducView.pyramidReferenceNextContributorBtn.setEnabled(this.ducView.pyramidReferenceTextPaneHighlightIndexes.length > 1);

                            break;
                        }
                    }
                }
            }

            int smallestHighlightIndex = Integer.MAX_VALUE;

            if (node.getLevel() == 1) {
                addBtn.setEnabled(true);
                removeBtn.setEnabled(true);
                commentBtn.setEnabled(true);
                if (renameBtn != null) {
                    renameBtn.setEnabled(true);
                    setLabelBtn.setEnabled(false);
                } else {
                    removeBtn.setEnabled(false);
                }

                Enumeration nodeEnum = node.children();
                while (nodeEnum.hasMoreElements()) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) nodeEnum.nextElement();

                    Iterator iterator = ((SCUContributor) childNode.getUserObject()).elements();
                    while (iterator.hasNext()) {
                        SCUContributorPart scuContributorPart = (SCUContributorPart) iterator.next();
                        this.textPane.modifyTextHighlight(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);

                        smallestHighlightIndex = Math.min(smallestHighlightIndex, scuContributorPart.getStartIndex());
                    }

                }
            } else if (node.getLevel() == 2) {
                addBtn.setEnabled(true);
                removeBtn.setEnabled(true);
                commentBtn.setEnabled(true);
                if (renameBtn != null) {
                    renameBtn.setEnabled(false);
                    setLabelBtn.setEnabled(true);
                }

                Iterator iterator = ((SCUContributor) node.getUserObject()).elements();
                while (iterator.hasNext()) {
                    SCUContributorPart scuContributorPart = (SCUContributorPart) iterator.next();
                    this.textPane.modifyTextHighlight(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);

                    smallestHighlightIndex = Math.min(smallestHighlightIndex, scuContributorPart.getStartIndex());
                }

            } else {
                addBtn.setEnabled(false);
                removeBtn.setEnabled(true);
                commentBtn.setEnabled(false);
                if (renameBtn != null) {
                    renameBtn.setEnabled(false);
                    setLabelBtn.setEnabled(false);
                }

                SCUContributorPart scuContributorPart = (SCUContributorPart) node.getUserObject();

                this.textPane.modifyTextHighlight(scuContributorPart.getStartIndex(), scuContributorPart.getEndIndex(), true);

                smallestHighlightIndex = scuContributorPart.getStartIndex();
            }


            if ((this.textPane.getSelectedText() == null) && (smallestHighlightIndex != Integer.MAX_VALUE) && (!this.noScrollOnNextNodeSelection)) {


                this.textPane.showText(smallestHighlightIndex);
            }
            this.noScrollOnNextNodeSelection = false;
        }
    }

    public void order() {
        ArrayList scuNodeList = new ArrayList();
        DefaultMutableTreeNode rootNode = getRootNode();
        Enumeration scuNodeEnum = rootNode.children();
        while (scuNodeEnum.hasMoreElements()) {
            scuNodeList.add(scuNodeEnum.nextElement());
        }
        java.util.Collections.sort(scuNodeList, this);
        Iterator scuNodeIterator = scuNodeList.iterator();
        while (scuNodeIterator.hasNext()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) scuNodeIterator.next();
            removeNodeFromParent(node);
            insertNodeInto(node, rootNode);
        }
        expandTree();
    }

    public int compare(Object o1, Object o2) {
        DefaultMutableTreeNode n1 = (DefaultMutableTreeNode) o1;
        DefaultMutableTreeNode n2 = (DefaultMutableTreeNode) o2;

        if (n1.getChildCount() < n2.getChildCount()) {
            return 1;
        }
        if (n1.getChildCount() > n2.getChildCount()) {
            return -1;
        }


        SCU scu1 = (SCU) n1.getUserObject();
        SCU scu2 = (SCU) n2.getUserObject();
        Pattern p = Pattern.compile("^\\((\\d+)\\) ");
        Matcher m1 = p.matcher(scu1.toString());
        Matcher m2 = p.matcher(scu2.toString());
        if ((m1.lookingAt()) && (m2.lookingAt())) {
            int num1 = Integer.parseInt(m1.group(1));
            int num2 = Integer.parseInt(m2.group(1));
            if (num1 < num2) {
                return 1;
            }
            if (num2 < num1) {
                return -1;
            }


            return 0;
        }


        return scu1.toString().compareToIgnoreCase(scu2.toString());
    }


    public Vector getSCUNodesContainingIndex(int index) {
        Vector scuNodes = new Vector();

        Enumeration scuNodeEnum = getRootNode().children();
        label133:
        while (scuNodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode scuNode = (DefaultMutableTreeNode) scuNodeEnum.nextElement();

            Enumeration scuContributorNodeEnum = scuNode.children();

            while (scuContributorNodeEnum.hasMoreElements()) {
                SCUContributor scuContributor = (SCUContributor) ((DefaultMutableTreeNode) scuContributorNodeEnum.nextElement()).getUserObject();


                for (int i = 0; i < scuContributor.getNumParts(); i++) {
                    SCUContributorPart scuContributorPart = scuContributor.getSCUContributorPart(i);

                    if ((scuContributorPart.getStartIndex() <= index) && (scuContributorPart.getEndIndex() >= index)) {

                        scuNodes.add(scuNode.getUserObject());

                        break label133;
                    }
                }
            }
        }
        return scuNodes;
    }

    public void selectSCUNode(int scuId) {
        Enumeration scuNodeEnum = getRootNode().children();
        while (scuNodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode scuNode = (DefaultMutableTreeNode) scuNodeEnum.nextElement();


            SCU scu = (SCU) scuNode.getUserObject();

            if (scu.getId() == scuId) {
                final TreePath path = new TreePath(scuNode.getPath());
                this.noScrollOnNextNodeSelection = true;
                setSelectionPath(path);


                scrollRowToVisible(getRowCount() - 1);
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    //private final TreePath val$path;

                    public void run() {
                        SCUTree.this.scrollPathToVisible(path);
                    }
                });
                break;
            }
        }
    }

    public int highlightSCUsNodesWithLabelmatchingPattern(Pattern pattern) {
        this.highlightedNodes.removeAllElements();
        Enumeration scuNodeEnum = getRootNode().children();
        while (scuNodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode scuNode = (DefaultMutableTreeNode) scuNodeEnum.nextElement();


            Matcher matcher = pattern.matcher(scuNode.toString());
            if (matcher.find()) {
                this.highlightedNodes.add(scuNode);
                nodeChanged(scuNode);
            }
        }
        return this.highlightedNodes.size();
    }

    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent e) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();

        if (selectedNode != null) {
            SCUContributorListTransferable scuContributors = new SCUContributorListTransferable();

            if (selectedNode.getLevel() == 2) {
                scuContributors.add(selectedNode);
            } else if (selectedNode.getLevel() == 1) {
                scuContributors.setDraggingSCU();
                Enumeration scuContributorNodes = selectedNode.children();
                while (scuContributorNodes.hasMoreElements()) {
                    scuContributors.add(scuContributorNodes.nextElement());
                }
            }
            if (scuContributors.size() > 0) {
                e.startDrag(java.awt.dnd.DragSource.DefaultMoveNoDrop, scuContributors, this);
            }
        }
    }


    public java.awt.Insets getAutoscrollInsets() {
        int margin = 10;
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new java.awt.Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y + outer.y + margin, outer.width - inner.width - inner.x + outer.x + margin);
    }


    public void autoscroll(Point p) {
        int realrow = getClosestRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();


        realrow = realrow < getRowCount() - 3 ? realrow + 3 : p.y + outer.y <= 10 ? realrow - 3 : realrow < 3 ? 0 : realrow;

        scrollRowToVisible(realrow);
    }


    public void dragExit(java.awt.dnd.DropTargetEvent e) {
    }


    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent e) {
    }


    public void dragEnter(java.awt.dnd.DropTargetDragEvent e) {
    }


    public void dragOver(java.awt.dnd.DropTargetDragEvent e) {
    }


    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent e) {
    }


    public void dragEnter(DragSourceDragEvent e) {
    }


    public void dragExit(java.awt.dnd.DragSourceEvent e) {
    }


    public void dropActionChanged(DragSourceDragEvent e) {
    }

    public void dragOver(DragSourceDragEvent e) {
        java.awt.dnd.DragSourceContext context = e.getDragSourceContext();
        Point loc = e.getLocation();
        javax.swing.SwingUtilities.convertPointFromScreen(loc, this);
        TreePath destinationPath = getPathForLocation(loc.x, loc.y);
        if (destinationPath == null) {
            context.setCursor(java.awt.dnd.DragSource.DefaultMoveNoDrop);
        } else {
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) destinationPath.getLastPathComponent();

            if (targetNode.getLevel() == 1) {
                context.setCursor(java.awt.dnd.DragSource.DefaultMoveDrop);
            } else {
                context.setCursor(java.awt.dnd.DragSource.DefaultMoveNoDrop);
            }
        }
    }

    public void drop(DropTargetDropEvent e) {
        Point loc = e.getLocation();
        TreePath destinationPath = getPathForLocation(loc.x, loc.y);
        if (destinationPath == null) {
            e.rejectDrop();
        } else {
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) destinationPath.getLastPathComponent();

            if (targetNode.getLevel() == 1) {
                SCUContributorListTransferable sourceNodeList = null;
                try {
                    sourceNodeList = (SCUContributorListTransferable) e.getTransferable().getTransferData(SCUContributorListTransferableDataFlavor);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) sourceNodeList.get(0);

                DefaultMutableTreeNode sourceSCUNode = (DefaultMutableTreeNode) sourceNode.getParent();


                if (targetNode.isNodeDescendant(sourceNode)) {
                    e.rejectDrop();


                } else if ((this.pyramidReferenceTextPane == null) && (sourceNodeList.isDraggingSCU()) && (this.ducView.draggingScuMove)) {

                    removeNodeFromParent(sourceSCUNode);
                    insertNodeInto(sourceSCUNode, getRootNode(), this.treeModel.getIndexOfChild(getRootNode(), targetNode) + 1);
                } else {
                    Iterator nodesEnum = sourceNodeList.iterator();
                    while (nodesEnum.hasNext()) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodesEnum.next();
                        removeNodeFromParent(node);
                        insertNodeInto(node, targetNode);
                    }


                    if ((sourceSCUNode.getChildCount() == 0) && (this.pyramidReferenceTextPane == null)) {
                        removeNodeFromParent(sourceSCUNode);
                    }

                    if (this.pyramidReferenceTextPane == null) {
                        this.ducView.setPyramidModified(true);
                    } else {
                        this.ducView.scoreDlg.setText(this.ducView.getScore());
                        this.ducView.setPeerModified(true);
                    }
                    e.acceptDrop(2);
                    e.dropComplete(true);
                    expandTree();
                    setSelectionPath(new TreePath(targetNode.getPath()));
                }

            } else {
                e.rejectDrop();
            }
        }
    }

    /**
     * Overrides the setFont method so the row height in the tree is adjusted
     * to accommodate changes in font size
     */
    public void setFont (Font font) {
        super.setFont(font);
        FontMetrics metrics = getFontMetrics(getFont());
        setRowHeight(metrics.getHeight());
    }

    public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
    }

    public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
        JButton btn = this.ducView.getCollapseBtn();
        btn.setText("Collapse");
        btn.setMnemonic('l');
        btn.setActionCommand("collapse");
    }

    private class SCUContributorListTransferable extends Vector implements java.awt.datatransfer.Transferable {


        boolean isDraggingSCU = false;

        public Object getTransferData(DataFlavor flavor) {
            return this;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{SCUTree.SCUContributorListTransferableDataFlavor};
        }


        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(SCUTree.SCUContributorListTransferableDataFlavor);
        }

        public void setDraggingSCU() {
            this.isDraggingSCU = true;
        }

        public boolean isDraggingSCU() {
            return this.isDraggingSCU;
        }

        private SCUContributorListTransferable() {
        }
    }

    class SCUTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer {
        public SCUTreeCellRenderer() {
            setClosedIcon(null);
            setOpenIcon(null);
            setLeafIcon(null);
        }


        public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);


            setFont(tree.getFont());

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            if (node.getLevel() == 1) {
                setFont(getFont().deriveFont(1));
            } else {
                setFont(getFont().deriveFont(0));
            }

            if ((node.getLevel() == 1) && (node.toString().equals("All non-matching SCUs go here"))) {

                setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.yellow));
            } else {
                setBorder(null);
            }

            if (SCUTree.this.highlightedNodes.contains(node)) {
                setForeground(java.awt.Color.magenta);
            }


            Object userObject = node.getUserObject();

            String comment = null;

            if ((userObject instanceof SCU)) {
                comment = ((SCU) userObject).getComment();
            } else if ((userObject instanceof SCUContributor)) {
                comment = ((SCUContributor) userObject).getComment();
            }

            if ((comment != null) && (comment.length() > 0)) {
                setToolTipText(comment);
                setText(getText() + " *");
            }

            return this;
        }
    }
}