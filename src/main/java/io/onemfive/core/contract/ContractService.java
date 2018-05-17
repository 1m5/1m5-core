package io.onemfive.core.contract;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.contract.ethreum.EthereumContract;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

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
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
            Contract c = selectContract(e);
            if(c instanceof BountyContract) {
                BountyContract bc = (BountyContract)c;
                switch (route.getOperation()) {
                    case "CreateBounty":{bc.createBounty(e);return;}
                    case "SelectBounty":{bc.selectBounty(e);return;}
                    case "VoteOnBounty":{bc.voteOnBounty(e);return;}
                    case "RemoveBounty":{bc.removeBounty(e);return;}
                    case "CloseBounty":{bc.closeBounty(e);return;}
                    case "ListBounties":{bc.listBounties(e);return;}
                    default: deadLetter(e);;
                }
            }
            switch (route.getOperation()) {
                case "CreateContract":{c.createContract(e);return;}
                case "SendCurrencyToContract":{c.sendCurrencyToContract(e);return;}
                case "AddVoter":{c.addVoter(e);return;}
                case "RemoveVoter":{c.removeVoter(e);return;}
                case "KillContract":{c.killContract(e);return;}
                default:deadLetter(e); // Operation not supported
            }
    }

    private Contract selectContract(Envelope e) {
        // Only Ethereum for now
        return ethereumContract;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(ContractService.class.getSimpleName()+": starting...");
        ethereumContract = new EthereumContract();
        System.out.println("ContractService started.");
        return true;
    }

}
