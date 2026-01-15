import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;

public class SwaggerApiPrinter {

    // Common HTTP methods in specs
    private static final String[] HTTP_METHODS = {
            "get", "post", "put", "patch", "delete", "head", "options", "trace"
    };

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java SwaggerApiPrinter <swagger_or_openapi_json_path>");
            System.exit(1);
        }

        File jsonFile = new File(args[0]);
        if (!jsonFile.exists()) {
            System.err.println("File not found: " + jsonFile.getAbsolutePath());
            System.exit(2);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile);

        // Build a "prefix" (optional) for nicer output:
        // - Swagger 2: basePath
        // - OAS3: first server url path portion isn't always safe to combine, so we skip by default
        String basePath = "";
        if (root.has("swagger") && root.get("swagger").asText().startsWith("2")) {
            basePath = root.path("basePath").asText("");
            if (basePath == null) basePath = "";
        }

        JsonNode pathsNode = root.get("paths");
        if (pathsNode == null || !pathsNode.isObject()) {
            System.err.println("No 'paths' object found. Is this a Swagger/OpenAPI JSON?");
            System.exit(3);
        }

        // Iterate all paths
        Iterator<String> pathNames = pathsNode.fieldNames();
        while (pathNames.hasNext()) {
            String path = pathNames.next();
            JsonNode pathItem = pathsNode.get(path);
            if (pathItem == null || !pathItem.isObject()) continue;

            // For each path, check methods
            for (String m : HTTP_METHODS) {
                JsonNode op = pathItem.get(m);
                if (op != null && op.isObject()) {
                    String method = m.toUpperCase(Locale.ROOT);
                    String fullPath = joinPaths(basePath, path);
                    System.out.println(method + " " + fullPath);
                }
            }
        }
    }

    private static String joinPaths(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;

        boolean aEnds = a.endsWith("/");
        boolean bStarts = b.startsWith("/");

        if (aEnds && bStarts) return a.substring(0, a.length() - 1) + b;
        if (!aEnds && !bStarts) return a + "/" + b;
        return a + b;
    }
}
