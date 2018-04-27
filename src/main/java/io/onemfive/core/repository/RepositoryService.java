package io.onemfive.core.repository;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class RepositoryService extends BaseService {

    public RepositoryService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public void handleDocument(Envelope envelope) {
        Route route = (Route) envelope.getHeader(Envelope.ROUTE);
        if (route.getOperation() != null) {
            Repository r = selectRepository(envelope);
            switch (route.getOperation()) {
                case "ProvideAccess": r.provideAccess(envelope);break;
                case "ListRepositories": r.listRepositories(envelope);break;
                case "GetRepository": r.getRepository(envelope);break;
                case "ListProjects": r.listProjects(envelope);break;
                case "GetProject": r.getProject(envelope);break;
                case "ListCards": r.listCards(envelope);break;
                case "GetCard": r.getCard(envelope);break;
                case "ListCardsWithBounties": r.listCardsWithBounties(envelope);break;
                case "ChooseCardWithBounty": r.chooseCardWithBounty(envelope);break;
                case "PullRequestForBounty": r.pullRequestForBounty(envelope);break;
                case "CommitPullRequest": r.commitPullRequest(envelope);break;
                default: deadLetter(envelope); // Operation not supported
            }
        }
    }

    private Repository selectRepository(Envelope envelope) {
        Repository repository = null;
        // Only GitHub for now
        repository = new GitHubRepository();

        return repository;
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("RepositoryService starting...");
        System.out.println("RepositoryService started.");
        return true;
    }


}
