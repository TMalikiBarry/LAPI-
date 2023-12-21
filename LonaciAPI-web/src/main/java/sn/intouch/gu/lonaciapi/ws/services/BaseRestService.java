package sn.intouch.gu.lonaciapi.ws.services;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BaseRestService {

    @RequestMapping(value = "/base/{network_group_code}", method = RequestMethod.GET)
    public ResponseEntity<String> getAllUsers(
            @RequestParam(value = "end_date", required = true) String end_date,
            @PathVariable() String network_group_code
    ) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello!");
    }
}
