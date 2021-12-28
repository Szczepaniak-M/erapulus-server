package com.erapulus.server.security;

import com.erapulus.server.database.model.UserType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.erapulus.server.web.common.CommonRequestVariable.STUDENT_PATH_PARAM;
import static com.erapulus.server.web.common.CommonRequestVariable.UNIVERSITY_PATH_PARAM;


public class JwtReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final List<String> allowedRoles;
    private final String validatePath;

    public JwtReactiveAuthorizationManager(List<UserType> allowedRoles, String validatePath) {
        this.allowedRoles = allowedRoles.stream()
                                        .map(UserType::toString)
                                        .toList();
        this.validatePath = validatePath;
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
        Map<String, Object> pathVariables = context.getVariables();
        return authentication
                .map(jwtAuthentication -> verifyRole(jwtAuthentication.getAuthorities(), pathVariables))
                .map(AuthorizationDecision::new);
    }

    private boolean verifyRole(Collection<? extends GrantedAuthority> roles, Map<String, Object> pathVariables) {
        boolean isUserTypeProper = roles.stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .anyMatch(allowedRoles::contains);
        if (isUserTypeProper) {
            String additionalRole = null;
            if (Objects.equals(validatePath, STUDENT_PATH_PARAM)) {
                additionalRole = "STUDENT_" + pathVariables.get(STUDENT_PATH_PARAM);
            } else if (Objects.equals(validatePath, UNIVERSITY_PATH_PARAM)) {
                additionalRole = "UNIVERSITY_" + pathVariables.get(UNIVERSITY_PATH_PARAM);
            }

            if (additionalRole != null) {
                String finalAdditionalRole = additionalRole;
                return roles.stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(role -> Objects.equals(role, finalAdditionalRole) || Objects.equals(role, UserType.ADMINISTRATOR.toString()));
            }
            return true;
        }
        return false;
    }
}
