package com.squarespace.template.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;

import java.util.List;

import static com.squarespace.template.GeneralUtils.getNodeAtPath;
import static com.squarespace.template.GeneralUtils.isTruthy;
import static com.squarespace.template.GeneralUtils.splitVariable;

public class FindUtils {

    public static class LookupAndPath {
        public final JsonNode lookup;
        public final Object[] path;

        LookupAndPath(JsonNode lookup, Object[] path) {
            this.lookup = lookup;
            this.path = path;
        }
    }

    public static LookupAndPath getLookupAndPath(Context ctx, List<String> args) {
        int argsCount = args.size();
        boolean hasLookup = argsCount == 2;
        boolean hasPath = argsCount >= 1;
        JsonNode lookup = hasLookup ? ctx.resolve(splitVariable(args.get(0))) : null;
        Object[] path = hasPath ? splitVariable(args.get(hasLookup ? 1 : 0)) : null;
        return new LookupAndPath(lookup, path);
    }

    public static JsonNode findNthValidEntry(JsonNode items, Object[] path, JsonNode lookup, int nth) {
        if (!items.isArray()) {
            return Constants.MISSING_NODE;
        }
        ArrayNode validEntries = JsonUtils.createArrayNode();
        boolean hasLookup = lookup != null;
        for (JsonNode element : items) {
            JsonNode candidate = hasLookup ? lookup.path(element.asText()) : element;
            if (isTruthy(getNodeAtPath(candidate, path))) {
                validEntries.add(candidate);
            }
        }
        int size = validEntries.size();
        if (size == 0) {
            return Constants.MISSING_NODE;
        }
        int index = nth < 0 ? size + nth : nth;
        if (index < 0 || index >= size) {
            return Constants.MISSING_NODE;
        }
        return validEntries.get(index);
    }
}
