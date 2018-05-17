package io.onemfive.core.admin;

import io.onemfive.core.BaseService;
import io.onemfive.core.MessageProducer;
import io.onemfive.core.bus.ServiceBus;
import io.onemfive.core.bus.ServiceNotAccessibleException;
import io.onemfive.core.bus.ServiceNotSupportedException;
import io.onemfive.core.bus.ServiceRegisteredException;
import io.onemfive.data.DLC;
import io.onemfive.data.DocumentMessage;
import io.onemfive.data.Envelope;
import io.onemfive.data.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class AdminService extends BaseService {

    public static final String OPERATION_REGISTER_SERVICES = "REGISTER_SERVICES";

    private ServiceBus serviceBus;

    public AdminService(MessageProducer producer) {
        super(producer);
        serviceBus = (ServiceBus)producer;
    }

    @Override
    public void handleDocument(Envelope e) {
        Route route = e.getRoute();
        switch(route.getOperation()) {
            case OPERATION_REGISTER_SERVICES:{registerServices(e);break;}
            default: deadLetter(e);
        }
    }

    private void registerServices(Envelope e){
        List<Class> servicesToRegister = (List<Class>)((DocumentMessage)e.getMessage()).data.get(0).get(DLC.ENTITY);
        List<Exception> exceptionList = new ArrayList<>();
        int i = 0;
        for(Class c : servicesToRegister) {
            try {
                serviceBus.register(c);
            } catch (ServiceNotAccessibleException e1) {
                exceptionList.add(i,e1);
            } catch (ServiceNotSupportedException e1) {
                exceptionList.add(i,e1);
            } catch (ServiceRegisteredException e1) {
                exceptionList.add(i,e1);
            }
            exceptionList.add(i,null);
            i++;
        }
        ((DocumentMessage)e.getMessage()).data.get(0).put(DLC.EXCEPTIONS,exceptionList);
    }

    @Override
    public boolean start(Properties properties) {
        System.out.println(AdminService.class.getSimpleName()+": starting...");

        System.out.println(AdminService.class.getSimpleName()+": started.");
        return true;
    }
}
