package tests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.CookieSpecs;

public class PetSearch {

    private static final String API_URL = "https://api.petfinder.com/v2/animals?breed=Golden+Retriever&type=dog";

    // Search for Golden Retrievers by breed
    public static String searchGoldenRetriever(String accessToken) throws Exception {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpGet request = new HttpGet(API_URL);
            request.setHeader("Authorization", "Bearer " + accessToken);

            // Execute the HTTP request and get the response
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.err.println("Error: Received HTTP status code " + statusCode);
                String responseBody = EntityUtils.toString(response.getEntity());
                System.err.println("Response body: " + responseBody);
                return null;
            }

            String responseString = EntityUtils.toString(response.getEntity());

            // Parse the response to a JSON object
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseString);

            // Check if there are results
            if (root.has("animals") && root.get("animals").size() > 0) {
                System.out.println("Found " + root.get("animals").size() + " Golden Retriever(s).");

                // Example: Print first animal's name (if available)
                JsonNode firstAnimal = root.get("animals").get(0);
                if (firstAnimal.has("name")) {
                    System.out.println("First found Golden Retriever: " + firstAnimal.get("name").asText());
                }
            } else {
                System.out.println("No Golden Retrievers found.");
            }

            return responseString;

        } catch (Exception e) {
            System.err.println("Error occurred while searching for Golden Retrievers: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to create a custom HTTP client with timeout settings
    private static CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)  // 5 seconds connection timeout
                .setSocketTimeout(5000)   // 5 seconds read timeout
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        // Use a pool to manage connections for reusability
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(10);
        cm.setMaxTotal(200);

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm)
                .build();
    }

    public static void main(String[] args) {
        // Replace with your actual access token
        String accessToken = "your-access-token-here";

        try {
            searchGoldenRetriever(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
