package io.onemfive.core.contract;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
interface Contract {

    void createContract(Envelope envelope);

    void sendCurrencyToContract(Envelope envelope);

    void createBounty(Envelope envelope);

    void selectBounty(Envelope envelope);

    void voteOnBounty(Envelope envelope);

    void removeBounty(Envelope envelope);

    void closeBounty(Envelope envelope);

    void listBounties(Envelope envelope);

    void addVoter(Envelope envelope);

    void removeVoter(Envelope envelope);

    void killContract(Envelope envelope);

}
