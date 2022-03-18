package netflix.nebula.testsuitesexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyController {

    private final MyService myService;

    @Autowired
    public MyController(MyService myService) {
        this.myService = myService;
    }

    @GetMapping("/ready")
    public ResponseEntity ready() {
        return myService.isOk() ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
    }
}
