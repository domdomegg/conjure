/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.QueryParameterType;
import com.palantir.conjure.spec.Type;
import java.util.List;
import org.junit.Test;

public final class ParamIdValidatorTest {
    private static final ArgumentName PARAMETER_NAME = ArgumentName.of("arg");

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValidNonHeader() {
        for (String paramId : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            // Passes validation
            createEndpoint(ParameterType.query(QueryParameterType.of(ParameterId.of(paramId))));
        }
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValidHeader() {
        List<String> paramIds = ImmutableList.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.X_XSS_PROTECTION,
                HttpHeaders.P3P,
                HttpHeaders.SET_COOKIE2);
        for (String paramId : paramIds) {
            // Passes validation
            createEndpoint(ParameterType.header(HeaderParameterType.of(ParameterId.of(paramId))));
        }
    }

    @Test
    public void testInvalidNonHeader() {
        for (String paramId : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            ParameterType parameterType = ParameterType.query(QueryParameterType.of(ParameterId.of(paramId)));
            assertThatThrownBy(() -> createEndpoint(parameterType))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            parameterType,
                            EndpointDefinitionValidator.ANCHORED_PATTERN,
                            paramId);
        }
    }

    @Test
    public void testInvalidHeader() {
        for (String paramId : ImmutableList.of("authorization", "123", "Foo_Bar", "Foo.Bar")) {
            ParameterType parameterType = ParameterType.header(HeaderParameterType.of(ParameterId.of(paramId)));
            assertThatThrownBy(() -> createEndpoint(parameterType))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            parameterType,
                            EndpointDefinitionValidator.HEADER_PATTERN,
                            paramId);
        }
    }

    private EndpointDefinition createEndpoint(ParameterType paramType) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .argName(PARAMETER_NAME)
                .paramType(paramType)
                .type(Type.primitive(PrimitiveType.ANY))
                .build();
        EndpointDefinition definition = EndpointDefinition.builder()
                .httpMethod(HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"))
                .args(arg)
                .endpointName(EndpointName.of("test"))
                .build();

        EndpointDefinitionValidator.validateAll(definition);
        return definition;
    }
}
