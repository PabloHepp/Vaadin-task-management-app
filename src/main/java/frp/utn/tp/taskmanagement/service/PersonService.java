package frp.utn.tp.taskmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import frp.utn.tp.taskmanagement.domain.Person;
import frp.utn.tp.taskmanagement.domain.PersonRepository;


@Service
@Transactional(propagation= Propagation.REQUIRES_NEW)
public class PersonService {

    @Autowired
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void createPerson(String nombre, String apellido, String dni) {
        var person = new Person();
        person.setNombre(nombre);
        person.setApellido(apellido);
        person.setDni(dni);
        personRepository.saveAndFlush(person);
    }

    public void updatePerson(Person person) {
        personRepository.saveAndFlush(person);
    }

    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }
    
    public List<Person> list(Pageable pageable) {
        return personRepository.findAllBy(pageable).toList();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }
    
}
