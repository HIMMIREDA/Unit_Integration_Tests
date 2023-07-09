package com.example.demo.student.service;

import com.example.demo.student.Gender;
import com.example.demo.student.Student;
import com.example.demo.student.StudentRepository;
import com.example.demo.student.StudentService;
import com.example.demo.student.exception.BadRequestException;
import com.example.demo.student.exception.StudentNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("Unit")
class StudentServiceTest {


    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService underTest;

    @Test
    void getAllStudents__ReturnsAllStudents() {
//        when
        underTest.getAllStudents();
//        then
        verify(studentRepository).findAll();
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
        underTest.addStudent(student);
//        then
        ArgumentCaptor<Student> studentArgumentCaptor = ArgumentCaptor.forClass(Student.class);

        verify(studentRepository).save(studentArgumentCaptor.capture());
        Student capturedStudent = studentArgumentCaptor.getValue();

        assertThat(capturedStudent).isEqualTo(student);
    }

    @Test
    void addStudent_EmailIsTaken_ThrowsBadRequestException() {
//        given
        String email = "jamila@gmail.com";
        Student student = new Student(
                "Jamila",
                "jamila@gmail.com",
                Gender.FEMALE
        );
        given(studentRepository.selectExistsEmail(anyString()))
                .willReturn(true);
//        when
//        then
        assertThatThrownBy(() -> underTest.addStudent(student))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email " + student.getEmail() + " taken");
        ;

        verify(studentRepository, never()).save(any());
    }

    @Test
    void deleteStudent_IdExists_DeleteStudent() {
//        given
        Long id = 2L;
        given(studentRepository.existsById(anyLong())).willReturn(true);

//        when
        underTest.deleteStudent(id);

//        then
        verify(studentRepository).deleteById(id);

    }

    @Test
    void deleteStudent_IdNotExists_ThrowsStudentNotFoundException() {
//        given
        Long id = 2L;

        given(studentRepository.existsById(anyLong())).willReturn(false);
//        when
//        then
        assertThatThrownBy(() -> underTest.deleteStudent(id)).isInstanceOf(
                StudentNotFoundException.class
        ).hasMessageContaining("Student with id " + id + " does not exists");


    }
}