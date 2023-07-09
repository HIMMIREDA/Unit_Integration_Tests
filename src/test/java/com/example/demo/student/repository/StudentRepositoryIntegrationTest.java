package com.example.demo.student.repository;


import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Tag("Integration")
@Testcontainers(disabledWithoutDocker = true, parallel = true)
public class StudentRepositoryIntegrationTest {

//    @BeforeAll
//    static void beforeAll() {
//        postgreSQLContainer.start();
//    }
//
//    @AfterAll
//    static void afterAll() {
//        postgreSQLContainer.stop();
//    }
    @Autowired
    private StudentRepository underTest;
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");


    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driverClassName", postgreSQLContainer::getDriverClassName);
    }


    @AfterEach
    void tearDown(){
        underTest.deleteAll();
    }

    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('1','Jamila','jamila@gmail.com','FEMALE')",executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE id=1",executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void selectExistsEmail_EmailExists_True() throws InterruptedException {
//        given
        String email = "jamila@gmail.com";
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
