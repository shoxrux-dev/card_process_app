package com.example.card_processing_app.common.handler;

import com.example.card_processing_app.common.handler.pojo.FieldErrorResponse;
import com.example.card_processing_app.common.handler.pojo.TraceableErrorResponse;
import com.example.card_processing_app.exception.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CommonExceptionHandler {
    private final Environment environment;

    @ExceptionHandler({BindException.class})
    @ResponseStatus(BAD_REQUEST)
    List<FieldErrorResponse> handle(BindException ex) {
        return Stream.concat(
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> FieldErrorResponse.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .code(fieldError.getCode())
                                .build()
                        ),
                ex.getBindingResult().getGlobalErrors().stream()
                        .map(globalError -> FieldErrorResponse.builder()
                                .field(globalError.getObjectName())
                                .message(globalError.getDefaultMessage())
                                .code(globalError.getCode())
                                .build())
        ).toList();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(MissingServletRequestParameterException ex) {
        return FieldErrorResponse.builder()
                .code("ParameterMissing")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    List<FieldErrorResponse> handle(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(constraintViolation -> FieldErrorResponse
                        .builder()
                        .field(extractField(constraintViolation.getPropertyPath()))
                        .code(extractCode(constraintViolation.getConstraintDescriptor()))
                        .message(constraintViolation.getMessage())
                        .build())
                .toList();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(HttpMessageNotReadableException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("InvalidRequest")
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(IllegalArgumentException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("IllegalArgument")
                .build();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(CONFLICT)
    FieldErrorResponse handle(IllegalStateException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("IllegalState")
                .build();
    }

    @ExceptionHandler(InvocationTargetException.class)
    @ResponseStatus(CONFLICT)
    FieldErrorResponse handle(InvocationTargetException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("IllegalState")
                .build();
    }

    @ExceptionHandler(URISyntaxException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(URISyntaxException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("URISyntaxException")
                .build();
    }

    // Dto request @NotBlank @NotNull @NotEmpty Checked
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        return FieldErrorResponse.builder()
                .message(errorMessage)
                .code("URISyntaxException")
                .build();
    }

    @ExceptionHandler(RecordNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    FieldErrorResponse handle(RecordNotFoundException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("not_found")
                .build();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    FieldErrorResponse handle(UsernameNotFoundException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("User not found")
                .build();
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(CONFLICT)
    FieldErrorResponse handle(AlreadyExistsException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("RecordAlreadyExist")
                .build();
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(PRECONDITION_FAILED)
    public FieldErrorResponse handle(OptimisticLockException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("VersionMismatch")
                .build();
    }

    @ExceptionHandler(UserConflictData.class)
    @ResponseStatus(CONFLICT)
    FieldErrorResponse handle(UserConflictData ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("This user data is already available")
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(FORBIDDEN)
    FieldErrorResponse handle(AccessDeniedException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("AccessDenied")
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(UNAUTHORIZED)
    FieldErrorResponse handle(AuthenticationException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("Authentication")
                .build();
    }

    @ExceptionHandler(UserUnauthorizedAction.class)
    @ResponseStatus(UNAUTHORIZED)
    FieldErrorResponse handle(UserUnauthorizedAction ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("Authentication")
                .build();
    }

    @ExceptionHandler(UserDataException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(UserDataException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code(BAD_REQUEST.toString())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(BAD_REQUEST)
    TraceableErrorResponse handle(Exception ex) {
        return TraceableErrorResponse.builder()
                .message(ex.getMessage())
                .correlationId(UUID.randomUUID().toString())
                .stackTrace(isNonProdEnv() ? ExceptionUtils.getStackTrace(ex) : null)
                .build();
    }

    private boolean isNonProdEnv() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equals(profile) || "qa".equals(profile));
    }

    private String extractCode(ConstraintDescriptor<?> constraintDescriptor) {
        ConstraintAnnotationDescriptor<?> annotationDescriptor = ((ConstraintDescriptorImpl<?>) constraintDescriptor).getAnnotationDescriptor();
        Class<?> type = annotationDescriptor.getType();
        return type.getName().replace(type.getPackageName(), "")
                .replaceFirst("\\.", "");
    }

    private String extractField(Path propertyPath) {
        String field = null;
        for (Path.Node node : propertyPath) {
            field = node.getName();
        }

        return field;
    }

    @ExceptionHandler(CardCreationLimitExceededException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(CardCreationLimitExceededException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("CardLimitExceeded")
                .build();
    }

    @ExceptionHandler(InvalidCurrencyTypeException.class)
    @ResponseStatus(BAD_REQUEST)
    FieldErrorResponse handle(InvalidCurrencyTypeException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("InvalidCurrencyType")
                .build();
    }

    @ExceptionHandler(EtagMismatchException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    FieldErrorResponse handle(EtagMismatchException ex) {
        return FieldErrorResponse.builder()
                .code("etag_mismatch")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(CardNotActiveException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    FieldErrorResponse handle(CardNotActiveException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("card_not_active")
                .build();
    }

    @ExceptionHandler(CardNotBlockedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    FieldErrorResponse handle(CardNotBlockedException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("card_not_blocked")
                .build();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    FieldErrorResponse handle(InsufficientFundsException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("InsufficientFunds")
                .build();
    }

    @ExceptionHandler(IdempotentRequestException.class)
    @ResponseStatus(CONFLICT)
    FieldErrorResponse handle(IdempotentRequestException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("RequestInProcessOrDuplicate")
                .build();
    }

    @ExceptionHandler(TechnicalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    FieldErrorResponse handle(TechnicalException ex) {
        return FieldErrorResponse.builder()
                .message(ex.getMessage())
                .code("SYSTEM_ERROR")
                .build();
    }

}
