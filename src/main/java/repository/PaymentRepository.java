package repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Payment;

/** Stores payments in memory and in payments.txt (fields separated by '|'). */
public class PaymentRepository {
    private final List<Payment> payments = new ArrayList<>();
    private final Path file;

    public PaymentRepository(Path file) {
        this.file = file;
        load();
    }

    public synchronized int count() {
        return payments.size();
    }

    public synchronized void add(Payment p) {
        payments.add(p);
        persist();
    }

    public synchronized Payment findByReceipt(String receiptNo) {
        for (Payment p : payments) {
            if (p.getReceiptNo().equals(receiptNo)) {
                return p;
            }
        }
        return null;
    }

    /** Newest payment first. */
    public synchronized List<Payment> findByUsername(String username) {
        List<Payment> list = new ArrayList<>();
        for (Payment p : payments) {
            if (p.getUsername().equalsIgnoreCase(username)) {
                list.add(0, p);
            }
        }
        return list;
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split("\\|", -1);
                if (f.length != 11) {
                    continue;
                }
                payments.add(new Payment(f[0], f[1], f[2], f[3], f[4],
                        Double.parseDouble(f[5]), f[6], f[7], LocalDateTime.parse(f[8]),
                        Double.parseDouble(f[9]), Double.parseDouble(f[10])));
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Cannot read payment file " + file, e);
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (Payment p : payments) {
            lines.add(String.join("|", p.getReceiptNo(), p.getUsername(), p.getStudentId(),
                    p.getStudentName(), p.getClassName(), Double.toString(p.getAmount()),
                    p.getMethod(), p.getTxnRef(), p.getDateTime().toString(),
                    Double.toString(p.getTotalFee()), Double.toString(p.getPaidAfter())));
        }
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write payment file " + file, e);
        }
    }
}