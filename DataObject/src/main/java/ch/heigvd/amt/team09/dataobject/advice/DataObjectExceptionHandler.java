package ch.heigvd.amt.team09.dataobject.advice;


import ch.heigvd.amt.team09.dataobject.exception.DeleteFailedException;
import ch.heigvd.amt.team09.dataobject.exception.FileUploadException;
import ch.heigvd.amt.team09.dataobject.exception.ObjectAlreadyExistsException;
import ch.heigvd.amt.team09.dataobject.exception.ObjectNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class DataObjectExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ObjectAlreadyExistsException.class)
    ResponseEntity<Object> handleObjectAlreadyExists(ObjectAlreadyExistsException e, WebRequest request) {
        return handleException(HttpStatus.CONFLICT, e, request);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    ResponseEntity<Object> handleObjectNotFound(ObjectNotFoundException e, WebRequest request) {
        return handleException(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(FileUploadException.class)
    ResponseEntity<Object> handleFileUploadFailed(FileUploadException e, WebRequest request) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e, request);
    }

    @ExceptionHandler(DeleteFailedException.class)
    ResponseEntity<Object> handleDeleteFailed(DeleteFailedException e, WebRequest request) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> handleValidationException(ConstraintViolationException e, WebRequest request) {
        var message = e.getConstraintViolations().stream()
                .map(violation -> {
                    String field = null;
                    for (var node : violation.getPropertyPath()) {
                        field = node.getName();
                    }
                    return formatTypeMismatch(field, violation.getInvalidValue(), violation.getMessage());
                })
                .collect(Collectors.joining("\r\n"));

        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, message, e, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        var message = formatTypeMismatch(ex.getName(), ex.getValue(), "wrong type");
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, message, ex, request);
    }

    private ResponseEntity<Object> handleException(HttpStatus status, Exception e, WebRequest request) {
        return handleException(status, e.getLocalizedMessage(), e, request);
    }

    private ResponseEntity<Object> handleException(HttpStatus status, String message, Exception e, WebRequest request) {
        var problem = createProblemDetail(e, status, message, null, null, request);
        return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
    }

    private String formatTypeMismatch(String propertyName, Object actual, String message) {
        return "Invalid value '%s' for field %s, %s".formatted(actual, propertyName, message);
    }
}
