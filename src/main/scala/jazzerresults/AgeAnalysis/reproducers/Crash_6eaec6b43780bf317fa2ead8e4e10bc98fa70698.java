package jazzerresults.AgeAnalysis.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_6eaec6b43780bf317fa2ead8e4e10bc98fa70698 {//numberformat,AgeAnalysis.scala:17
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAAgJCQkJCRqJGokJCokJCQkJCQkJCQkJCQkJFxbW1YkLC14");

    public static void main(String[] args) throws Throwable {
        Crash_6eaec6b43780bf317fa2ead8e4e10bc98fa70698.class.getClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetAgeAnalysis.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetAgeAnalysis.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetAgeAnalysis.fuzzerTestOneInput(input);
    }
}