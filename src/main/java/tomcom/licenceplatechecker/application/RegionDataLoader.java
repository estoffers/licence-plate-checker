package tomcom.licenceplatechecker.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tomcom.licenceplatechecker.domain.Region;
import tomcom.licenceplatechecker.domain.RegionRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class RegionDataLoader implements ApplicationRunner {

    private final RegionRepository regionRepository;

    public RegionDataLoader(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
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
                    Region region = new Region();
                    region.code = parts[0].trim();
                    region.label = parts[1].trim();
                    region.deprecated = Boolean.parseBoolean(parts[2].trim());
                    region.special = Boolean.parseBoolean(parts[3].trim());
                    
                    regionRepository.save(region);
                }
            }
        }
    }
}
