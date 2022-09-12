package jazzerresults.FlightDistance.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_9a8e73f7c33fb9bc0e827691dbd091d14755c561
public class line30oob {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdACtc3x8fHx8fHx8fHx8fHx8fHx8fHxzeGNvdmVyYWdlZS5vc3ZtY1FRYWdlZS5vc3ZtfHx8fHwGfHxff39/f39/f39/f39/f39/f39/f39/f2NRUVFRUVFRUVFRUVFRUVFRUSwsLCwsLCwJCmdlLm1zY292ZQ4sLCwsf39/f39/f39/f39/f39/f1FRUVFRUVFRUVFRUVFRUXx8fHx8Y292ZXJdZ2UubWVhc3VyLgp0AAB4");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetFlightDistance.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetFlightDistance.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetFlightDistance.fuzzerTestOneInput(input);
    }
}
