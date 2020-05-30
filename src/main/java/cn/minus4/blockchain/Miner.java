package cn.minus4.blockchain;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Miner {
    private static Logger logger = Logger.getLogger(Miner.class);
    private final List<Block> blockchain = new ArrayList<>();
    private final List<Miner> Peers;
    private final int me;
    private final BlockingQueue<Transaction> transactions = new LinkedBlockingQueue<>();
    private final BlockingQueue<List<Block>> broadcastChannel = new LinkedBlockingQueue<>();
    private final CheckTask checkTask;
    private State state = State.Preparing;
    private Thread gatherTask;
    private Thread mineTask;

    public Miner(List<Miner> peers, int me) {
        Peers = peers;
        this.me = me;
        String allPrevious = "00000000";
        Block firstBlock = new Block(allPrevious);
        firstBlock.setZero();
        blockchain.add(firstBlock);
        CheckTask checker = new CheckTask(1, allPrevious);
        checkTask = checker;
        new Thread(checker).start();
        newGatherTask();
    }

    private String getPreviousHash() {
        if (blockchain.size() == 0) {
            return "";
        } else {
            return blockchain.get(blockchain.size() - 1).getHash();
        }
    }

    void pushTransaction(Transaction transaction) {
        transactions.offer(transaction);
    }

    private void newGatherTask() {
        state = State.Preparing;
        Thread gTask = new Thread(new GatherTransactionTask(getPreviousHash(), blockchain.size()));
        gatherTask = gTask;
        gTask.start();
    }

    // TODO: re-calculate the hash of each block, and verify the record inside each block.
    private boolean verifyBlockChain(List<Block> blockchain) {
        return true;
    }

    public void sendNewBlockChain(List<Block> blockchain) throws InterruptedException {
        broadcastChannel.put(blockchain);
    }

    enum State {
        Preparing, Mining
    }

    class CheckTask implements Runnable {
        volatile int currentLength;

        public CheckTask(int currentLength, String previousHash) {
            this.currentLength = currentLength;
        }

        public void setCurrentLength(int currentLength) {
            this.currentLength = currentLength;
        }

        @Override public void run() {
            while (true) {
                try {
                    List<Block> newBlockChain = broadcastChannel.take();
                    if (newBlockChain.size() > currentLength) {
                        // #1 verify is this blockchain is valid, or if it is proof by its power.
                        //TODO replace current empty logic
                        if (!verifyBlockChain(newBlockChain)) {
                            continue;
                        }
                        synchronized (Miner.this) {
                            currentLength = blockchain.size();
                            if (newBlockChain.size() > currentLength) {
                                blockchain.clear();
                                blockchain.addAll(newBlockChain);
                                transactions.clear();
                                switch (state) {
                                    case Mining:
                                        mineTask.interrupt();
                                        newGatherTask();
                                    case Preparing:
                                        gatherTask.interrupt();
                                        newGatherTask();
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    class MineTask implements Runnable {
        private final Block block;
        private final String previousHash;
        private final int length;

        MineTask(Block block, String previousHash, int length) {
            this.block = block;
            this.previousHash = previousHash;
            this.length = length;
        }

        @Override public void run() {
            block.mineBlock(5);
            if (Thread.interrupted()) {
                return;
            }
            synchronized (Miner.this) {
                if (state == State.Mining && getPreviousHash().endsWith(previousHash) && length == blockchain.size()) {
                    blockchain.add(block);
                    logger.info(
                        "Thread " + me + " mined, current chain length: " + blockchain.size() + " new block hash:"
                            + getPreviousHash());
                    checkTask.setCurrentLength(blockchain.size());
                    // broadcast
                    for (int i = 0; i < Peers.size(); i++) {
                        if (i == me) {
                            continue;
                        }
                        try {
                            Peers.get(i).sendNewBlockChain(blockchain);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    newGatherTask();
                }
            }
        }
    }

    class GatherTransactionTask implements Runnable {
        private final String previousHash;
        private final int length;

        GatherTransactionTask(String previousHash, int length) {
            this.previousHash = previousHash;
            this.length = length;
        }

        @Override public void run() {
            ArrayList<Transaction> localTransactions = new ArrayList<>();
            while (true) {
                try {
                    Transaction newTransaction = transactions.take();
                    Transaction.verifySignature(newTransaction, newTransaction.getSignature());
                    localTransactions.add(newTransaction);
                    if (localTransactions.size() == 1000) {
                        synchronized (Miner.this) {
                            if (state == State.Preparing) {
                                if (previousHash.equals(getPreviousHash()) && length == blockchain.size()) {
                                    state = State.Mining;
                                    gatherTask = null;
                                    Block block = new Block(previousHash);
                                    block.setTransactions(localTransactions);
                                    Thread task = new Thread(new MineTask(block, previousHash, length));
                                    mineTask = task;
                                    task.start();
                                }
                            }
                        }
                        return;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
