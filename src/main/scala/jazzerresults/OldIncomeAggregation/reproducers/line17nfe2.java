package jazzerresults.OldIncomeAggregation.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_6e3b18adc5d21bed52bd0953110f39394440e387
public class line17nfe2 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdADZHywtDQ0NDQ0NDQ0NDcCALMCAwIDAgMCAwIDAgMCACMCAwIDAgMCAwIDAgMCADMCAwIDAgMCAwIDAgMCAwIDAgMCADQ0NDQ3AgMCADQ0NDQ0NDQ0NwIDAgMCAwIB/f39/f39/f8CAwIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgMCAwIBAwIDAgMCAwIDAgMCAwIDAgEDAgMCAwIANDQ0NwIDAgDvAgAzAgMCAMMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgA0NDQ0NDQ0NDXg=");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
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
