package io.onemfive.core.repository;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.ServiceStatus;
import io.onemfive.core.ServiceStatusListener;
import io.onemfive.core.repository.gitlab.GitLabRepository;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class RepositoryService extends BaseService {

    private static final Logger LOG = Logger.getLogger(RepositoryService.class.getName());

    public RepositoryService(MessageProducer producer, ServiceStatusListener serviceStatusListener) {
        super(producer, serviceStatusListener);
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        Repository r = selectRepository(e);
        switch (route.getOperation()) {
            case "ProvideAccess": r.provideAccess(e);break;
            case "ListRepositories": r.listRepositories(e);break;
            case "GetRepository": r.getRepository(e);break;
            case "ListProjects": r.listProjects(e);break;
            case "GetProject": r.getProject(e);break;
            case "ListCards": r.listCards(e);break;
            case "GetCard": r.getCard(e);break;
            case "ListCardsWithBounties": r.listCardsWithBounties(e);break;
            case "ChooseCardWithBounty": r.chooseCardWithBounty(e);break;
            case "PullRequestForBounty": r.pullRequestForBounty(e);break;
            case "CommitPullRequest": r.commitPullRequest(e);break;
            default: deadLetter(e); // Operation not supported
        }
    }

    private Repository selectRepository(Envelope e) {
        Repository repository = null;
        // Only GitHub for now
        repository = new GitLabRepository();

        return repository;
    }

    @Override
    public boolean start(Properties properties) {
        LOG.info("Starting...");
        updateStatus(ServiceStatus.STARTING);

        updateStatus(ServiceStatus.RUNNING);
        LOG.info("Started.");
        return true;
    }


}
