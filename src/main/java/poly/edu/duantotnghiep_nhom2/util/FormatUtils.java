package poly.edu.duantotnghiep_nhom2.util;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class FormatUtils {

    private FormatUtils() {}

    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);

    // Format tiền: 500000 -> "500.000 ₫"
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return CURRENCY_FORMATTER.format(amount);
    }

    // Tạo mã hóa đơn ngẫu nhiên (VD: INV-837492)
    public static String generateInvoiceCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return "INV-" + number;
    }
}
