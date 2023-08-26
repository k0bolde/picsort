import javax.swing.*;

public class MainWindow {

    private JPanel panel1;
    private JButton deleteButton;
    private JTree fileTree;
    private JToolBar mainToolBar;
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

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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
