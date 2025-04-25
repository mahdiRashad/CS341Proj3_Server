import java.io.*;
import java.util.*;

public class Authenticator {
    private static final String INFO_FILE = "src/main/resources/playersInfo.txt";
    private final List<List<String>> userInfo = new ArrayList<>();
    private final Set<String> online = new HashSet<>();

    public Authenticator() {
        loadData();
    }

    public synchronized boolean validateExistingLogin(String username, String password) {
        for (List<String> info : userInfo) {
            if (info.get(0).equals(username) && info.get(1).equals(password)) {
                if (online.contains(username)) return false;
                online.add(username);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean createNewAccount(String username, String password) {
        for (List<String> info : userInfo) {
            if (info.get(0).equals(username)) return false;
        }
        userInfo.add(new ArrayList<>(Arrays.asList(username, password, "0", "0")));
        online.add(username);
        saveData();
        return true;
    }

    public synchronized void logout(String username) {
        online.remove(username);
    }

    public synchronized void recordWin(String username) {
        for (List<String> info : userInfo) {
            if (info.get(0).equals(username)) {
                int numberOfWins = Integer.parseInt(info.get(2));
                info.set(2, String.valueOf(numberOfWins + 1));
                break;
            }
        }
        saveData();
    }

    public synchronized void recordLoss(String username) {
        for (List<String> info : userInfo) {
            if (info.get(0).equals(username)) {
                int numberOfLosses = Integer.parseInt(info.get(3));
                info.set(3, String.valueOf(numberOfLosses + 1));
                break;
            }
        }
        saveData();
    }

    public synchronized int[] getStats(String username) {
        for (List<String> info : userInfo) {
            if (info.get(0).equals(username)) {
                int numberOfWins = Integer.parseInt(info.get(2));
                int numberOfLosses = Integer.parseInt(info.get(3));
                return new int[]{numberOfWins, numberOfLosses};
            }
        }
        return new int[]{0, 0};
    }

    private void saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INFO_FILE))) {
            for (List<String> info : userInfo) {
                writer.write(String.join(",", info));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        File loadFile = new File(INFO_FILE);
        if (!loadFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(INFO_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] indexes = line.split(",");
                if (indexes.length >= 2) {
                    List<String> entry = new ArrayList<>(Arrays.asList(indexes));
                    while (entry.size() < 4) entry.add("0");
                    userInfo.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
