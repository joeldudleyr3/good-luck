package com.template;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.NetworkMapCache;
import net.corda.core.node.services.VaultService;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferTokenFlow extends FlowLogic<Void> {
    private final int amount;
    private final String oldAccount;
    private final String newAccount;
    private final Party newOwner;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public TransferTokenFlow(int amount, String oldAccount, String newAccount, Party newOwner) {
        this.amount = amount;
        this.oldAccount = oldAccount;
        this.newAccount = newAccount;
        this.newOwner = newOwner;
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

        VaultService vault = getServiceHub().getVaultService();
        List<StateAndRef<TokenState>> stateAndRefs = vault.queryBy(TokenState.class).getStates();

        List<StateAndRef<TokenState>> matchingStates = stateAndRefs.stream().filter(stateAndRef -> {
            TokenState state = stateAndRef.getState().getData();
            Boolean isCorrectAmount = state.getAmount() == amount;
            Boolean isCorrectAccount = state.getAccount().equals(oldAccount);
            return isCorrectAmount && isCorrectAccount;
        }).collect(Collectors.toList());

        if (matchingStates.isEmpty())
            throw new IllegalArgumentException("No matching token to transfer.");
        StateAndRef<TokenState> inputStateAndRef = matchingStates.get(0);
        TokenState inputState = inputStateAndRef.getState().getData();

        TokenState outputToken = new TokenState(inputState.getAmount(), newAccount, newOwner);
        CommandData transferCommand = new TokenContract.Commands.Transfer();

        TransactionBuilder txBuilder = new TransactionBuilder(notary);
        txBuilder.addInputState(inputStateAndRef);
        txBuilder.addOutputState(outputToken, TokenContract.ID);
        txBuilder.addCommand(transferCommand, getOurIdentity().getOwningKey());

        txBuilder.verify(getServiceHub());

        SignedTransaction stx = getServiceHub().signInitialTransaction(txBuilder);

        subFlow(new FinalityFlow(stx));

        return null;
    }
}
