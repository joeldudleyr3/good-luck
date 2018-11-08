package com.template;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

// ************
// * Contract *
// ************
public class TokenContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.TokenContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        List<Command<TokenContract.Commands>> tokenCommands = tx.commandsOfType(TokenContract.Commands.class);
        if (tokenCommands.size() != 1) throw new IllegalArgumentException("There should only be a single token command.");
        Command tokenCommand = tokenCommands.get(0);

        if (tokenCommand.getValue() instanceof Commands.Issue) {
            if (tx.inputsOfType(TokenState.class).size() != 0) throw new IllegalArgumentException();
            if (tx.outputsOfType(TokenState.class).size() != 1) throw new IllegalArgumentException();

            TokenState output = tx.outputsOfType(TokenState.class).get(0);
            if (output.getAmount() < 0) throw new IllegalArgumentException();

            List requiredSigners = tokenCommand.getSigners();
            PublicKey tokenOwnersKey = output.getOwner().getOwningKey();
            if (!(requiredSigners.contains(tokenOwnersKey)))
                throw new IllegalArgumentException("Owner of the issued token must sign.");

        } else if (tokenCommand.getValue() instanceof Commands.Transfer) {
            if (tx.inputsOfType(TokenState.class).size() != 1) throw new IllegalArgumentException();
            if (tx.outputsOfType(TokenState.class).size() != 1) throw new IllegalArgumentException();

            // TODO: More transfer rules.

        } else throw new IllegalArgumentException("Unrecognised command.");
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Issue implements Commands {}
        class Transfer implements Commands {}
    }
}