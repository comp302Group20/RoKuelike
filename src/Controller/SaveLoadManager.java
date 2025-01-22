package Controller;

import Domain.GameState;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadManager {

    public static void saveGame(GameState gameState, String saveFileName) {
        // Ensure "saves" folder exists
        File folder = new File("saves");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Enforce .rkl extension
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

    public static GameState loadGame(String saveFileName) {
        File folder = new File("saves");
        // Enforce .rkl extension
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

    public static List<String> listSaves() {
        File folder = new File("saves");
        List<String> saves = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".rkl")) {
                    // e.g. "save3.rkl" -> "save3"
                    saves.add(f.getName().replace(".rkl", ""));
                }
            }
        }
        return saves;
    }
}
