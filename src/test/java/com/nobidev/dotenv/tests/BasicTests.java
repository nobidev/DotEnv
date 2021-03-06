package com.nobidev.dotenv.tests;

import com.nobidev.dotenv.DotEnv;
import com.nobidev.dotenv.DotEnvException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BasicTests {
    private final Map<String, String> envVars = new HashMap<>() {{
        put("MY_TEST_EV1", "my test ev 1");
        put("MY_TEST_EV2", "my test ev 2");
        put("WITHOUT_VALUE", "");
        put("MULTI_LINE", "Hello\\nWorld");
    }};

    @Test(expected = DotEnvException.class)
    public void dotenvMalformed() {
        DotEnv.configure()
                .directory("./src/test/resources")
                .load();
    }

    @Test
    public void dotenvIgnoreMalformed() {
        var dotenv = DotEnv.configure()
                .directory("./src/test/resources")
                .ignoreIfMalformed()
                .load();

        envVars.forEach((key, expected) -> {
            var actual = dotenv.get(key);
            assertEquals(expected, actual);
        });

        assertHostEnvVar(dotenv);
    }

    @Test
    public void dotenvFilename() {
        var dotenv = DotEnv.configure()
                .directory("./src/test/resources")
                .filename("env")
                .ignoreIfMalformed()
                .load();

        envVars.forEach((key, expected) -> {
            var actual = dotenv.get(key);
            assertEquals(expected, actual);
        });

        assertHostEnvVar(dotenv);
    }

    @Test
    public void resourceRelative() {
        var dotenv = DotEnv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .load();
        assertEquals("my test ev 1", dotenv.get("MY_TEST_EV1"));

        assertHostEnvVar(dotenv);
    }

    @Test
    public void resourceCurrent() {
        var dotenv = DotEnv.configure()
                .ignoreIfMalformed()
                .load();
        assertEquals("my test ev 1", dotenv.get("MY_TEST_EV1"));

        assertHostEnvVar(dotenv);
    }

    @Test
    public void resourceCurrentWithDefaultValue() {
        var dotenv = DotEnv.configure()
                .ignoreIfMalformed()
                .load();
        assertEquals("default_value", dotenv.get("MY_TEST_EV_NOT_EXIST", "default_value"));

        assertHostEnvVar(dotenv);
    }

    @Test
    public void systemProperties() {
        var dotenv = DotEnv.configure()
                .ignoreIfMalformed()
                .systemProperties()
                .load();

        assertHostEnvVar(dotenv);
        assertEquals("my test ev 1", dotenv.get("MY_TEST_EV1"));
        assertEquals("my test ev 1", System.getProperty("MY_TEST_EV1"));
        dotenv.entries().forEach(entry -> System.clearProperty(entry.getKey()));
    }

    @Test
    public void noSystemProperties() {
        var dotenv = DotEnv.configure()
                .ignoreIfMalformed()
                .load();

        assertHostEnvVar(dotenv);
        assertEquals("my test ev 1", dotenv.get("MY_TEST_EV1"));
        assertNull(System.getProperty("MY_TEST_EV1"));
    }

    @Test
    public void iterateOverDotEnv() {
        var dotenv = DotEnv.configure()
                .ignoreIfMalformed()
                .load();

        for (var e : dotenv.entries()) {
            assertEquals(dotenv.get(e.getKey()), e.getValue());
        }
    }

    @Test(expected = DotEnvException.class)
    public void dotenvMissing() {
        DotEnv.configure()
                .directory("/missing/.env")
                .load();
    }

    @Test
    public void dotenvIgnoreMissing() {
        var dotenv = DotEnv.configure()
                .directory("/missing/.env")
                .ignoreIfMissing()
                .load();

        assertHostEnvVar(dotenv);

        assertNull(dotenv.get("MY_TEST_EV1"));
    }

    private void assertHostEnvVar(DotEnv env) {
        var isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            var path = env.get("PATH");
            assertNotNull(path);
        } else {
            var expectedHome = System.getProperty("user.home");
            var actualHome = env.get("HOME");
            assertEquals(expectedHome, actualHome);
        }
    }
}
