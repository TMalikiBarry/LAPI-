package sn.intouch.gu.lonaciapi.ws.utils;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sn.intouch.gu.lonaciapi.ejb.authutils.KCUser;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;
import sn.intouch.gu.lonaciapi.ws.config.APIException;

import java.util.*;

public class AuthUtils {
    public static final String BEARER_TOKEN_KEY_WORD = "Bearer";
    private static final String BO_CLIENT_ID = "BO_CLIENT_ID";
    private static final String BOOKMAKER_ROLE = "BOOKMAKER";
    private static final Logger log = LoggerFactory.getLogger(AuthUtils.class);
    private static String lonaciBoClientID = null;

    public static KCUser getUserFromToken(String header) {
        AccessToken token = AuthUtils.getAccessTokenFromToken(header);
        return KCUser.builder()
                .firstname(token.getGivenName())
                .lastname(token.getFamilyName())
                .username(token.getPreferredUsername())
                .groups(token.getOtherClaims().get("groups") != null ? (ArrayList<String>) token.getOtherClaims().get("groups") : null)
                .email(token.getEmail())
                .businessRoles(getBusinessRoles(token.getResourceAccess()))
                .operators(getOperators(token.getOtherClaims().get("operators")))
                .id(token.getSubject())
                .build();
    }

    private static Set<String> getOperators(Object operators) {
        if (operators == null || operators.toString().isEmpty())
            return new HashSet<>();
        return new HashSet<>(Arrays.asList(operators.toString().split(";")));
    }

    public static Set<String> getBusinessRoles(Map<String, AccessToken.Access> resourceRoles) {
        if (resourceRoles.get(getClientID()) != null)
            return new HashSet<>(resourceRoles.get(getClientID()).getRoles());
        return null;
    }

    public static Boolean doesBookMakerHasAccessToOperator(String header, String operator) {
        return doesBookMakerHasAccessToOperator(getUserFromToken(header), operator);
    }
    public static Boolean doesBookMakerHasAccessToOperator(KCUser kcUser, String operator) {
        if (!isBookMaker(kcUser)) {
            log.info("User {} is not a bookmaker", kcUser.getUsername());
            return true;
        }
        if (kcUser.getOperators() == null || kcUser.getOperators().isEmpty()) {
            log.warn("User {} has no operator", kcUser.getUsername());
            return false;
        }
        if (operator == null || operator.isEmpty()) {
            log.warn("Operator is null or empty");
            return false;
        }
        if (kcUser.getOperators().contains(operator)) {
            log.warn("User {} has access to operator {}", kcUser.getUsername(), operator);
            return true;
        }
        log.warn("User {} does not have access to operator {}", kcUser.getUsername(), operator);
        return false;
    }

    public static boolean isBookMaker(KCUser kcUser) {
        return kcUser.getBusinessRoles() != null && kcUser.getBusinessRoles().contains(BOOKMAKER_ROLE);
    }
    private static String getClientID() {
        if (lonaciBoClientID == null) {
            ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);
            Parameter boBusinessClientIdParam = parameterService.getParameterByCode(BO_CLIENT_ID);
            if (boBusinessClientIdParam == null)
                throw new APIException("Cannot find parameter of code " + BO_CLIENT_ID);
            lonaciBoClientID = boBusinessClientIdParam.getPrmStringValue();
        }
        return lonaciBoClientID;
    }

    public static AccessToken getAccessTokenFromToken(String token) {
        token = getTokenFromHeader(token);
        AccessToken accessToken;
        try {
            accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
        return accessToken;
    }

    public static String getTokenFromHeader(String token) {
        if (token == null) return null;
        String auth = token;
        if (token.startsWith(BEARER_TOKEN_KEY_WORD)) {
            auth = token.split("\\s+")[1];
        }
        return auth;
    }
}
