package jazzerresults.DeliveryFaults.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_e83073f0773bc7f922f2061c419028c94558e7e8
public class line40nfe4 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAM7wIAsLCwsLCwsLCwsLCwsLCwHwIDAgMCAwIDAgMCAwIAsLCwsLCwsLCwswIDAgMCAwIDAgCwswIDAgMCAwIDAgArAgMCAwIAxwIDAgMCAIMCAc2NvdmVyYWdlLm1lYXN1cmVtZW50cy5lcmFnZS5tZWFzdXJlbWVudHMuAcCAwIDAgMCAwIDAgDk5OcCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIA5OTk5OTk5BDk5OSk5OTk5OTk5OTk5OTk5OTk5OTk5OTk5OTl/f39/f39/fxAsLCzAgMCAwIDAgMCACmZ1enrAgMCALCwsKywsLMCAwIDAgMCAwIAsLMCAwIDAgMCAwIAKwIDAgMCAwIDAgMCAwIAgwIDAgMCAwIDAgAEsLCwsLCwswIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAwIDAgMCAOTk5OTk5OQQ5OTkpOTk5OTk5OTk5OTk5OTk5OTk5OTk5OTk5f39/f39/f38QLCwswIDAgMCAwIDAgApmdXp6wIDAgCwsLCwsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIABLCwsLCwsLCwsLCwsLCwQLCwsf39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f8CAwIDAgMCAwIAKZnV6esCAwIAsLCwsLCwsLCwsLAfAgMCAwIDAgMCAwIDAgCwsLCwsLCwsLCzAgMCAwIDAgMCALCzAgMCAwIDAgMCACsCAwIDAgMCAwIDAgMCAIMCAwIDAgMCAwIB/f39/f385OWFzASwsLCwsLCwsLCwsLHVyZW1lbnRzLCwQwIAsLCzAgMCAwIDAgMCAwIDAgAp4");

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
