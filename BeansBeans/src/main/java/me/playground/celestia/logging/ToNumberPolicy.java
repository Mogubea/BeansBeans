package me.playground.celestia.logging;

import com.google.gson.JsonParseException;
import com.google.gson.ToNumberStrategy;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;

public enum ToNumberPolicy implements ToNumberStrategy {

    INTEGER_OR_DOUBLE {
        public Number readNumber(JsonReader in) throws IOException, JsonParseException {
            String value = in.nextString();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException longE) {
                try {
                    Double d = Double.valueOf(value);
                    if ((d.isInfinite() || d.isNaN()) && !in.isLenient()) {
                        throw new MalformedJsonException("JSON forbids NaN and infinities: " + d + "; at path " + in.getPath());
                    }
                    return d;
                } catch (NumberFormatException doubleE) {
                    throw new JsonParseException("Cannot parse " + value + "; at path " + in.getPath(), doubleE);
                }
            }
        }
    }

}
