import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ATM {
    private Bank bank;
    private AccountHolder accountHolder;
    private Scanner scanner;
    private int loginAttempts;

    public ATM() {
        bank = new Bank();
        scanner = new Scanner(System.in);
        loginAttempts = 0;
    }

    public static void main(String[] args) {
        ATM atm = new ATM();
        atm.run();
    }

    public void run() {
        // Prompt the user to select an option: Login or Create Account
        System.out.println("Welcome to the ATM. Please select an option:");
        System.out.println("1. Login");
        System.out.println("2. Create Account");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1:
                login();
                break;
            case 2:
                createAccount();
                break;
            default:
                System.out.println("Invalid option. Exiting...");
                break;
        }

        scanner.close();
    }

    private void login() {
        // Prompt the user for their user ID and PIN.
        System.out.println("Enter your user ID: ");
        String userID = scanner.nextLine();
        System.out.println("Enter your PIN: ");
        String pin = scanner.nextLine();

        accountHolder = bank.getAccountHolder(userID);
        if (accountHolder != null && accountHolder.validateCredentials(pin)) {
            loginAttempts = 0; // Reset login attempts on successful login
            showMainMenu();
        } else {
            System.out.println("Invalid user ID or PIN.");

            loginAttempts++;
            if (loginAttempts < 3) {
                System.out.println("Please try again.");
                login();
            } else {
                System.out.println("Maximum login attempts exceeded. Exiting...");
            }
        }
    }

    private void createAccount() {
        // Prompt the user to enter their desired user ID and PIN.
        System.out.println("Enter a user ID: ");
        String userID = scanner.nextLine();
        System.out.println("Enter a PIN: ");
        String pin = scanner.nextLine();

        if (bank.isUserIDAvailable(userID)) {
            // Create a new account holder and add it to the bank
            accountHolder = new AccountHolder(userID, pin);
            bank.addAccountHolder(accountHolder);

            System.out.println("Account created successfully. Please log in to continue.");
            login();
        } else {
            System.out.println("User ID is already taken. Please try again.");
            createAccount();
        }
    }

    private void showMainMenu() {
        System.out.println("Welcome to the ATM, " + accountHolder.getUserID() + ".");
        int choice;
        do {
            // Prompt the user to select an ATM operation.
            System.out.println("What would you like to do?");
            System.out.println("1. Show transaction history");
            System.out.println("2. Withdraw money");
            System.out.println("3. Deposit money");
            System.out.println("4. Transfer money");
            System.out.println("5. Check account balance");
            System.out.println("6. Quit");

            // Parse the choice as an integer
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    // Show the user's transaction history.
                    accountHolder.showTransactionsHistory();
                    break;
                case 2:
                    // Withdraw money from the user's account.
                    System.out.println("Enter the amount to withdraw: ");
                    double withdrawAmount = Double.parseDouble(scanner.nextLine());
                    accountHolder.withdraw(withdrawAmount);
                    break;
                case 3:
                    // Deposit money into the user's account.
                    System.out.println("Enter the amount to deposit: ");
                    double depositAmount = Double.parseDouble(scanner.nextLine());
                    accountHolder.deposit(depositAmount);
                    break;
                case 4:
                    // Transfer money from one account to another.
                    System.out.println("Enter the recipient's user ID: ");
                    String recipientUserID = scanner.nextLine();
                    System.out.println("Enter the amount to transfer: ");
                    double transferAmount = Double.parseDouble(scanner.nextLine());
                    accountHolder.transfer(recipientUserID, transferAmount, bank);
                    break;
                case 5:
                    // Check account balance.
                    System.out.println("Account Balance: " + accountHolder.getBalance());
                    break;
                case 6:
                    // Quit the ATM.
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (choice != 6);
    }

    class Bank {
        private List<AccountHolder> accountHolders;

        public Bank() {
            accountHolders = new ArrayList<>();
            // Add some sample account holders to the bank
            accountHolders.add(new AccountHolder("123", "John Doe"));
            accountHolders.add(new AccountHolder("456", "Jane Smith"));
        }

        public AccountHolder getAccountHolder(String userID) {
            for (AccountHolder accountHolder : accountHolders) {
                if (accountHolder.getUserID().equals(userID)) {
                    return accountHolder;
                }
            }
            return null;
        }

        public void addAccountHolder(AccountHolder accountHolder) {
            accountHolders.add(accountHolder);
        }

        public boolean isUserIDAvailable(String userID) {
            for (AccountHolder accountHolder : accountHolders) {
                if (accountHolder.getUserID().equals(userID)) {
                    return false;
                }
            }
            return true;
        }
    }

    class AccountHolder {
        private String userID;
        private String pin;
        private List<Transaction> transactionHistory;

        public AccountHolder(String userID, String pin) {
            this.userID = userID;
            this.pin = pin;
            transactionHistory = new ArrayList<>();
        }

        public String getUserID() {
            return userID;
        }

        public boolean validateCredentials(String pin) {
            return this.pin.equals(pin);
        }

        public void showTransactionsHistory() {
            if (transactionHistory.isEmpty()) {
                System.out.println("No transaction history available.");
            } else {
                System.out.println("Transaction History:");
                for (Transaction transaction : transactionHistory) {
                    double amount = transaction.getAmount();
                    String transactionType = amount < 0 ? "Outgoing" : "Incoming";
                    System.out.println("Description: " + transaction.getDescription());
                    System.out.println("Amount: " + Math.abs(amount) + " (" + transactionType + ")");
                    System.out.println("Date/Time: " + transaction.getFormattedDateTime());
                    System.out.println();
                }
            }
        }

        public double getBalance() {
            double balance = 0.0;
            for (Transaction transaction : transactionHistory) {
                balance += transaction.getAmount();
            }
            return balance;
        }

        public void withdraw(double amount) {
            if (amount <= getBalance()) {
                if (amount > 0) {
                    Transaction transaction = new Transaction("Withdrawal", -amount);
                    transactionHistory.add(transaction);
                    System.out.println("Withdrawal successful. Amount: " + amount);
                } else {
                    System.out.println("Invalid amount. Withdrawal failed.");
                }
            } else {
                System.out.println("Insufficient balance. Withdrawal failed.");
            }
        }

        public void deposit(double amount) {
            if (amount > 0) {
                Transaction transaction = new Transaction("Deposit", amount);
                transactionHistory.add(transaction);
                System.out.println("Deposit successful. Amount: " + amount);
            } else {
                System.out.println("Invalid amount. Deposit failed.");
            }
        }

        public void transfer(String recipientUserID, double amount, Bank bank) {
            if (amount <= getBalance()) {
                AccountHolder recipientAccountHolder = bank.getAccountHolder(recipientUserID);
                if (recipientAccountHolder != null) {
                    if (amount > 0) {
                        Transaction senderTransaction = new Transaction("Transfer to " + recipientUserID, -amount);
                        Transaction recipientTransaction = new Transaction("Transfer from " + userID, amount);
                        transactionHistory.add(senderTransaction);
                        recipientAccountHolder.transactionHistory.add(recipientTransaction);
                        System.out.println("Transfer successful. Amount: " + amount);
                    } else {
                        System.out.println("Invalid amount. Transfer failed.");
                    }
                } else {
                    System.out.println("Recipient user ID not found. Transfer failed.");
                }
            } else {
                System.out.println("Insufficient balance. Transfer failed.");
            }
        }
    }

    class Transaction {
        private String description;
        private double amount;
        private LocalDateTime transactionDateTime;

        public Transaction(String description, double amount) {
            this.description = description;
            this.amount = amount;
            this.transactionDateTime = LocalDateTime.now();
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public String getFormattedDateTime() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
            return transactionDateTime.format(formatter);
        }
    }
}
