package ch.heigvd.amt.team09.labelizer.advice;

import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnknownException;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

// TODO améliorer les messages
@ControllerAdvice
public class RekognitionExceptionHandler {
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
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleMissingRequestBody(HttpMessageNotReadableException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleValidationExceptions(MethodArgumentNotValidException e) {
        return e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("\r\n"));
    }
}
