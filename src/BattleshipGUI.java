import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Swing front end for the single-player Battleship game. */
public class BattleshipGUI extends JFrame {
    private static final int CELL_PX = 44;
    private static final Color WATER = new Color(173, 216, 230);   // light blue

    private final Game game;
    private final JButton[][] cells = new JButton[Board.SIZE][Board.SIZE];
    private final JLabel missLabel = new JLabel();
    private final JLabel strikeLabel = new JLabel();
    private final JLabel totalMissLabel = new JLabel();
    private final JLabel totalHitLabel = new JLabel();

    private final ImageIcon blankIcon = makeIcon(CellState.BLANK);
    private final ImageIcon missIcon = makeIcon(CellState.MISS);
    private final ImageIcon hitIcon = makeIcon(CellState.HIT);

    public BattleshipGUI() {
        this(new Game());
    }

    /** Lets a test or demo supply a Game with a seeded Board. */
    public BattleshipGUI(Game game) {
        super("Battleship");
        this.game = game;
        buildGui();
        refreshAll();
    }

    // ---------- GUI construction ----------

    private void buildGui() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("BATTLESHIP", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(Board.SIZE, Board.SIZE, 1, 1));
        grid.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(CELL_PX, CELL_PX));
                b.setMargin(new java.awt.Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                final int row = r;
                final int col = c;
                b.addActionListener(e -> cellClicked(row, col));
                cells[r][c] = b;
                grid.add(b);
            }
        }
        add(grid, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(4, 4));
        JPanel status = new JPanel(new GridLayout(2, 2, 6, 4));
        status.setBorder(BorderFactory.createEmptyBorder(4, 14, 0, 14));
        status.add(missLabel);
        status.add(strikeLabel);
        status.add(totalMissLabel);
        status.add(totalHitLabel);
        south.add(status, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        JButton playAgain = new JButton("Play Again");
        JButton quit = new JButton("Quit");
        playAgain.addActionListener(e -> playAgainPressed());
        quit.addActionListener(e -> quitPressed());
        buttons.add(playAgain);
        buttons.add(quit);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // ---------- event handling ----------

    private void cellClicked(int row, int col) {
        if (game.isOver()) {
            return;
        }
        ShotResult result = game.fire(row, col);
        if (result == ShotResult.ALREADY_FIRED) {
            return;
        }
        updateCell(row, col);
        updateStatus();

        if (result == ShotResult.SUNK) {
            Ship ship = game.getBoard().getShipAt(row, col);
            if (game.getStatus() != Game.Status.WON) {
                JOptionPane.showMessageDialog(this,
                    "You sunk the " + ship.getName() + "! (" + ship.getSize() + " cells)",
                    "Ship Sunk!", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        if (game.getStatus() == Game.Status.WON) {
            endOfGame("YOU WIN! All ships sunk with " + game.getTotalMiss()
                + " misses.\nPlay again?", "Victory");
        } else if (game.getStatus() == Game.Status.LOST) {
            endOfGame("GAME OVER - 3 strikes! You lose.\nPlay again?", "Defeat");
        }
    }

    private void endOfGame(String message, String title) {
        int choice = JOptionPane.showConfirmDialog(this, message, title,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            disableAllCells();
        }
    }

    private void playAgainPressed() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to start a new game?", "Play Again",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        }
    }

    private void quitPressed() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Do you want to play again?\n(Yes = new game, No = quit program)", "Quit",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            dispose();
            System.exit(0);
        }
    }

    private void startNewGame() {
        game.newGame();
        refreshAll();
    }

    // ---------- display updates ----------

    private void refreshAll() {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                updateCell(r, c);
            }
        }
        updateStatus();
    }

    private void updateCell(int r, int c) {
        JButton b = cells[r][c];
        CellState state = game.getBoard().getCellState(r, c);
        ImageIcon icon;
        switch (state) {
            case HIT:  icon = hitIcon;  break;
            case MISS: icon = missIcon; break;
            default:   icon = blankIcon; break;
        }
        b.setIcon(icon);
        b.setDisabledIcon(icon);          // keep colours when disabled
        b.setEnabled(state == CellState.BLANK);
    }

    private void disableAllCells() {
        for (JButton[] row : cells) {
            for (JButton b : row) {
                b.setEnabled(false);
            }
        }
    }

    private void updateStatus() {
        missLabel.setText("MISS counter: " + game.getMissCount() + " / "
            + Game.MISSES_PER_STRIKE);
        strikeLabel.setText("STRIKE counter: " + game.getStrikeCount() + " / "
            + Game.MAX_STRIKES);
        totalMissLabel.setText("TOTAL MISS: " + game.getTotalMiss());
        totalHitLabel.setText("TOTAL HIT: " + game.getTotalHit() + " / "
            + Board.totalShipCells());
    }

    /** Draws the cell artwork so no image files are needed. */
    private static ImageIcon makeIcon(CellState state) {
        BufferedImage img = new BufferedImage(CELL_PX, CELL_PX, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(WATER);
        g.fillRect(0, 0, CELL_PX, CELL_PX);

        if (state == CellState.BLANK) {
            g.setColor(new Color(70, 130, 180));
            g.setStroke(new BasicStroke(2f));
            g.drawArc(6, 14, 16, 12, 0, 180);
            g.drawArc(22, 14, 16, 12, 180, 180);
            g.drawArc(6, 26, 16, 12, 0, 180);
            g.drawArc(22, 26, 16, 12, 180, 180);
        } else if (state == CellState.MISS) {
            g.setColor(new Color(255, 215, 0));
            g.fillOval(8, 8, CELL_PX - 16, CELL_PX - 16);
            g.setColor(new Color(120, 90, 0));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            g.drawString("M", CELL_PX / 2 - 9, CELL_PX / 2 + 9);
        } else {
            g.setColor(new Color(255, 140, 0));
            g.fillOval(4, 4, CELL_PX - 8, CELL_PX - 8);
            g.setColor(Color.RED);
            g.fillOval(10, 10, CELL_PX - 20, CELL_PX - 20);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(4f));
            g.drawLine(14, 14, CELL_PX - 14, CELL_PX - 14);
            g.drawLine(CELL_PX - 14, 14, 14, CELL_PX - 14);
        }
        g.dispose();
        return new ImageIcon(img);
    }

    // ---------- hooks for tests / demos ----------

    /** Simulates a click on a cell (used by the screenshot harness). */
    void clickCell(int row, int col) {
        cells[row][col].doClick();
    }

    Game getGame() {
        return game;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BattleshipGUI().setVisible(true));
    }
}
