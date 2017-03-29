/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.ObjectDefinitions;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.DateTimeType;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SafeLongType;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.lib.SafeLong;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Maps the conjure type into the 'standard' java type i.e.
 * the type one would use in beans/normal variables (as opposed
 * to e.g. service definitions).
 */
public final class DefaultClassNameVisitor implements ClassNameVisitor {

    private final TypesDefinition types;
    private final ConjureImports importedTypes;

    DefaultClassNameVisitor(TypesDefinition types, ConjureImports importedTypes) {
        this.types = types;
        this.importedTypes = importedTypes;
    }

    @Override
    public TypeName visit(AnyType type) {
        return ClassName.get(Object.class);
    }

    @Override
    public TypeName visit(ListType type) {
        TypeName itemType = boxIfPrimitive(type.itemType().visit(this));
        return ParameterizedTypeName.get(ClassName.get(java.util.List.class), itemType);
    }

    @Override
    public TypeName visit(MapType type) {
        return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                boxIfPrimitive(type.keyType().visit(this)),
                boxIfPrimitive(type.valueType().visit(this)));
    }

    @Override
    public TypeName visit(OptionalType type) {
        if (type.itemType() instanceof PrimitiveType) {
            // special handling for primitive optionals with Java 8
            switch ((PrimitiveType) type.itemType()) {
                case DOUBLE:
                    return ClassName.get(OptionalDouble.class);
                case INTEGER:
                    return ClassName.get(OptionalInt.class);
                case BOOLEAN:
                    // no OptionalBoolean type
                case STRING:
                default:
                    // treat normally
            }
        }

        TypeName itemType = type.itemType().visit(this);
        if (itemType.isPrimitive()) {
            // Safe for primitives (e.g. Booleans with Java 8)
            itemType = itemType.box();
        }
        return ParameterizedTypeName.get(ClassName.get(Optional.class), itemType);
    }

    @Override
    public TypeName visit(PrimitiveType type) {
        switch (type) {
            case STRING:
                return ClassName.get(String.class);
            case DOUBLE:
                return TypeName.DOUBLE;
            case INTEGER:
                return TypeName.INT;
            case BOOLEAN:
                return TypeName.BOOLEAN;
            default:
                throw new IllegalStateException("Unknown primitive type: " + type);
        }
    }

    @Override
    public TypeName visit(ReferenceType refType) {
        if (!refType.namespace().isPresent()) {
            // Types without namespace are either defined locally in this conjure definition, or raw imports.
            BaseObjectTypeDefinition type = types.definitions().objects().get(refType.type());
            if (type != null) {
                String packageName = ObjectDefinitions.getPackageName(type.packageName(),
                        types.definitions().defaultPackage(), refType.type());
                return ClassName.get(packageName, refType.type());
            } else {
                ExternalTypeDefinition depType = types.imports().get(refType.type());
                checkNotNull(depType, "Unable to resolve type %s", refType.type());
                return ClassName.bestGuess(depType.external().get("java"));
            }
        } else {
            // Types with namespace are imported Conjure types.
            return ClassName.get(importedTypes.getPackage(refType), refType.type());
        }
    }

    @Override
    public TypeName visit(SetType type) {
        TypeName itemType = boxIfPrimitive(type.itemType().visit(this));
        return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), itemType);
    }

    @Override
    public TypeName visit(BinaryType binaryType) {
        return ClassName.get(ByteBuffer.class);
    }

    @Override
    public TypeName visit(SafeLongType safeLongType) {
        return ClassName.get(SafeLong.class);
    }

    @Override
    public TypeName visit(DateTimeType dateTimeType) {
        return ClassName.get(ZonedDateTime.class);
    }

    private static TypeName boxIfPrimitive(TypeName type) {
        if (type.isPrimitive()) {
            return type.box();
        }
        return type;
    }
}
