package digit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import digit.repository.MachinesRepository;
@Service
public class MachineService {
    
    private final  MachinesRepository machinesRepository;
    @Autowired
    public MachineService(MachinesRepository machinesRepository) {
        this.machinesRepository = machinesRepository;
    }

}
