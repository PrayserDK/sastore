package hundeklemmen.nikolaialex05.Skript;
import org.json.simple.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;

public class Serializers {
    private static Map<String, Serializer> serializers = new HashMap<String, Serializer>();

    private static class Serializer {
        Function<Object, JSONObject> serializer;
        Function<JSONObject, Object> deserializer;

        Serializer(Function<Object, JSONObject> serializer, Function<JSONObject, Object> deserializer) {
            this.serializer = serializer;
            this.deserializer = deserializer;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void register(Class<T> cls, Function<T, JSONObject> serializer,
                                    Function<JSONObject, T> deserializer) {
        serializers.put(cls.getName(), new Serializer((Function<Object, JSONObject>) serializer,
                (Function<JSONObject, Object>) deserializer));
    }

    @SuppressWarnings("unchecked")
    public static JSONObject serialize(Object o) {
        JSONObject obj;
        String cls = o.getClass().getName();

        if (serializers.containsKey(cls)) {
            obj = serializers.get(cls).serializer.apply(o);
            obj.put("__javaclass__", cls);
        } else {
            obj = new JSONObject();
            SerializedVariable.Value value = Classes.serialize(o);

            if (value == null) {
                return null;
            }

            obj.put("__skriptclass__", value.type);
            obj.put("value", Base64.getEncoder().encodeToString(value.data));
        }

        return obj;
    }

    public static Object deserialize(JSONObject obj) {
        String cls = (String) obj.get("__javaclass__");

        if (cls != null && serializers.containsKey(cls)) {
            return serializers.get(cls).deserializer.apply(obj);
        } else {
            String type = (String) obj.get("__skriptclass__");
            String content = (String) obj.get("value");

            if (type == null || content == null) {
                return null;
            }

            return Classes.deserialize(type, Base64.getDecoder().decode(content));
        }
    }
}