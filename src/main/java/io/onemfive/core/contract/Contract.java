package io.onemfive.core.contract;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Contract {

    void createContract(Envelope envelope);

    void sendCurrencyToContract(Envelope envelope);

    void addVoter(Envelope envelope);

    void removeVoter(Envelope envelope);

    void killContract(Envelope envelope);

}
