import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
//Work Units
//browse images - open folder, next, prev
//rename, delete
//move by clicking the tree
//browser ordering
//png, jpg, jpeg, gif, animated gif, webp work - animated webp shows error pic


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
    //dunno why it needs callbackplayer but it errors out otherwise
    private final CallbackMediaPlayerComponent mediaPlayer;
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
    private JComboBox<String> sortTypeComboBox;
    private JButton renameButton;
    private java.util.List<File> filesInDir;
    private int imgIdx = 0;
    private String lastSortOrder = "Asc";
    private String lastSort = "Date";
    private boolean mediaPlayerShowing = true;

    public MainWindow() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayer.release();
                System.exit(0);
            }
        });
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);

        mediaPlayer = new CallbackMediaPlayerComponent();
        mediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                //Loop videos
                SwingUtilities.invokeLater(() -> mediaPlayer.media().play(mediaPlayer.media().info().mrl()));
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "ERROR! Couldn't display media " + mediaPlayer.media().info().mrl()));
            }

            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {
            }

            @Override
            public void opening(MediaPlayer mediaPlayer) {
            }

            @Override
            public void buffering(MediaPlayer mediaPlayer, float v) {
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
            }

            @Override
            public void forward(MediaPlayer mediaPlayer) {
            }

            @Override
            public void backward(MediaPlayer mediaPlayer) {
            }


            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long l) {
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float v) {
            }

            @Override
            public void seekableChanged(MediaPlayer mediaPlayer, int i) {
            }

            @Override
            public void pausableChanged(MediaPlayer mediaPlayer, int i) {
            }

            @Override
            public void titleChanged(MediaPlayer mediaPlayer, int i) {
            }

            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String s) {
            }

            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long l) {
            }

            @Override
            public void videoOutput(MediaPlayer mediaPlayer, int i) {
            }

            @Override
            public void scrambledChanged(MediaPlayer mediaPlayer, int i) {
            }

            @Override
            public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i) {
            }

            @Override
            public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i) {
            }

            @Override
            public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, int i) {
            }

            @Override
            public void corked(MediaPlayer mediaPlayer, boolean b) {
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean b) {
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float v) {
            }

            @Override
            public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {
            }

            @Override
            public void chapterChanged(MediaPlayer mediaPlayer, int i) {
            }


            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
            }
        });
        mainSplit.remove(imageLabel);
        mainSplit.add(mediaPlayer);
        mainSplit.revalidate();
        mainSplit.repaint();

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        openMenuItem = new JMenuItem("Open Images in Folder");
        openMenuItem.addActionListener(actionEvent -> {
            int ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File selected = fc.getSelectedFile();
                File[] found = selected.listFiles(file -> {
                    String name = file.getName().toLowerCase();
                    return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".tiff") || name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".webp") || name.endsWith(".mkv"));
                });
                if (found != null) {
                    filesInDir = Arrays.asList(found);
                } else {
                    filesInDir = new ArrayList<>();
                }
                filesInDir.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                totalImagesLabel.setText("/" + filesInDir.size());
                imgIdx = 0;
                currImageTextField.setText(String.valueOf(imgIdx));
                sortTypeComboBox.setSelectedIndex(0);
                sortOrderComboBox.setSelectedIndex(0);
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
//                new Thread(ccn).start();
                ccn.run();
                fileTree.expandRow(0);
            }
        });
        menu.add(openFolderMenuItem);
        //TODO menu option for controlling folder depth
        menu.addSeparator();
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(actionEvent -> System.exit(0));
        menu.add(exitMenuItem);
        menuBar.add(menu);
        menuHelp = new JMenu("Help");
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.addActionListener(actionEvent -> JOptionPane.showMessageDialog(frame, "Basic usage:"));
        menuHelp.add(helpMenuItem);
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(actionEvent -> JOptionPane.showMessageDialog(frame, """
                Made by k0bold
                I made this because I have a large collection of furry art that's gotten disorganized over the years and most relevant software is focused on photography.
                https://k0bold.com
                https://github.com/k0bolde"""));
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
        cellRenderer.setOpenIcon(null);
        cellRenderer.setClosedIcon(null);
        fileTree.setCellRenderer(cellRenderer);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
        ccn.run();
        //TODO change back to threading
