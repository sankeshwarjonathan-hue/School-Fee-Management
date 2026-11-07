package util;

/** Names of the attributes stored in the HttpSession. */
public final class SessionKeys {
    public static final String USERNAME  = "username";
    public static final String FULL_NAME = "fullName";
    public static final String ROLE      = "role";
    public static final String ADMIN_CSRF_TOKEN = "adminCsrfToken";

    private SessionKeys() { }
        public static final String CSRF_TOKEN = "csrfToken";   // form token for the pay button
    public static final String PAY_ERRORS = "payErrors";   // one-time error list
}
