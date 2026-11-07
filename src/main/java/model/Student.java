package model;

/** Student entity (Phase 1 + Phase 2): ID, name, class, total fee and amount paid. */
public class Student {
    private final String studentId;
    private final String studentName;
    private final String className;
    private final double totalFee;
    private final double amountPaid;

    public Student(String studentId, String studentName, String className,
                   double totalFee, double amountPaid) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.totalFee = totalFee;
        this.amountPaid = amountPaid;
    }

    public String getStudentId()   { return studentId; }
    public String getStudentName() { return studentName; }
    public String getClassName()   { return className; }
    public double getTotalFee()    { return totalFee; }
    public double getAmountPaid()  { return amountPaid; }
    public double getBalance()     { return totalFee - amountPaid; }
}
