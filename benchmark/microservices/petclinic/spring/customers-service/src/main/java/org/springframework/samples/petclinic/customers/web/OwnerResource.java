package org.springframework.samples.petclinic.customers.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/owners")
@RestController
class OwnerResource {

    private final OwnerRepository ownerRepository;

    OwnerResource(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@RequestBody OwnerRequest ownerRequest) {
        Owner owner = new Owner();
        mapOwner(owner, ownerRequest);
        return ownerRepository.save(owner);
    }

    @GetMapping(value = "/{ownerId}")
    public ResponseEntity<Owner> findOwner(@PathVariable("ownerId") int ownerId) {
        return ResponseEntity.of(ownerRepository.findById(ownerId));
    }

    @GetMapping
    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    @PutMapping(value = "/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOwner(@PathVariable("ownerId") int ownerId, @RequestBody OwnerRequest ownerRequest) {
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));
        mapOwner(owner, ownerRequest);
        ownerRepository.save(owner);
    }

    private void mapOwner(Owner owner, OwnerRequest request) {
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
    }
}
