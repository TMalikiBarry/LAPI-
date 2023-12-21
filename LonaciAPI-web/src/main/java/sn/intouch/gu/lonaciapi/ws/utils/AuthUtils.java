package sn.intouch.gu.lonaciapi.ws.utils;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import sn.intouch.gu.lonaciapi.ejb.authutils.KCUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthUtils {
    public static final String BEARER_TOKEN_KEY_WORD = "Bearer";
    public static AccessToken getAccessTokenFromToken (String token) {
        token = getTokenFromHeader(token);
        AccessToken accessToken;
        try {
            accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
        return accessToken;
    }

    public static String getLoginFromToken(String token) {
        if (token == null) return null;
        String auth = token;
        if (token.startsWith(BEARER_TOKEN_KEY_WORD)) {
            auth = token.split("\\s+")[1];
        }
        try {
            AccessToken accessToken = TokenVerifier.create(auth, AccessToken.class).getToken();
            return accessToken.getPreferredUsername();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTokenFromHeader(String token) {
        if (token == null) return null;
        String auth = token;
        if (token.startsWith(BEARER_TOKEN_KEY_WORD)) {
            auth = token.split("\\s+")[1];
        }
        return auth;
    }

    public static KCUser getUserFromToken(String header) {
        AccessToken token = AuthUtils.getAccessTokenFromToken(header);

        return KCUser.builder()
                .firstname(token.getGivenName())
                .lastname(token.getFamilyName())
                .username(token.getPreferredUsername())
                .groups(token.getOtherClaims().get("groups") != null ? (ArrayList<String>) token.getOtherClaims().get("groups") : null)
                .email(token.getEmail())
                .resourceRoles(getRolesList(token.getResourceAccess()))
                .id(token.getSubject())
                .build();
    }

    private static Map<String, List<String>> getRolesList(Map<String, AccessToken.Access> resourceAccess) {
        Map<String, List<String>> roles = new HashMap<>();
        for (String key : resourceAccess.keySet()) {
            roles.put(key, new ArrayList<>(resourceAccess.get(key).getRoles()));
        }
        return roles;
    }

    public static void main(String[] args) {
        String login = getLoginFromToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1TXRYZzNzNWI3Yzl0TnNyelFQMG5ZQjN6eEhQNndRNzUwN3A3QXAxa3E0In0.eyJleHAiOjE2ODk3NzY0OTQsImlhdCI6MTY4OTc3Mjg5NCwianRpIjoiY2EwZjc0OWEtM2E5OS00ZDM0LTgxNjUtYWIyNmIzMTMyNTExIiwiaXNzIjoiaHR0cHM6Ly9kZXYtc3NvLmd1dG91Y2gubmV0L2F1dGgvcmVhbG1zL3Nzby1pbnRvdWNoIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjdmMTM3Mzk5LTE3NmEtNGM3OC1iNGJmLTUwMWM5YzZiYzlmYSIsInR5cCI6IkJlYXJlciIsImF6cCI6InRpY2tldGluZy1lbmdpbmUiLCJzZXNzaW9uX3N0YXRlIjoiNjQ3ZjE0ZTUtOTZiMS00NTJlLWExMDQtMmU5OGNkMGJkMTY2IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLXNzby1pbnRvdWNoIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkFsaW91bmUgU2FyciIsInByZWZlcnJlZF91c2VybmFtZSI6ImFsaW91bmUua2V5Y2xvYWNrIiwiZ2l2ZW5fbmFtZSI6IkFsaW91bmUiLCJmYW1pbHlfbmFtZSI6IlNhcnIiLCJlbWFpbCI6ImFsaW91bmUua2V5Y2xvYWNrQHlvcG1haWwuY29tIn0.a4ld8-wJWxVKJQfOYpsg7MN4HqfKUmsGNTjbqJy6i64jq6Dve85i91YyjMbG4M40-B4Hhc3opl7A9ow85oSjgaXqXznmBqEM1OuAYlducdpU3hR9VC_dL41z59nVzVMDY8qc0i1UUlSY_aT8vQP7Q3VjWieIaHE9xuAahf3kepacOTpbFCEBZ3IX5_PV17hh3h-VWW57r3GUs7brpczlgOO6L4ZizewRBC67XgTV8h6jlvvYvam1YCrYnJt2e4WyP795gePtz9UjCc8WhAB0bo8xUkuS2eZ7hlZ6OBgmUecNJk9CS8IN63XmieposClJg4_IFBfC_lxmJOINxBeeEQ");
        System.out.println(login);
    }
}
