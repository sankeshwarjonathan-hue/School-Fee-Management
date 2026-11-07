package service;

import exception.InvalidFeeException;
import java.util.List;

/** Fee operations interface (Phase 2), extended with the class fee table. */
public interface FeeOperations {
    boolean validateFee(double totalFee, double amountPaid);

    boolean processPayment(String studentId, double totalFee, double amountPaid);

    double feeForClass(String className) throws InvalidFeeException;

    List<String> classNames();
}
