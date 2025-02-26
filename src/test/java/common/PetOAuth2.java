package common;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PetOAuth2 {

    public static String getAccessToken() throws Exception {
        String url = "https://api.petfinder.com/v2/oauth2/token";

        // Retrieve these values from environment variables or a config file
        String clientId = System.getenv("PETFINDER_CLIENT_ID");
        String clientSecret = System.getenv("PETFINDER_CLIENT_SECRET");
        String fallbackAccessCode = System.getenv("PETFINDER_ACCESSCODE");  // The fallback access code

        if (clientId == null || clientSecret == null || fallbackAccessCode == null) {
            throw new IllegalArgumentException("Client ID, Client Secret, or Access Code not provided.");
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);

            // Prepare the entity (application/x-www-form-urlencoded format)
            StringEntity entity = new StringEntity(
                    "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret
            );
            post.setEntity(entity);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpResponse response = client.execute(post);

            // Log response status code for debugging
            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());

            // Check for success (HTTP 200 OK)
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity());

                // Parse the JSON response to extract the access token
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseString);

                // Validate the response for the expected 'access_token' field
                if (root.has("access_token")) {
                    return root.get("access_token").asText();  // Return the access token
                } else {
                    throw new RuntimeException("Access token not found in response: " + responseString);
                }
            } else {
                // If the OAuth2 request fails, pass the fallback access code directly
                System.out.println("OAuth2 request failed, using fallback access code instead...");
                return fallbackAccessCode;  // Return the stored fallback access code directly
            }

        } catch (Exception e) {
            System.err.println("Error obtaining access token: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for debugging
            // If OAuth2 fails due to an exception, return the fallback access code
            System.out.println("Using fallback access code...");
            return fallbackAccessCode;  // Return the fallback access code if there is an exception
        }
    }

    public static void main(String[] args) {
        try {
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
