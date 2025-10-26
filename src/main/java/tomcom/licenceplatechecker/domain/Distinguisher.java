package tomcom.licenceplatechecker.domain;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Transactional
@Entity
public class Distinguisher {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    @Column(nullable = false)
    public String label;
    @Column(nullable = false)
    public String code;
    @Column(nullable = false)
    public Boolean deprecated;
    @Column(nullable = false)
    public Boolean special;

}
