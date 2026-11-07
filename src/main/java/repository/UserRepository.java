package repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import model.Student;
import model.User;

/**
 * Stores users in memory and saves them to a text file (one user per line,
 * fields separated by '|'). All methods are synchronized because many
 * request threads share one repository.
 */
public class UserRepository {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
        load();
    }

    private static String key(String username) {
        return username == null ? "" : username.toLowerCase(Locale.ROOT);
    }

    public synchronized boolean exists(String username) {
        return users.containsKey(key(username));
    }

    public synchronized User find(String username) {
        return users.get(key(username));
    }

    public synchronized boolean studentIdExists(String studentId) {
        for (User u : users.values()) {
            if (u.getStudent() != null && u.getStudent().getStudentId().equalsIgnoreCase(studentId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public synchronized void save(User user) {
        users.put(key(user.getUsername()), user);
        persist();
    }

    public synchronized boolean delete(String username) {
        if (users.remove(key(username)) == null) {
            return false;
        }
        persist();
        return true;
    }

    // ---------- file handling ----------

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split("\\|", -1);
                if (f.length != 11) {
                    continue; // skip damaged lines
                }
                Student s = f[6].isEmpty() ? null : new Student(f[6], f[7], f[8],
                        Double.parseDouble(f[9]), Double.parseDouble(f[10]));
                users.put(key(f[0]), new User(f[0], f[1], f[2], f[3], f[4], f[5], s));
            }
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Cannot read user file " + file, e);
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (User u : users.values()) {
            Student s = u.getStudent();
            lines.add(String.join("|", u.getUsername(), u.getFullName(), u.getEmail(),
                    u.getRole(), u.getSalt(), u.getPasswordHash(),
                    s == null ? "" : s.getStudentId(),
                    s == null ? "" : s.getStudentName(),
                    s == null ? "" : s.getClassName(),
                    s == null ? "" : Double.toString(s.getTotalFee()),
                    s == null ? "" : Double.toString(s.getAmountPaid())));
        }
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write user file " + file, e);
        }
    }
}
