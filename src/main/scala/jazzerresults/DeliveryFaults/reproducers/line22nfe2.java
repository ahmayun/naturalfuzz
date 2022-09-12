package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_63785032318c4c25f82b44edaaf8be966b1fced7
public class line22nfe2 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAKHZnV6esCAZnV6esCAwIDAgCwsLCxwLCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwsB8CAwIDAgMCAwIDAgMCALCwsLCwsLBssLCjAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAZnV6LCwsJyzAgMCAwIDAgMCALCzAgH9/fxkKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIABLCwsJyzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAASwswIAsLCwsLCwsLCwsLCwsLCwHwIDAgMCAwIAgwIDAgCwsLCwsLCwsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCALCwsLCwsGywsKMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCwswIBmdXosLCwnLMCAwIDAgMCAwIAsLMCAf39/GQrAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgAEsLCwnLMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIABLCzAgCwsLCwsLCwsLCwsLCwsLAfAgMCAwIDAgCDAgMCALCwsLCwsLCwsLMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCwswIDAgMCAwIDAgAp4");

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
