package ch.heigvd.amt.team09.labelizer.advice;

import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnknownException;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
public class AnalyzerExceptionHandler {
    @ResponseBody
    @ExceptionHandler(UnreachableUrlException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String unreachableUrlHandler(UnreachableUrlException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(InvalidBase64Exception.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String invalidBase64Handler(InvalidBase64Exception e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UnknownException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String unknownExceptionHandler(UnknownException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UnrecognizedPropertyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String unknownPropertyHandler(UnrecognizedPropertyException e) {
        return String.format("Unknown property '%s', excepting %s", e.getPropertyName(), e.getKnownPropertyIds());
    }

    @ResponseBody
    @ExceptionHandler(JsonParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String malformedJsonHandler(JsonParseException e) {
        return String.format("Malformed JSON: %s at line %d, column %d",
                e.getOriginalMessage(),
                e.getLocation().getLineNr(),
                e.getLocation().getColumnNr()
        );
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String validationExceptionsHandler(MethodArgumentNotValidException e) {
        return e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("\r\n"));
    }

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String unreadableHttpHandler(HttpMessageNotReadableException e) {
        return switch (e.getCause()) {
            case InvalidFormatException ex -> handleFormatException(ex);
            default -> String.format("Unknown error: %s", e.getMessage());
        };
    }

    private String handleFormatException(InvalidFormatException e) {
        return String.format("Invalid value '%s' for field '%s', expecting %s",
                e.getValue(),
                e.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining(".")),
                e.getTargetType().getSimpleName()
        );
    }
}
