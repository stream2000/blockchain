package cn.minus4.blockchain;

import cn.minus4.blockchain.utils.StringUtil;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * 交易类 Created on 2018/3/10 0010.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
public class Transaction {

    private final String transactionId; //Contains a hash of transaction*
    private final PublicKey sender; //Senders address/public key.
    private final PublicKey recipient; //Recipients address/public key.
    private byte[] signature; //This is to prevent anybody else from spending funds in our wallet.

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, PrivateKey privateKey) {
        this.sender = from;
        this.recipient = to;
        generateSignature(privateKey);
        transactionId = calculateHash();
    }

    public static boolean verifySignature(Transaction t, byte[] signature) {
        String data = StringUtil.getStringFromKey(t.getSender()) + StringUtil
            .getStringFromKey(t.getRecipient()) + 0;
        return StringUtil.verifyECDSASig(t.getSender(), data, signature);
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PublicKey getSender() {
        return sender;
    }

    public byte[] getSignature() {
        return signature;
    }

    private boolean processTransaction() {
        return true;
    }

    private void generateSignature(PrivateKey privateKey) {
        String data =
            StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + 0;
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    public String calculateHash() {
        return StringUtil
            .applySha256(
                StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient)
                    + new Date().getTime());
    }

}
