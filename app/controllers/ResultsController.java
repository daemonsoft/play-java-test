package controllers;

import actions.AuthAction;
import play.libs.concurrent.Futures;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.time.temporal.ChronoUnit.SECONDS;

public class ResultsController extends Controller {

    private final Futures futures;

    @Inject
    public ResultsController(Futures futures) {
        this.futures = futures;
    }

    public Result customResponse() {

        response().setHeader("header-name", "header-value");

        response().setCookie(
                Http.Cookie.builder("mycookie", "cookie-test")
                        .withSecure(false)
                        .withHttpOnly(true)
                        .withSameSite(Http.Cookie.SameSite.STRICT)
                        .build()
        );

        return ok("Response with custom header and cookie").as("text/html");
    }

    @With(AuthAction.class)
    public Result composedAction() {
        if (request().getHeaders().get(AuthAction.TOKEN).isPresent()) {
            return ok("Composed action token: " + request().getHeaders().get(AuthAction.TOKEN).get()).as("text/html");
        }
        return internalServerError();
    }


    public CompletionStage<Result> future() {
        return getMessageFromFuture().thenApplyAsync(string -> ok("rendered after " + string));
    }

    private CompletionStage<String> getMessageFromFuture() {
        long start = System.currentTimeMillis();
        return futures.delayed(() -> CompletableFuture.supplyAsync(() -> {
            long end = System.currentTimeMillis();
            long milliseconds = end - start;
            return milliseconds + " milliseconds";
        }), Duration.of(1, SECONDS));
    }
}