//        new Thread(ccn).start();
        fileTree.expandRow(0);

        nextButton.setMnemonic(KeyEvent.VK_RIGHT);
        nextButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            if (imgIdx < filesInDir.size()) {
                imgIdx += 1;
            } else {
                imgIdx = 0;
            }
            updateImg();
        });
        prevButton.setMnemonic(KeyEvent.VK_LEFT);
        prevButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            if (imgIdx > 0) {
                imgIdx -= 1;
            } else {
                imgIdx = filesInDir.size() - 1;
            }
            updateImg();
        });
        currImageTextField.addActionListener(actionEvent -> {
            try {
                int userNum = Integer.parseInt(currImageTextField.getText());
                if (userNum >= 0 && userNum < filesInDir.size() - 1) {
                    imgIdx = userNum;
                } else {
                    //just throw the same thing as a bad int so we can reuse that code
                    throw new NumberFormatException();
                }
                updateImg();
            } catch (NumberFormatException e) {
                currImageTextField.setText(String.valueOf(imgIdx));
            }
        });
        sortOrderComboBox.addActionListener(new ChangeSort());
        sortTypeComboBox.addActionListener(new ChangeSort());
        renameButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            File oldName = filesInDir.get(imgIdx);
            File newName = new File(oldName.getPath().replace(oldName.getName(), renameTextField.getText()));
            boolean renamed = filesInDir.get(imgIdx).renameTo(newName);
            if (!renamed) {
                JOptionPane.showMessageDialog(frame, "ERROR! Could not rename file.");
            }
        });
        deleteButton.setMnemonic(KeyEvent.VK_DELETE);
        deleteButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            boolean deleted = Desktop.getDesktop().moveToTrash(filesInDir.get(imgIdx));
            if (!deleted) {
                JOptionPane.showMessageDialog(frame, "ERROR! Could not delete file.");
                return;
            }
            filesInDir.remove(imgIdx);
            totalImagesLabel.setText("/" + filesInDir.size());
            if (imgIdx >= filesInDir.size()) {
                imgIdx = 0;
            }
            updateImg();
        });
        undoButton.addActionListener(actionEvent -> {
            //TODO implement. Need to keep track of file moves and renames. Dunno how to undo a delete.
        });
        fileTree.addTreeSelectionListener(treeSelectionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            //move the current image to the selected folder and advance to next image
            PathTreeNode selectedNode = (PathTreeNode) fileTree.getLastSelectedPathComponent();
            File toMove = filesInDir.get(imgIdx);
            try {
                //might need:
                //Files.copy(file, target.resolve(source.relativize(file))); //target and source are paths
                Files.move(toMove.toPath(), selectedNode.getFilePath().resolve(toMove.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "ERROR! Couldn't move file.");
                return;
            }
            filesInDir.remove(imgIdx);
            if (imgIdx >= filesInDir.size()) {
                imgIdx = 0;
            }
            updateImg();
            totalImagesLabel.setText("/" + filesInDir.size());
        });

        imageLabel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (filesInDir == null || filesInDir.isEmpty()) return;
                rescaleImageIcon();
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
        filesInDir = new ArrayList<>();
//        filesInDir.add(new File("/home/kobold/sdb/pics/porn/me!/2023/4/rep-openbrushd.gif"));
//        filesInDir.add(new File("/home/kobold/Desktop/1.webp"));
        filesInDir.add(new File("/home/kobold/Desktop/IMG_20230114_211628785.jpg"));
