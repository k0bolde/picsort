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
import java.util.*;
//TODO full test - browse, set base folder, move, rename, delete, sort order, jump to image num
//TODO Keybinds https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html for next/prev/delete image
//TODO exr hdr avif heif animated webp psd support
//TODO txt rtf pdf doc docx support for stories?
//TODO direct reverse lookup for FA, IB, youtube, other sites we can pattern match and get the post url from - tineye for other images?


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
    //keep track manually instead of checking mediaPlayer.isAncestorOf() since that doesn't seem to work?
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
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);
//        fc.setSize(800, 600);
        fc.setPreferredSize(new Dimension(700, 500));

        mediaPlayer = new CallbackMediaPlayerComponent();
        mediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                //Loop videos
                SwingUtilities.invokeLater(() -> mediaPlayer.submit(() -> mediaPlayer.media().play(mediaPlayer.media().info().mrl())));
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "ERROR! Couldn't display media " + mediaPlayer.media().info().mrl()));
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
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
            fc.setDialogTitle("Open Images in Folder");
            var ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                var selected = fc.getSelectedFile();
                var found = selected.listFiles(file -> {
                    var name = file.getName().toLowerCase();
                    return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp") || name.endsWith(".tiff") || name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".mkv") || name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac"));
                });
                if (found != null) {
                    filesInDir = new ArrayList<>(Arrays.asList(found));
                } else {
                    filesInDir = new ArrayList<>();
                }
                filesInDir.sort(Comparator.comparingLong(File::lastModified));
                totalImagesLabel.setText("/" + filesInDir.size());
                imgIdx = 0;
                currImageTextField.setText(String.valueOf(imgIdx + 1));
                sortTypeComboBox.setSelectedIndex(0);
                sortOrderComboBox.setSelectedIndex(0);
                updateImg();
                frame.setTitle("PicSort - " + selected.getPath());
            }
        });
        menu.add(openMenuItem);
        openFolderMenuItem = new JMenuItem("Pick Base Folder");
        openFolderMenuItem.addActionListener(actionEvent -> {
            fc.setDialogTitle("Pick Base Folder");
            var ret = fc.showOpenDialog(frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                var selected = fc.getSelectedFile();
                var root = new PathTreeNode(new FileNode(selected));
                var model = new DefaultTreeModel(root);
                fileTree.setModel(model);
                var ccn = new CreateChildNodes(selected, root);
//                new Thread(ccn).start();
                ccn.run();
                fileTree.expandRow(0);
            }
        });
        menu.add(openFolderMenuItem);
        //TODO menu option for controlling folder depth
        menu.addSeparator();
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(actionEvent -> {
            mediaPlayer.release();
            System.exit(0);
        });
        menu.add(exitMenuItem);
        menuBar.add(menu);
        menuHelp = new JMenu("Help");
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.addActionListener(actionEvent -> JOptionPane.showMessageDialog(frame, """
                Basic usage: File -> Pick Base Folder to select what folder root to move files into.
                Then File -> Open Images In Folder to browse the images in that folder.
                Click on a folder in the tree on the left to move the currently viewed image to that folder. Files in the destination folder with the same name are overwritten.
                Delete will move the currently viewed image to the recycle bin. WARNING full delete on linux.
                Enter an image number to jump to that image in the folder.
                Supported filetypes: jpg, png, gif, mp4, mkv, webm, webp (not animated), bmp, tiff, mp3, wav, flac
                """));
        menuHelp.add(helpMenuItem);
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(actionEvent -> JOptionPane.showMessageDialog(frame, """
                Made by k0bold
                                
                I made this because I have a large collection of furry art that's gotten disorganized
                over the years and most relevant software is focused on photography.
                                
                https://k0bold.com
                https://github.com/k0bolde"""));
        menuHelp.add(aboutMenuItem);
        menuBar.add(menuHelp);
        frame.setJMenuBar(menuBar);
        mainSplit.setDividerLocation(300);

        //TODO disable default jtree keybinds
//        for (KeyListener kl : fileTree.getKeyListeners()) {
//            fileTree.removeKeyListener(kl);
//        }
//        fileTree.setUI(new BasicTreeUI() {
//            @Override
//            protected KeyListener createKeyListener() {
//                return null;
//            }
//        });
//        InputMap inputMap = fileTree.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
//        inputMap.clear();
//        fileTree.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
//        inputMap = fileTree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        inputMap.clear();
//        fileTree.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
//        inputMap = fileTree.getInputMap(JComponent.WHEN_FOCUSED);
//        inputMap.clear();
//        fileTree.setInputMap(JComponent.WHEN_FOCUSED, inputMap);

        var fileRoot = new File(System.getProperty("user.home"));
        var root = new PathTreeNode(new FileNode(fileRoot));
        var model = new DefaultTreeModel(root);
        fileTree.setModel(model);
        var cellRenderer = new DefaultTreeCellRenderer();
        cellRenderer.setLeafIcon(null);
        cellRenderer.setOpenIcon(null);
        cellRenderer.setClosedIcon(null);
        fileTree.setCellRenderer(cellRenderer);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        var ccn = new CreateChildNodes(fileRoot, root);
        ccn.run();
        //TODO change back to threading
