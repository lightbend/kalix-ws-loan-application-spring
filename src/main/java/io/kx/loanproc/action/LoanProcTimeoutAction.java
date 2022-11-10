package io.kx.loanproc.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanproc.LoanProcConfig;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import io.kx.loanproc.domain.LoanProcDomainEvent;
import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import kalix.springsdk.annotations.Subscribe;

import java.time.Duration;

@Subscribe.EventSourcedEntity(value = LoanProcService.class, ignoreUnknown = true)
public class LoanProcTimeoutAction extends Action {

    private final KalixClient kalixClient;
    private final LoanProcConfig config;

    public LoanProcTimeoutAction(KalixClient kalixClient, LoanProcConfig config) {
        this.kalixClient = kalixClient;
        this.config = config;
    }

    private String getTimerName(String loanAppId){
        return "timeout-"+loanAppId;
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
        var deferredCall = kalixClient.post("/loanproc/"+event.loanAppId()+"/decline",new LoanProcApi.DeclineRequest("SYSTEM", "timeout by timer"),LoanProcApi.EmptyResponse.class);
        timers().startSingleTimer(getTimerName(event.loanAppId()), Duration.ofMillis(config.getTimeoutMillis()),deferredCall);
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }

    public Action.Effect<LoanAppApi.EmptyResponse> onApproved(LoanProcDomainEvent.Approved event){
        timers().cancel(getTimerName(event.loanAppId()));
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }
    public Action.Effect<LoanAppApi.EmptyResponse> onDeclined(LoanProcDomainEvent.Declined event){
        timers().cancel(getTimerName(event.loanAppId()));
        return effects().reply(LoanAppApi.EmptyResponse.of());
    }

}
