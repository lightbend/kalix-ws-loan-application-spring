package io.kx.loanproc.view;

public interface LoanProcViewModel {
    record ViewRecord(String statusId, String loanAppId, long lastUpdated) implements LoanProcViewModel{}
    record ViewRequest(String statusId) implements LoanProcViewModel{}
}
