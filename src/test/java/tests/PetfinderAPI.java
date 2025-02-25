package tests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PetfinderAPI {

    // Helper method to make the API request
    private static JsonNode makeApiRequest(String url, String accessToken) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Bearer " + accessToken);

            HttpResponse response = client.execute(request);

            // Check if the response is successful (HTTP 200)
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed to fetch data. HTTP code: " + response.getStatusLine().getStatusCode());
            }

            String responseString = EntityUtils.toString(response.getEntity());

            // Parse the response JSON
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(responseString);  // Return the parsed JsonNode
        } catch (Exception e) {
            System.err.println("Error during API request: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to handle at a higher level
        }
    }

    // Getting Animal Types
    public static String getAnimalTypes(String accessToken) throws Exception {
        String url = "https://api.petfinder.com/v2/types";
        JsonNode root = makeApiRequest(url, accessToken);

        JsonNode types = root.has("types") ? root.get("types") : null;
        if (types != null) {
            boolean dogFound = false;
            for (JsonNode type : types) {
                if (type.get("name").asText().equals("Dog")) {
                    dogFound = true;
                    break;
                }
            }
            if (dogFound) {
                System.out.println("Dog is an animal type.");
            } else {
                System.out.println("Dog is NOT an animal type.");
            }
        } else {
            System.out.println("No animal types found in response.");
        }
        return url;
    }

    // Getting Dog Breeds
    public static String getDogBreeds(String accessToken) throws Exception {
        String url = "https://api.petfinder.com/v2/breeds/dog";
        JsonNode root = makeApiRequest(url, accessToken);

        JsonNode breeds = root.has("breeds") ? root.get("breeds") : null;
        if (breeds != null && breeds.size() > 0) {
            System.out.println("Dog Breeds:");
            for (JsonNode breed : breeds) {
                System.out.println("- " + breed.asText());
            }
        } else {
            System.out.println("No dog breeds found.");
        }
        return url;
    }

    public static void main(String[] args) {
        // Replace with a valid access token
        String accessToken = "your-access-token-here";

        try {
            // Get animal types and verify "Dog"
            getAnimalTypes(accessToken);

            // Get dog breeds
            getDogBreeds(accessToken);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
