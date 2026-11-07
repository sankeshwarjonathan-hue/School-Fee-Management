package service;
import exception.InvalidFeeException;
import exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import model.Payment;
import model.Student;
import model.User;
import repository.PaymentRepository;
import repository.UserRepository;
import util.HtmlUtil;
import util.PasswordUtil;

/** All server-side rules for registration and login live here. */
public class UserService {
    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASS = "Admin@123";
    public static final List<String> PAYMENT_METHODS = List.of("UPI", "Credit/Debit Card", "Net Banking");

    private static final Pattern NAME     = Pattern.compile("^[A-Za-z][A-Za-z .'-]{2,49}$");
    private static final Pattern EMAIL    = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{4,20}$");
    private static final Pattern STUDENT_ID = Pattern.compile("^[A-Za-z0-9-]{1,15}$");

    private final UserRepository repository;
    private final PaymentRepository payments;
    private final FeeOperations feeService;

    /** Hash used to keep login time similar when the username does not exist. */
    private final String dummySalt = PasswordUtil.newSalt();
    private final String dummyHash = PasswordUtil.hash("not-a-real-password", dummySalt);

   public UserService(UserRepository repository, PaymentRepository payments,
                       FeeOperations feeService) {
        this.repository = repository;
        this.payments = payments;
        this.feeService = feeService;
    }

    /** Creates the admin account the first time the application starts. */
    public void seedAdmin() {
        if (!repository.exists(DEFAULT_ADMIN_USER)) {
            String salt = PasswordUtil.newSalt();
            repository.save(new User(DEFAULT_ADMIN_USER, "School Administrator",
                    "admin@school.local", User.ROLE_ADMIN, salt,
                    PasswordUtil.hash(DEFAULT_ADMIN_PASS, salt), null));
        }
    }

