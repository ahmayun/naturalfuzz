package jazzerresults.LoanType.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_49478b69352a6d3814b041f6c5fb7e46d6bfc24f
public class line15oob {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdADYwIDAgMCAwIDAgMCAwIDAgMCAMsCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIA4wIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIA4wIDAgMCAwIDAgMCAwIDAgEvAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIAKeA==");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetLoanType.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetLoanType.class.getMethod("fuzzerInitialize", String[].class);
                fuzzerInitialize.invoke(null, (Object) args);
            } catch (NoSuchMethodException ignored1) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
        }
        com.code_intelligence.jazzer.api.CannedFuzzedDataProvider input = new com.code_intelligence.jazzer.api.CannedFuzzedDataProvider(base64Bytes);
        jazzer.JazzerTargetLoanType.fuzzerTestOneInput(input);
    }
}
