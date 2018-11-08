package com.template;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Collections;
import java.util.List;

// *********
// * State *
// *********
public class TokenState implements ContractState {
    private final int amount;
    private final String account;
    private final Party owner;

    public TokenState(int amount, String account, Party owner) {
        this.amount = amount;
        this.account = account;
        this.owner = owner;
    }

    public int getAmount() { return amount; }
    public String getAccount() { return account; }
    public Party getOwner() { return owner; }

    @Override
    public List<AbstractParty> getParticipants() {
        return Collections.singletonList(owner);
    }
}