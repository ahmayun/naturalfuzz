import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_6d55ff7073eba6f11c1e22e9cab5125403ab1e40 {// runtimeexception,NumberSeries.scala:47
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAArJRrAgMCAwIDAgMCAwIDAgMCAwIAIwIDAgMCAwIDAgMCAwIAsMjU3MTgyN3g=");

    public static void main(String[] args) throws Throwable {
        Crash_6d55ff7073eba6f11c1e22e9cab5125403ab1e40.class.getClassLoader().setDefaultAssertionStatus(true);
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