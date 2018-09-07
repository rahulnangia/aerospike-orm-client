package com.rn.aerospike.client.test;

import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.annotations.StorableRecord;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.records.impl.AerospikeRecordStringKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rahul
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@StorableRecord(setname = "test")
@ToString
public class AerospikeTestErrorEntityA implements AerospikeRecordStringKey {

    @StorableBin(name = "string", type = StorableType.STRING)
    private String value1;

    @StorableBin(name = "string", type = StorableType.INT)
    private Integer valueInt;

    @StorableBin(name = "set", type = StorableType.SET)
    private Set<String> value2;

    @StorableBin(name = "double", type = StorableType.DOUBLE)
    private Double value3;

    @StorableBin(name = "float", type = StorableType.FLOAT)
    private Float value4;

    @StorableBin(name = "int", type = StorableType.INT)
    private int value5;

    @StorableBin(name = "long", type = StorableType.LONG)
    private long value6;

    @StorableBin(name = "bool", type = StorableType.BOOLEAN)
    private boolean value7;

    @StorableBin(name = "enum", type = StorableType.ENUM)
    private AerospikeTestEnum value8;

    @StorableBin(name = "bytes", type = StorableType.BYTES)
    private byte[] value9;

    @StorableBin(name = "json", type = StorableType.JSON)
    private AerospikeJsonEntity value10;

    @StorableBin(name = "list", type = StorableType.LIST)
    private List<String> value11;

    @StorableBin(name = "map", type = StorableType.MAP)
    private Map<String, String> value12;

    @StorableBin(name = "custom", type = StorableType.CUSTOM, keyConverter = AerospikeCustomEntityConverterA.class)
    private AerospikeCustomEntityA value13;

    @StorableBin(name = "customList", type = StorableType.LIST, keyConverter = AerospikeCustomEntityConverterA.class)
    private List<AerospikeCustomEntityA> value14;

    @StorableBin(name = "convertible", type = StorableType.CUSTOM)
    private AerospikeConvertibleEntityA value15;

    @StorableBin(name = "convertibleLst", type = StorableType.LIST)
    private List<AerospikeConvertibleEntityA> value16;

    @StorableBin(name = "customMap", type = StorableType.MAP, keyConverter = AerospikeCustomEntityConverterA.class, valueConverter = AerospikeCustomEntityConverterB.class)
    private Map<AerospikeCustomEntityA, AerospikeCustomEntityB> value17;

    @StorableBin(name = "customMap2", type = StorableType.MAP, valueConverter = AerospikeCustomEntityConverterB.class)
    private Map<String, AerospikeCustomEntityB> value18;

    @StorableBin(name = "convertiblMap", type = StorableType.MAP)
    private Map<AerospikeConvertibleEntityA, AerospikeConvertibleEntityB> value19;

    @StorableBin(name = "convertiblMap2", type = StorableType.MAP)
    private Map<String, AerospikeConvertibleEntityB> value20;

    @StorableBin(name = "customMap3", type = StorableType.MAP, valueConverter = ListAerospikeCustomEntityConverterA.class)
    private Map<String, List<AerospikeConvertibleEntityA>> value21;

    @StorableBin(name = "customMap4", type = StorableType.MAP, valueConverter = StringSetConverter.class)
    private Map<String, Set<String>> value22;

    @StorableBin(name = "setInt", type = StorableType.CUSTOM, keyConverter = IntegerSetConverter.class)
    private Set<Integer> value23;


    private String key;

