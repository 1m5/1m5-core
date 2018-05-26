package io.onemfive.core.repository.github;

import io.onemfive.core.repository.Repository;
import io.onemfive.data.Envelope;

import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * https://developer.github.com/v3/
 *
 * @author objectorange
 */
public class GitHubRepository implements Repository {

    private final Logger LOG = Logger.getLogger(GitHubRepository.class.getName());

    @Override
    public void provideAccess(Envelope envelope) {

    }

    @Override
    public void listRepositories(Envelope envelope) {

    }

    @Override
    public void getRepository(Envelope envelope) {

    }

    @Override
    public void listProjects(Envelope envelope) {

    }

    @Override
    public void getProject(Envelope envelope) {

    }

    @Override
    public void listCards(Envelope envelope) {

    }

    @Override
    public void getCard(Envelope envelope) {

    }

    @Override
    public void listCardsWithBounties(Envelope envelope) {

    }

    @Override
    public void chooseCardWithBounty(Envelope envelope) {

    }

    @Override
    public void pullRequestForBounty(Envelope envelope) {

    }

    @Override
    public void commitPullRequest(Envelope envelope) {

    }
}
