package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.TransactionVerificationException;
import net.corda.core.flows.FlowException;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(ImmutableList.of("com.template"));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void cannotIssueNegativeTokens() throws Exception {
        IssueTokenFlow flow = new IssueTokenFlow(-1, "Joel");
        CordaFuture future = a.startFlow(flow);
        network.runNetwork();

        exception.expectCause(instanceOf(TransactionVerificationException.class));
        future.get();
    }

    @Test
    public void canIssuePositiveTokens() throws Exception {
        IssueTokenFlow flow = new IssueTokenFlow(1, "Joel");
        CordaFuture future = a.startFlow(flow);
        network.runNetwork();
        future.get();
    }

    @Test
    public void cannotTransferTokensYouDontHave() throws Exception {
        IssueTokenFlow flow = new IssueTokenFlow(1, "Joel");
        CordaFuture future = a.startFlow(flow);
        network.runNetwork();
        future.get();

        TransferTokenFlow flow2 = new TransferTokenFlow(1, "NonExistentAccount", "Antony", b.getInfo().getLegalIdentities().get(0));
        CordaFuture future2 = a.startFlow(flow2);
        network.runNetwork();
        exception.expectCause(instanceOf(IllegalArgumentException.class));
        future2.get();
    }

    @Test
    public void canTransferTokensYouDoHave() throws Exception {
        IssueTokenFlow flow = new IssueTokenFlow(1, "Joel");
        CordaFuture future = a.startFlow(flow);
        network.runNetwork();
        future.get();

        TransferTokenFlow flow2 = new TransferTokenFlow(1, "Joel", "Antony", b.getInfo().getLegalIdentities().get(0));
        CordaFuture future2 = a.startFlow(flow2);
        network.runNetwork();
        future2.get();
    }
}
