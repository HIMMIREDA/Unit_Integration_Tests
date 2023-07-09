package com.example.demo.student.controller;


import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("Integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class StudentControllerIntegrationTest {

    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");
    private static HttpHeaders httpHeaders;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @LocalServerPort
    private Integer port;
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeAll
    public static void init() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driverClassName", postgreSQLContainer::getDriverClassName);
    }

    private String getApiUrl() {
        return "http://localhost:" + port + "/api/v1/students";
    }

    @AfterEach
    public void tearDown() {
        studentRepository.deleteAll();
    }

    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('2','reda','reda@gmail.com','MALE'),('5','wafaa','wafaa@gmail.com','FEMALE')")
    void getAllStudents_ReturnsListOfStudentsAnd200() throws Exception {
//        given
        List<Student> expectedStudents = Arrays.asList(
                new Student(2L, "reda", "reda@gmail.com", Gender.MALE),
                new Student(5L, "wafaa", "wafaa@gmail.com", Gender.FEMALE)
        );
        HttpEntity<?> httpEntity = new HttpEntity<>(null, httpHeaders);
//        when
        ResponseEntity<List<Student>> responseEntity = testRestTemplate.exchange(getApiUrl(), HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<Student>>() {
        });

        List<Student> actualStudents = responseEntity.getBody();

//        then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualStudents).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedStudents);
    }

    @Test
    void addStudent_ValidPayload_ReturnsStudentAnd200() throws Exception {
//        given
        Student student = new Student(1L, "reda", "reda@gmail.com", Gender.MALE);
        HttpEntity<Student> httpEntity = new HttpEntity<>(student, httpHeaders);
//      when

        ResponseEntity<Student> responseEntity = testRestTemplate.exchange(getApiUrl(), HttpMethod.POST, httpEntity, Student.class);

        Student actualStudent = responseEntity.getBody();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(actualStudent).usingRecursiveComparison().isEqualTo(student);
    }

    @Test
    void addStudent_NotValidPayload_Returns400() throws Exception {
//        given
        Student student = new Student(1L, "", "redagmail.com", Gender.MALE);
        HttpEntity<Student> httpEntity = new HttpEntity<>(student, httpHeaders);
//      when

        ResponseEntity<String> responseEntity = testRestTemplate.exchange(getApiUrl(), HttpMethod.POST, httpEntity, String.class);

//        then
        Map<String, Object> responseObject = objectMapper.readValue(responseEntity.getBody(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(responseObject.get("message")).isEqualTo("Validation failed for object='student'. Error count: 2");
    }

    @Test
    @Sql(statements = "INSERT INTO student(id,name,email,gender) VALUES ('1','reda','reda@gmail.com','MALE')", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteStudent_StudentExists_Returns200() throws Exception {
//        given
        Long studentId = 1L;
        HttpEntity<?> httpEntity = new HttpEntity(null, httpHeaders);
//        when
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(getApiUrl() + "/" + studentId, HttpMethod.DELETE, httpEntity, String.class);

//        then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteStudent_StudentNotExists_Return404() throws Exception {
//        given
        Long studentId = 1L;
        String exceptionMessage = "Student with id " + studentId + " does not exists";
        HttpEntity<?> httpEntity = new HttpEntity<>(null, httpHeaders);
//        when
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(getApiUrl() + "/" + studentId, HttpMethod.DELETE, httpEntity, String.class);

//        then
        Map<String, Object> responseObject = objectMapper.readValue(responseEntity.getBody(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(responseObject.get("message"))).isEqualTo(exceptionMessage);
    }

}
