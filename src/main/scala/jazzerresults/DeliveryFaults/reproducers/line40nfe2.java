package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_397fc160e23ce9d7b846e7b96e5dadab8f500e29
public class line40nfe2 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAJJwIAsLCwsLCwsLCwsLCwsLCwHwIDAgMCAwIACwIDAgMCAwIDAgMCALCwsLCwsLCwsLMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAsLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsfywsLBAsLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsf39/f39/f3/AgCwswIDAgMCAwIDAgAoqwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKn9/f39/f3/AgCwswIDAgMCAwIDAgAoqwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwsLCwsLCwsLBAqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKioqKiosLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIDAgMCAwIDAgCDAgMCAwIDAgMCAASwsLCwsLCwsLCwsLCwsECwsLMCAwIDAgMCAwIAKeA==");

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
