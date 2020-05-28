package cn.minus4.blockchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockchainDemo {
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Miner> miners = new ArrayList<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<Transaction> transactions = new ArrayList<>();

    BlockchainDemo() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();
        for (int i = 0; i < 8; i++) {
            wallets.add(new Wallet());
        }
        for (int i = 0; i < 8; i++) {
            Miner m  = new Miner(miners,i);
            miners.add(m);
        }
        Thread t = new Thread(()->{
            while (true){
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                    miners.forEach((c)->{
                       c.pushTransaction(sender.newEmptyTransaction(receiver.getPublicKey()));
                    });
                } catch (InterruptedException e) {
                   return;
                }
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        new BlockchainDemo();
    }
}