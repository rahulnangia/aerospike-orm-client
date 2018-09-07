package com.rn.aerospike.common.utils;

import com.google.gson.*;
import com.rn.aerospike.common.exceptions.JsonUtilityException;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by rahul
 */
public class JsonUtility {
    private static JsonUtility instance;
    private Gson gson;

    private JsonUtility() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }

    public static JsonUtility getInstance() {
        if (null == instance) {
            synchronized (JsonUtility.class) {
                if (null == instance) {
                    instance = new JsonUtility();
                }
            }
        }
        return instance;
    }

    public <T> T fromJson(String json, Class<T> clazz) throws JsonUtilityException {
        try {
            return this.gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonUtilityException(e);
        }
    }

    public <T> List<T> fromJsonAsList(String json, JsonList<T> clazz) throws JsonUtilityException {
        try {
            return this.gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonUtilityException(e);
        }
    }

    public String toJson(Object obj) {
        return this.gson.toJson(obj);
    }

    public JsonElement toJsonTree(Object obj) {
        return this.gson.toJsonTree(obj);
    }

    class DateTimeSerializer implements JsonSerializer<DateTime> {

        @Override
        public JsonElement serialize(DateTime dateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(dateTime.toString());
        }
    }

    class DateTimeDeserializer implements JsonDeserializer<DateTime> {

        @Override
        public DateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return DateTime.parse(jsonElement.getAsString());
        }
    }
}
