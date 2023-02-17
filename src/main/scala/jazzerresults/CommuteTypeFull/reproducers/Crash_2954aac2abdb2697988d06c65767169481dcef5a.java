package jazzerresults.CommuteTypeFull.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Crash_2954aac2abdb2697988d06c65767169481dcef5a {// numberformat,CommuteTypeFull.scala:23
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdADtwIDAgMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFQCwIDAgA/AgHZuVFRUVFRUVFRUVMCAwIDAgFRUVFRUVFRUVFRUVFRUVFRUVCwtLC1UVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVALAgMCAD8CAdm5UVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUAsCAwIAPwIBUVFRUVFRUVFRUVFRUVFRUVFQCwIDAgA/AgHZudAAAeA==");

    public static void main(String[] args) throws Throwable {
        Crash_2954aac2abdb2697988d06c65767169481dcef5a.class.getClassLoader().setDefaultAssertionStatus(true);
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