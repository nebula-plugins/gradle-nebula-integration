package consistent.resolution.persistence.api.issue;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "my_table")
public class MyClass {

    @Id
    @GeneratedValue(
            generator = "id_seq",
            strategy = GenerationType.SEQUENCE)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "output", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MyOtherClass> others;

}
