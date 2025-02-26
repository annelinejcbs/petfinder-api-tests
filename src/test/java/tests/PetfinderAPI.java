package tests;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static common.PetOAuth2.getAccessToken;

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

    // Step 1: Retrieve all animal types and check if Dog is found
    public static String getAnimalTypes(String accessToken) throws Exception {
        String url = "https://api.petfinder.com/v2/types";
        JsonNode root = makeApiRequest(url, accessToken);

        JsonNode types = root.has("types") ? root.get("types") : null;
        if (types != null) {

            boolean dogFound = false;
            for (JsonNode type : types) {

                if (type.get("name").asText().equalsIgnoreCase("Dog")) {
                    dogFound = true;
                }
            }

            // Step 2: Verify if "Dog" is an animal type
            if (dogFound) {
                System.out.println("Dog is one of the available animal types.");
            } else {
                System.out.println("Dog is NOT an available animal type.");
            }
        } else {
            System.out.println("No animal types found in response.");
        }
        return url;
    }

    // Step 3: Retrieve all dog breeds
    public static String getDogBreeds(String accessToken) throws Exception {
        String url = "https://api.petfinder.com/v2/types/dog/breeds";
        JsonNode root = makeApiRequest(url, accessToken);

        JsonNode breeds = root.has("breeds") ? root.get("breeds") : null;
        if (breeds != null && breeds.size() > 0) {

            Boolean Golden_Retrieverfound = false;

            // Iterate over each breed and print its name
            for (JsonNode breed : breeds) {

                // Check if the breed is Golden Retriever
                if (breed.get("name").asText().equalsIgnoreCase("Golden Retriever")) {
                    Golden_Retrieverfound = true;
                }
            }

            if (Golden_Retrieverfound) {
                System.out.println("Golden Retriever is one of the breeds.");
            } else {
                System.out.println("Golden Retriever is not one of the breeds.");
            }
        } else {
            System.out.println("No breed types found in response.");
        }

        return url;
    }

    // Step 4: Search for Golden Retriever Name
    public static String searchGoldenRetrieverDogs(String accessToken) throws Exception {
        String url = "https://api.petfinder.com/v2/animals?type=dog&breed=Golden%20Retriever";
        JsonNode root = makeApiRequest(url, accessToken);

        JsonNode animals = root.has("animals") ? root.get("animals") : null;
        if (animals != null && animals.isArray() && animals.size() > 0) {
            // Get the first animal directly
            JsonNode firstAnimal = animals.get(0);  // Access the first animal in the array

            // Print only the first animal
            System.out.println("Found the following Golden Retriever dog:");
            System.out.println("ID: " + firstAnimal.get("id").asText() + " | Name: " + firstAnimal.get("name").asText());
        } else {
            System.out.println("No Golden Retriever dogs found.");
        }
        return url;

    }

        public static void main (String[]args) throws Exception {
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);

            try {
                // Step 1: Get animal types and verify "Dog"
                getAnimalTypes(accessToken);

                // Step 2: Get dog breeds
                getDogBreeds(accessToken);

                // Step 3: Search for Golden Retrievers and verify results
                searchGoldenRetrieverDogs(accessToken);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

