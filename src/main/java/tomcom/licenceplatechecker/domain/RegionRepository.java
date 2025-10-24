package tomcom.licenceplatechecker.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface RegionRepository extends CrudRepository<Region, Long> {
    Optional<Region> findByCode(String code);
}
