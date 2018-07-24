package io.onemfive.core.contract;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.core.contract.omni.OmniContract;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ContractService extends BaseService {

    private static final Logger LOG = Logger.getLogger(ContractService.class.getName());

    // Bounties
    public static final String OPERATION_CREATE_BOUNTY = "CREATE_BOUNTY";
    public static final String OPERATION_SELECT_BOUNTY = "SELECT_BOUNTY";
    public static final String OPERATION_VOTE_ON_BOUNTY = "VOTE_ON_BOUNTY";
    public static final String OPERATION_REMOVE_BOUNTY = "REMOVE_BOUNTY";
    public static final String OPERATION_CLOSE_BOUNTY = "CLOSE_BOUNTY";
    public static final String OPERATION_LIST_BOUNTIES = "LIST_BOUNTIES";

    // General Contracts
    public static final String OPERATION_CREATE_CONTRACT = "CREATE_CONTRACT";
    public static final String OPERATION_SEND_CURRENCY_TO_CONTRACT = "SEND_CURRENCY_TO_CONTRACT";
    public static final String OPERATION_ADD_VOTER = "ADD_VOTER";
    public static final String OPERATION_REMOVE_VOTER = "REMOVE_VOTER";
    public static final String OPERATION_KILL_CONTRACT = "KILL_CONTRACT";

    private OmniContract contract;

    public ContractService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
            Contract c = selectContract(e);
            if(c instanceof BountyContract) {
                BountyContract bc = (BountyContract)c;
                switch (route.getOperation()) {
                    case OPERATION_CREATE_BOUNTY:{bc.createBounty(e);return;}
                    case OPERATION_SELECT_BOUNTY:{bc.selectBounty(e);return;}
                    case OPERATION_VOTE_ON_BOUNTY:{bc.voteOnBounty(e);return;}
                    case OPERATION_REMOVE_BOUNTY:{bc.removeBounty(e);return;}
                    case OPERATION_CLOSE_BOUNTY:{bc.closeBounty(e);return;}
                    case OPERATION_LIST_BOUNTIES:{bc.listBounties(e);return;}
                    default: deadLetter(e);;
                }
            }
            switch (route.getOperation()) {
                case OPERATION_CREATE_CONTRACT:{c.createContract(e);return;}
                case OPERATION_SEND_CURRENCY_TO_CONTRACT:{c.sendCurrencyToContract(e);return;}
                case OPERATION_ADD_VOTER:{c.addVoter(e);return;}
                case OPERATION_REMOVE_VOTER:{c.removeVoter(e);return;}
                case OPERATION_KILL_CONTRACT:{c.killContract(e);return;}
                default:deadLetter(e); // Operation not supported
            }
    }

    private Contract selectContract(Envelope e) {
        // Only Omni for now
        return contract;
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        contract = new OmniContract();
        LOG.info("Started");
        return true;
    }

}
