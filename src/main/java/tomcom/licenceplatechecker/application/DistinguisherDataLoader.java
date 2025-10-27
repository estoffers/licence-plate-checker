package tomcom.licenceplatechecker.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;
import tomcom.licenceplatechecker.domain.licenceplate.DistinguisherRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DistinguisherDataLoader implements ApplicationRunner {

    private final DistinguisherRepository distinguisherRepository;

    public DistinguisherDataLoader(DistinguisherRepository distinguisherRepository) {
        this.distinguisherRepository = distinguisherRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (distinguisherRepository.count() > 0)
            return;
        ClassPathResource resource = new ClassPathResource("kennzeichen.csv");

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            // Skip header line
            String line = reader.readLine();

            int processedLines = 0;
            int lastLoggedMilestone = 0;
            int logInterval = 25; // Log every 25 records (adjust based on your CSV size)

            System.out.print("Loading distinguishers: 0 records");

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length >= 4) {
                    Distinguisher distinguisher = new Distinguisher();
                    distinguisher.code = parts[0].trim();
                    distinguisher.label = parts[1].trim().replace("\"", "");
                    distinguisher.deprecated = Boolean.parseBoolean(parts[2].trim());
                    distinguisher.special = Boolean.parseBoolean(parts[3].trim());

                    distinguisherRepository.save(distinguisher);
                    processedLines++;

                    // Log every logInterval records
                    if (processedLines - lastLoggedMilestone >= logInterval) {
                        lastLoggedMilestone = processedLines;
                        System.out.print("\rLoading distinguishers: " + processedLines + " records");
                    }
                }
            }

            System.out.println("\rLoading distinguishers: " + processedLines + " records - Complete!");
        }
    }
}