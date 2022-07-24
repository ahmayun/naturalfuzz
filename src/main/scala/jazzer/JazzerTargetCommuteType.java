package jazzer;

import examples.fuzzable.CommuteType;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;


public class JazzerTargetCommuteType {
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        CommuteType.main(new String[] {"/seeds/weak_seed/commute/trips"});
    }

    public static void main(String[] args) {
        CommuteType.main(new String[] {"seeds/weak_seed/commute/trips"});
    }
}
