package cn.minus4.blockchain;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BlockchainDemo {
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Miner> miners = new ArrayList<>();

    BlockchainDemo() {
        Security.addProvider(new BouncyCastleProvider());
        for (int i = 0; i < 8; i++) {
            wallets.add(new Wallet());
        }
        for (int i = 0; i < 8; i++) {
            Miner m = new Miner(miners, i);
            miners.add(m);
        }

    }

    public static void main(String[] args) {
        new BlockchainDemo().run();
    }

    void run() {
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();

        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                    miners.forEach((c) -> {
                        c.pushTransaction(sender.newEmptyTransaction(receiver.getPublicKey()));
                    });
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();
    }
}