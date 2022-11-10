package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcToLoanAppEventingAction extends Action {

    private final KalixClient kalixClient;

    public LoanProcToLoanAppEventingAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.post("/loanapp/"+event.loanAppId()+"/approve", LoanAppApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> LoanProcApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }

    public Effect<LoanProcApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.post("/loanapp/"+event.loanAppId()+"/decline",new LoanAppApi.DeclineRequest(event.reason()),LoanAppApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> LoanProcApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }

}
