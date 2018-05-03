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
public class DIDAuthenticationRoute extends RoutingSlip {

    public DIDAuthenticationRoute() {
        super(DIDAuthenticationRoute.class.getName(), "Authenticate");
        try {
            addRoute(new SimpleRoute(InfoVaultService.class.getName(), InfoVaultService.OPERATION_LOAD));
            addRoute(new SimpleRoute(DIDService.class.getName(), DIDService.OPERATION_AUTHENTICATE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
