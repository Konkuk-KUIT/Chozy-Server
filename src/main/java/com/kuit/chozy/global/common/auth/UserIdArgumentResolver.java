package com.kuit.chozy.global.common.auth;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        String authHeader = webRequest.getHeader("Authorization");
        UserId annotation = parameter.getParameterAnnotation(UserId.class);
        boolean required = annotation == null || annotation.required();


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (!required) {
                return null; // 게스트 허용
            }
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        String token = extractBearerToken(authHeader);

        return jwtUtil.getUserId(token);
    }

    private String extractBearerToken(String header) {
        String prefix = "Bearer ";

        if (!header.startsWith(prefix)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        return header.substring(prefix.length()).trim();
    }
}
