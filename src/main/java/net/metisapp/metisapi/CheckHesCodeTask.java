package net.metisapp.metisapi;

import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Component
public class CheckHesCodeTask {

    private static final Logger log = LoggerFactory.getLogger(CheckHesCodeTask.class);

    @Autowired
    private UserRepository userRepository;
        
	@Scheduled(fixedRate = 24*60*60*1000)
    @Transactional(propagation = Propagation.REQUIRED)
	public void checkHesCodes() {

            List<MetisUser> users = userRepository.getAllPatients();
            for(int i = 0; i < users.size(); i++){
                boolean result = users.get(i).checkHesCode();
                userRepository.updateCovidStatus(users.get(i).getId(), result);
	        }
            log.info("Done updating user covid status");
    }
}
