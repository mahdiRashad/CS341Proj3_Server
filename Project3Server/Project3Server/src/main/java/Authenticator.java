import java.io.*;
import java.util.*;

public class Authenticator {
    private static final String FILE = "src/main/resources/playersInfo.txt";
    private final List<List<String>> userInfo = new ArrayList<>();

    public Authenticator() {
        loadData();
    }

    public synchronized boolean validateExistingLogin(String username, String password) {
        return userInfo.stream().anyMatch(entry -> entry.get(0).equals(username) && entry.get(1).equals(password));
    }

    public synchronized boolean createNewAccount(String username, String password) {
        for (List<String> entry : userInfo) {
            if (entry.get(0).equals(username)) return false;
        }
        userInfo.add(new ArrayList<>(Arrays.asList(username, password, "0", "0")));
        saveData();
        return true;
    }

    public synchronized void recordWin(String username) {
        for (List<String> entry : userInfo) {
            if (entry.get(0).equals(username)) {
                int wins = Integer.parseInt(entry.get(2));
                entry.set(2, String.valueOf(wins + 1));
                break;
            }
        }
        saveData();
    }

    public synchronized void recordLoss(String username) {
        for (List<String> entry : userInfo) {
            if (entry.get(0).equals(username)) {
                int losses = Integer.parseInt(entry.get(3));
                entry.set(3, String.valueOf(losses + 1));
                break;
            }
        }
        saveData();
    }

    public synchronized int[] getStats(String username) {
        for (List<String> entry : userInfo) {
            if (entry.get(0).equals(username)) {
                int wins = Integer.parseInt(entry.get(2));
                int losses = Integer.parseInt(entry.get(3));
                return new int[]{wins, losses};
            }
        }
        return new int[]{0, 0};
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE))) {
            for (List<String> entry : userInfo) {
                writer.write(String.join(",", entry));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        File file = new File(FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    List<String> entry = new ArrayList<>(Arrays.asList(parts));
                    while (entry.size() < 4) entry.add("0");
                    userInfo.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
