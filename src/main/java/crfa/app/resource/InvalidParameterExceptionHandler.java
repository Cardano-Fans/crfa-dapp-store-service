package crfa.app.resource;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = InvalidParameterException.class )
public class InvalidParameterExceptionHandler implements ExceptionHandler<InvalidParameterException, HttpResponse<String>> {

    @Override
    public HttpResponse<String> handle(HttpRequest request, InvalidParameterException exception) {
        return HttpResponse.notFound(exception.getMessage());
    }

}
