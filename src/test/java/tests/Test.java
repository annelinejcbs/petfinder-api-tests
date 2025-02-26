package tests;

import common.PetOAuth2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;

public class Test {

    private static final Logger logger = LogManager.getLogger(Test.class);

    public static void main(String[] args) {
        try {
            // Test 1: Get Access Token measuring response time of api
            long startTime = System.currentTimeMillis();
            String accessToken = PetOAuth2.getAccessToken();
            long endTime = System.currentTimeMillis();
            System.out.println("Access Token retrieval took " + (endTime - startTime) + " ms.");
            logger.info("Access Token retrieval took " + (endTime - startTime) + " ms.");


            if (accessToken == null || accessToken.isEmpty()) {
                System.out.println("Failed to retrieve access token.");
                logger.info("Failed to retrieve access token.");
                return;
            }

            // Test 2: Retrieve animal types and verify "Dog" measuring response time of api
            measureApiResponseTime(() -> {
                try {
                    String animalTypesResponse = PetfinderAPI.getAnimalTypes(accessToken);
                    Assertions.assertTrue(animalTypesResponse.contains("Dog"), "Expected 'Dog' in animal types response");
                    System.out.println("Dog found in animal types.");
                    logger.info("Dog found in animal types.");
                } catch (Exception e) {
                    System.out.println("Failed to retrieve animal types: " + e.getMessage());
                    logger.info("Failed to retrieve animal types: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // Test 3: Retrieve dog breeds and check for "Golden Retriever" measuring response time of api
            measureApiResponseTime(() -> {
                try {
                    String dogBreedsResponse = PetfinderAPI.getDogBreeds(accessToken);
                    Assertions.assertTrue(dogBreedsResponse.contains("Golden Retriever"), "Expected 'Golden Retriever' in dog breeds response");
                    System.out.println("Golden Retriever found in dog breeds.");
                    logger.info("Golden Retriever found in dog breeds.");
                } catch (Exception e) {
                    System.out.println("Failed to retrieve dog breeds: " + e.getMessage());
                    logger.info("Failed to retrieve dog breeds: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });

            // Test 4: Search for Golden Retriever dogs measuring response time of api
            measureApiResponseTime(() -> {
                try {
                    String searchResults = PetSearch.searchGoldenRetriever(accessToken);
                    Assertions.assertTrue(searchResults.contains("Golden Retriever"), "Expected 'Golden Retriever' in search results");
                    System.out.println("Found Golden Retrievers in search results.");
                    logger.info("Found Golden Retrievers in search results.");
                } catch (Exception e) {
                    System.out.println("Failed to search Golden Retrievers: " + e.getMessage());
                    logger.info("Found Golden Retrievers in search results.");
                    throw new RuntimeException(e);
                }
            });

            // Performance testing for throughput (making multiple API calls in 1 minute)
            measureThroughput(accessToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Validating API response time doesn't exceed 2 seconds
    private static void measureApiResponseTime(Runnable apiCall) {
        long startTime = System.currentTimeMillis();
        apiCall.run();
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        System.out.println("API call took " + responseTime + " ms.");
        logger.info("API call took " + responseTime + " ms.");
        Assertions.assertTrue(responseTime < 2000, "API response time exceeded 2 seconds");
    }

    // Measuring how many api calls can be made in 1 minute
    private static void measureThroughput(String accessToken) {
        long startTime = System.currentTimeMillis();
        int totalCalls = 0;
        int failedCalls = 0;

        while (System.currentTimeMillis() - startTime < 60000) {
            try {
                PetfinderAPI.getAnimalTypes(accessToken);  // Sample API call for throughput
                totalCalls++;
            } catch (Exception e) {
                failedCalls++;
                System.out.println("API call failed: " + e.getMessage());
                logger.info("API call failed: " + e.getMessage());
                // Optional: Add delay between calls to prevent server overload
                try {
                    Thread.sleep(50); // Sleep for 50ms between calls to avoid hitting the server too hard
                } catch (InterruptedException interruptedEx) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double throughput = totalCalls / ((endTime - startTime) / 1000.0);
        System.out.println("Total API calls made in 1 minute: " + totalCalls);
        logger.info("Total API calls made in 1 minute: " + totalCalls);
        System.out.println("Failed API calls: " + failedCalls);
        logger.info("Failed API calls: " + failedCalls);
        System.out.println("Throughput: " + throughput + " calls/second");
        logger.info("Throughput: " + throughput + " calls/second");
    }
}
