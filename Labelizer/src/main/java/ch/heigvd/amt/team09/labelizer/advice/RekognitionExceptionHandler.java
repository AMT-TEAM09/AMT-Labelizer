package ch.heigvd.amt.team09.labelizer.advice;

import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnknownException;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RekognitionExceptionHandler {
    @ResponseBody
    @ExceptionHandler(UnreachableUrlException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String unreachableUrlHandler(UnreachableUrlException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UnreachableUrlException.class)
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
}
