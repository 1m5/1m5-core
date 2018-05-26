package io.onemfive.core.payment;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class PaymentService extends BaseService {

    private final Logger LOG = Logger.getLogger(PaymentService.class.getName());

    public PaymentService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(PaymentService.class.getSimpleName()+": starting...");

        System.out.println(PaymentService.class.getSimpleName()+": started.");
        return true;
    }
}
