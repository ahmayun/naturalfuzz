package jazzer;


import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import examples.fuzzable.CommuteType;

public class DummyFuzzTarget {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        // This function is called inside the docker container
        // General format for fuzzing:
        //      => Write the data to a file inside the container
        //      => Pass the file path as an argument to the main func

        System.out.println("<=== DummyFuzzTarget.fuzzerTestOneInput() ===>");
        System.out.println("len (bytes): " + data.remainingBytes());
        CommuteType.main(new String[] {"/seeds/weak_seed/commute/trips"});
    }

    public static void main(String[] args) {
        System.out.println("<=== DummyFuzzTarget.main() ===>");
        CommuteType.main(new String[] {"seeds/weak_seed/commute/trips"});
    }
}
