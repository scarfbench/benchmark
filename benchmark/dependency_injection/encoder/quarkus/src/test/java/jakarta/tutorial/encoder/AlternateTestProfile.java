package jakarta.tutorial.encoder;

import java.util.Set;
import io.quarkus.test.junit.QuarkusTestProfile;

public class AlternateTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Set.of(TestCoderImpl.class);
    }

}
