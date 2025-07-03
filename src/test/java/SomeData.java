import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomeData {

    private String id;
    private String name;
    private String description;
    private int value;
    private boolean active;

}
