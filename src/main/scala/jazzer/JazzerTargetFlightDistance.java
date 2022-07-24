package jazzer;

import examples.fuzzable.CommuteType;

public class JazzerTargetFlightDistance {

    public static void fuzzerTestOneInput(byte[] input) {
        CommuteType.main(new String[] {"/seeds/weak_seed/commute/trips"});
    }

}
