package com.homecook.service;

import com.homecook.entity.Chef;
import com.homecook.repository.ChefRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChefService {

    private final ChefRepository chefRepository;

    public ChefService(ChefRepository chefRepository) {
        this.chefRepository = chefRepository;
    }

    // Register a new chef
    public Chef registerChef(Chef chef) {

        chef.setApproved(false);

        return chefRepository.save(chef);
    }

    // Save chef
    public Chef saveChef(Chef chef) {

        return chefRepository.save(chef);
    }

    // Get all chefs
    public List<Chef> getAllChefs() {

        return chefRepository.findAll();
    }

    // Get approved chefs
    public List<Chef> getApprovedChefs() {

        return chefRepository.findByApprovedTrue();
    }

    // Get chef by ID
    public Optional<Chef> getChefById(Long id) {

        return chefRepository.findById(id);
    }

    // Approve chef
    public Chef approveChef(Long id) {

        Chef chef = chefRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Chef not found"));

        chef.setApproved(true);

        return chefRepository.save(chef);
    }

    // Delete chef
    public void deleteChef(Long id) {

        chefRepository.deleteById(id);
    }
}