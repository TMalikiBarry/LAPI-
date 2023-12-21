package sn.intouch.gu.lonaciapi.ejb.authutils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parametre;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class KCUser implements Serializable {
    private static final String BO_CLIENT_ID = "BO_CLIENT_ID";
    private String id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private Map<String, List<String>> resourceRoles;
    private List<String> groups;
    
    @Builder.Default
    private Set<String> businessRoles = new HashSet<>();

    public Set<String> getBusinessRolesFromAccessToken() {
        if (!businessRoles.isEmpty()) return businessRoles;
        ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);
        Parametre boBusinessClientIdParam = parameterService.getParameterByCode(BO_CLIENT_ID);
        log.info("CLIENT ROLES :: " + new Gson().toJson(resourceRoles));
        if (boBusinessClientIdParam == null)
            throw new RuntimeException(BO_CLIENT_ID + " parameter not found.");
        if (resourceRoles.get(boBusinessClientIdParam.getPrmStringValue()) != null)
            businessRoles = new HashSet<>(resourceRoles.get(boBusinessClientIdParam.getPrmStringValue()));
        return businessRoles;
    }
}
