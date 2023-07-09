package com.example.demo.student.controller;

import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentController;
import com.example.demo.student.StudentService;
import com.example.demo.student.exception.StudentNotFoundException;
import com.example.demo.utils.ResponseBodyMatchers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = StudentController.class)
@ExtendWith(MockitoExtension.class)
@Tag("Unit")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private Student validStudent;
    private Student notValidStudent;

    private List<Student> expectedListOfStudents;

    @BeforeEach
    void setUp() {
        System.out.println();
        validStudent = new Student();
        validStudent.setId(10L);
        validStudent.setEmail("student@gmail.com");
        validStudent.setGender(Gender.MALE);
        validStudent.setName("student");
        notValidStudent = new Student();
        notValidStudent.setId(11L);
        notValidStudent.setEmail("studentgmail.com");
        notValidStudent.setGender(Gender.MALE);
        notValidStudent.setName("");
        expectedListOfStudents = Arrays.asList(
                new Student(1L, "reda", "reda@gmail.com", Gender.MALE),
                new Student(2L, "wafaa", "wafaa@gmail.com", Gender.FEMALE)
        );
    }


    @Test
    void getAllStudents_ReturnsListOfStudentsAnd200() throws Exception {
//        given


        given(studentService.getAllStudents()).willReturn(expectedListOfStudents);
        mockMvc.perform(get("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(ResponseBodyMatchers.response().containsObjectAsJson(expectedListOfStudents, new TypeReference<List<Student>>() {
                }));

    }

    @Test
    void addStudent_ValidPayload_ReturnsStudentAnd200() throws Exception {
//        given
        given(studentService.addStudent(eq(validStudent))).willReturn(validStudent);

//      when
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validStudent))
                ).andExpect(status().isOk())
                .andExpect(ResponseBodyMatchers.response().containsObjectAsJson(validStudent, new TypeReference<Student>() {
                }))
                .andReturn();


//        String expectedResponseBody = objectMapper.writeValueAsString(validStudent);
//        String resultResponseBody = mvcResult.getResponse().getContentAsString();

//        then
        ArgumentCaptor<Student> studentArgumentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentService, times(1)).addStudent(studentArgumentCaptor.capture());

        assertThat(studentArgumentCaptor.getValue()).isEqualTo(validStudent);
//        assertThat(resultResponseBody).isNotEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void addStudent_NotValidPayload_Returns400() throws Exception {
//        when
        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notValidStudent))
        ).andExpect(status().isBadRequest()).andReturn();

//        then

        assertThat(mvcResult.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
        verify(studentService, times(0)).addStudent(any());
    }

    @Test
    void deleteStudent_StudentExists_Returns200() throws Exception {
//        given
        Long studentId = 1L;
//        when
        mockMvc.perform(delete("/api/v1/students/{studentId}", studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ArgumentCaptor<Long> studentIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(studentService).deleteStudent(studentIdArgumentCaptor.capture());

        assertThat(studentIdArgumentCaptor.getValue()).isEqualTo(studentId);
    }

    @Test
//    @Disabled
    void deleteStudent_StudentNotExists_Return404() throws Exception {
//        given
        Long studentId = 1L;
        String exceptionMessage = "Student with id " + studentId + " does not exists";
        doThrow(
                new StudentNotFoundException(exceptionMessage)
        )
                .when(studentService)
                .deleteStudent(anyLong());

//        when
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/students/{studentId}", studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

        ArgumentCaptor<Long> studentIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(studentService).deleteStudent(studentIdArgumentCaptor.capture());

        assertThat(mvcResult.getResolvedException()).isInstanceOf(StudentNotFoundException.class);
        assertThat(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage()).isEqualToIgnoringWhitespace(exceptionMessage);
        assertThat(studentIdArgumentCaptor.getValue()).isEqualTo(studentId);


    }
}
