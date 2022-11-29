package ch.heigvd.amt.team09.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonHelper {
    private static final Logger LOG = Logger.getLogger(JsonHelper.class.getName());

    private JsonHelper() {

    }

    public static String toJson(Object object) {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.log(Level.SEVERE, "Could not convert object to JSON", e);
            return "{}";
        }
    }
}
