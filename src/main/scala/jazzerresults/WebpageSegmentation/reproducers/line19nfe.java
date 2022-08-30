package jazzerresults.WebpageSegmentation.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_359001026ebc90fb84b0f244948fbbdb102d2f5c
public class line19nfe {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdANLLC4jLiNlYXNxcmVhc3FyYQpzY28BHBoaGhoaHhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGnJhZ2UubRoaGhoaGhoaGhoaGhoaWhoeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHmceHh4eHh4BwIDAgMCAwIDAgMCAwIAeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4aGhoaGhpaGh4cGhoaGhoeGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoacmFnZS5tGhoaGhoaGhoaGhoaGhpaGh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eZx4eHh4eHgHAgMCAwIDAgMCAwIDAgB4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHhoaGhoaGloaHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eGh4eHh4eHh4eHh4eHh4eHh4eHh4eZx4eHh4eHgHAgMCAwIDAgMCAwIDAgB4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHhoaGhoaGloaHhwaGhoaGh4aGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhoaGhpyYWdlLm0aGhoaGhoaGhoaGhoaGloaHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh5nHh4eHh4eAcCAwIDAgMCAwIDAgMCAHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eGhoaGhoaWhoeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4aGhoaZSwachgYZS5tZWFzdXJlbWVudHNjb3ZlcmFnbWUubWUaGhplLBpyGBhlLm1lYXN1cmVtZW50c2NvdmVyYWdtZS5tZWEeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHhoaGhplLBpyGBhlLm1lYXN1cmVtZW50c2NvdmVyYWdtZS5tZWFzdXJlbWVuARwucy4KdAAAeA==");

    public static void main(String[] args) throws Throwable {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        try {
            Method fuzzerInitialize = jazzer.JazzerTargetWebpageSegmentation.class.getMethod("fuzzerInitialize");
            fuzzerInitialize.invoke(null);
        } catch (NoSuchMethodException ignored) {
            try {
                Method fuzzerInitialize = jazzer.JazzerTargetWebpageSegmentation.class.getMethod("fuzzerInitialize", String[].class);
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
        jazzer.JazzerTargetWebpageSegmentation.fuzzerTestOneInput(input);
    }
}
