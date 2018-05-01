package io.onemfive.core.contract.ethreum;

import io.onemfive.core.contract.Contract;
import io.onemfive.data.Envelope;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class EthereumContract implements Contract {

    private Web3j web3j;

    public EthereumContract() {
        this.web3j = Web3jFactory.build(new HttpService());
    }

    @Override
    public void createContract(Envelope envelope) {

    }

    @Override
    public void sendCurrencyToContract(Envelope envelope) {

    }

    @Override
    public void addVoter(Envelope envelope) {

    }

    @Override
    public void removeVoter(Envelope envelope) {

    }

    @Override
    public void killContract(Envelope envelope) {

    }
}
