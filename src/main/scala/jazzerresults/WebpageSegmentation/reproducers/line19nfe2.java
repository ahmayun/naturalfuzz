package jazzerresults.WebpageSegmentation.reproducers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//Crash_7956291219df02fcc3be0b49fcda4e0d333e7ef2
public class line19nfe2 {
    static final String base64Bytes = String.join("", "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAACdwQAAAACdALLBCcnLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCcnJycnJ2ZhbHNlJycnJycnJycnJycnJycnJycnJycKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoBwIDAgBYKCgoKCgoaKgoKCgoKCgoKCgoKCgoKc2NvdmVyYWdlLm1lYXN1cmVtZW50cy4KCgoKCgoKCgoKCgoKCgoKCgQnJycnJycnJycnJycnJycnJycnJycnJycnJycnCgoKCgoKCgoKCgoKJycnJycnJycnJwoKCh8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHxgfHx8fHx8fHx8fCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgojIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIx8jIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMKCgoKCgoKCgoKCgoKCgoKCicnJycnJwwuegoKCgoKCgoKCgoKCgoKCgoMCgoKCgoKCgoKCgoKCgoKCgoKCicnJycnJycnJycnJycnCgoKHx8fHx8fHx8fHx8fHx8fHxcfc2NvdmVyYWdlLm1lYXN1cgwMOgwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwKCgoKCgoKCgoKCgoKCgpDCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCi56MXQAAHg=");

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
