package io.onemfive.core.payment;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;

import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class PaymentService extends BaseService {

    public PaymentService(MessageProducer producer) {
        super(producer);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println("PaymentService starting...");

        System.out.println("PaymentService started.");
        return true;
    }
}
