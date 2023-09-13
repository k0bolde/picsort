import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
//on start, populate treelist with the folder structure of the computer
//  remember last root folder?
//


public class MainWindow {

    static private JFrame frame;
    private final JMenuBar menuBar;
    private final JMenu menu;
    private final JMenu menuHelp;
    private final JMenuItem aboutMenuItem;
    private final JMenuItem openMenuItem;
    private final JMenuItem helpMenuItem;
    private final JMenuItem exitMenuItem;
    private final JMenuItem openFolderMenuItem;
    private final JFileChooser fc;
    private JPanel panel1;
    private JButton deleteButton;
    private JTree fileTree;
    private JSplitPane mainSplit;
    private JPanel imagePanel;
    private JPanel buttonPanel;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox<String> sortOrderComboBox;
    private JButton undoButton;
    private JLabel sortOrderLabel;
    private JLabel renameLabel;
    private JTextField renameTextField;
    private JTextField currImageTextField;
    private JLabel totalImagesLabel;
    private JLabel imageLabel;
    private java.util.List<File> filesInDir;
    private int imgIdx = 0;

    public MainWindow() {
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        openMenuItem = new JMenuItem("Open Images in Folder");
        openMenuItem.addActionListener(actionEvent -> {
            int ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File selected = fc.getSelectedFile();
                //TODO load up the images in the dir
                File[] found = selected.listFiles(file -> {
                    String name = file.getName().toLowerCase();
                    return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif"));
                });
                if (found != null) {
                    filesInDir = Arrays.asList(found);
                } else {
                    filesInDir = new ArrayList<>();
                }
                filesInDir.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                totalImagesLabel.setText(String.valueOf(filesInDir.size()));
                imgIdx = 0;
                currImageTextField.setText(String.valueOf(imgIdx));
                updateImg();
            }
        });
        menu.add(openMenuItem);
        openFolderMenuItem = new JMenuItem("Pick Base Folder");
        openFolderMenuItem.addActionListener(actionEvent -> {
            int ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File selected = fc.getSelectedFile();
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(selected));
                DefaultTreeModel model = new DefaultTreeModel(root);
                fileTree.setModel(model);
                CreateChildNodes ccn = new CreateChildNodes(selected, root);
                new Thread(ccn).start();
            }
        });
        menu.add(openFolderMenuItem);
        menu.addSeparator();
        exitMenuItem = new JMenuItem("Exit");
        menu.add(exitMenuItem);
        menuBar.add(menu);
        menuHelp = new JMenu("Help");
        helpMenuItem = new JMenuItem("Help");
        menuHelp.add(helpMenuItem);
        aboutMenuItem = new JMenuItem("About");
        menuHelp.add(aboutMenuItem);
        menuBar.add(menuHelp);
        frame.setJMenuBar(menuBar);
        mainSplit.setDividerLocation(300);

        File fileRoot = new File("/home/kobold/Desktop");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        DefaultTreeModel model = new DefaultTreeModel(root);
        fileTree.setModel(model);
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
        cellRenderer.setLeafIcon(null);
        fileTree.setCellRenderer(cellRenderer);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //TODO expand one folder down from root - need to run AFTER running the thread that populates it
