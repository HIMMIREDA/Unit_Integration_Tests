package com.example.demo.student.repository;

import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository underTest;

    @AfterEach
    void tearDown(){
        underTest.deleteAll();
    }
    @Test
    void selectExistsEmail_EmailExists_True() {
//        given
        String email = "jamila@gmail.com";
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE
        );
        underTest.save(student);
//        when
        Boolean result = underTest.selectExistsEmail(email);

//        then
        assertThat(result).isTrue();
    }

    @Test
    void selectExistsEmail_EmailNotExists_False() {
//        given
        String email = "jamila1@gmail.com";

//        when
        Boolean result = underTest.selectExistsEmail(email);
//        then
        assertThat(result).isFalse();
    }
}
