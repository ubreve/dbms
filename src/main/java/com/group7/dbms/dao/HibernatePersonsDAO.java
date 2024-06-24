package com.group7.dbms;

import java.util.List;

import org.hibernate.SessionFactory;

public class HibernatePersonsDAO implements PersonsDAO {
    private SessionFactory sessionFactory;

    HibernatePersonsDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Object by id value or null if not found
     */
    @Override
    public Person getByID(Long id) {
        return sessionFactory.fromTransaction(session -> {
            return session.get(Person.class, id);
        });
    }

    @Override
    public List<Person> getAllPersons() {
        return sessionFactory.fromTransaction(session -> {
            return session.createQuery(
                "FROM Person", Person.class
            ).list();
        });
    }

    @Override
    public Person save(Person person) {
        return sessionFactory.fromTransaction(session -> {
            session.persist(person);
            session.flush();
            return person;
        });
    }

    @Override
    public void update(Person person) {
        sessionFactory.inTransaction(session -> {
            session.merge(person);
        });
    }

    @Override
    public void remove(Person person) {
        sessionFactory.inTransaction(session -> {
            session.remove(person);
        });
    }

    /**
     * Remove by identifier
     */
    @Override
    public void remove(Long id) {
        remove(getByID(id));
    }
}