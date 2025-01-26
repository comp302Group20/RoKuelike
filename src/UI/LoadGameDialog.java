package UI;

import Controller.GameController;
import Controller.SaveLoadManager;
import Domain.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * A dialog that displays a list of saved games and allows loading of a selected save.
 */
public class LoadGameDialog extends JDialog {
    private JList<String> savesList;
    private DefaultListModel<String> listModel;
    private JButton loadButton;
    private JButton cancelButton;
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 300;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BORDER_SIZE = 20;

    /**
     * Constructs a modal LoadGameDialog with the given parent frame as owner.
     * @param owner the parent frame for this dialog
     */
    public LoadGameDialog(Frame owner) {
        super(owner, "Load Game", true);
        initializeUI();
        loadSaves();
        setLocationRelativeTo(owner);
    }

    /**
     * Sets up the UI components, including the list of saved games and the load/cancel buttons.
     */
    private void initializeUI() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

        JLabel titleLabel = new JLabel("Select a saved game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        savesList = new JList<>(listModel);
        savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savesList.setFont(new Font("Arial", Font.PLAIN, 14));

        savesList.addListSelectionListener(e -> {
            loadButton.setEnabled(savesList.getSelectedValue() != null);
        });

        savesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && savesList.getSelectedValue() != null) {
                    loadSelectedGame();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(savesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        loadButton = new JButton("Load");
        loadButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        loadButton.setEnabled(false);
        loadButton.addActionListener(e -> loadSelectedGame());

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().setDefaultButton(loadButton);
    }

    /**
     * Loads the list of available save files from the SaveLoadManager and populates the JList.
     */
    private void loadSaves() {
        listModel.clear();
        List<String> saves = SaveLoadManager.listSaves();

        if (saves.isEmpty()) {
            listModel.addElement("No saved games found");
            savesList.setEnabled(false);
            loadButton.setEnabled(false);
        } else {
            saves.forEach(listModel::addElement);
            savesList.setEnabled(true);
        }
    }

    /**
     * Loads the game state for the selected save file, then launches the game with that state.
     */
    private void loadSelectedGame() {
        String selected = savesList.getSelectedValue();
        if (selected != null && !selected.equals("No saved games found")) {
            GameState loadedState = SaveLoadManager.loadGame(selected);
            if (loadedState != null) {
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof JFrame &&
                            !(window instanceof RokueLikeMainMenu)) {
                        window.dispose();
                    }
                }

                GameController gameController = new GameController(
                        new Domain.Hall(loadedState.getHallName(), 13, 13, 6)
                );
                gameController.loadGame(loadedState);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load save file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Refreshes the list of saves to ensure the UI is up to date.
     */
    private void refreshSavesList() {
        loadSaves();
        savesList.revalidate();
        savesList.repaint();
    }

    /**
     * Displays the LoadGameDialog, updating the list of saves first.
     */
    public void showDialog() {
        refreshSavesList();
        setVisible(true);
    }

    /**
     * Sets this dialog's visibility. When made visible, refreshes the saves list.
     * @param visible true to make visible, false to hide
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            refreshSavesList();
        }
        super.setVisible(visible);
    }
}
