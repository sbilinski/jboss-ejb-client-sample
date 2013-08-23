package com.example.service;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Remote(InternalService.class)
@Stateless
public class InternalServiceBean implements InternalService {

    private static final Logger logger = LoggerFactory.getLogger(InternalServiceBean.class);
    
    @Resource
    private SessionContext sessionContext;
    
    @PermitAll
    @Override
    public void internalBusinessMethod() {
        logger.info("!! INTERNAL BUSINESS METHOD !!");
        logger.info("Principal: {}", sessionContext.getCallerPrincipal().getName());
    }
    
}
