/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.TypeDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.List;

public final class Retrofit2MethodTypeClassNameVisitor implements ClassNameVisitor {

    private static final ClassName REQUEST_BODY_TYPE = ClassName.get("okhttp3", "RequestBody");

    private final DefaultClassNameVisitor delegate;

    public Retrofit2MethodTypeClassNameVisitor(List<TypeDefinition> types) {
        this.delegate = new DefaultClassNameVisitor(types);
    }

    @Override
    public TypeName visitList(ListType type) {
        return delegate.visitList(type);
    }

    @Override
    public TypeName visitMap(MapType type) {
        return delegate.visitMap(type);
    }

    @Override
    public TypeName visitOptional(OptionalType type) {
        return delegate.visitOptional(type);
    }

    @Override
    public TypeName visitPrimitive(PrimitiveType type) {
        if (type.get() == PrimitiveType.Value.BINARY) {
            return REQUEST_BODY_TYPE;
        } else {
            return delegate.visitPrimitive(type);
        }
    }

    @Override
    public TypeName visitReference(com.palantir.conjure.spec.TypeName type) {
        return delegate.visitReference(type);
    }

    @Override
    public TypeName visitExternal(ExternalReference type) {
        return delegate.visitExternal(type);
    }

    @Override
    public TypeName visitSet(SetType type) {
        return delegate.visitSet(type);
    }

}
