package Controller;

import Domain.GameState;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadManager {

    /**
     * Serializes and saves the provided GameState to a file within the "saves" directory.
     * @param gameState the GameState object to serialize
     * @param saveFileName the desired name for the save file
     */
    public static void saveGame(GameState gameState, String saveFileName) {
        File folder = new File("saves");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!saveFileName.endsWith(".rkl")) {
            saveFileName += ".rkl";
        }
        File file = new File(folder, saveFileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(gameState);
            System.out.println("Game saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a GameState from a file with the given name inside the "saves" directory.
     * @param saveFileName the name of the saved file (without or with ".rkl" extension)
     * @return the deserialized GameState, or null if an error occurs
     */
    public static GameState loadGame(String saveFileName) {
        File folder = new File("saves");
        if (!saveFileName.endsWith(".rkl")) {
            saveFileName += ".rkl";
        }
        File file = new File(folder, saveFileName);
        if (!file.exists()) {
            System.out.println("Save file does not exist: " + file.getAbsolutePath());
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a list of available save files (names without the ".rkl" extension) from the "saves" folder.
     * @return a list of save file names
     */
    public static List<String> listSaves() {
        File folder = new File("saves");
        List<String> saves = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".rkl")) {
                    saves.add(f.getName().replace(".rkl", ""));
                }
            }
        }
        return saves;
    }
}
