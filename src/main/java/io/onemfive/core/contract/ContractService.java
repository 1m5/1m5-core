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
    public void handleDocument(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        if(route.getOperation() != null) {
            Contract c = selectContract(envelope);
            if(c instanceof BountyContract) {
                BountyContract bc = (BountyContract)c;
                switch (route.getOperation()) {
                    case "CreateBounty":
                        bc.createBounty(envelope);
                        return;
                    case "SelectBounty":
                        bc.selectBounty(envelope);
                        return;
                    case "VoteOnBounty":
                        bc.voteOnBounty(envelope);
                        return;
                    case "RemoveBounty":
                        bc.removeBounty(envelope);
                        return;
                    case "CloseBounty":
                        bc.closeBounty(envelope);
                        return;
                    case "ListBounties":
                        bc.listBounties(envelope);
                        return;
                }
            }
            switch (route.getOperation()) {
                case "CreateContract":
                    c.createContract(envelope);
                    return;
                case "SendCurrencyToContract":
                    c.sendCurrencyToContract(envelope);
                    return;
                case "AddVoter":
                    c.addVoter(envelope);
                    return;
                case "RemoveVoter":
                    c.removeVoter(envelope);
                    return;
                case "KillContract":
                    c.killContract(envelope);
                    return;
                default:
                    deadLetter(envelope); // Operation not supported
            }
        }
    }

    private Contract selectContract(Envelope envelope) {
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