    public User register(String fullName, String email, String username, String password,
                         String confirm, String studentId, String studentName,
                         String className) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (fullName == null || !NAME.matcher(fullName).matches()) {
            errors.add("Full name must be 3-50 letters (spaces, . ' - allowed).");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            errors.add("Enter a valid e-mail address.");
        }
        if (username == null || !USERNAME.matcher(username).matches()) {
            errors.add("Username must be 4-20 characters: letters, digits or underscore.");
        } else if (repository.exists(username)) {
            errors.add("That username is already taken.");
        }
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            errors.add("Password must be at least 8 characters with a letter and a digit.");
        } else if (password.length() > 100) {
            errors.add("Password is too long.");
        } else if (!password.equals(confirm)) {
            errors.add("Password and confirm password do not match.");
        }
        if (studentId == null || !STUDENT_ID.matcher(studentId).matches()) {
            errors.add("Student ID must be 1-15 letters, digits or hyphens.");
        } else if (repository.studentIdExists(studentId)) {
            errors.add("A parent is already registered for this Student ID.");
        }
        if (studentName == null || !NAME.matcher(studentName).matches()) {
            errors.add("Student name must be 3-50 letters.");
        }

        double totalFee = 0;
        try {
            totalFee = feeService.feeForClass(className);
            if (!feeService.validateFee(totalFee, 0)) {
                errors.add("Fee configuration error. Contact the school office.");
            }
        } catch (InvalidFeeException e) {
            errors.add(e.getMessage());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        String salt = PasswordUtil.newSalt();
        User user = new User(username, fullName, email, User.ROLE_PARENT, salt,
                PasswordUtil.hash(password, salt),
                new Student(studentId, studentName, className, totalFee, 0));
        repository.save(user);
        return user;
    }

    /** Checks the credentials on the server. The message never says which part was wrong. */
    public User authenticate(String username, String password) throws ValidationException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Username and password are required.");
        }
        User user = repository.find(username);
        if (user == null) {
            PasswordUtil.matches(password, dummySalt, dummyHash); // equalise timing
            throw new ValidationException("Invalid username or password.");
        }
        if (!PasswordUtil.matches(password, user.getSalt(), user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        return user;
    }

    public User findByUsername(String username) {
        return repository.find(username);
    }

    public List<User> findAllParents() {
        List<User> parents = new ArrayList<>();
        for (User u : repository.findAll()) {
            if (!u.isAdmin() && u.getStudent() != null) {
                parents.add(u);
            }
        }
        return parents;
    }

    public synchronized User saveAdminStudentRecord(String username, String parentFullName,
            String parentEmail, String password, String studentName, String className,
            String totalFeeText, String amountPaidText, String existingUsername)
            throws ValidationException {
        List<String> errors = new ArrayList<>();
        String normalizedUsername = username == null ? "" : username.trim();
        String existingKey = existingUsername == null ? "" : existingUsername.trim();
        User parent = existingKey.isEmpty() ? repository.find(normalizedUsername) : repository.find(existingKey);
        boolean editing = !existingKey.isEmpty();

        if (editing && (parent == null || parent.isAdmin() || parent.getStudent() == null)) {
            errors.add("The student record no longer exists.");
        }

        if (editing) {
            normalizedUsername = parent == null ? existingKey : parent.getUsername();
        } else {
            if (!USERNAME.matcher(normalizedUsername).matches()) {
                errors.add("Parent username must be 4-20 letters, digits or underscores.");
            }
            if (parent != null && (parent.isAdmin() || parent.getStudent() != null)) {
                errors.add("That username already belongs to an account with a student record.");
            }
        }

        boolean creatingParent = !editing && parent == null;
        String fullName = parentFullName == null ? "" : parentFullName.trim();
        String email = parentEmail == null ? "" : parentEmail.trim();
        if (creatingParent) {
            if (!NAME.matcher(fullName).matches()) {
                errors.add("Parent full name must be 3-50 letters (spaces, . ' - allowed).");
            }
            if (!email.isEmpty() && !EMAIL.matcher(email).matches()) {
                errors.add("Enter a valid parent e-mail address or leave it blank.");
            }
            if (password == null || password.length() < 8 || password.length() > 100
                    || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
                errors.add("Parent password must be 8-100 characters with a letter and a digit.");
            }
        }

        String normalizedStudentName = studentName == null ? "" : studentName.trim();
        if (!NAME.matcher(normalizedStudentName).matches()) {
            errors.add("Student name must be 3-50 letters (spaces, . ' - allowed).");
        }
        String normalizedClass = className == null ? "" : className.trim();
        try {
            feeService.feeForClass(normalizedClass);
        } catch (InvalidFeeException e) {
            errors.add(e.getMessage());
        }

        BigDecimal totalFee = parseMoney(totalFeeText, "Total fee", errors);
        BigDecimal amountPaid = parseMoney(amountPaidText, "Amount paid", errors);
        if (totalFee != null && totalFee.signum() <= 0) {
            errors.add("Total fee must be greater than zero.");
        }
        if (amountPaid != null && amountPaid.signum() < 0) {
            errors.add("Amount paid cannot be negative.");
        }
        if (totalFee != null && amountPaid != null && amountPaid.compareTo(totalFee) > 0) {
            errors.add("Amount paid cannot exceed total fee.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        String studentId = editing ? parent.getStudent().getStudentId() : newStudentId();
        Student student = new Student(studentId, normalizedStudentName, normalizedClass,
                totalFee.doubleValue(), amountPaid.doubleValue());

        if (creatingParent) {
            String salt = PasswordUtil.newSalt();
            parent = new User(normalizedUsername, fullName, email, User.ROLE_PARENT, salt,
                    PasswordUtil.hash(password, salt), student);
        } else if (parent.getStudent() == null) {
            parent = parent.withStudent(student);
        } else {
            parent = parent.withStudent(student);
        }
        repository.save(parent);
        return parent;
    }

    public synchronized void deleteAdminStudentRecord(String username) throws ValidationException {
        User parent = repository.find(username);
        if (parent == null || parent.isAdmin() || parent.getStudent() == null) {
            throw new ValidationException("The student record no longer exists.");
        }
        repository.delete(parent.getUsername());
    }

    private static BigDecimal parseMoney(String text, String field, List<String> errors) {
        try {
            BigDecimal amount = new BigDecimal(text == null ? "" : text.trim());
            if (amount.scale() > 2) {
                errors.add(field + " can have at most 2 decimal places.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            errors.add("Enter a valid " + field.toLowerCase(Locale.ROOT) + ".");
            return null;
        }
    }

    private String newStudentId() {
        String id;
        do {
            id = "STU" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 12).toUpperCase(Locale.ROOT);
        } while (repository.studentIdExists(id));
        return id;
    }
    /**
     * Records a (simulated) fee payment for a parent and updates the amount paid.
     * synchronized: two payments for the same student can never run at the same moment.
     */
    public synchronized Payment makePayment(String username, String amountText, String method)
            throws ValidationException {
        User user = repository.find(username);
        if (user == null || user.isAdmin() || user.getStudent() == null) {
            throw new ValidationException("Only parent accounts can pay fees.");
        }
        Student st = user.getStudent();
        List<String> errors = new ArrayList<>();

        if (method == null || !PAYMENT_METHODS.contains(method)) {
            errors.add("Choose a payment method.");
        }

        BigDecimal balance = BigDecimal.valueOf(st.getBalance()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountText == null ? "" : amountText.trim());
            if (amount.scale() > 2) {
                errors.add("Amount can have at most 2 decimal places.");
            } else if (amount.signum() <= 0) {
                errors.add("Amount must be greater than zero.");
            } else if (balance.signum() <= 0) {
                errors.add("No fee is due. The fee is fully paid.");
            } else if (amount.compareTo(balance) > 0) {
                errors.add("Amount cannot be more than the balance due (" + HtmlUtil.money(balance.doubleValue()) + ").");
            }
        } catch (NumberFormatException e) {
            errors.add("Enter a valid amount.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        double newPaid = BigDecimal.valueOf(st.getAmountPaid()).add(amount).doubleValue();
        if (!feeService.processPayment(st.getStudentId(), st.getTotalFee(), newPaid)) {
            throw new ValidationException("The payment could not be processed.");
        }

        // Update the student's record first, then write the receipt
        Student updated = new Student(st.getStudentId(), st.getStudentName(), st.getClassName(),
                st.getTotalFee(), newPaid);
        repository.save(user.withStudent(updated));

        LocalDateTime now = LocalDateTime.now();
        String receiptNo = "RCPT-" + now.format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + String.format("%04d", payments.count() + 1);
        String txnRef = "SIM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        Payment payment = new Payment(receiptNo, user.getUsername(), st.getStudentId(),
                st.getStudentName(), st.getClassName(), amount.doubleValue(), method, txnRef,
                now, st.getTotalFee(), newPaid);
        payments.add(payment);
        return payment;
    }

    public List<Payment> findPayments(String username) {
        return payments.findByUsername(username);
    }

    public Payment findPayment(String receiptNo) {
        return receiptNo == null ? null : payments.findByReceipt(receiptNo);
    }

    public FeeOperations getFeeService() {
        return feeService;
    }
}
