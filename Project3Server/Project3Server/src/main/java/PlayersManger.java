import java.io.*;
import java.util.*;

public class PlayersManger {
    private static final String FILE = "src/main/resources/players.txt";
    private final List<List<String>> userData = new ArrayList<>();

    public PlayersManger() {
        load();
    }

    public synchronized boolean validateLogin(String username, String password) {
        return userData.stream().anyMatch(entry -> entry.get(0).equals(username) && entry.get(1).equals(password));
    }

    public synchronized boolean createAccount(String username, String password) {
        for (List<String> entry : userData) {
            if (entry.get(0).equals(username)) return false;
        }
        userData.add(Arrays.asList(username, password));
        save();
        return true;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE))) {
            for (List<String> entry : userData) {
                writer.write(entry.get(0) + "," + entry.get(1));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        File file = new File(FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    userData.add(Arrays.asList(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
