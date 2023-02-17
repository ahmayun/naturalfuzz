package jazzerresults.StudentGrade.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_629aa4bf43ea3123b95f6a008c06060b2c2f7170 {// numberformat,StudentGrade.scala:32
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdAAOAQEBAQEBAQEDLAEBAS14");

    public static void main(String[] args) throws Throwable {
        Crash_629aa4bf43ea3123b95f6a008c06060b2c2f7170.class.getClassLoader().setDefaultAssertionStatus(true);
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