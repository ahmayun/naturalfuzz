package jazzerresults.CommuteTypeFull.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_3c0d90cce698156fb8e187da35b713382abfa3a0 {// numberformat,CommuteTypeFull.scala:23
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdAEGwIDAgMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFQCwIDAgA/AgHZuVFRUVFRUVFRUVMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFQCwIDAgA/AgHZuVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVALAgMCAD8CAVFRUVFRUVFRUVFRUVFRUVFRUAsCAwIAPwIB2bnQAAHg=");

    public static void main(String[] args) throws Throwable {
        Crash_3c0d90cce698156fb8e187da35b713382abfa3a0.class.getClassLoader().setDefaultAssertionStatus(true);
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