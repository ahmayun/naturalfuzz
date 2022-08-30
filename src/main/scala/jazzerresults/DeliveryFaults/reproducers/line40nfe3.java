package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_b3cd8551e2e1c0b6136a3705651ea6810810c65f
public class line40nfe3 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAFPwIAsLCwsLCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsLCwsLAfAgMCAwIDAgMCAwIDAgCwsLCwsLCwsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCwswIDAgMCAwIDAgAp4");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetDeliveryFaults.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetDeliveryFaults.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetDeliveryFaults.fuzzerTestOneInput(input);
    }
}
