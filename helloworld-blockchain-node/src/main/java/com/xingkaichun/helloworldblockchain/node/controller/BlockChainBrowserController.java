package com.xingkaichun.helloworldblockchain.node.controller;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutputId;
import com.xingkaichun.helloworldblockchain.core.tools.BlockTool;
import com.xingkaichun.helloworldblockchain.core.tools.StructureSizeTool;
import com.xingkaichun.helloworldblockchain.node.dto.node.PingRequest;
import com.xingkaichun.helloworldblockchain.node.dto.node.PingResponse;
import com.xingkaichun.helloworldblockchain.node.dto.account.GenerateAccountRequest;
import com.xingkaichun.helloworldblockchain.node.dto.account.GenerateAccountResponse;
import com.xingkaichun.helloworldblockchain.util.StringUtil;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.NetBlockchainCore;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.ServiceResult;
import com.xingkaichun.helloworldblockchain.netcore.dto.common.page.PageCondition;
import com.xingkaichun.helloworldblockchain.netcore.dto.netserver.NodeDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitTransactionRequest;
import com.xingkaichun.helloworldblockchain.netcore.dto.transaction.SubmitTransactionResponse;
import com.xingkaichun.helloworldblockchain.netcore.transport.dto.TransactionDTO;
import com.xingkaichun.helloworldblockchain.node.dto.BlockChainApiRoute;
import com.xingkaichun.helloworldblockchain.node.dto.block.*;
import com.xingkaichun.helloworldblockchain.node.dto.transaction.*;
import com.xingkaichun.helloworldblockchain.node.service.BlockChainBrowserService;
import com.xingkaichun.helloworldblockchain.util.DateUtil;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 区块链浏览器控制器
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
@Controller
@RequestMapping
public class BlockChainBrowserController {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainBrowserController.class);

    @Autowired
    private NetBlockchainCore netBlockchainCore;
    @Autowired
    private BlockChainBrowserService blockChainBrowserService;

   /**
     * 生成账户(公钥、私钥、地址)
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.GENERATE_ACCOUNT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<GenerateAccountResponse> generateAccount(@RequestBody GenerateAccountRequest request){
        try {
            Account account = AccountUtil.randomAccount();
            GenerateAccountResponse response = new GenerateAccountResponse();
            response.setAccount(account);
            return ServiceResult.createSuccessServiceResult("生成账户成功",response);
        } catch (Exception e){
            String message = "生成账户失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 提交交易到区块链网络
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.SUBMIT_TRANSACTION,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<SubmitTransactionResponse> submitTransaction(@RequestBody SubmitTransactionRequest request){
        try {
            SubmitTransactionResponse submitTransactionResponse = netBlockchainCore.submitTransaction(request);
            return ServiceResult.createSuccessServiceResult("提交交易到区块链网络成功", submitTransactionResponse);
        } catch (Exception e){
            String message = "提交交易到区块链网络失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易哈希查询交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionByTransactionHashResponse> queryTransactionByTransactionHash(@RequestBody QueryTransactionByTransactionHashRequest request){
        try {
            TransactionView transactionView = blockChainBrowserService.queryTransactionByTransactionHash(request.getTransactionHash());
            QueryTransactionByTransactionHashResponse response = new QueryTransactionByTransactionHashResponse();
            response.setTransactionView(transactionView);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易高度查询交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_LIST_BY_TRANSACTION_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionListByTransactionHeightResponse> queryTransactionListByTransactionHeight(@RequestBody QueryTransactionListByTransactionHeightRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 1L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<Transaction> transactionList = getBlockChainCore().queryTransactionListByTransactionHeight(from,size);
            if(transactionList == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在交易高度[%s]，请检查输入的交易哈希。",request.getPageCondition().getFrom()));
            }
            QueryTransactionListByTransactionHeightResponse response = new QueryTransactionListByTransactionHeightResponse();
            response.setTransactionList(transactionList);
            return ServiceResult.createSuccessServiceResult("根据交易高度查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易高度查询交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易高度查询交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_LIST_BY_BLOCK_HASH_TRANSACTION_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionListByBlockHashTransactionHeightResponse> queryTransactionListByBlockHashTransactionHeight(@RequestBody QueryTransactionListByBlockHashTransactionHeightRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 1L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            if(StringUtil.isNullOrEmpty(request.getBlockHash())){
                return ServiceResult.createFailServiceResult("区块哈希不能是空");
            }
            List<TransactionView> transactionViewList = blockChainBrowserService.queryTransactionListByBlockHashTransactionHeight(request.getBlockHash(),from,size);
            QueryTransactionListByBlockHashTransactionHeightResponse response = new QueryTransactionListByBlockHashTransactionHeightResponse();
            response.setTransactionViewList(transactionViewList);
            return ServiceResult.createSuccessServiceResult("根据交易高度查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易高度查询交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_LIST_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionListByAddressResponse> queryTransactionListByAddress(@RequestBody QueryTransactionListByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionView> transactionViewList = blockChainBrowserService.queryTransactionListByAddress(request.getAddress(),from,size);
            QueryTransactionListByAddressResponse response = new QueryTransactionListByAddressResponse();
            response.setTransactionViewList(transactionViewList);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易哈希查询挖矿中交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_MINING_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryMiningTransactionByTransactionHashResponse> queryMiningTransactionByTransactionHash(@RequestBody QueryMiningTransactionByTransactionHashRequest request){
        try {
            TransactionDTO transactionDTO = getBlockChainCore().queryMiningTransactionDtoByTransactionHash(request.getTransactionHash());
            if(transactionDTO == null){
                return ServiceResult.createFailServiceResult(String.format("交易哈希[%s]不是正在被挖矿的交易。",request.getTransactionHash()));
            }

            QueryMiningTransactionByTransactionHashResponse response = new QueryMiningTransactionByTransactionHashResponse();
            response.setTransactionDTO(transactionDTO);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询挖矿中交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询挖矿中交易失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据地址获取未花费交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_UNSPEND_TRANSACTION_OUTPUT_LIST_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryUnspendTransactionOutputListByAddressResponse> queryUnspendTransactionOutputListByAddress(@RequestBody QueryUnspendTransactionOutputListByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionOutputDetailView> transactionOutputDetailViewList = blockChainBrowserService.queryUnspendTransactionOutputListByAddress(request.getAddress(),from,size);
            QueryUnspendTransactionOutputListByAddressResponse response = new QueryUnspendTransactionOutputListByAddressResponse();
            response.setTransactionOutputDetailViewList(transactionOutputDetailViewList);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 根据地址获取未花费交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_SPEND_TRANSACTION_OUTPUT_LIST_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QuerySpendTransactionOutputListByAddressResponse> querySpendTransactionOutputListByAddress(@RequestBody QuerySpendTransactionOutputListByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionOutputDetailView> transactionOutputDetailViewList = blockChainBrowserService.querySpendTransactionOutputListByAddress(request.getAddress(),from,size);
            QuerySpendTransactionOutputListByAddressResponse response = new QuerySpendTransactionOutputListByAddressResponse();
            response.setTransactionOutputDetailViewList(transactionOutputDetailViewList);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 根据地址获取交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_OUTPUT_LIST_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionOutputListByAddressResponse> queryTransactionOutputListByAddress(@RequestBody QueryTransactionOutputListByAddressRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionOutputDetailView> transactionOutputDetailViewList = blockChainBrowserService.queryTransactionOutputListByAddress(request.getAddress(),from,size);
            QueryTransactionOutputListByAddressResponse response = new QueryTransactionOutputListByAddressResponse();
            response.setTransactionOutputDetailViewList(transactionOutputDetailViewList);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * 根据交易输出ID获取交易输出
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_TRANSACTION_OUTPUT_BY_TRANSACTION_OUTPUT_ID,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionOutputByTransactionOutputIdResponse> queryTransactionOutputByTransactionOutputId(@RequestBody QueryTransactionOutputByTransactionOutputIdRequest request){
        try {
            TransactionOutputId transactionOutputId = request.getTransactionOutputId();
            TransactionOutputDetailView transactionOutputDetailView = blockChainBrowserService.queryTransactionOutputByTransactionOutputId(transactionOutputId);
            QueryTransactionOutputByTransactionOutputIdResponse response = new QueryTransactionOutputByTransactionOutputIdResponse();
            response.setTransactionOutputDetailView(transactionOutputDetailView);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    /**
     * Ping节点
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.PING,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<PingResponse> ping(@RequestBody PingRequest request){
        try {
            List<NodeDto> nodeList = netBlockchainCore.getNodeService().queryAllNoForkNodeList();
            long blockChainHeight = getBlockChainCore().queryBlockChainHeight();
            PingResponse response = new PingResponse();
            response.setNodeList(nodeList);
            response.setBlockChainHeight(blockChainHeight);
            response.setBlockChainId(GlobalSetting.BLOCK_CHAIN_ID);
            response.setBlockChainVersion(GlobalSetting.SystemVersionConstant.obtainVersion());
            return ServiceResult.createSuccessServiceResult("查询节点信息成功",response);
        } catch (Exception e){
            String message = "查询节点信息失败";
            logger.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 查询挖矿中的交易
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_MINING_TRANSACTION_LIST,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryMiningTransactionListResponse> queryMiningTransactionList(@RequestBody QueryMiningTransactionListRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            long from = pageCondition.getFrom() == null ? 0L : pageCondition.getFrom();
            long size = pageCondition.getSize() == null ? 10L : pageCondition.getSize();
            List<TransactionDTO> transactionDtoList = getBlockChainCore().queryMiningTransactionList(from,size);
            QueryMiningTransactionListResponse response = new QueryMiningTransactionListResponse();
            response.setTransactionDtoList(transactionDtoList);
            return ServiceResult.createSuccessServiceResult("查询挖矿中的交易成功",response);
        } catch (Exception e){
            String message = "查询挖矿中的交易失败";
            logger.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 根据区块高度查询区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_BLOCKDTO_BY_BLOCK_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockDtoByBlockHeightResponse> queryBlockDtoByBlockHeight(@RequestBody QueryBlockDtoByBlockHeightRequest request){
        try {
            Block block = getBlockChainCore().queryBlockByBlockHeight(request.getBlockHeight());
            if(block == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在区块高度[%d]，请检查输入高度。",request.getBlockHeight()));
            }
            QueryBlockDtoByBlockHeightResponse response = new QueryBlockDtoByBlockHeightResponse();
            response.setBlock(block);
            return ServiceResult.createSuccessServiceResult("成功获取区块",response);
        } catch (Exception e){
            String message = "查询获取失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据区块哈希查询区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_BLOCKDTO_BY_BLOCK_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockDtoByBlockHashResponse> queryBlockDtoByBlockHash(@RequestBody QueryBlockDtoByBlockHashRequest request){
        try {
            Block block = getBlockChainCore().queryBlockByBlockHash(request.getBlockHash());
            if(block == null){
                return ServiceResult.createFailServiceResult(String.format("区块链中不存在区块哈希[%s]，请检查输入哈希。",request.getBlockHash()));
            }
            Block nextBlock = getBlockChainCore().queryBlockByBlockHeight(block.getHeight()+1);

            QueryBlockDtoByBlockHashResponse.BlockDto blockDto = new QueryBlockDtoByBlockHashResponse.BlockDto();
            blockDto.setHeight(block.getHeight());
            blockDto.setConfirmCount(BlockTool.getTransactionCount(block));
            blockDto.setBlockSize(StructureSizeTool.calculateBlockTextSize(block)+"字符");
            blockDto.setTransactionCount(BlockTool.getTransactionCount(block));
            blockDto.setTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));
            blockDto.setMinerIncentiveValue(BlockTool.getMinerIncentiveValue(block));
            blockDto.setMinerDifficulty(block.getBits());
            blockDto.setNonce(String.valueOf(block.getNonce()));
            blockDto.setHash(block.getHash());
            blockDto.setPreviousBlockHash(block.getPreviousBlockHash());
            blockDto.setNextBlockHash(nextBlock==null?null:nextBlock.getHash());
            blockDto.setMerkleTreeRoot(block.getMerkleTreeRoot());

            QueryBlockDtoByBlockHashResponse response = new QueryBlockDtoByBlockHashResponse();
            response.setBlockDto(blockDto);
            return ServiceResult.createSuccessServiceResult("[根据区块哈希查询区块]成功",response);
        } catch (Exception e){
            String message = "[根据区块哈希查询区块]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询最近的10个区块
     */
    @ResponseBody
    @RequestMapping(value = BlockChainApiRoute.QUERY_LAST10_BLOCKDTO,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryLast10BlockDtoResponse> queryLast10BlockDto(@RequestBody QueryLast10BlockDtoRequest request){
        try {
            List<Block> blockList = new ArrayList<>();
            long blockChainHeight = getBlockChainCore().queryBlockChainHeight();
            long minBlockHeight = blockChainHeight-9>0?blockChainHeight-9:1;
            while (blockChainHeight >= minBlockHeight){
                Block block = getBlockChainCore().queryBlockByBlockHeight(blockChainHeight);
                blockList.add(block);
                blockChainHeight--;
            }

            QueryLast10BlockDtoResponse response = new QueryLast10BlockDtoResponse();
            List<QueryLast10BlockDtoResponse.BlockDto> blockDtoList = new ArrayList<>();
            for(Block block : blockList){
                QueryLast10BlockDtoResponse.BlockDto blockDto = new QueryLast10BlockDtoResponse.BlockDto();
                blockDto.setHeight(block.getHeight());
                blockDto.setBlockSize(StructureSizeTool.calculateBlockTextSize(block)+"字符");
                blockDto.setTransactionCount(BlockTool.getTransactionCount(block));
                blockDto.setMinerIncentiveValue(BlockTool.getMinerIncentiveValue(block));
                blockDto.setTime(DateUtil.timestamp2ChinaTime(block.getTimestamp()));
                blockDto.setHash(block.getHash());
                blockDtoList.add(blockDto);
            }
            response.setBlockDtoList(blockDtoList);
            return ServiceResult.createSuccessServiceResult("[查询最近的10个区块]成功",response);
        } catch (Exception e){
            String message = "[查询最近的10个区块]失败";
            logger.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
    private BlockchainCore getBlockChainCore(){
        return netBlockchainCore.getBlockChainCore();
    }
}