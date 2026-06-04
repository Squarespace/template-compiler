package com.squarespace.template.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Constants;
import com.squarespace.template.Context;

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

        boolean forward = nth > 0;
        int start = forward ? 0 : items.size() - 1;
        int end = forward ? items.size() : -1;
        int step = forward ? 1 : -1;

        int count = 0;
        boolean hasLookup = lookup != null;
        boolean hasPath = path != null;

        for (int i = start; forward ? (i < end) : (i > end); i += step ) {
            JsonNode node = items.get(i);
            if (!hasPath){
                count += step;
                if (count == nth) { return node;}
            } else {
                JsonNode candidate = hasLookup ? lookup.path(node.asText()) : node;
                if (isTruthy(getNodeAtPath(candidate, path))) {
                    count += step;
                    if (count == nth) { return node;}
                }
            }
        }
        return Constants.MISSING_NODE;
    }
}
