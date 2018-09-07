package com.rn.aerospike.common.annotations;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.exceptions.ConversionException;
import lombok.NoArgsConstructor;
import org.atteo.evo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rahul
 */
@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StorableBin {

    String name();

    StorableType type();

    Class<? extends BinConverter> keyConverter() default Default.class;

    Class<? extends BinConverter> valueConverter() default Default.class;


    @NoArgsConstructor
    class Default implements BinConverter {

        @Override
        public Value writeInAerospike(Object source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object readFromAerospike(Object dest) throws ConversionException {
            throw new UnsupportedOperationException();
        }
    }
}
