package com.xingkaichun.helloworldblockchain.netcore.dto.transaction;

import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class SubmitTransactionToBlockchainRequest {

    private TransactionDTO transactionDTO;




    //region get set

    public TransactionDTO getTransactionDTO() {
        return transactionDTO;
    }

    public void setTransactionDTO(TransactionDTO transactionDTO) {
        this.transactionDTO = transactionDTO;
    }


    //endregion
}