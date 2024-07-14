import java.util.Scanner;

class Account {
    private int accountNumber;
    private int balance;

    public Account(int accountNumber, int balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public void withdraw(int amount) {
        if(amount > balance) {
            System.out.println("Insufficient balance!");
        } else {
            balance -= amount;
            System.out.println("Withdrawn: " + amount);
        }
    }

    public void deposit(int amount) {
        balance += amount;
        System.out.println("Deposited: " + amount);
    }

    public int getBalance() {
        return balance;
    }
}

public class AtmInterface {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Initial balance");
        int bal = sc.nextInt();
        int amt = 0;
        String z = "";
        Account ac = new Account((int)(Math.random() * 1000000000), bal);
        while (true) {
            System.out.println("Enter 1. Withdraw 2. Deposit 3. Check Balance");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Enter the amount to Withdraw");
                    amt = sc.nextInt();
                    ac.withdraw(amt);
                    break;
                case 2:
                    System.out.println("Enter the amount to Deposit");
                    amt = sc.nextInt();
                    ac.deposit(amt);
                    break;
                case 3:
                    int x = ac.getBalance();
                    System.out.println("The current Balance of the User is " + x);
                    break;
                default:
                    System.out.println("Enter a valid choice!");
            }
            System.out.println("To CONTINUE Banking Press any Key. To EXIT press \"N\" ");
            z = sc.next();
            if (z.equalsIgnoreCase("N")) break;
        }
        sc.close();
    }
}