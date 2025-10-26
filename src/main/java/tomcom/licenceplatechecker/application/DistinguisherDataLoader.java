package tomcom.licenceplatechecker.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tomcom.licenceplatechecker.domain.Distinguisher;
import tomcom.licenceplatechecker.domain.DistinguisherRepository;

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
        ClassPathResource resource = new ClassPathResource("kennzeichen.csv");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            // Skip header line
            String line = reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    Distinguisher distinguisher = new Distinguisher();
                    distinguisher.code = parts[0].trim();
                    distinguisher.label = parts[1].trim();
                    distinguisher.deprecated = Boolean.parseBoolean(parts[2].trim());
                    distinguisher.special = Boolean.parseBoolean(parts[3].trim());
                    
                    distinguisherRepository.save(distinguisher);
                }
            }
        }
    }
}