//        filesInDir.add(new File("/home/kobold/Desktop/Salazzle and scolipede sex LQ.mp4"));
        updateImg();
    }

    public static void main(String[] args) {
        frame = new JFrame("PicSort");
        frame.setContentPane(new MainWindow().panel1);
//        frame.pack();
        frame.setSize(1280, 720);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public void updateImg() {
        if (filesInDir == null || filesInDir.isEmpty()) return;
        String filename = filesInDir.get(imgIdx).getName().toLowerCase();
        if (filename.endsWith(".gif")) {
            //use imageIcon constructor
            if (mediaPlayerShowing) {
                mainSplit.remove(mediaPlayer);
                mainSplit.add(imageLabel);
                mainSplit.revalidate();
                mainSplit.repaint();
                mediaPlayerShowing = false;
            }
            ImageIcon imgIcon = new ImageIcon(filesInDir.get(imgIdx).getPath());
            imageLabel.setIcon(imgIcon);
            rescaleImageIcon();
        } else if (filename.endsWith(".webp")) {
            //use imageIO with imageIcon
            if (mediaPlayerShowing) {
                mainSplit.remove(mediaPlayer);
                mainSplit.add(imageLabel);
                mainSplit.revalidate();
                mainSplit.repaint();
                mediaPlayerShowing = false;
            }
            ImageIcon imgIcon;
            try {
                imgIcon = new ImageIcon(ImageIO.read(filesInDir.get(imgIdx)));
            } catch (IOException e) {
                if (e.getMessage().equals("Decode returned code UnsupportedFeature")) {
                    //set image to a builtin pic saying
                    imgIcon = new ImageIcon("./assets/animatedwebperror.png");
                } else {
                    throw new RuntimeException(e);
                }
            }
            imageLabel.setIcon(imgIcon);
            rescaleImageIcon();
        } else {
            //use vlcj
            if (!mediaPlayerShowing) {
                mainSplit.remove(imageLabel);
                mainSplit.add(mediaPlayer);
                mainSplit.revalidate();
                mainSplit.repaint();
                mediaPlayerShowing = true;
            }
            mediaPlayer.mediaPlayer().media().play(filesInDir.get(imgIdx).getPath());
        }
        currImageTextField.setText(String.valueOf(imgIdx));
        renameTextField.setText(filesInDir.get(imgIdx).getName());
    }

    public void rescaleImageIcon() {
        Dimension d = imageLabel.getSize();
        //check if it hasn't been drawn yet
        if (d.width == 0 || d.height == 0) return;
        ImageIcon imgIcon = (ImageIcon) imageLabel.getIcon();
        //TODO more logic for widescreen images going off the screen when the label isn't wide enough but the smallest dimension is still height.
        if (imgIcon.getIconWidth() > imgIcon.getIconHeight()) {
            imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
        } else {
            imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
        }
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
            createChildren(fileRoot, root, 0);
        }

        //TODO depth limit? so it doesn't freeze when loading
        private void createChildren(File fileRoot, DefaultMutableTreeNode node, int depth) {
            if (depth > 5) return;
            //only dirs
            ArrayList<DefaultMutableTreeNode> level = new ArrayList<>();
//            try {
//                Files.walkFileTree(fileRoot.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
//                    @Override
//                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//                        if (Files.isDirectory(file)) {
//                            PathTreeNode childNode = new PathTreeNode(new FileNode(file.toFile()));
//                            childNode.setFilePath(file);
//                            level.add(childNode);
//                        }
//                        return FileVisitResult.CONTINUE;
//                    }
//                });
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            File[] files = fileRoot.listFiles(File::isDirectory);
            if (files == null) return;
//            FIXME only picks up a few folders when pointed at sdb, why? Probably permissions?
            for (File file : files) {
                PathTreeNode childNode = new PathTreeNode(new FileNode(file));
                childNode.setFilePath(file.toPath());
                level.add(childNode);
                createChildren(file, childNode, depth + 1);
            }
            level.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
            level.forEach(node::add);
        }
    }

    public static class PathTreeNode extends DefaultMutableTreeNode {
        private Path filePath;

        public PathTreeNode(Object usrObj) {
            super(usrObj);
        }

        public Path getFilePath() {
            return filePath;
        }

        public void setFilePath(Path filePath) {
            this.filePath = filePath;
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
            if (name.isEmpty()) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

    /**
     * It's own class so we can reuse it for both sort comboboxes
     */
    public class ChangeSort implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            //TODO set the imgIdx to the old current image, need to search the list for it in the new order
            //we know this should never be null because we fill it out
            if (!Objects.equals(lastSort, sortOrderComboBox.getSelectedItem())) {
                lastSort = (String) sortOrderComboBox.getSelectedItem();
                lastSortOrder = "Asc";
                switch ((String) sortOrderComboBox.getSelectedItem()) {
                    case "Name" -> filesInDir.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    case "Date" ->
                        //TODO catch int overflow
                            filesInDir.sort((file, t1) -> Math.toIntExact(file.lastModified() - t1.lastModified()));
                    case "Random" -> Collections.shuffle(filesInDir);
                }
            }
            if (!Objects.equals(sortTypeComboBox.getSelectedItem(), lastSortOrder)) {
                lastSortOrder = (String) sortTypeComboBox.getSelectedItem();
                Collections.reverse(filesInDir);
            }
            updateImg();
        }
    }
}
