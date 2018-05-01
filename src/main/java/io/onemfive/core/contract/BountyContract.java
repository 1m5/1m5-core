package io.onemfive.core.contract;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface BountyContract extends Contract {

    void createBounty(Envelope envelope);

    void selectBounty(Envelope envelope);

    void voteOnBounty(Envelope envelope);

    void removeBounty(Envelope envelope);

    void closeBounty(Envelope envelope);

    void listBounties(Envelope envelope);

}
