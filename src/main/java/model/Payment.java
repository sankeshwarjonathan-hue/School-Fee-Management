package model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** One fee payment. It keeps a snapshot of the fee figures so a receipt never changes later. */
public class Payment {
    private static final DateTimeFormatter SHOW =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final String receiptNo;
    private final String username;
    private final String studentId;
    private final String studentName;
    private final String className;
    private final double amount;
    private final String method;
    private final String txnRef;
    private final LocalDateTime dateTime;
    private final double totalFee;     // total fee at the time of payment
    private final double paidAfter;    // amount paid in total after this payment

    public Payment(String receiptNo, String username, String studentId, String studentName,
                   String className, double amount, String method, String txnRef,
                   LocalDateTime dateTime, double totalFee, double paidAfter) {
        this.receiptNo = receiptNo;
        this.username = username;
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.amount = amount;
        this.method = method;
        this.txnRef = txnRef;
        this.dateTime = dateTime;
        this.totalFee = totalFee;
        this.paidAfter = paidAfter;
    }

    public String getReceiptNo()        { return receiptNo; }
    public String getUsername()         { return username; }
    public String getStudentId()        { return studentId; }
    public String getStudentName()      { return studentName; }
    public String getClassName()        { return className; }
    public double getAmount()           { return amount; }
    public String getMethod()           { return method; }
    public String getTxnRef()           { return txnRef; }
    public LocalDateTime getDateTime()  { return dateTime; }
    public double getTotalFee()         { return totalFee; }
    public double getPaidAfter()        { return paidAfter; }
    public double getBalanceAfter()     { return totalFee - paidAfter; }
    public String getDateText()         { return dateTime.format(SHOW); }
}