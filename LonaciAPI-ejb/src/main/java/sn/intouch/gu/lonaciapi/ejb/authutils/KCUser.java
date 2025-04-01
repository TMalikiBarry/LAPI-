package sn.intouch.gu.lonaciapi.ejb.authutils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class KCUser implements Serializable {
    private String id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private Map<String, List<String>> resourceRoles;
    private List<String> groups;
    private Set<String> operators;
    
    @Builder.Default
    private Set<String> businessRoles = new HashSet<>();
}
