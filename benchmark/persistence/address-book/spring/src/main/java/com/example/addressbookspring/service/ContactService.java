package com.example.addressbookspring.service;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.repo.ContactRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactService {
    private final ContactRepository repo;

    public ContactService(ContactRepository repo) {
        this.repo = repo;
    }


    public void create(Contact entity) {
        repo.save(entity); 
    }

    public void edit(Contact entity) {
        repo.save(entity); 
    }

    public void remove(Contact entity) {
        if (entity == null) return;
        Long id = entity.getId();
        if (id != null && repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            repo.delete(entity); 
        }
    }

    public Contact find(Long id) {
        return (id == null) ? null : repo.findById(id).orElse(null);
    }

    public List<Contact> findAll() {
        return repo.findAll();
    }

    public List<Contact> findRange(int[] range) {
        int start = range[0];
        int endExclusive = range[1];
        int size = Math.max(0, endExclusive - start);
        if (size <= 0) return List.of();
        int page = start / size;
        return repo.findAll(PageRequest.of(page, size)).getContent();
    }

    public int count() {
        long c = repo.count();
        return (c > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) c;
    }
}
