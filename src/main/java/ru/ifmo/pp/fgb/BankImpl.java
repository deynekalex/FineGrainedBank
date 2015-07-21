package ru.ifmo.pp.fgb;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.max;
import static java.lang.Integer.min;


/**
 * Bank implementation.
 * <p/>
 * <p>:TODO: This implementation has to be made thread-safe.
 *
 * @author Дейнека
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
        long sum = 0;
        try {
            accounts[index].lock.lock();
            sum = accounts[index].amount;
        } finally {
            accounts[index].lock.unlock();
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        try {
            for (int i = 0; i < getNumberOfAccounts(); i++) {
                accounts[i].lock.lock();
            }
            sum = 0;
            for (Account account : accounts) {
                sum += account.amount;
            }
        } finally {
            for (int i = getNumberOfAccounts() - 1; i > -1; i--) {
                accounts[i].lock.unlock();
            }
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    //from inside to bank account
    @Override
    public long deposit(int index, long amount) {
        long ret = 0;
        try {
            accounts[index].lock.lock();
            if (amount <= 0)
                throw new IllegalArgumentException("Invalid amount: " + amount);
            Account account = accounts[index];
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            account.amount += amount;
            ret = account.amount;
        } finally {
            accounts[index].lock.unlock();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    //from bank account to client
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        long ret = 0;
        try {
            accounts[index].lock.lock();
            Account account = accounts[index];
            if (account.amount - amount < 0)
                throw new IllegalStateException("Underflow");
            account.amount -= amount;
            ret = account.amount;
        } finally {
            accounts[index].lock.unlock();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     * <p>:TODO: This method has to be made thread-safe.
     */
    //from client to client
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        try {
            accounts[min(fromIndex, toIndex)].lock.lock();
            accounts[max(fromIndex, toIndex)].lock.lock();
            Account from = accounts[fromIndex];
            Account to = accounts[toIndex];
            if (amount > from.amount)
                throw new IllegalStateException("Underflow");
            else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
            from.amount -= amount;
            to.amount += amount;
        } finally {
            accounts[max(fromIndex, toIndex)].lock.unlock();
            accounts[min(fromIndex, toIndex)].lock.unlock();
        }
    }

    /**
     * Private account data structure.
     */
    private static class Account {
        /**
         * Amount of funds in this account.
         */
        long amount;
        Lock lock = new ReentrantLock();
    }
}