//        new Thread(ccn).start();
        fileTree.expandRow(0);

//        nextButton.setMnemonic(KeyEvent.VK_RIGHT);
        nextButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            if (imgIdx < filesInDir.size() - 1) {
                imgIdx += 1;
            } else {
                imgIdx = 0;
            }
            updateImg();
        });
//        prevButton.setMnemonic(KeyEvent.VK_LEFT);
        prevButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            if (imgIdx > 0) {
                imgIdx -= 1;
            } else {
                imgIdx = filesInDir.size() - 1;
            }
            updateImg();
        });
        currImageTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                try {
                    var userNum = Integer.parseInt(currImageTextField.getText()) - 1;
                    if (userNum >= 0 && userNum < filesInDir.size() - 1) {
                        imgIdx = userNum;
                    } else {
                        //just throw the same thing as a bad int so we can reuse that code
                        throw new NumberFormatException();
                    }
                    updateImg();
                } catch (NumberFormatException e) {
                    currImageTextField.setText(String.valueOf(imgIdx + 1));
                }
            }
        });
        sortOrderComboBox.addActionListener(new ChangeSort());
        sortTypeComboBox.addActionListener(new ChangeSort());
        renameTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                //highlight the basename
                renameTextField.select(0, renameTextField.getText().lastIndexOf('.'));
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
            }
        });
        renameButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            var oldName = filesInDir.get(imgIdx);
            var newName = new File(oldName.getPath().replace(oldName.getName(), renameTextField.getText()));
            System.out.println("Renamed: " + oldName.getName() + " to: " + newName.getName());
            var renamed = oldName.renameTo(newName);
            if (!renamed) {
                JOptionPane.showMessageDialog(frame, "ERROR! Could not rename file.");
            }
            filesInDir.set(imgIdx, newName);
        });
        deleteButton.setMnemonic(KeyEvent.VK_DELETE);
        deleteButton.addActionListener(actionEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            System.out.println("Deleted file at path: " + filesInDir.get(imgIdx).getPath());
            try {
                var deleted = Desktop.getDesktop().moveToTrash(filesInDir.get(imgIdx));
                if (!deleted) {
                    JOptionPane.showMessageDialog(frame, "ERROR! Could not delete file.");
                    return;
                }
            } catch (UnsupportedOperationException e) {
                try {
                    Files.delete(filesInDir.get(imgIdx).toPath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "ERROR! Could not delete file.");
                    return;

                }
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
        fileTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (filesInDir == null || filesInDir.isEmpty()) return;
                //move the current image to the selected folder and advance to next image
                var selectedNode = (PathTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    var toMove = filesInDir.get(imgIdx);
                    System.out.println("Moved file at path: " + toMove.getPath() + " to path: " + selectedNode.getFilePath().resolve(toMove.getName()));
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
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });

        frame.addWindowStateListener(windowEvent -> {
            if (filesInDir == null || filesInDir.isEmpty()) return;
            updateImg();
            //lets the user resize the window and shrink the image
            imageLabel.setMinimumSize(new Dimension(100, 100));

        });
        imageLabel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                updateImg();
                //lets the user resize the window and shrink the image
                imageLabel.setMinimumSize(new Dimension(100, 100));
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
                componentResized(componentEvent);
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
//        filesInDir.add(new File("/home/kobold/sdb/pics/porn/me!/2023/4/rep-openbrush.webp"));
//        filesInDir.add(new File("/home/kobold/sdb/Music/youtube/Don't Come Out The House (feat. 21 Savage) [523554936].mp3"));

//        filesInDir.add(new File("/home/kobold/Desktop/1.webp"));
//        filesInDir.add(new File("/home/kobold/Desktop/IMG_20230114_211628785.jpg"));
//        filesInDir.add(new File("/home/kobold/Desktop/Salazzle and scolipede sex LQ.mp4"));
        updateImg();
    }

    public static void main(String[] args) {
        frame = new JFrame("PicSort");
        frame.setContentPane(new MainWindow().panel1);
//        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
    }

    public void updateImg() {
        if (filesInDir == null || filesInDir.isEmpty()) {
            //if there are no images currently being browsed, show a specific pic. Fixes when moving/deleting last pic in a folder.
            swapImagePanel(false);
            //TODO how to access/pack this in the jar?
            ImageIcon imgIcon;
            if (getClass().getResource("/assets/nobrowse.png") != null) {
                imgIcon = new ImageIcon(getClass().getResource("/assets/nobrowse.png"));
            } else {
                imgIcon = new ImageIcon("./assets/nobrowse.png");
            }
            imageLabel.setIcon(imgIcon);
            currImageTextField.setText("0");
            renameTextField.setText("");
            rescaleImageIcon();
        } else {
            var filename = filesInDir.get(imgIdx).getName().toLowerCase();
            if (filename.endsWith(".gif") || filename.endsWith(".webp")) {
                ImageIcon imgIcon;
                if (filename.endsWith(".gif")) {
                    //use imageIcon constructor
                    imgIcon = new ImageIcon(filesInDir.get(imgIdx).getPath());
                } else {
                    //use imageIO with imageIcon
                    try {
                        imgIcon = new ImageIcon(ImageIO.read(filesInDir.get(imgIdx)));
                    } catch (IOException e) {
                        if (e.getMessage().equals("Decode returned code UnsupportedFeature")) {
                            //set image to a builtin pic saying
                            if (getClass().getResource("./assets/animatedwebperror.png") != null) {
                                imgIcon = new ImageIcon(getClass().getResource("./assets/animatedwebperror.png"));
                            } else {
                                imgIcon = new ImageIcon("./assets/animatedwebperror.png");
                            }
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
                swapImagePanel(false);
                imageLabel.setIcon(imgIcon);
                rescaleImageIcon();
            } else {
                //use vlcj
                //TODO don't restart on resize
                swapImagePanel(true);
                mediaPlayer.mediaPlayer().media().play(filesInDir.get(imgIdx).getPath());
            }
            currImageTextField.setText(String.valueOf(imgIdx + 1));
            renameTextField.setText(filesInDir.get(imgIdx).getName());
        }
        //don't let the tree get shrunk by the bully images
        mainSplit.setDividerLocation(mainSplit.getDividerLocation());
    }

    private void swapImagePanel(boolean isVlc) {
        if (isVlc) {
            if (!mediaPlayerShowing) {
                mainSplit.remove(imageLabel);
                mainSplit.add(mediaPlayer);
                mainSplit.revalidate();
                mainSplit.repaint();
                mediaPlayerShowing = true;
            }
        } else {
            if (mediaPlayerShowing) {
                mainSplit.remove(mediaPlayer);
                mainSplit.add(imageLabel);
                mainSplit.revalidate();
                mainSplit.repaint();
                mediaPlayerShowing = false;
            }
        }
    }

    public void rescaleImageIcon() {
        var d = imageLabel.getSize();
        //check if it hasn't been drawn yet
        if (d.width == 0 || d.height == 0) return;
        ImageIcon imgIcon = (ImageIcon) imageLabel.getIcon();
        if (imgIcon.getIconWidth() > imgIcon.getIconHeight()) {
            //image is wide
            if (d.width > d.height) {
                //label is wide too. use the label height
                imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
            } else {
                //label is tall. use the label width
                imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
            }
        } else {
            //image is tall
            if (d.width > d.height) {
                //label is wide. use the label height
                imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
            } else {
                //label is tall too. use the label width
                imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
            }
        }
    }

    public static class CreateChildNodes implements Runnable {
        private final PathTreeNode root;
        private final File fileRoot;

        public CreateChildNodes(File fileRoot, PathTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        private static void createChildren(File fileRoot, PathTreeNode node, int depth) {
            if (depth > 4) return;
            //only dirs
            var level = new ArrayList<PathTreeNode>();
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
            var files = fileRoot.listFiles(file1 -> file1.isDirectory() && !file1.isHidden());
            if (files == null) return;
            for (File file : files) {
                var childNode = new PathTreeNode(new FileNode(file));
                childNode.setFilePath(file.toPath());
                level.add(childNode);
                createChildren(file, childNode, depth + 1);
            }
            level.sort((a, b) -> a.toString().compareToIgnoreCase(b.toString()));
            level.forEach(node::add);
        }

        @Override
        public void run() {
            createChildren(fileRoot, root, 0);
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
            var name = file.getName();
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
            var lastFile = filesInDir.get(imgIdx);
            //we know this should never be null because we fill it out
            if (!Objects.equals(lastSort, sortOrderComboBox.getSelectedItem())) {
                lastSort = (String) sortOrderComboBox.getSelectedItem();
                lastSortOrder = "Asc";
                switch ((String) sortOrderComboBox.getSelectedItem()) {
                    case "Name" -> filesInDir.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    case "Date" -> filesInDir.sort(Comparator.comparingLong(File::lastModified));
                    case "Random" -> Collections.shuffle(filesInDir);
                    default ->
                            JOptionPane.showMessageDialog(frame, "ERROR! Unknown sort type: " + sortOrderComboBox.getSelectedItem());
                }
            }
            if (!Objects.equals(sortTypeComboBox.getSelectedItem(), lastSortOrder)) {
                lastSortOrder = (String) sortTypeComboBox.getSelectedItem();
                Collections.reverse(filesInDir);
            }
            imgIdx = filesInDir.indexOf(lastFile);
            updateImg();
        }
    }
}
