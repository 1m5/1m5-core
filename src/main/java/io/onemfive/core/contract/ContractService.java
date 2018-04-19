package io.onemfive.core.contract;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ContractService extends BaseService {

    private EthereumContract ethereumContract;

    public ContractService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        String operation = (String) envelope.getHeader(Envelope.OPERATION);
        if(operation != null) {
            Contract c = selectContract(envelope);
            switch (operation) {
                case "CreateContract": c.createContract(envelope);break;
                case "SendCurrencyToContract": c.sendCurrencyToContract(envelope);break;
                case "CreateBounty": c.createBounty(envelope);break;
                case "SelectBounty": c.selectBounty(envelope);break;
                case "VoteOnBounty": c.voteOnBounty(envelope);break;
                case "RemoveBounty": c.removeBounty(envelope);break;
                case "CloseBounty": c.closeBounty(envelope);break;
                case "AddVoter": c.addVoter(envelope);break;
                case "RemoveVoter": c.removeVoter(envelope);break;
                case "ListBounties": c.listBounties(envelope);break;
                case "KillContract": c.killContract(envelope);break;
                default: deadLetter(envelope); // Operation not supported
            }
        }
    }

    private Contract selectContract(Envelope envelope) {
        // Only Ethereum for now
        return ethereumContract;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("ContractService starting...");
        ethereumContract = new EthereumContract();
        System.out.println("ContractService started.");
        return true;
    }

}
