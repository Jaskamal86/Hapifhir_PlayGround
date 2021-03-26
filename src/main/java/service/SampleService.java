package service;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class SampleService {

    private static final Logger logger = LoggerFactory.getLogger(SampleService.class);

    public static void calculateAvgResponseTime(IGenericClient client) throws IOException {
        List<Double> loopTime = new ArrayList<>();

        double avgResponseTime;
        double totalResponseTime = 0.0;

        CacheControlDirective cacheControlDirective = new CacheControlDirective();

        for (int i = 0; i < 3; i++) {

            InputStream is = SampleService.class.getClassLoader().getResourceAsStream("lastNames.txt");

            assert is != null;
            InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);

            for (String line; (line = reader.readLine()) != null; ) {

                long startTime = System.currentTimeMillis();

                // Search for Patient resources
                Bundle response = client
                        .search()
                        .forResource("Patient")
                        .where(Patient.FAMILY.matches().value(line))
                        .returnBundle(Bundle.class)
                        .cacheControl(i == 2 ? cacheControlDirective.setNoCache(true).setNoStore(true)
                                : cacheControlDirective.setNoCache(false).setNoStore(false))
                        .execute();

                long endTime = System.currentTimeMillis();

                long responseTime = endTime - startTime;

                totalResponseTime += responseTime;
            }

            avgResponseTime = totalResponseTime / 20;

            logger.info("Avg Response Time: " + avgResponseTime + "ms");

            loopTime.add(avgResponseTime);
        }
        logger.info("First Loop Time:" + loopTime.get(0));
        logger.info("Second Loop Time:" + loopTime.get(1));
        logger.info("Third Loop Time:" + loopTime.get(2));
    }
}
