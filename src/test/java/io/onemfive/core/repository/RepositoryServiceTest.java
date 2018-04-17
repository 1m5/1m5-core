package io.onemfive.core.repository;

import io.onemfive.data.Envelope;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class RepositoryServiceTest {

    private static RepositoryService service;

    @BeforeClass
    public static void startUp() {
        service = new RepositoryService(null);
        service.start(null);
    }

    @Test
    public void provideAccess() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ProvideAccess");
        service.handleDocument(e);

    }

    @Test
    public void listRepositories() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ListRepositories");
        service.handleDocument(e);

    }

    @Test
    public void getRepository() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"GetRepository");
        service.handleDocument(e);

    }

    @Test
    public void listProjects() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ListProjects");
        service.handleDocument(e);

    }

    @Test
    public void getProject() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"GetProject");
        service.handleDocument(e);

    }

    @Test
    public void listCards() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ListCards");
        service.handleDocument(e);

    }

    @Test
    public void getCard() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"GetCard");
        service.handleDocument(e);

    }

    @Test
    public void listCardsWithBounties() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ListCardsWithBounties");
        service.handleDocument(e);

    }

    @Test
    public void chooseCardWithBounty() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"ChooseCardWithBounty");
        service.handleDocument(e);

    }

    @Test
    public void pullRequestForBounty() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"PullRequestForBounty");
        service.handleDocument(e);

    }

    @Test
    public void commitPullRequest() {
        Envelope e = Envelope.documentFactory();
        e.setHeader(Envelope.OPERATION,"CommitPullRequest");
        service.handleDocument(e);

    }

    @AfterClass
    public static void tearDown() {
        service = null;
    }

}
