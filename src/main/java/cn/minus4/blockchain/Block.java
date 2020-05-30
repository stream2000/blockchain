package cn.minus4.blockchain;

import cn.minus4.blockchain.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;

public class Block {
    private String hash;
    private String previousHash;
    private String merkleRoot;
    private ArrayList<Transaction> transactions = new ArrayList<>(); //our data will be a simple message.
    private long timeStamp; //as number of milliseconds since 1/1/1970.
    private int nonce;
    //Block Constructor.
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        calculateHash(); //Making sure we do this after we set the other values.
    }
    public void setZero(){
        StringBuilder zero = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            zero.append("0");
        }
        hash = zero.toString();
    }
    //Calculate new hash based on blocks contents
    public void calculateHash() {
        this.hash =
            StringUtil.applySha256(previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + merkleRoot);
    }

    //Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) throws InterruptedException{
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0"
        while (!hash.startsWith(target)) {
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException();
            }
            nonce++;
            calculateHash();
        }
    }

    //Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null)
            return false;
        if ((!previousHash.equals("0"))) {
            //            if ((!transaction.processTransaction())) {
            //                System.out.println("Transaction failed to process. Discarded.");
            //                return false;
            //            }
        }

        transactions.add(transaction);
        return true;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
        calculateHash();
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
