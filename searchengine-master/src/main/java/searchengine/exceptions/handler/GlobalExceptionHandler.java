package searchengine.exceptions.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.exceptions.IndexingProcessException;
import searchengine.exceptions.PageIndexingException;
import searchengine.exceptions.SearchingException;
import searchengine.web.services.dto.responses.ErrorResponse;

@SuppressWarnings("ALL")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IndexingProcessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIndexing(IndexingProcessException e){
        return new ErrorResponse(false, e.getMessage());
    }

    @ExceptionHandler(PageIndexingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePageIndexing(PageIndexingException e){
        return new ErrorResponse(false, e.getMessage());
    }

    @ExceptionHandler(SearchingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSearching(SearchingException e){
        return new ErrorResponse(false, e.getMessage());
    }
}