    private AerospikeTestErrorEntityA(Builder builder) {
        key = builder.key;
        value10 = builder.value10;
        value1 = builder.value1;
        value2 = builder.value2;
        value3 = builder.value3;
        value4 = builder.value4;
        value5 = builder.value5;
        value6 = builder.value6;
        value7 = builder.value7;
        value8 = builder.value8;
        value9 = builder.value9;
        value11 = builder.value11;
        value12 = builder.value12;
        value13 = builder.value13;
        value14 = builder.value14;
        value15 = builder.value15;
        value16 = builder.value16;
        value17 = builder.value17;
        value18 = builder.value18;
        value19 = builder.value19;
        value20 = builder.value20;
        value21 = builder.value21;
        value22 = builder.value22;
        value23 = builder.value23;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(AerospikeTestErrorEntityA copy) {
        Builder builder = new Builder();
        builder.key = copy.key;
        builder.value10 = copy.value10;
        builder.value1 = copy.value1;
        builder.value2 = copy.value2;
        builder.value3 = copy.value3;
        builder.value4 = copy.value4;
        builder.value5 = copy.value5;
        builder.value6 = copy.value6;
        builder.value7 = copy.value7;
        builder.value8 = copy.value8;
        builder.value9 = copy.value9;
        builder.value11 = copy.value11;
        builder.value12 = copy.value12;
        builder.value13 = copy.value13;
        builder.value14 = copy.value14;
        builder.value15 = copy.value15;
        builder.value16 = copy.value16;
        builder.value17 = copy.value17;
        builder.value18 = copy.value18;
        builder.value19 = copy.value19;
        builder.value20 = copy.value20;
        builder.value21 = copy.value21;
        builder.value22 = copy.value22;
        builder.value23 = copy.value23;
        return builder;
    }

    @Override
    public String getAerospikeKey() {
        return key;
    }

    @Override
    public void setAerospikeKey(String key) {
        this.key = key;
    }

    @Override
    public Class<String> getAerospikeKeyType() {
        return String.class;
    }

    public static final class Builder {
        private AerospikeJsonEntity value10;
        private String value1;
        private Set<String> value2;
        private Double value3;
        private Float value4;
        private int value5;
        private long value6;
        private boolean value7;
        private AerospikeTestEnum value8;
        private byte[] value9;
        private List<String> value11;
        private Map<String, String> value12;
        private AerospikeCustomEntityA value13;
        private String key;
        private List<AerospikeCustomEntityA> value14;
        public AerospikeConvertibleEntityA value15;
        public List<AerospikeConvertibleEntityA> value16;
        public Map<AerospikeCustomEntityA, AerospikeCustomEntityB> value17;
        public Map<String, AerospikeCustomEntityB> value18;
        public Map<AerospikeConvertibleEntityA, AerospikeConvertibleEntityB> value19;
        public Map<String, AerospikeConvertibleEntityB> value20;
        public Map<String, List<AerospikeConvertibleEntityA>> value21;
        public Map<String, Set<String>> value22;
        public Set<Integer> value23;

        private Builder() {
        }

        public Builder value10(AerospikeJsonEntity value10) {
            this.value10 = value10;
            return this;
        }

        public Builder value1(String value1) {
            this.value1 = value1;
            return this;
        }

        public Builder value2(Set<String> value2) {
            this.value2 = value2;
            return this;
        }

        public Builder value3(Double value3) {
            this.value3 = value3;
            return this;
        }

        public Builder value4(Float value4) {
            this.value4 = value4;
            return this;
        }

        public Builder value5(int value5) {
            this.value5 = value5;
            return this;
        }

        public Builder value6(long value6) {
            this.value6 = value6;
            return this;
        }

        public Builder value7(boolean value7) {
            this.value7 = value7;
            return this;
        }

        public Builder value8(AerospikeTestEnum value8) {
            this.value8 = value8;
            return this;
        }

        public Builder value9(byte[] value9) {
            this.value9 = value9;
            return this;
        }

        public Builder value11(List<String> value11) {
            this.value11 = value11;
            return this;
        }

        public Builder value12(Map<String, String> value12) {
            this.value12 = value12;
            return this;
        }

        public Builder value13(AerospikeCustomEntityA value13) {
            this.value13 = value13;
            return this;
        }

        public Builder value14(List<AerospikeCustomEntityA> value14) {
            this.value14 = value14;
            return this;
        }

        public Builder value15(AerospikeConvertibleEntityA value15) {
            this.value15 = value15;
            return this;
        }

        public Builder value16(List<AerospikeConvertibleEntityA> value16) {
            this.value16 = value16;
            return this;
        }

        public Builder value17(Map<AerospikeCustomEntityA, AerospikeCustomEntityB> value17) {
            this.value17 = value17;
            return this;
        }

        public Builder value18(Map<String, AerospikeCustomEntityB> value18) {
            this.value18 = value18;
            return this;
        }

        public Builder value19(Map<AerospikeConvertibleEntityA, AerospikeConvertibleEntityB> value19) {
            this.value19 = value19;
            return this;
        }

        public Builder value20(Map<String, AerospikeConvertibleEntityB> value20) {
            this.value20 = value20;
            return this;
        }

        public Builder value21(Map<String, List<AerospikeConvertibleEntityA>> value21) {
            this.value21 = value21;
            return this;
        }

        public Builder value22(Map<String, Set<String>> value22) {
            this.value22 = value22;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value23(Set<Integer> value23) {
            this.value23 = value23;
            return this;
        }

        public AerospikeTestErrorEntityA build() {
            return new AerospikeTestErrorEntityA(this);
        }
    }
}
