package jazzerresults.StudentGrade.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_a1601b963bb9108382d6141e230e9c32fe17c076
public class line21rte {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdADMf39/f39/f391MDAwMDAwMCwwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMTAwMDAwMDAsMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMBcwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwwIDAgGFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFbMDAwMDAwMDAwMDAwMAbAgDAwMDAwMDAwMDAwMDAwMDAwf39/f39/f39/eA==");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetStudentGrade.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetStudentGrade.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetStudentGrade.fuzzerTestOneInput(input);
    }
}
