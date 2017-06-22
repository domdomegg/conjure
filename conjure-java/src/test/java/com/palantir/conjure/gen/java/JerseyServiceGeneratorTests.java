/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JerseyServiceGeneratorTests extends TestBase {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testServiceGeneration_exampleService() throws IOException {
        testServiceGeneration("example-service.yml");
    }

    @Test
    public void testServiceGeneration_cookieService() throws IOException {
        testServiceGeneration("cookie-service.yml");
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        File src = folder.newFolder("src");
        JerseyServiceGenerator generator = new JerseyServiceGenerator();
        generator.emit(conjure, src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "test/api/with/imports/TestService.java"))
                .contains("import test.api.StringExample;");
    }

    private void testServiceGeneration(String conjureFile) throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/" + conjureFile));
        List<Path> files = new JerseyServiceGenerator().emit(def, folder.getRoot());

        for (Path file : files) {
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                Path output = Paths.get("src/test/resources/test/api/" + file.getFileName() + ".jersey");
                Files.delete(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(readFromResource("/test/api/" + file.getFileName() + ".jersey"));
        }
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return new String(Files.readAllBytes(Paths.get(srcDir.getPath(), clazz)), StandardCharsets.UTF_8);
    }

}
