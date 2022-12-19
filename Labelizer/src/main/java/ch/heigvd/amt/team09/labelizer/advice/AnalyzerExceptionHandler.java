package ch.heigvd.amt.team09.labelizer.advice;

import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class AnalyzerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(UnreachableUrlException.class)
    ResponseEntity<Object> handleUnreachableUrl(UnreachableUrlException e, WebRequest request) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e, request);
    }

    @ExceptionHandler(InvalidBase64Exception.class)
    ResponseEntity<Object> handleUnreachableUrl(InvalidBase64Exception e, WebRequest request) {
        return handleException(HttpStatus.UNPROCESSABLE_ENTITY, e, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var message = e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("\r\n"));
        return handleException(HttpStatus.BAD_REQUEST, message, e, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return switch (e.getCause()) {
            case UnrecognizedPropertyException ex -> handleUnknownProperty(ex, request);
            case JsonParseException ex -> handleInvalidJson(ex, request);
            case InvalidFormatException ex -> handleInvalidFormat(ex, request);
            default -> super.handleHttpMessageNotReadable(e, headers, status, request);
        };
    }

    private ResponseEntity<Object> handleUnknownProperty(UnrecognizedPropertyException e, WebRequest request) {
        var message = "Unknown property '%s', excepting %s".formatted(e.getPropertyName(), e.getKnownPropertyIds());
        return handleException(HttpStatus.BAD_REQUEST, message, e, request);
    }

    private ResponseEntity<Object> handleInvalidJson(JsonParseException e, WebRequest request) {
        var message = "Invalid JSON: unexpected token at line %d, column %d".formatted(
                e.getLocation().getLineNr(),
                e.getLocation().getColumnNr()
        );
        return handleException(HttpStatus.BAD_REQUEST, message, e, request);
    }

    private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException e, WebRequest request) {
        var message = "Invalid value '%s' for field '%s', wrong type".formatted(
                e.getValue(),
                e.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("."))
        );
        return handleException(HttpStatus.BAD_REQUEST, message, e, request);
    }

    private ResponseEntity<Object> handleException(HttpStatus status, Exception e, WebRequest request) {
        return handleException(status, e.getLocalizedMessage(), e, request);
    }

    private ResponseEntity<Object> handleException(HttpStatus status, String message, Exception e, WebRequest request) {
        var problem = createProblemDetail(e, status, message, null, null, request);
        return handleExceptionInternal(e, problem, new HttpHeaders(), status, request);
    }
}
