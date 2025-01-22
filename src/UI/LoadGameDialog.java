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

    public LoadGameDialog(Frame owner) {
        super(owner, "Load Game", true);
        initializeUI();
        loadSaves();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

        // Title label
        JLabel titleLabel = new JLabel("Select a saved game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // List model and JList
        listModel = new DefaultListModel<>();
        savesList = new JList<>(listModel);
        savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savesList.setFont(new Font("Arial", Font.PLAIN, 14));

        // Add selection listener to enable/disable load button
        savesList.addListSelectionListener(e -> {
            loadButton.setEnabled(savesList.getSelectedValue() != null);
        });

        // Add double-click listener
        savesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && savesList.getSelectedValue() != null) {
                    loadSelectedGame();
                }
            }
        });

        // Scroll pane for the list
        JScrollPane scrollPane = new JScrollPane(savesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Load button
        loadButton = new JButton("Load");
        loadButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        loadButton.setEnabled(false); // Initially disabled
        loadButton.addActionListener(e -> loadSelectedGame());

        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to dialog
        add(mainPanel);

        // Handle escape key
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Set default button
        getRootPane().setDefaultButton(loadButton);
    }

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

    private void loadSelectedGame() {
        String selected = savesList.getSelectedValue();
        if (selected != null && !selected.equals("No saved games found")) {
            GameState loadedState = SaveLoadManager.loadGame(selected);
            if (loadedState != null) {
                // Close all existing game windows
                Window[] windows = Window.getWindows();
                for (Window window : windows) {
                    if (window instanceof JFrame &&
                            !(window instanceof RokueLikeMainMenu)) {
                        window.dispose();
                    }
                }

                // Create new game controller and load the state
                GameController gameController = new GameController(
                        new Domain.Hall(loadedState.getHallName(), 13, 13, 6)
                );
                gameController.loadGame(loadedState);

                // Close this dialog
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

    private void refreshSavesList() {
        loadSaves();
        savesList.revalidate();
        savesList.repaint();
    }

    public void showDialog() {
        refreshSavesList();
        setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            refreshSavesList();
        }
        super.setVisible(visible);
    }
}