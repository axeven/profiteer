In here, user can list his physical and logical wallet, create new wallets, and manage existing 
wallets.

By default, it will list user's physical wallet. User can select an alternative view where it will 
show list of the logical ones.

When viewing list of logical balance, show information of the amount of unallocated physical 
balance, which is basically the sum of physical wallet balance minus the sum of logical wallet 
balance.

The list is ordered in descending order by the wallet balance.

The wallet balance is shown in the default currency.

- **Create New Wallet**
    - Wallet name (with uniqueness validation)
    - Wallet type selection (Physical or Logical)
    - Currency selection (USD, EUR, GBP, JPY, CAD, AUD, IDR)
    - Initial balance setup (with thousands separator formatting)
    - Real-time form validation with error messages

- **Existing Wallet Management**
    - List of all user wallets with balance display
    - Each wallet shows: Name, Type, Currency, and formatted balance
    - Edit wallet functionality (modify name, type, currency, initial balance)
    - Delete wallet capability
    - Real-time balance updates