package jazzerresults.CommuteTypeFull.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_71853c6197a6a7f222db0f1978c7cb232b87c5ee {// outofbounds,CommuteTypeFull.scala:29
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdAACCgp0AAB4");

    public static void main(String[] args) throws Throwable {
        Crash_71853c6197a6a7f222db0f1978c7cb232b87c5ee.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetCommuteTypeFull.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetCommuteTypeFull.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetCommuteTypeFull.fuzzerTestOneInput(input);
    }
}