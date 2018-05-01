package io.onemfive.core.repository;

import io.onemfive.data.Envelope;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Repository {

    void provideAccess(Envelope envelope);

    void listRepositories(Envelope envelope);

    void getRepository(Envelope envelope);

    void listProjects(Envelope envelope);

    void getProject(Envelope envelope);

    void listCards(Envelope envelope);

    void getCard(Envelope envelope);

    void listCardsWithBounties(Envelope envelope);

    void chooseCardWithBounty(Envelope envelope);

    void pullRequestForBounty(Envelope envelope);

    void commitPullRequest(Envelope envelope);
}
