package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_6e35bb961f489d723b227c2118430e5253eab79c
public class line22nfe {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdATswIAsLCwsLCwsLCwsLCwsLCwHwIDAgB/AgMCAwIDAgMCAwIDAgMCALCwsLCwsLCzAgCzAgCzAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAMcCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIAxwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCzAgMCAH8CAwIDAgMCAwIDAgMCAwIAsLCwsLCwsLMCALMCALMCAwIDAgCzAgMCAwIAgwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgDHAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAsLCwsLCwsLCwsLCwsB8CAwIDAgMCAwIDAgMCALCwsLCwsLCwsLMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAMcCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsNsCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBABwIDAgMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsLCwsLAfAgMCAwIDAgMCAwIDAgCzAgCwTExMTExMTExMTExMTLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBDAgAHAgCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLDbAgMCALMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsNsCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBABwIDAgMCAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsLCwsLAfAgMCAwIDAgMCAwIDAgCwTExMTExMTExMTExMTLD3AgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsNsCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBABwIDAgMCAwIDAgMCAwIAKZnV6esCAwIAsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIDAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsExMTExMTExMTExMTEyzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIAswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAwIDAgMCAwIAKeA==");

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
