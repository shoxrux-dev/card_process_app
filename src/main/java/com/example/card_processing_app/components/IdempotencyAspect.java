package com.example.card_processing_app.components;

import com.example.card_processing_app.annotations.Idempotent;
import com.example.card_processing_app.dto.IdempotencyResult;
import com.example.card_processing_app.enums.IdempotencyStatus;
import com.example.card_processing_app.exception.IdempotentRequestException;
import com.example.card_processing_app.services.IdempotencyKeyService;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private final IdempotencyKeyService idempotencyKeyService;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest httpRequest;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = resolveKey(joinPoint, idempotent);

        IdempotencyResult result = idempotencyKeyService.checkAndLock(key);

        if (result.status() == IdempotencyStatus.PROCESSING) {
            throw new IdempotentRequestException("Request is currently being processed by another thread");
        }

        if (result.status() == IdempotencyStatus.COMPLETED) {
            return deserializeResponse(joinPoint, result.cachedValue());
        }

        try {
            Object response = joinPoint.proceed();

            Object bodyToCache = (response instanceof org.springframework.http.ResponseEntity<?> entity)
                    ? entity.getBody()
                    : response;

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        idempotencyKeyService.markAsComplete(key, bodyToCache);
                    }
                });
            } else {
                idempotencyKeyService.markAsComplete(key, bodyToCache);
            }
            return response;
        } catch (Exception e) {
            idempotencyKeyService.deleteKey(key);
            throw e;
        }
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String rawKey;

        if (!idempotent.key().isBlank()) {
            rawKey = parseSpelKey(joinPoint, idempotent.key());
        } else {
            rawKey = httpRequest.getHeader(idempotent.headerName());
        }

        if (rawKey == null || rawKey.isBlank()) {
            throw new IdempotentRequestException(
                    String.format("Required idempotency key/header is missing for method: %s",
                            joinPoint.getSignature().getName())
            );
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        return String.format("%s:%s:%s", className, methodName, rawKey);
    }

    private Object deserializeResponse(ProceedingJoinPoint joinPoint, String cachedValue) throws Exception {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (returnType.equals(Void.TYPE) || cachedValue == null) {
            return null;
        }

        JavaType type = objectMapper.getTypeFactory().constructType(signature.getMethod().getGenericReturnType());

        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(returnType)) {
            type = type.getBindings().getTypeParameters().get(0);
        }

        Object decodedBody = objectMapper.readValue(cachedValue, type);

        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(returnType)) {
            return org.springframework.http.ResponseEntity.status(201).body(decodedBody);
        }

        return decodedBody;
    }
    private String parseSpelKey(ProceedingJoinPoint joinPoint, String expressionStr) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        EvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), signature.getMethod(), joinPoint.getArgs(), discoverer);
        return parser.parseExpression(expressionStr).getValue(context, String.class);
    }
}