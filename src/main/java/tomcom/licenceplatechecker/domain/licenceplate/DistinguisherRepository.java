package tomcom.licenceplatechecker.domain.licenceplate;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface DistinguisherRepository extends CrudRepository<Distinguisher, Long> {
    Optional<Distinguisher> findByCode(String code);
    Optional<Distinguisher> findByCodeAndDeprecated(String code, boolean deprecated);
    Optional<Distinguisher> findByCodeAndDeprecatedAndSpecial(String code, boolean deprecated, boolean special);
}
