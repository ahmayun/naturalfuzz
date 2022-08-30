package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_c1994e94224f084229bc836d0a049af1952705f6
public class line22nfe3 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdATVwIAsLCwsLCwsLCwsLCwsLCwHwIDAgB/AgMCAwIDAgMCAwIDAgMCALCwsLCwsLCzAgCzAgCzAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAMcCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIAxwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCzAgMCAH8CAwIDAgMCAwIDAgMCAwIAsLCwsLCwsLMCALMCALMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIAxwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCwsLCwsLCwsLCwsLAfAgMCAwIDAgMCAwIDAgCwsLCwsLCwsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgDHAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAsLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLDbAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIDAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsExMTExMTExMTExMTEyzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIAswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwswIDAgMCAwIDAgApmdXp6wIDAgCwsLCwsLCw2wIDAgCzAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLDbAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIDAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsExMTExMTExMTExMTEyzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQAcCAwIAswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwswIDAgMCAwIDAgApmdXp6wIDAgCwsLCwsLCw2wIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsEAHAgMCAwIDAgMCAwIDAgApmdXp6wIDAgCwsLCwsLCwsLCwsB8CAwIDAgMCAwIDAgMCALBMTExMTExMTExMTExMswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsEAHAgMCALMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAsLCzAgMCAwIDAgMCACng=");

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
