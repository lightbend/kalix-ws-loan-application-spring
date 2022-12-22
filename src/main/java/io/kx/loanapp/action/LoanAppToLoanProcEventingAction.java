package io.kx.loanapp.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanapp.domain.LoanAppDomainEvent;
import io.kx.loanproc.api.LoanProcApi;
import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanAppService.class, ignoreUnknown = true)
public class LoanAppToLoanProcEventingAction extends Action {

    private final KalixClient kalixClient;

    public LoanAppToLoanProcEventingAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onSubmitted(LoanAppDomainEvent.Submitted event){
        CompletionStage<LoanAppApi.EmptyResponse> processRes =
                kalixClient.post("/loanproc/"+event.loanAppId()+"/process", LoanProcApi.EmptyResponse.class).execute()
                        .thenApply(res -> LoanAppApi.EmptyResponse.of())
                        .exceptionally(e -> LoanAppApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }
}
