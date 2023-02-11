package jazzerresults.AgeAnalysis.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_2c1217b695fa667853b78f55d189011cad4451bd { //numberformat,AgeAnalysis.scala:17
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAC1LgJ7e3t7e3t7e3t7e3t7e3vAgMCAwIDAgMCAwIDAgMCAe3t7e3t7e397e3t7e3t7e3t7e3t7e3t7e3t7e3t7e3t7e3t7e3t/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39/f39ISEhISEhISEhISEgsSEhISEhISEhISEhISEhISEhISEhISEhISEhISEhISEhISEhISEhISEhISEh/f39/e3t7e3t7ewIEBAR7e3g=");

    public static void main(String[] args) throws Throwable {
        Crash_2c1217b695fa667853b78f55d189011cad4451bd.class.getClassLoader().setDefaultAssertionStatus(true);
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