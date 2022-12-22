package io.kx.loanapp.action;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.domain.LoanAppDomainState;
import kalix.javasdk.action.Action;
import kalix.springsdk.KalixClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@RequestMapping("/loanapp-gw")
public class LoanAppServiceGatewayAction extends Action {

    private final KalixClient kalixClient;

    public LoanAppServiceGatewayAction(KalixClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    @PostMapping("/submit")
    public Action.Effect<LoanAppDomainState> submit(@RequestBody LoanAppApi.SubmitRequest request){
        var loanAppId = UUID.randomUUID().toString();
        CompletionStage<LoanAppApi.EmptyResponse> submit = kalixClient.post("/loanapp/"+loanAppId+"/submit",request, LoanAppApi.EmptyResponse.class).execute();
        CompletionStage<LoanAppDomainState> res = submit.thenCompose(r ->
                                                        kalixClient.get("/loanapp/"+loanAppId, LoanAppApi.GetResponse.class).execute()
                                                   ).thenApply(LoanAppApi.GetResponse::state);
        return effects().asyncReply(res);
    }
}
