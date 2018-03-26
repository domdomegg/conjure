/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.util;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.names.FieldNameWrapper;
import com.palantir.conjure.spec.FieldName;
import javax.lang.model.SourceVersion;

public final class JavaNameSanitizer {

    private static final ImmutableSet<String> RESERVED_FIELD_NAMES = ImmutableSet.of("memoizedHashCode");

    /**
     * Sanitizes the given {@link FieldName} for use as a java specifier.
     */
    public static String sanitize(FieldName fieldName) {
        String identifier = FieldNameWrapper.toCase(fieldName, FieldNameWrapper.Case.LOWER_CAMEL_CASE).get();
        return sanitize(identifier);
    }

    private static String sanitize(String name) {
        return SourceVersion.isName(name) && !RESERVED_FIELD_NAMES.contains(name) ? name : name + "_";
    }

    private JavaNameSanitizer() {}
}
