package io.onemfive.core.orchestration.routes.security;

import io.onemfive.core.did.DIDService;
import io.onemfive.core.infovault.InfoVaultService;
import io.onemfive.core.orchestration.routes.RoutingSlip;
import io.onemfive.core.orchestration.routes.SimpleRoute;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class DIDVerificationRoute extends RoutingSlip {

    public DIDVerificationRoute() {
        super(DIDVerificationRoute.class.getName(), "Verify");
        try {
            addRoute(new SimpleRoute(InfoVaultService.class.getName(),InfoVaultService.OPERATION_LOAD));
            addRoute(new SimpleRoute(DIDService.class.getName(),DIDService.OPERATION_VERIFY));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
