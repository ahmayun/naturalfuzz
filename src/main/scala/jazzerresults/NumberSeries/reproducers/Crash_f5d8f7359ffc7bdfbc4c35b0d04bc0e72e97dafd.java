import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_f5d8f7359ffc7bdfbc4c35b0d04bc0e72e97dafd {// numberformat,NumberSeries.scala:17
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAAfJ10sLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCddX3g=");

    public static void main(String[] args) throws Throwable {
        Crash_f5d8f7359ffc7bdfbc4c35b0d04bc0e72e97dafd.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetNumberSeries.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetNumberSeries.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetNumberSeries.fuzzerTestOneInput(input);
    }
}