//读取配置文件的类
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {
    private String rootDirectory;
    private Map<String, String> users = new HashMap<>();

    public void readConfig(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            rootDirectory = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                int id = Integer.parseInt(line);
                String username = reader.readLine();
                String password = reader.readLine();
                users.put(username, password);
            }
        }
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public Map<String, String> getUsers() {
        return users;
    }
}