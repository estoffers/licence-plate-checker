package tomcom.licenceplatechecker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DiplomaticCode {
    @Id
    private Long id;

    public String label;
    public String code;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
