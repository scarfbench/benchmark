package org.springframework.tutorial.producerfields.service;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.tutorial.producerfields.db.UserDatabase;
import org.springframework.tutorial.producerfields.entity.ToDo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    @UserDatabase
    private EntityManager em;

    @Transactional
    public ToDo createToDo(String inputString) {
        try {
            ToDo toDo = new ToDo();
            Date currentTime = Calendar.getInstance().getTime();
            toDo.setTaskText(inputString);
            toDo.setTimeCreated(currentTime);
            em.persist(toDo);
            return toDo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ToDo", e);
        }
    }

    @Transactional
    public List<ToDo> getToDos() {
        try {
            return em.createQuery("SELECT t FROM ToDo t ORDER BY t.timeCreated", ToDo.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch ToDos", e);
        }
    }
}

