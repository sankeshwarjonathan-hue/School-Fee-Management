package service;

import exception.InvalidFeeException;
import java.util.ArrayList;
import java.util.List;

public class FeeService implements FeeOperations {

    @Override
    public boolean validateFee(double totalFee, double amountPaid) {
        try {
            if (totalFee <= 0) {
                throw new InvalidFeeException("Total fee must be greater than zero.");
            }
            if (amountPaid < 0) {
                throw new InvalidFeeException("Amount paid cannot be negative.");
            }
            if (amountPaid > totalFee) {
                throw new InvalidFeeException("Amount paid cannot exceed total fee.");
            }
            return true;
        } catch (InvalidFeeException e) {
            return false;
        }
    }

    @Override
    public boolean processPayment(String studentId, double totalFee, double amountPaid) {
        return validateFee(totalFee, amountPaid);
    }

    /** Fee slabs: classes 1-5, 6-8, 9-10 and 11-12. */
    @Override
    public double feeForClass(String className) throws InvalidFeeException {
        int c;
        try {
            c = Integer.parseInt(className);
        } catch (NumberFormatException e) {
            throw new InvalidFeeException("Please select a valid class.");
        }
        if (c < 1 || c > 12) {
            throw new InvalidFeeException("Please select a valid class.");
        }
        if (c <= 5)  return 25000;
        if (c <= 8)  return 35000;
        if (c <= 10) return 45000;
        return 55000;
    }

    @Override
    public List<String> classNames() {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(Integer.toString(i));
        }
        return list;
    }
}