//        DefaultMutableTreeNode currentNode = root.getNextNode();
//        do {
//            if (currentNode.getLevel() == 1)
//                fileTree.expandPath(new TreePath(currentNode.getPath()));
//            currentNode = currentNode.getNextNode();
//        } while (currentNode != null);
//        fileTree.scrollPathToVisible(new TreePath(root.getPath()));
//        fileTree.expandRow(0);

        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
        new Thread(ccn).start();

        nextButton.addActionListener(actionEvent -> {
            if (filesInDir.isEmpty()) {
                return;
            }
            if (imgIdx < filesInDir.size()) {
                imgIdx += 1;
            } else {
                imgIdx = 0;
            }
            updateImg();
        });
        prevButton.addActionListener(actionEvent -> {
            if (filesInDir.isEmpty()) {
                return;
            }
            if (imgIdx > 0) {
                imgIdx -= 1;
            } else {
                imgIdx = filesInDir.size() - 1;
            }
            updateImg();
        });
        currImageTextField.addActionListener(actionEvent -> {
            //TODO maybe only after user mouses away?
            updateImg();
        });
        sortOrderComboBox.addActionListener(actionEvent -> {
            String sortOrder = (String) sortOrderComboBox.getSelectedItem();
            if (Objects.equals(sortOrder, "Name")) {
                filesInDir.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            } else if (Objects.equals(sortOrder, "Date")) {
                //TODO catch int overflow
                filesInDir.sort((file, t1) -> Math.toIntExact(file.lastModified() - t1.lastModified()));
            } else if (Objects.equals(sortOrder, "Random")) {
                Collections.shuffle(filesInDir);
            }
        });
        renameTextField.addActionListener(actionEvent -> {
            //TODO maybe only after user mouses away?
            File oldName = filesInDir.get(imgIdx);
            File newName = new File(oldName.getPath().replace(oldName.getName(), renameTextField.getText()));
            //TODO allow undo
            filesInDir.get(imgIdx).renameTo(newName);
        });
        deleteButton.addActionListener(actionEvent -> {
            //TODO allow undo
            //TODO implement
//            filesInDir.get(imgIdx).delete();
            filesInDir.remove(imgIdx);
            if (imgIdx >= filesInDir.size()) {
                imgIdx = 0;
            }
            updateImg();
        });
        undoButton.addActionListener(actionEvent -> {
            //TODO implement
        });
        fileTree.addTreeSelectionListener(treeSelectionEvent -> {
            //move the current image to the selected folder and advance to next image
            //TODO allow undo
            //TODO implement
            File toMove = filesInDir.get(imgIdx);
//            Files.move(toMove.getPath(), fileTree.getSelectionPath());
            filesInDir.remove(imgIdx);
            if (imgIdx >= filesInDir.size()) {
                imgIdx = 0;
            }
            updateImg();
        });

        imageLabel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
//                Dimension d = imageLabel.getSize();
//                ImageIcon imgIcon = new ImageIcon("/home/kobold/Desktop/Motherload_32_pt2_export (1).gif");
//                imageLabel.setIcon(imgIcon);
//                //TODO more logic for widescreen images going off the screen when the label isn't wide enough.
//                if (imgIcon.getImage().getWidth(null) > imgIcon.getImage().getHeight(null)) {
//                    imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
//                } else {
//                    imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
//                }
                updateImg();
                //lets the user resize the window and shrink the image
                imageLabel.setMinimumSize(new Dimension(100, 100));
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
            }

            @Override
            public void componentShown(ComponentEvent componentEvent) {
            }

            @Override
            public void componentHidden(ComponentEvent componentEvent) {
            }
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("PicSort");
        frame.setContentPane(new MainWindow().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setSize(1280, 720);
        frame.setVisible(true);
    }

    public void updateImg() {
        Dimension d = imageLabel.getSize();
        ImageIcon imgIcon = new ImageIcon(filesInDir.get(imgIdx).getPath());
        imageLabel.setIcon(imgIcon);
        //TODO more logic for widescreen images going off the screen when the label isn't wide enough.
        if (imgIcon.getImage().getWidth(null) > imgIcon.getImage().getHeight(null)) {
            imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
        } else {
            imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
        }
        currImageTextField.setText(String.valueOf(imgIdx));
        renameTextField.setText(filesInDir.get(imgIdx).getName());
    }

    public static class CreateChildNodes implements Runnable {
        private final DefaultMutableTreeNode root;
        private final File fileRoot;

        public CreateChildNodes(File fileRoot, DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        @Override
        public void run() {
            createChildren(fileRoot, root);
        }

        private void createChildren(File fileRoot, DefaultMutableTreeNode node) {
            File[] files = fileRoot.listFiles();
            if (files == null) return;

            //only dirs
            ArrayList<DefaultMutableTreeNode> level = new ArrayList<>();
            //FIXME only picks up a few folders when pointed at sdb, why?
            for (File file : files) {
                if (file.isDirectory()) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
                    level.add(childNode);
                    createChildren(file, childNode);
                }
            }
            level.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
            level.forEach(node::add);
        }
    }

    public static class FileNode {
        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }
}
