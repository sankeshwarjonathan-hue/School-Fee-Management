package listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import repository.PaymentRepository;
import repository.UserRepository;
import service.FeeService;
import service.UserService;

/** Runs once when Tomcat starts the application: builds the shared services. */
@WebListener
public class AppContextListener implements ServletContextListener {
    public static final String USER_SERVICE = "userService";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        try {
            Path dir = dataDirectory();
            Files.createDirectories(dir);
            UserRepository repo = new UserRepository(dir.resolve("users.txt"));
            PaymentRepository payRepo = new PaymentRepository(dir.resolve("payments.txt"));
            UserService service = new UserService(repo, payRepo, new FeeService());
            service.seedAdmin();
            ctx.setAttribute(USER_SERVICE, service);
            ctx.log("School Fee app started. Data folder: " + dir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data folder", e);
        }
    }

    /** Data lives outside the WAR so redeploying does not delete users. */
    private Path dataDirectory() {
        String custom = System.getProperty("schoolfee.data.dir");
        if (custom != null && !custom.isBlank()) {
            return Paths.get(custom);
        }
        String base = System.getProperty("catalina.base");
        if (base != null) {
            return Paths.get(base, "schoolfee-data");
        }
        return Paths.get(System.getProperty("user.home"), "schoolfee-data");
    }

}
