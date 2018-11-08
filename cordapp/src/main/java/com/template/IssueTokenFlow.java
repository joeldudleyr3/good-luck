package com.template;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.CommandData;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.NetworkMapCache;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssueTokenFlow extends FlowLogic<Void> {
    private final int amount;
    private final String account;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public IssueTokenFlow(int amount, String account, Party owner) {
        this.amount = amount;
        this.account = account;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        NetworkMapCache networkMap = getServiceHub().getNetworkMapCache();
        Party notary = networkMap.getNotaryIdentities().get(0);

        TokenState outputToken = new TokenState(amount, account, getOurIdentity());
        CommandData issueCommand = new TokenContract.Commands.Issue();

        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addOutputState(outputToken, TokenContract.ID);
        txBuilder.addCommand(issueCommand, getOurIdentity().getOwningKey());

        txBuilder.verify(getServiceHub());

        SignedTransaction stx = getServiceHub().signInitialTransaction(txBuilder);

        subFlow(new FinalityFlow(stx));

        return null;
    }
}
