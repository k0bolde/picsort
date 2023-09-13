import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//on start, populate treelist with the folder structure of the computer
//  remember last root folder?
//


public class MainWindow {

    static private JMenuBar menuBar;
    static private JMenu menu;
    static private JMenu menuHelp;
    static private JMenuItem aboutMenuItem;
    static private JMenuItem openMenuItem;
    static private JMenuItem helpMenuItem;
    static private JMenuItem exitMenuItem;
    private JPanel panel1;
    private JButton deleteButton;
    private JTree fileTree;
    private JSplitPane mainSplit;
    private JPanel imagePanel;
    private JPanel buttonPanel;
    private JButton nextButton;
    private JButton prevButton;
    private JComboBox sortOrderComboBox;
    private JButton undoButton;
    private JLabel sortOrderLabel;
    private JLabel renameLabel;
    private JTextField renameTextField;
    private JRadioButton browseModeRadioButton;
    private JRadioButton editModeRadioButton;
    private JTextField currImageTextField;
    private JLabel totalImagesLabel;
    private JLabel imageLabel;
//    private final BufferedImage currPic;

    public MainWindow() {
        mainSplit.setDividerLocation(300);

        File fileRoot = new File("/home/kobold/Desktop");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        DefaultTreeModel model = new DefaultTreeModel(root);
        fileTree.setModel(model);
        fileTree.expandRow(0);
        fileTree.updateUI();
//        fileTree.expandPath(new TreePath(root));

        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
        new Thread(ccn).start();

//        try {
//            currPic = ImageIO.read(new File("/home/kobold/Desktop/Motherload_32_pt2_export (1).gif"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        imageLabel.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                Dimension d = imageLabel.getSize();
                ImageIcon imgIcon = new ImageIcon("/home/kobold/Desktop/Motherload_32_pt2_export (1).gif");
                imageLabel.setIcon(imgIcon);
                //TODO more logic for widescreen images going off the screen when the label isn't wide enough.
                if (imgIcon.getImage().getWidth(null) > imgIcon.getImage().getHeight(null)) {
                    imgIcon.setImage(imgIcon.getImage().getScaledInstance(-1, d.height, Image.SCALE_DEFAULT));
                } else {
                    imgIcon.setImage(imgIcon.getImage().getScaledInstance(d.width, -1, Image.SCALE_DEFAULT));
                }
                //let the user resize the window and shrink the image
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
        JFrame frame = new JFrame("PicSort");
        frame.setContentPane(new MainWindow().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setSize(1280, 720);
        frame.setVisible(true);

        menuBar = new JMenuBar();
        menu = new JMenu("File");
        openMenuItem = new JMenuItem("Open Folder");
        menu.add(openMenuItem);
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
    }

    public class CreateChildNodes implements Runnable {
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
            //TODO make leafs not show up as files
            for (File file : files) {
                if (file.isDirectory()) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
                    node.add(childNode);
                    createChildren(file, childNode);
                }
            }
        }
    }

    public class FileNode {
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

    /**
     * need a custom class for gifs to animate
     * https://stackoverflow.com/questions/10836832/show-an-animated-bg-in-swing
     */
    public class ImagePanel extends JPanel {

        private final Image image;

        public ImagePanel(Image image) {
            super();
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(this.image, 0, 0, getWidth(), getHeight(), this);
        }
    }

  /*  public void paintComponent(Graphics page)
    {
        super.paintComponent(page);

        //img from ImageIO
        int h = img.getHeight(null);
        int w = img.getWidth(null);

        // Scale Horizontally:
        if ( w > this.getWidth() )
        {
            img = img.getScaledInstance( getWidth(), -1, Image.SCALE_DEFAULT );
            h = img.getHeight(null);
        }

        // Scale Vertically:
        if ( h > this.getHeight() )
        {
            img = img.getScaledInstance( -1, getHeight(), Image.SCALE_DEFAULT );
        }

        // Center Images
        int x = (getWidth() - img.getWidth(null)) / 2;
        int y = (getHeight() - img.getHeight(null)) / 2;

        // Draw it
        page.drawImage( img, x, y, null );
    }*/

}
