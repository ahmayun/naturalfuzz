package jazzerresults.StudentGrade.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_371d2fa0a681a74a05f8b0d55d9462771b7600df {// runtimeexception,StudentGrade.scala:52
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAABdwQAAAABdACtf39/wIDAgH9/f39/C8CAfywyChHAgMCAwIAsMQosMQp4LDEsMQosMQp4LDExMQplLm3AgH9/f39/f39/C8CAfywyCiwxCiwxCngsMTExCmVtZS5lLDEKfywxCiwxCgHAgMCAwIDAgMCAwIDAgH9/f39/f38LwIB/LDIKLDEKLDEKeCwxMTEKZW1lLmUsMQp/LDEKLDEKAcCAwIDAgMCAwIBlZSwxCn8sMQosMQp4");

    public static void main(String[] args) throws Throwable {
        Crash_371d2fa0a681a74a05f8b0d55d9462771b7600df.class.getClassLoader().setDefaultAssertionStatus(true);
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