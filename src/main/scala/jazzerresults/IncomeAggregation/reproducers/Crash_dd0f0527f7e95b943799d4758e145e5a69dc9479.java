package jazzerresults.IncomeAggregation.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_dd0f0527f7e95b943799d4758e145e5a69dc9479 {// numberformat,IncomeAggregation.scala:17
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdABdwIDAgMCAwIDAgMCAwIDAgMCAwIDAgH9/f39/f39/f39/f39/f39/f39/LH9/f39/f39/f39/f39/f39/fyrAgMCAwIDAgMCAf39/f39/f39/f3/AgMCAwIDAgMCAeA==");

    public static void main(String[] args) throws Throwable {
        Crash_dd0f0527f7e95b943799d4758e145e5a69dc9479.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetIncomeAggregation.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetIncomeAggregation.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetIncomeAggregation.fuzzerTestOneInput(input);
    }
}