package com.rn.aerospike.client.mapping;

import com.rn.aerospike.client.mapping.method.AerospikeReader;
import com.rn.aerospike.client.mapping.method.AerospikeWriter;
import com.rn.aerospike.client.exceptions.InvalidDTOException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rahul
 */
@Getter
public class Record {

    private String setName;

    private String namespace;

    private Map<String, List<RecordBin>> bins;

    @Setter
    private Class<?> keyClass;

    private Map<String, Boolean> nonDeprecatedFound;

    public Record(String setName, String namespace) {
        this(setName, namespace, null);
    }

    public Record(String setName, String namespace, Class<?> keyClass) {
        this.setName = setName;
        this.namespace = namespace;
        this.bins = new HashMap<>();
        this.keyClass = keyClass;
        this.nonDeprecatedFound = new HashMap<>();
    }

    public void addBin(String binName, AerospikeWriter<?> writer, AerospikeReader<?> reader, Class baseClass, boolean addFirst) {
        List<RecordBin> bin = bins.get(binName);
        if (bin == null) {
            bin = new ArrayList<>();
            bins.put(binName, bin);
        }
        Boolean nonDepFound = nonDeprecatedFound.get(binName);
        if (nonDepFound == null) {
            nonDepFound = false;
        }
        if (nonDepFound && addFirst) {
            throw new InvalidDTOException("Multiple non-deprecated fields for bin " + binName + " found in " + baseClass);
        }
        RecordBin binWriter = new RecordBin(writer, reader);
        if (addFirst) {
            nonDeprecatedFound.put(binName, true);
            bin.add(0, binWriter);
        } else {
            bin.add(binWriter);
        }
    }

    public void addBin(String binName, AerospikeWriter<?> writer, AerospikeReader<?> reader) {
        addBin(binName, writer, reader, null, false);
    }
}
