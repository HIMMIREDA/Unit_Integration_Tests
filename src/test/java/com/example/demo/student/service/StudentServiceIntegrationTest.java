package com.example.demo.student.service;

import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import com.example.demo.student.StudentService;
import com.example.demo.student.exception.BadRequestException;
import com.example.demo.student.exception.StudentNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Tag("Integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class StudentServiceIntegrationTest {

    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private StudentService underTest;

    @Autowired
    private StudentRepository studentRepository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driverClassName", postgreSQLContainer::getDriverClassName);
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
    }


    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('1','reda','reda@gmail.com','MALE'),('2','wafaa','wafaa@gmail.com','FEMALE')", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getAllStudents__ReturnsAllStudents() {
        List<Student> expectedStudents = Arrays.asList(
                new Student(1L, "reda", "reda@gmail.com", Gender.MALE),
                new Student(2L, "wafaa", "wafaa@gmail.com", Gender.FEMALE)
        );
//        when
        List<Student> students = underTest.getAllStudents();
//        then
        assertThat(students).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedStudents);
    }

    @Test
    void addStudent_EmailIsNotTaken_ReturnsStudent() {
//        given
        String email = "jamila@gmail.com";
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE
        );
//        when
        Student actualStudent = underTest.addStudent(student);
//        then
        assertThat(actualStudent).isEqualTo(student);
    }

    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('1','Jamila','jamila@gmail.com','FEMALE')", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void addStudent_EmailIsTaken_ThrowsBadRequestException() {
//        given
        String email = "jamila@gmail.com";
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE
        );
//        when
//        then
        assertThatThrownBy(() -> underTest.addStudent(student))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email " + student.getEmail() + " taken");
    }

    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('2','reda','reda@gmail.com','MALE')", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteStudent_IdExists_DeleteStudent() {
//        given
        Long id = 2L;
//        when
        underTest.deleteStudent(id);
//        then
        Boolean exists = studentRepository.existsById(id);
        assertThat(exists).isFalse();
    }

    @Test
    void deleteStudent_IdNotExists_ThrowsStudentNotFoundException() {
//        given
        Long id = 2L;
//        when
        assertThatThrownBy(() -> underTest.deleteStudent(id)).isInstanceOf(
                StudentNotFoundException.class
        ).hasMessageContaining("Student with id " + id + " does not exists");

    }

}
