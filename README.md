# Running the nodes

1. Deploy the nodes: `gradlew deployNodes`
2. Build nodes: `build/nodes/runnodes`

# Interacting with the nodes

Issue tokens:

    flow start IssueTokenFlow amount: 100, account: Joel
   
Transfer tokens:

    flow start TransferTokenFlow amount: 100, oldAccount: Joel, newAccount: Antony, newOwner: PartyB

See the tokens:

    run vaultQuery contractStateType: com.template.TokenState
